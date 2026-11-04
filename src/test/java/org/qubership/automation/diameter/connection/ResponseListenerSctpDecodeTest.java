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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.qubership.automation.diameter.HexDumpReader;
import org.qubership.automation.diameter.MarbenConfigProvider;
import org.qubership.automation.diameter.data.XmlDecoder;

/**
 * Fails when a CCA longer than the 512-byte listener buffer does not come out of {@link ResponseListener} as XML.
 *
 * <p>{@link ResponseListenerSctpTest} hands the bytes to a mock decoder. This test decodes them with
 * {@link XmlDecoder} and the dictionaries in {@code src/test/resources/marben}, so it covers the path from the SCTP
 * read to the XML message that the listener's callers receive. {@code cca_big.hexdump.txt} is the 612-byte CCA that a
 * customer's server sent, with {@code Error-Message} both at the top level and inside
 * {@code Multiple-Services-Credit-Control}.</p>
 */
public class ResponseListenerSctpDecodeTest extends MarbenConfigProvider {

    private static final long WAIT_SECONDS = 5;

    private final FakeSctpAssociation association = new FakeSctpAssociation();

    private final List<String> decoded = new CopyOnWriteArrayList<>();

    private final CountDownLatch decodedLatch = new CountDownLatch(1);

    private ResponseListener listener;

    private Thread listenerThread;

    @Before
    public void startListener() {
        listener = new ResponseListener();
        listener.setDecoder(new XmlDecoder(DICTIONARY_CONFIG) {
            @Override
            public String decode(final ByteBuffer data) {
                String xml = super.decode(data);
                decoded.add(xml);
                decodedLatch.countDown();
                return xml;
            }
        });
        listener.setChannel(ExtraChannel.withSctpReceiver(association));
        listenerThread = new Thread(listener, "sctp-listener-decode-test");
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
    public void ccaLongerThanReadBufferIsDecodedToXml() throws Exception {
        ByteBuffer dump = HexDumpReader.read("/hexdump/", "cca_big.hexdump.txt");
        byte[] cca = new byte[dump.remaining()];
        dump.get(cca);
        assertEquals("cca_big.hexdump.txt must hold the 612-byte CCA", 612, cca.length);

        association.deliver(cca);

        assertTrue("The decoder got no message within " + WAIT_SECONDS + " seconds",
                decodedLatch.await(WAIT_SECONDS, TimeUnit.SECONDS));
        String xml = decoded.get(0);
        assertTrue("Not a whole CCA: " + xml, xml.startsWith("<CCA>") && xml.endsWith("</CCA>"));
        assertEquals("Error-Message elements in " + xml, 2, xml.split("<Error-Message>", -1).length - 1);
    }
}
