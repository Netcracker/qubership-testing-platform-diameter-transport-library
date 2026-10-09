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

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.MessageInfo;

/**
 * Stands in for an SCTP association on the receiving side.
 *
 * <p>Like {@code SctpChannel.receive}, it blocks until a message is queued and copies at most
 * {@code dst.remaining()} bytes per call. A message that does not fit is reported as incomplete,
 * and the rest of it is delivered by the following calls.</p>
 */
final class FakeSctpAssociation implements ExtraChannel.SctpReceiver {

    private static final byte[] SHUTDOWN = new byte[0];

    private final BlockingQueue<byte[]> inbox = new LinkedBlockingQueue<>();

    private byte[] current;

    private int offset;

    /**
     * Queues a message as one SCTP packet.
     */
    void deliver(final byte[] message) {
        inbox.add(message);
    }

    /**
     * Queues the end of the association; the next {@code receive} returns {@code null}, as the JDK does
     * when the notification handler returns {@code HandlerResult.RETURN} on a shutdown notification.
     */
    void shutdown() {
        inbox.add(SHUTDOWN);
    }

    @Override
    public MessageInfo receive(final ByteBuffer dst, final ExtraChannel.ReceiveNotificationHandler handler)
            throws IOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new ClosedByInterruptException();
        }
        if (current == null) {
            try {
                current = inbox.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ClosedByInterruptException();
            }
            offset = 0;
            if (current == SHUTDOWN) {
                current = null;
                return null;
            }
        }
        int count = Math.min(dst.remaining(), current.length - offset);
        dst.put(current, offset, count);
        offset += count;
        boolean complete = offset == current.length;
        if (complete) {
            current = null;
        }
        return new PartInfo(count, complete);
    }

    private static final class PartInfo extends MessageInfo {

        private final int bytes;

        private final boolean complete;

        PartInfo(final int bytes, final boolean complete) {
            this.bytes = bytes;
            this.complete = complete;
        }

        @Override
        public int bytes() {
            return bytes;
        }

        @Override
        public boolean isComplete() {
            return complete;
        }

        @Override
        public SocketAddress address() {
            return null;
        }

        @Override
        public Association association() {
            return null;
        }

        @Override
        public MessageInfo complete(final boolean complete) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isUnordered() {
            return false;
        }

        @Override
        public MessageInfo unordered(final boolean unordered) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int payloadProtocolID() {
            return 0;
        }

        @Override
        public MessageInfo payloadProtocolID(final int ppid) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int streamNumber() {
            return 0;
        }

        @Override
        public MessageInfo streamNumber(final int streamNumber) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long timeToLive() {
            return 0;
        }

        @Override
        public MessageInfo timeToLive(final long millis) {
            throw new UnsupportedOperationException();
        }
    }
}
