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

import static org.qubership.automation.diameter.data.XMLStringDataProcessor.getInterceptorTypeFromAnswerMessage;
import static org.qubership.automation.diameter.data.XMLStringDataProcessor.isNotificationAnswer;
import static org.qubership.automation.diameter.interceptor.InterceptorUtils.addHbhAndE2eHeaders;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.qubership.automation.diameter.data.Decoder;
import org.qubership.automation.diameter.data.Encoder;
import org.qubership.automation.diameter.data.XMLStringDataProcessor;
import org.qubership.automation.diameter.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class DiameterConnection implements Closeable {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DiameterConnection.class);

    /**
     * DPR message.
     */
    private final String dpr;

    /**
     * Decoder object used to decode messages from binary format.
     */
    @lombok.Setter
    @lombok.Getter
    private Decoder decoder;

    /**
     * Encoder object used to encode messages to binary format.
     */
    @lombok.Setter
    @lombok.Getter
    private Encoder encoder;

    /**
     * Channel.
     */
    @lombok.Getter
    private ExtraChannel channel;

    /**
     * Response Listener.
     */
    private ResponseListener responseListener = new ResponseListener();

    /**
     * Constructor.
     */
    public DiameterConnection() {
        this.dpr = null;
    }

    /**
     * Constructor.
     *
     * @param dpr String DPR message.
     */
    public DiameterConnection(final String dpr) {
        this.dpr = dpr;
    }

    /**
     * Setter for channel field.
     *
     * @param channel ExtraChannel object.
     */
    public void setSocketChannel(final ExtraChannel channel) {
        this.channel = channel;
    }

    /**
     * Send message through the connection.
     *
     * @param message - message body to send.
     * @return DiameterConnection itself.
     * @throws Exception - in case errors while sending.
     */
    public DiameterConnection send(final String message) throws Exception {
        LOGGER.debug("Send message to {}, message:\n{}", channel, message);
        try {
            ByteBuffer byteBuffer = prepare(message);
            this.channel.write(byteBuffer);
        } catch (Exception e) {
            LOGGER.error("Unable to send message to {}, message:\n{}", channel, message, e);
            throw e;
        }
        return this;
    }

    private ByteBuffer prepare(final String message) throws Exception {
        if (isNotificationAnswer(message)) {
            String interceptorType = getInterceptorTypeFromAnswerMessage(message);
            Map<String, Object> headers = new HashMap<>();
            String sessionId = XMLStringDataProcessor.getSessionId(message);
            Set<Interceptor> interceptors = responseListener.getInterceptors();
            addHbhAndE2eHeaders(sessionId, headers, interceptorType, interceptors);
            return encoder.encode(message, headers);
        } else {
            return encoder.encode(message);
        }
    }

    /**
     * Start listening connection in a new thread.
     * Synchronizing - like in the 'stopListening' method - is not needed here,
     * because 'startListening' method is invoked only from methods/blocks, which are already synchronized.
     *
     * @param connectionName - name of connection to set as thread name.
     */
    public void startListening(final Object connectionName) {
        responseListener.setChannel(channel);
        responseListener.setDecoder(decoder);
        Thread thread = new Thread(responseListener);
        thread.setName(String.format("Connection listener: %s", connectionName));
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Add all interceptors into responseListener interceptors set.
     *
     * @param interceptors Collection of interceptors to be added.
     */
    public void addInterceptors(final Collection<Interceptor> interceptors) {
        this.responseListener.addInterceptors(Sets.newHashSet(interceptors));
    }

    /**
     * Get interceptors of the responseListener.
     *
     * @return Set of responseListener's interceptors.
     */
    public Set<Interceptor> getInterceptors() {
        return this.responseListener.getInterceptors();
    }

    /**
     * Stop listening on the connection.
     * Synchronization is added because a connection can be used by more than one context executed simultaneously.
     * So, they possibly invoke 'stopListening' method in parallel,
     * and can face NullPointerException, if responseListener becomes null.
     */
    public synchronized void stopListening() {
        if (responseListener != null) {
            responseListener.stop();
            responseListener = null;
        }
    }

    /**
     * Remove the interceptor from the responseListener.
     *
     * @param interceptor Interceptor object to be removed.
     */
    public void removeInterceptor(final Interceptor interceptor) {
        this.responseListener.remove(interceptor);
    }

    /**
     * Send DPR message, sleep 500 ms, then close the channel.
     *
     * @throws IOException in case IO errors occurred.
     */
    @Override
    public void close() throws IOException {
        sendDpr();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            LOGGER.warn("Interrupted waiting after DPR is sent", ex);
        }
        channel.close();
    }

    private void sendDpr() {
        if (Objects.nonNull(dpr) && dpr.startsWith("<DPR>")) {
            try {
                send(dpr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Check if channel is open or not.
     *
     * @return true if channel is open, otherwise false.
     */
    public boolean isOpen() {
        return channel.isOpen();
    }
}
