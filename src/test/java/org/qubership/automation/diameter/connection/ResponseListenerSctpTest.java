/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 *
 */

package org.qubership.automation.diameter.connection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.qubership.automation.diameter.HexDumpReader;
import org.qubership.automation.diameter.data.Decoder;

/**
 * Fails when {@link ResponseListener} loses or truncates a message received over SCTP.
 *
 * <p>An SCTP message arrives as one unit, and the listener has to hand every byte of it to the decoder
 * whatever its size. Messages longer than the initial read buffer (512 bytes) are the case that matters:
 * a CCA of 612 bytes used to be dropped without any log entry.</p>
 */
public class ResponseListenerSctpTest {

    private static final String HEX_DUMP_RESOURCE = "/hexdump/";

    private static final int DIAMETER_HEADER_LENGTH = 20;

    private static final long WAIT_SECONDS = 5;

    private final FakeSctpAssociation association = new FakeSctpAssociation();

    private final List<byte[]> decoded = new CopyOnWriteArrayList<>();

    private CountDownLatch decodedLatch;

    private ResponseListener listener;

    private Thread listenerThread;

    @Before
    public void startListener() {
        Decoder decoder = mock(Decoder.class);
        when(decoder.decode(any(ByteBuffer.class))).thenAnswer(invocation -> {
            ByteBuffer message = (ByteBuffer) invocation.getArguments()[0];
            decoded.add(Arrays.copyOf(message.array(), message.limit()));
            decodedLatch.countDown();
            return "<message/>";
        });
        listener = new ResponseListener();
        listener.setDecoder(decoder);
        listener.setChannel(ExtraChannel.withSctpReceiver(association));
        listenerThread = new Thread(listener, "sctp-listener-test");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    @After
    public void stopListener() throws InterruptedException {
        listener.stop();
        listenerThread.interrupt();
        listenerThread.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
    }

    @Test
    public void ccaLongerThanReadBufferIsDecodedWhole() throws IOException, URISyntaxException, InterruptedException {
        ByteBuffer dump = HexDumpReader.read(HEX_DUMP_RESOURCE, "cca1.hexdump.txt");
        byte[] cca = new byte[dump.remaining()];
        dump.get(cca);
        assertEquals("cca1.hexdump.txt must hold the 612-byte CCA", 612, cca.length);

        awaitDecoded(1, () -> association.deliver(cca));

        assertArrayEquals(cca, decoded.get(0));
    }

    @Test
    public void messageOfExactlyReadBufferSizeIsDecodedWhole() throws InterruptedException {
        byte[] message = diameterMessage(512);

        awaitDecoded(1, () -> association.deliver(message));

        assertArrayEquals(message, decoded.get(0));
    }

    @Test
    public void messageOneWordLongerThanReadBufferIsDecodedWhole() throws InterruptedException {
        byte[] message = diameterMessage(516);

        awaitDecoded(1, () -> association.deliver(message));

        assertArrayEquals(message, decoded.get(0));
    }

    @Test
    public void messageSeveralTimesLongerThanReadBufferIsDecodedWhole() throws InterruptedException {
        byte[] message = diameterMessage(4000);

        awaitDecoded(1, () -> association.deliver(message));

        assertArrayEquals(message, decoded.get(0));
    }

    @Test
    public void shortMessageAfterLongOneIsStillDecoded() throws InterruptedException {
        byte[] longMessage = diameterMessage(612);
        byte[] shortMessage = diameterMessage(100);

        awaitDecoded(2, () -> {
            association.deliver(longMessage);
            association.deliver(shortMessage);
        });

        assertArrayEquals(longMessage, decoded.get(0));
        assertArrayEquals(shortMessage, decoded.get(1));
    }

    private void awaitDecoded(final int expectedCount, final Runnable delivery) throws InterruptedException {
        decodedLatch = new CountDownLatch(expectedCount);
        delivery.run();
        boolean arrived = decodedLatch.await(WAIT_SECONDS, TimeUnit.SECONDS);
        assertTrue("Decoder got " + decoded.size() + " of " + expectedCount + " messages within "
                + WAIT_SECONDS + " seconds", arrived);
    }

    /**
     * Builds a Diameter message of the given length (a multiple of 4) with a valid header and filler payload.
     */
    private static byte[] diameterMessage(final int length) {
        byte[] message = new byte[length];
        message[0] = 1;
        message[1] = (byte) (length >> 16);
        message[2] = (byte) (length >> 8);
        message[3] = (byte) length;
        message[4] = 64;
        for (int i = DIAMETER_HEADER_LENGTH; i < length; i++) {
            message[i] = (byte) (i % 100 + 1);
        }
        return message;
    }
}
