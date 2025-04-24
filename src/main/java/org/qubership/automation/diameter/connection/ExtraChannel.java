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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.ShutdownNotification;

public class ExtraChannel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtraChannel.class);
    private static final long MINUTE = 60 * 1000;
    private static final int CONNECTION_TIMEOUT = Integer.parseInt(
            System.getProperty("diameter.connection.timeout", "10000"));
    private TransportType transport = TransportType.getType("tcp");
    private SctpChannel sctpChannel;
    private SocketChannel socketChannel;
    private Selector selector;
    private long lastDwrTime;

    private MessageInfo messageInfo = null;

    private InetSocketAddress inetSocketAddress;

    public ExtraChannel() {
        lastDwrTime = System.currentTimeMillis();
    }

    /**
     * Open a new channel via proper TransportType.
     *
     * @param transportType - SCTP/TCP transport type to open a channel.
     * @return ExtraChannel channel if opened successfully.
     * @throws IOException - in case errors while opening a channel.
     */
    public static ExtraChannel open(TransportType transportType) throws IOException {
        ExtraChannel result = new ExtraChannel();
        result.transport = transportType;
        if (result.transport == TransportType.SCTP) {
            LOGGER.debug("Open connection by sctp");
            result.sctpChannel = SctpChannel.open();
            LOGGER.debug("Connection is opened by sctp");
        } else {
            result.selector = Selector.open();
            result.socketChannel = SocketChannel.open();
            result.socketChannel.configureBlocking(false);
            result.socketChannel.register(result.selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
        }
        return result;
    }

    /**
     * Close the channel.
     *
     * @throws IOException - in case errors while closing of the channel.
     */
    public void close() throws IOException {
        if (this.transport == TransportType.SCTP) {
            this.sctpChannel.close();
        } else {
            this.socketChannel.close();
        }
    }

    /**
     * Read data from the channel into ByteBuffer allocate.
     *
     * @param allocate - buffer to place data read,
     * @return - count of bytes read.
     * @throws IOException - in case errors while reading from the channel.
     */
    public int read(ByteBuffer allocate) throws IOException {
        if (this.transport == TransportType.SCTP) {
            messageInfo = null;
            ReceiveNotificationHandler receive = new ReceiveNotificationHandler(this.sctpChannel);
            LOGGER.debug("Reading message by sctp");
            do {
                messageInfo = this.sctpChannel.receive(allocate, null, receive);
                LOGGER.debug("Reading [{}]", messageInfo.isComplete());
            } while (!messageInfo.isComplete());
            LOGGER.debug("Reading message by sctp size [{}]", messageInfo.bytes());
            return messageInfo.bytes();
        } else {
            int size = this.socketChannel.read(allocate);
            LOGGER.debug("Reading message by tcp size [{}]", size);
            return size;
        }
    }

    /**
     * Write ByteBuffer encode into the channel.
     *
     * @param encode - buffer to write into the channel.
     * @throws IOException - in case errors while sending.
     */
    public void write(ByteBuffer encode) throws IOException {
        if (this.transport == TransportType.SCTP) {
            Association association = sctpChannel.association();
            InetSocketAddress socketAddress = (InetSocketAddress) this.sctpChannel.getRemoteAddresses().toArray()[0];
            messageInfo = MessageInfo.createOutgoing(association, socketAddress, 0);
            LOGGER.debug("Sending message by sctp {}", ArrayUtils.toString(encode.array()));
            this.sctpChannel.send(encode, messageInfo);
            LOGGER.debug("Message is sent by sctp");
        } else {
            this.socketChannel.write(encode);
        }
    }

    /**
     * Connect to inetSocketAddress, using proper Transport Type.
     *
     * @param inetSocketAddress - address to connect to,
     * @return true if connected successfully; if not connected - false or throw IOException, in different cases,
     * @throws IOException - in case connection errors.
     */
    public boolean connect(InetSocketAddress inetSocketAddress) throws IOException {
        this.inetSocketAddress = inetSocketAddress;
        LOGGER.info("Connecting [{}]:{}", this.transport, inetSocketAddress);
        if (this.transport == TransportType.SCTP) {
            this.sctpChannel.connect(inetSocketAddress, 10000, 10000);
            LOGGER.debug("Connected by sctp to {}", inetSocketAddress);
            return true;
        } else {
            boolean connected = this.socketChannel.connect(inetSocketAddress);
            if (connected) {
                LOGGER.info("Connected to {}", inetSocketAddress);
                return true;
            }
            if (selector.select(CONNECTION_TIMEOUT) < 1) {
                throw new IOException("Connection timed out " + CONNECTION_TIMEOUT);
            }
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                if (!key.isValid() || !key.isConnectable()) {
                    continue;
                }
                keys.remove();
                try {
                    SocketChannel channel = (SocketChannel) key.channel();
                    while (channel.isConnectionPending()) {
                        channel.finishConnect();
                    }
                    LOGGER.info("Connecting to {} is finished", inetSocketAddress);
                    return true;
                } catch (IOException e) {
                    LOGGER.error("Connection failed", e);
                    key.cancel();
                }
            }
            return false;
        }
    }

    public boolean isOpen() {
        return lastDwrTime == 0L || lastDwrTime + MINUTE > System.currentTimeMillis();
    }

    class SendNotificationHandler extends AbstractNotificationHandler<Void> {
        boolean receivedCommUp;  // false 
        int maxInStreams;
        int maxOutStreams;

        @Override
        public HandlerResult handleNotification(
                Notification notification, Void attachment) {
            LOGGER.debug("Unknown notification type");
            return HandlerResult.CONTINUE;
        }

        @Override
        public HandlerResult handleNotification(AssociationChangeNotification notification, Void attachment) {
            LOGGER.debug("Association change notification: {}", notification.association());
            AssocChangeEvent event = notification.event();
            Association association = notification.association();
            LOGGER.debug("Event: {}", event);
            if (AssocChangeEvent.COMM_UP.equals(event)) {
                receivedCommUp = true;
            }
            this.maxInStreams = association.maxInboundStreams();
            this.maxOutStreams = association.maxOutboundStreams();

            return HandlerResult.RETURN;
        }

    }

    public class ReceiveNotificationHandler extends AbstractNotificationHandler<Object> {
        SctpChannel channel;

        ReceiveNotificationHandler(SctpChannel channel) {
            this.channel = channel;
        }

        @Override
        public HandlerResult handleNotification(Notification notification, Object attachment) {
            LOGGER.debug("Unknown notification type");
            return HandlerResult.CONTINUE;
        }

        @Override
        public HandlerResult handleNotification(AssociationChangeNotification notification, Object attachment) {
            LOGGER.debug("Association notification:  {}", notification.association());
            AssocChangeEvent event = notification.event();
            Association association = notification.association();
            LOGGER.debug("Event: {}", event);
            if (AssocChangeEvent.COMM_UP.equals(event)) {
                int outbound = association.maxOutboundStreams();
                int inbound = association.maxInboundStreams();
                LOGGER.debug("association [in: {}, out: {}]", inbound, outbound);
            }
            return HandlerResult.CONTINUE;
        }

        @Override
        public HandlerResult handleNotification(
                ShutdownNotification notification, Object attachment) {
            LOGGER.debug("Association shutdown: {}", notification.association());
            return HandlerResult.RETURN;
        }
    }

    public void setLastDwrTime(long lastDwrTime) {
        this.lastDwrTime = lastDwrTime;
    }

    public Selector getSelector() {
        return selector;
    }

    public TransportType getTransport() {
        return transport;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    @Override
    public String toString() {
        return inetSocketAddress == null ? "Not Connected yet" : inetSocketAddress.toString();
    }
}
