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
import org.qubership.automation.diameter.data.Converter;
import org.qubership.automation.diameter.data.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("AbbreviationAsWordInName")
public class RARInterceptor extends Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RARInterceptor.class);
    private final String dwa;
    private final Encoder encoder;
    private int hopByHop = 0;
    private int end2End = 0;

    public RARInterceptor(String dwa, Encoder encoder) {
        this(false, dwa, encoder);
    }

    /**
     * Constructor.
     *
     * @param deleteOnReceive - true/false, if interceptor should be deleted just after the message is received,
     * @param dwa - template of response,
     * @param encoder - Encoder instance link.
     */
    public RARInterceptor(boolean deleteOnReceive, String dwa, Encoder encoder) {
        super(deleteOnReceive, InterceptorTypes.RAR);
        this.dwa = dwa;
        this.encoder = encoder;
        setDeleteOnReceive(false);
    }

    @Override
    @SuppressWarnings("MethodName")
    public boolean _onReceive(String message, ExtraChannel channel) {
        if (StringUtils.startsWithIgnoreCase(message, "<RAR>")) {
            LOGGER.debug("RAR Received:\n{}", message);
            try {
                ByteBuffer encode = encoder.encode(dwa);
                encode = InterceptorUtils.setHbHAndE2E(encode, hopByHop, end2End);
                channel.write(encode);
                LOGGER.debug("RAA Sent:\n{}", dwa);
            } catch (Exception e) {
                LOGGER.error("Unable to encode RAA message", e);
            }
        }
        return false;
    }

    public void setE2e(byte[] e2e) {
        end2End = Converter.bytesToInt(e2e);
    }

    public void setHbh(byte[] hbh) {
        hopByHop = Converter.bytesToInt(hbh);
    }
}
