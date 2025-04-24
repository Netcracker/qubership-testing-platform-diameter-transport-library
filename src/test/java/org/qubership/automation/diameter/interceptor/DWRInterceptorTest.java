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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.connection.ExtraChannel;
import org.qubership.automation.diameter.data.encoder.XmlEncoder;

public class DWRInterceptorTest extends StandardConfigProvider {

    @Test
    public void testInterceptorEncodeMessage() throws Exception {
        DWRInterceptor dwrInterceptor = new DWRInterceptor("<DWA></DWA>", new XmlEncoder(DICTIONARY_CONFIG));
        ExtraChannel channelMock = mock(ExtraChannel.class);
        dwrInterceptor.onReceive("<DWR></DWR>", channelMock);
        verify(channelMock).write(any(ByteBuffer.class));
    }

    @Test
    public void testInterceptorRAAEncodeMessage() throws Exception {
        RARInterceptor dwrInterceptor = new RARInterceptor(
                "<RAA>\n" + "	<Session-Id>479057006158</Session-Id>\n" + "	<Result-Code>2001</Result-Code>\n"
                        + "	<Origin-Host>457280362642.our-company.com</Origin-Host>\n"
                        + "	<Origin-Realm>our-company.com</Origin-Realm>\n"
                        + "	<Origin-State-Id>10</Origin-State-Id>\n" + "</RAA>", new XmlEncoder(DICTIONARY_CONFIG));
        ExtraChannel channelMock = mock(ExtraChannel.class);
        dwrInterceptor.onReceive("<RAR></RAR>", channelMock);
        verify(channelMock).write(any(ByteBuffer.class));
    }
}
