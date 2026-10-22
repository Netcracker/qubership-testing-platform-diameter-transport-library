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
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Test;

/**
 * Fails when {@link ExtraChannel} returns a part of an SCTP message, or loses the rest of it,
 * because the message is longer than the buffer the caller offered.
 */
public class ExtraChannelSctpReadTest {

    private final FakeSctpAssociation association = new FakeSctpAssociation();

    private final ExtraChannel channel = ExtraChannel.withSctpReceiver(association);

    @Test
    public void messageLongerThanInitialBufferIsReturnedWhole() throws IOException {
        byte[] message = sequence(612);
        association.deliver(message);

        ByteBuffer received = channel.receiveSctpMessage(512);

        assertArrayEquals(message, Arrays.copyOf(received.array(), received.position()));
    }

    @Test
    public void messageSpanningSeveralEnlargementsIsReturnedWhole() throws IOException {
        byte[] message = sequence(100_000);
        association.deliver(message);

        ByteBuffer received = channel.receiveSctpMessage(512);

        assertArrayEquals(message, Arrays.copyOf(received.array(), received.position()));
    }

    @Test
    public void nextMessageStartsAfterTheEnlargedOne() throws IOException {
        byte[] first = sequence(612);
        byte[] second = sequence(40);
        association.deliver(first);
        association.deliver(second);

        channel.receiveSctpMessage(512);
        ByteBuffer received = channel.receiveSctpMessage(512);

        assertArrayEquals(second, Arrays.copyOf(received.array(), received.position()));
    }

    @Test
    public void shutdownOfAssociationIsReportedAsNull() throws IOException {
        association.shutdown();

        assertNull(channel.receiveSctpMessage(512));
    }

    @Test
    public void readFillsTheBufferWithAMessageThatFitsIt() throws IOException {
        byte[] message = sequence(612);
        association.deliver(message);
        ByteBuffer allocate = ByteBuffer.allocate(4096);

        int size = channel.read(allocate);

        assertEquals(612, size);
        assertArrayEquals(message, Arrays.copyOf(allocate.array(), size));
    }

    @Test(expected = IOException.class)
    public void readRefusesAMessageThatDoesNotFitTheBuffer() throws IOException {
        association.deliver(sequence(612));

        channel.read(ByteBuffer.allocate(512));
    }

    private static byte[] sequence(final int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (i % 251);
        }
        return bytes;
    }
}
