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

import static org.qubership.automation.diameter.data.XMLStringDataProcessor.getInterceptorTypeByRequestMessage;
import static org.qubership.automation.diameter.data.XMLStringDataProcessor.isNotificationRequest;
import static org.qubership.automation.diameter.interceptor.InterceptorUtils.saveHBhAndE2eToInterceptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.diameter.data.Converter;
import org.qubership.automation.diameter.data.Decoder;
import org.qubership.automation.diameter.interceptor.DWRInterceptor;
import org.qubership.automation.diameter.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ResponseListener implements Runnable {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseListener.class);

    /**
     * Empty ByteBuffer[] constant.
     */
    private static final ByteBuffer[] EMPTY = {};

    /**
     * Set of interceptors.
     */
    @lombok.Getter
    private final Set<Interceptor> interceptors = Sets.newConcurrentHashSet();

    /**
     * Channel.
     */
    @lombok.Setter
    @lombok.Getter
    private ExtraChannel channel;

    /**
     * Flag if listening is stopped (true) or not.
     */
    private boolean stopped = false;

    /**
     * Decoder object to use to decode received messages.
     */
    @lombok.Setter
    @lombok.Getter
    private Decoder decoder;

    /**
     * Byte[] buffer to process messages.
     */
    private byte[] buffer = new byte[]{};

    /**
     * Invoke listening loop via TCP/SCTP channel, according to channel transport.
     */
    @Override
    public void run() {
        ByteBuffer allocate = ByteBuffer.allocate(512);
        switch (channel.getTransport()) {
            case TCP:
                runTcp(allocate);
                break;
            case SCTP:
                runSctp(allocate);
                break;
            default:
                throw new IllegalStateException("Unexpected type of transport: " + channel.getTransport());
        }
    }

    private void runTcp(final ByteBuffer allocate) {
        while (!Thread.interrupted() && !stopped) {
            try {
                int keys = channel.getSelector().selectNow();
                if (keys < 1) {
                    waitTimeout();
                    continue;
                }
            } catch (IOException e) {
                LOGGER.error("Exception occurred on channels selection", e);
                continue;
            }
            int read = readTcp(allocate);
            if (read < 0) {
                LOGGER.info("Socket is closed, thread will be interrupted");
                return;
            }
            processData(allocate, read);
        }
        try {
            channel.close();
        } catch (IOException ex) {
            LOGGER.warn("Exception occurred while connection closing", ex);
        }
    }

    private void runSctp(final ByteBuffer allocate) {
        while (!Thread.interrupted() && !stopped) {
            int read = readSctp(allocate);
            if (read < 0) {
                LOGGER.info("Socket is closed, thread will be interrupted");
                return;
            }
            if (read == 0) {
                waitTimeout();
                continue;
            }
            processData(allocate, read);
        }
        try {
            channel.close();
        } catch (IOException ex) {
            LOGGER.warn("Exception occurred while connection closing", ex);
        }
    }

    /**
     * Method will parse data. In case is not full data received,
     * the part of received must be saved into {@code buffer}.
     * In case data is not properly completed, but received new message,
     * then the data will be written as fallout to log file.
     * Method is rewritten to avoid too detailed logging in case no errors,
     * but not to miss important information in case error.
     * So, the method code became excessive.
     *
     * @param allocate   - {@link ByteBuffer} data which has been read from socket
     * @param readLength - how many bytes were read from socket.
     */
    public void processData(final ByteBuffer allocate, final int readLength) {
        byte[] content = null;
        ByteBuffer[] messages = null;
        ByteBuffer message = null;
        String currentData = null;
        try {
            content = sliceContent(allocate, readLength);
            cleanupBuffer(content);
            LOGGER.debug("Received byte data:\n{}", Arrays.toString(content));
            appendBuffer(content);
            messages = getMessages(buffer);
            for (int i = 0, messagesLength = messages.length; i < messagesLength; i++) {
                currentData = null;
                message = messages[i];
                LOGGER.debug("Found command data:\n{}", Arrays.toString(message.array()));
                String data = getData(message);
                currentData = data;
                LOGGER.debug("Found message:\n{}", data);
                processInterceptors(data, message.array());
            }
        } catch (Exception e) {
            processError("Failed parsing diameter message", e);
            LOGGER.error("Failed parsing diameter message, byte data:\n{}{}{}",
                    Arrays.toString(content),
                    (message == null)
                            ? ""
                            : (messages.length == 1)
                                ? ""
                                : "\nFound command data:\n" + Arrays.toString(message.array()),
                    (currentData == null)
                            ? ""
                            : "\nFound message:\n" + currentData,
                    e);
        } finally {
            allocate.clear();
        }
    }

    /**
     * Perform buffer cleanup after checking that content contains diameter message & version, but buffer isn't empty.
     *
     * @param content - content buffer, possibly containing diameter message.
     */
    public void cleanupBuffer(final byte[] content) {
        if (content.length < 8) {
            return;
        }
        boolean isVersionPresent = content[0] == 1;
        boolean isDiameterMessage =
                content[4] == (byte) -128
                        || content[4] == (byte) 64
                        || content[4] == (byte) 40
                        || content[4] == (byte) 20;
        if (isDiameterMessage && isVersionPresent && buffer.length > 0) {
            LOGGER.warn("Fall out message with buffer {} \n content: {}", Arrays.toString(buffer),
                    Arrays.toString(content));
            buffer = new byte[]{};
        }
    }

    /**
     * Append content into the end of buffer.
     * Buffer is reallocated with a proper size.
     *
     * @param content - content buffer to append to the buffer.
     */
    public void appendBuffer(final byte[] content) {
        byte[] array = new byte[content.length + buffer.length];
        System.arraycopy(buffer, 0, array, 0, buffer.length);
        System.arraycopy(content, 0, array, buffer.length, content.length);
        buffer = array;
    }

    /**
     * Get messages array from byte[] data.
     *
     * @param data - byte array to process.
     * @return ByteBuffer[] array of messages.
     */
    public ByteBuffer[] getMessages(byte[] data) {
        List<ByteBuffer> buffers = new ArrayList<>();
        while (data.length > 4) {
            byte[] bytes = Arrays.copyOfRange(data, 0, 4);
            bytes[0] = 0;
            int length = getLength(Converter.bytesToInt(bytes));
            ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, length));
            buffers.add(buffer);
            LOGGER.debug("Add message part to set: {}", Arrays.toString(buffer.array()));
            if (length <= data.length) {
                data = Arrays.copyOfRange(data, length, data.length);
                LOGGER.debug("Remaining buffer size to process: {}", data.length);
            } else {
                return EMPTY;
            }
        }
        //Remove found messages
        buffers.forEach(buffer ->
                this.buffer = Arrays.copyOfRange(this.buffer, buffer.limit(), this.buffer.length));
        return buffers.toArray(new ByteBuffer[buffers.size()]);
    }

    private int getLength(final int length) {
        return length % 4 == 0 ? length : (int) (Math.ceil((double) length / 4) * 4);
    }

    private String getData(final ByteBuffer data) {
        return decoder.decode(data);
    }

    private byte[] sliceContent(final ByteBuffer allocate, final int read) {
        return Arrays.copyOfRange(allocate.array(), 0, read);
    }

    private void processError(final String message, final Throwable ex) {
        for (Interceptor interceptor : interceptors) {
            if (StringUtils.isBlank(interceptor.getResponse())) {
                interceptor.setError(message, ex);
            }
        }
    }

    private void processInterceptors(final String data, @Nullable final byte[] content) {
        Iterator<Interceptor> iterator = interceptors.iterator();
        while (iterator.hasNext()) {
            Interceptor interceptor = iterator.next();
            if (interceptor.isExpired()) {
                iterator.remove();
                continue;
            }
            if (ArrayUtils.isNotEmpty(content)) {
                setHbHAndE2E(data, content, interceptor);
            }
            if (interceptor.onReceive(data, channel) && interceptor.isDeleteOnReceive()) {
                iterator.remove();
            }
        }
    }

    private void setHbHAndE2E(final String data, final byte[] content, final Interceptor interceptor) {
        if (interceptor instanceof DWRInterceptor) {
            DWRInterceptor dwr = (DWRInterceptor) interceptor;
            dwr.setHopByHop(Arrays.copyOfRange(content, 12, 16));
            dwr.setEnd2End(Arrays.copyOfRange(content, 16, 20));
        } else if (isNotificationRequest(data)) {
            String interceptorType = getInterceptorTypeByRequestMessage(data);
            saveHBhAndE2eToInterceptor(data, content, interceptorType, interceptors);
        }
    }

    private int readTcp(final ByteBuffer allocate) {
        try {
            Iterator<SelectionKey> iter = channel.getSelector().selectedKeys().iterator();
            int read = 0;
            while (iter.hasNext()) {
                SelectionKey selectionKey = iter.next();
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                if (selectionKey.isValid() && selectionKey.isReadable()) {
                    iter.remove();
                    read += socketChannel.read(allocate);
                }
            }
            return read;
        } catch (IOException e) {
            if (stopped) {
                return -1;
            }
            throw new RuntimeException("Unable to read data from socket", e);
        }
    }

    private int readSctp(final ByteBuffer allocate) {
        try {
            return channel.read(allocate);
        } catch (IOException e) {
            if (stopped) {
                return -1;
            }
            throw new RuntimeException("Unable to read data from socket", e);
        }
    }

    private void waitTimeout() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            LOGGER.error("Sleep failed with error.", e);
        }
    }

    /**
     * Stop listening.
     */
    public void stop() {
        stopped = true;
    }

    /**
     * Add Set of interceptors into this.interceptors list.
     * (technical dept: it was List in the previous implementation, but currently it's Set.
     * So, the method can be simplified.)
     *
     * @param interceptor - set of interceptors to add to the list.
     */
    public void addInterceptors(final Set<Interceptor> interceptor) {
        for (Iterator<Interceptor> interceptorIterator = interceptor.iterator(); interceptorIterator.hasNext(); ) {
            Interceptor next = interceptorIterator.next();
            for (Interceptor interceptor1 : interceptors) {
                if (interceptor1.equals(next)) {
                    interceptorIterator.remove();
                    break;
                }
            }
        }
        this.interceptors.addAll(interceptor);
    }

    /**
     * Remove interceptor from interceptors Set.
     *
     * @param interceptor Interceptor to be removed.
     */
    public void remove(final Interceptor interceptor) {
        this.interceptors.remove(interceptor);
    }

    /**
     * Get byte[] copy of the buffer.
     *
     * @return byte[] copy of the buffer truncated to the actual buffer.length.
     */
    public byte[] getBuffer() {
        return Arrays.copyOf(buffer, buffer.length);
    }

    /**
     * Make String representation of the object.
     *
     * @return String representation of the object.
     */
    @Override
    public String toString() {
        return "ResponseListener{interceptors=" + interceptors + '}';
    }
}
