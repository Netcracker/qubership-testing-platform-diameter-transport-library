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

package org.qubership.automation.diameter.data.encoder;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.qubership.automation.diameter.data.encoder.wireshark.AvpRecord;
import org.qubership.automation.diameter.data.encoder.wireshark.WireSharkMessage;
import org.qubership.automation.diameter.data.encoder.wireshark.WireSharkMessageParser;

public class WireSharkMessageParserTest {

    private final String CEA = "    Version: 0x01\n" +
            "    Length: 240\n" +
            "    Flags: 0x00\n" +
            "    Command Code: 257 Capabilities-Exchange\n" +
            "    ApplicationId: Diameter Common Messages (0)\n" +
            "    Hop-by-Hop Identifier: 0x00000000\n" +
            "    End-to-End Identifier: 0x00000000\n" +
            "    [Request In: 952]\n" +
            "    [Response Time: 0.167462000 seconds]\n" +
            "    AVP: Result-Code(268) l=12 f=-M- val=DIAMETER_SUCCESS (2001)\n" +
            "    AVP: Host-IP-Address(257) l=14 f=-M- val=192.168.0.224\n" +
            "    AVP: Origin-Host(264) l=35 f=-M- val=cuberosmsmms.our-company.com\n" +
            "    AVP: Origin-Realm(296) l=22 f=-M- val=our-company.com\n" +
            "    AVP: Vendor-Id(266) l=12 f=-M- val=43069\n" +
            "    AVP: Product-Name(269) l=22 f=--- val=OurCompany OCS\n" +
            "    AVP: Supported-Vendor-Id(265) l=12 f=-M- val=10415\n" +
            "    AVP: Supported-Vendor-Id(265) l=12 f=-M- val=13019\n" +
            "    AVP: Supported-Vendor-Id(265) l=12 f=-M- val=5535\n" +
            "    AVP: Supported-Vendor-Id(265) l=12 f=-M- val=13421\n" +
            "    AVP: Supported-Vendor-Id(265) l=12 f=-M- val=3830\n" +
            "    AVP: Inband-Security-Id(299) l=12 f=-M- val=NO_INBAND_SECURITY (0)\n" +
            "    AVP: Firmware-Revision(267) l=12 f=--- val=1114482\n" +
            "    AVP: Auth-Application-Id(258) l=12 f=-M- val=Diameter Credit Control Application (4)";
    private WireSharkMessageParser parser;

    @Before
    public void setUp() {
        parser = new WireSharkMessageParser();
    }

    @Test
    public void testParseAVP() {
        AvpRecord avp = parser.parseAvp(
                "AVP: Origin-Host(264) l=32 f=-M- val=some-address.our-company.com",
                "").getRight();
        assertEquals("some-address.our-company.com", avp.getValue());
        assertEquals("264", avp.getCode());
        assertEquals("32", avp.getLength());
        assertEquals("-M-", avp.getFlags());
    }


    @Test
    public void testParseMessage() {
        WireSharkMessage message = parser.parse(this.CEA);
        assertEquals("0x00", message.getFlag());
        assertEquals("257", message.getCommand());
        assertEquals("0x01", message.getVersion());
        assertEquals("0", message.getApplicationId());
        List<Pair<Integer, AvpRecord>> records = message.getAvpRecords();
        assertAVP(records.get(0).getRight(), "268", "12", "-M-", "DIAMETER_SUCCESS (2001)");
        assertAVP(records.get(records.size() / 2).getRight(), "265", "12", "-M-", "13019");
        assertAVP(records.get(records.size() - 1).getRight(), "258", "12", "-M-", "Diameter Credit Control Application (4)");
    }

    @Test
    public void testParseAvpWithIncludedMessage() throws IOException {
        WireSharkMessage parse = parser.parse(IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/wireshark/CCR.wireshark"))));
        assertEquals("272", parse.getCommand());
        assertEquals(26, parse.getAvpRecords().size());
    }

    @Test
    //Original value is a value from a string, there is a first symbol equals AVP name. Line with SGSN-Address below contains an originValue for AVP SGSN-Address.
    /*
        AVP: SGSN-Address(1228) l=18 f=VM- vnd=TGPP val=217.72.232.3
            AVP Code: 1228 SGSN-Address
            AVP Flags: 0xc0, Vendor-Specific: Set, Mandatory: Set
            AVP Length: 18
            AVP Vendor Id: 3GPP (10415)
            SGSN-Address: 0001d948e803
                SGSN-Address Address Family: IPv4 (1)
                SGSN-Address Address: 217.72.232.3
            Padding: 0000
     */
    public void givenCCRWithSGSN_andSGSNHasHexValue_whenWeParseThisMessage_thenAVPHasHexValue() throws IOException {
        WireSharkMessage parse = parser.parse(IOUtils.toString(Objects.requireNonNull(
                        getClass().getResourceAsStream("/wireshark/givenCCR_andAVPwithOriginalValue.txt"))));
        assertEquals("0001d948e803", parse.getAvpRecords().get(35).getValue().getHexValue());
    }

    private void assertAVP(AvpRecord avpRecord, String code, String length, String flags, String value) {
        assertEquals(flags, avpRecord.getFlags());
        assertEquals(code, avpRecord.getCode());
        assertEquals(length, avpRecord.getLength());
        assertEquals(value, avpRecord.getValue());
    }
}
