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

    private static final Logger LOGGER = LoggerFactory.getLogger(DiameterConnection.class);
    private final String dpr;
    private Decoder decoder;
    private Encoder encoder;
    private ExtraChannel channel;
    private ResponseListener responseListener = new ResponseListener();

    public DiameterConnection() {
        this.dpr = null;
    }

    public DiameterConnection(String dpr) {
        this.dpr = dpr;
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public void setDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    public void setSocketChannel(ExtraChannel channel) {
        this.channel = channel;
    }

    /**
     * Send message through the connection.
     *
     * @param message - message body to send.
     * @return DiameterConnection itself.
     * @throws Exception - in case errors while sending.
     */
    public DiameterConnection send(String message) throws Exception {
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

    private ByteBuffer prepare(String message) throws Exception {
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
    public void startListening(Object connectionName) {
        responseListener.setChannel(channel);
        responseListener.setDecoder(decoder);
        Thread thread = new Thread(responseListener);
        thread.setName(String.format("Connection listener: %s'", connectionName));
        thread.setDaemon(true);
        thread.start();
    }

    public void addInterceptors(Collection<Interceptor> interceptors) {
        this.responseListener.addInterceptors(Sets.newHashSet(interceptors));
    }

    public Set<Interceptor> getInterceptors() {
        return this.responseListener.getInterceptors();
    }

    /**
     * Stop listening on the connection.
     * Synchronization is added because a connection can be used by more than one context executed simultaneously.
     * So, they possibly invoke 'stopListening' method in parallel, and can face NullPointerException,
     * if responseListener becomes null (TASUP-11062)
     */
    public synchronized void stopListening() {
        if (responseListener != null) {
            responseListener.stop();
            responseListener = null;
        }
    }

    public void removeInterceptor(Interceptor interceptor) {
        this.responseListener.remove(interceptor);
    }

    public ExtraChannel getChannel() {
        return channel;
    }

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

    public boolean isOpen() {
        return channel.isOpen();
    }
}
