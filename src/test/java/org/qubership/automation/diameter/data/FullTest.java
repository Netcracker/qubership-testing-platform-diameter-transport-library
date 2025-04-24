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

package org.qubership.automation.diameter.data;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.data.encoder.XmlEncoder;

public class FullTest extends StandardConfigProvider {

    private final Encoder encoder = new XmlEncoder(DICTIONARY_CONFIG);
    private final Decoder decoder = new XmlDecoder(DICTIONARY_CONFIG);

    private static final String CER = "<CER>\n" +
            "    <Origin-Host code=\"264\">10.106.1.221</Origin-Host>\n" +
            "    <Teleservice-Code code=\"282\" vendor=\"13421\">555</Teleservice-Code>" +
            "    <Origin-Realm>our-company.com</Origin-Realm>\n" +
            "    <Host-IP-Address>10.232.4.144</Host-IP-Address>\n" +
            "    <Vendor-Id>0</Vendor-Id>\n" +
            "    <Product-Name>OurCompanySimulator</Product-Name>\n" +
            "    <Origin-State-Id>0</Origin-State-Id>\n" +
            "    <Auth-Application-Id>4</Auth-Application-Id>\n" +
            "    <SGSN-Address>20.43.23.56</SGSN-Address>\n" +
            "    <Vendor-Specific-Application-Id>\n" +
            "        <Vendor-Id>10415</Vendor-Id>\n" +
            "        <Auth-Application-Id>4</Auth-Application-Id>\n" +
            "    </Vendor-Specific-Application-Id>\n" +
            "    <Supported-Vendor-Id>10415</Supported-Vendor-Id>\n" +
            "</CER>";

    private static final String CER_WITH_AVP_CODE =
            "<CER>"
                + "<Origin-Host code=\"264\">10.106.1.221</Origin-Host>"
                + "<Teleservice-Code code=\"282\">555</Teleservice-Code>"
                + "<Origin-Realm code=\"296\">our-company.com</Origin-Realm>"
                + "<Host-IP-Address code=\"257\">10.232.4.144</Host-IP-Address>"
                + "<Vendor-Id code=\"266\">0</Vendor-Id>"
                + "<Product-Name code=\"269\">OurCompanySimulator</Product-Name>"
                + "<Origin-State-Id code=\"278\">0</Origin-State-Id>"
                + "<Auth-Application-Id code=\"258\">4</Auth-Application-Id>"
                + "<SGSN-Address code=\"1228\">20.43.23.56</SGSN-Address>"
                + "<Vendor-Specific-Application-Id code=\"260\">"
                    + "<Vendor-Id code=\"266\">10415</Vendor-Id>"
                    + "<Auth-Application-Id code=\"258\">4</Auth-Application-Id>"
                + "</Vendor-Specific-Application-Id>"
                + "<Supported-Vendor-Id code=\"265\">10415</Supported-Vendor-Id>"
            + "</CER>";

    @Test
    public void testDecodeAndEncodeMessage() throws Exception {
        decoder.setAppendAvpcode(true);
        decoder.setAppendAvpvendor(false);
        ByteBuffer encodedMessage = encoder.encode(CER);
        String message = decoder.decode(encodedMessage);
        decoder.setAppendAvpcode(false);
        assertEquals(CER_WITH_AVP_CODE, message);
    }
}
