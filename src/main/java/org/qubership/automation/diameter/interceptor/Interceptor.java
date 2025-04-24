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

package org.qubership.automation.diameter.interceptor;

import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.qubership.automation.diameter.connection.ExtraChannel;
import org.qubership.automation.diameter.data.Converter;

public abstract class Interceptor {
    private final String id = UUID.randomUUID().toString();
    private boolean deleteOnReceive = true;
    private boolean isExpired = false;
    private String message;
    private boolean isFailed = false;
    private boolean receive;
    private String type = "unknown";
    private String sessionId = "";
    private int hopByHop = 0;
    private int end2End = 0;

    public Interceptor() {
    }

    public Interceptor(boolean deleteOnReceive, String interceptorType) {
        this.deleteOnReceive = deleteOnReceive;
        this.type = interceptorType;
    }

    /**
     * On-Receive method, which is invoked on each message is received.
     *
     * @param message - Received message,
     * @param channel - channel where the message is received,
     * @return true/false result of processing.
     */
    public final boolean onReceive(final String message, final ExtraChannel channel) {
        boolean receive = false;
        try {
            receive = _onReceive(message, channel);
            if (receive) {
                isFailed = false;
                this.message = message;
                synchronized (this) {
                    this.receive = true;
                    if (channel != null) {
                        channel.setLastDwrTime(System.currentTimeMillis());
                    }
                }
            }
            return receive;
        } finally {
            if (receive) {
                wakeUpMainThread();
            }
        }
    }

    private void wakeUpMainThread() {
        synchronized (this) {
            notify();
        }
    }

    @SuppressWarnings("MethodName")
    protected abstract boolean _onReceive(final String message, final ExtraChannel channel);

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getHopByHop() {
        return hopByHop;
    }

    public void setHopByHop(byte[] hopByHop) {
        this.hopByHop = Converter.bytesToInt(hopByHop);
    }

    public int getEnd2End() {
        return end2End;
    }

    public void setEnd2End(byte[] e2e) {
        this.end2End = Converter.bytesToInt(e2e);
    }

    public boolean isDeleteOnReceive() {
        return deleteOnReceive;
    }

    public void setDeleteOnReceive(boolean deleteOnReceive) {
        this.deleteOnReceive = deleteOnReceive;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    /**
     * Set error information into message.
     *
     * @param message - String message to fill out with error information,
     * @param ex - Throwable to get stack trace for message.
     */
    public void setError(String message, Throwable ex) {
        this.isFailed = true;
        this.message = message;
        this.message += '\n' + ExceptionUtils.getStackTrace(ex);
    }

    public String getResponse() {
        return this.message;
    }
    
    public boolean isReceived() {
        return this.receive;
    }

    public boolean isFailed() {
        return isFailed;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Interceptor)) {
            return false;
        }
        Interceptor that = (Interceptor) object;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
