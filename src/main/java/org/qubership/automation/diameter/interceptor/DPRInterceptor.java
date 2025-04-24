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

import java.nio.ByteBuffer;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.diameter.connection.ExtraChannel;
import org.qubership.automation.diameter.data.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("AbbreviationAsWordInName")
public class DPRInterceptor extends Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DPRInterceptor.class);
    private final String dpa;
    private final Encoder encoder;

    public DPRInterceptor(String dpa, Encoder encoder) {
        this(false, dpa, encoder);
    }

    /**
     * Constructor.
     *
     * @param deleteOnReceive - true/false, if interceptor should be deleted just after the message is received,
     * @param dpa - template of response,
     * @param encoder - Encoder instance link.
     */
    public DPRInterceptor(boolean deleteOnReceive, String dpa, Encoder encoder) {
        super(deleteOnReceive, InterceptorTypes.DPR);
        this.dpa = dpa;
        this.encoder = encoder;
        setDeleteOnReceive(false);
    }

    @Override
    @SuppressWarnings("MethodName")
    public boolean _onReceive(String message, ExtraChannel channel) {
        if (StringUtils.startsWithIgnoreCase(message, "<DPR>")) {
            LOGGER.debug("DPR Received:\n{}", message);
            try {
                ByteBuffer encode = encoder.encode(dpa);
                encode = InterceptorUtils.setHbHAndE2E(encode, getHopByHop(), getEnd2End());
                channel.write(encode);
                channel.close();
                LOGGER.debug("DPA Sent:\n{}", dpa);
            } catch (Exception e) {
                LOGGER.error("Unable to encode DPA message", e);
            }
        }
        return false;
    }
}
