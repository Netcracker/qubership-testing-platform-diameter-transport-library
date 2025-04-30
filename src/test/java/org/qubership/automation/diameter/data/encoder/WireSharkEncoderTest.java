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

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.data.Decoder;
import org.qubership.automation.diameter.data.XmlDecoder;
import org.qubership.automation.diameter.data.encoder.wireshark.WireSharkEncoder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class WireSharkEncoderTest extends StandardConfigProvider {

    private final Decoder decoder = new XmlDecoder(DICTIONARY_CONFIG);
    private WireSharkEncoder encoder;

    public static Document loadXML(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource source = new InputSource(new StringReader(xml));
        return builder.parse(source);
    }

    @Before
    public void setUp() {
        encoder = new WireSharkEncoder(DICTIONARY_CONFIG);
    }

    @Test
    public void testParseWireSharkHeader() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/wireshark/CCR.wireshark")),
                StandardCharsets.UTF_8);
        ByteBuffer encode = encoder.encode(message);
        byte[] array = encode.array();
        assertEquals(0x1, array[0]);
        assertEquals(0x4, array[11]);
    }

    @Test
    public void givenCCRwithSGSN_andSGSNHasHumanValue_andSGSNHasHexValue_andFormatIsWireShark_whenWeEncodeThisMessage_thenAVPConverted2Byte_andUsedHexValue()
            throws Exception {
        String message = IOUtils.toString(Objects.requireNonNull(
                getClass().getResourceAsStream("/wireshark/givenCCR_andAVPwithOriginalValue.txt")),
                StandardCharsets.UTF_8);
        ByteBuffer encode = encoder.encode(message);
        byte[] actual = encode.array();
        String actualStr = new String(actual, StandardCharsets.UTF_8);
        byte[] expected = new byte[]{0, 1, -39, 72, -24, 3};
        String expectedStr = new String(expected, StandardCharsets.UTF_8);
        assertEquals(String.format("message \n %s \n does not contain value \n %s \n for AVP %s ",
                        ArrayUtils.toString(actual), ArrayUtils.toString(expected), "SGSN"), 608,
                actualStr.indexOf(expectedStr));
    }

    private void encodeDecodeAndCheck(String message, String tagName, String expectedNodeName) throws Exception {
        ByteBuffer encode = encoder.encode(message);
        String actual = decoder.decode(encode);
        Pattern p = Pattern.compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFF]+");
        String actual2XmlString = p.matcher(actual).replaceAll("");
        assertEquals(tagName + " AVP should be on a first level", expectedNodeName,
                loadXML(actual2XmlString).getElementsByTagName(tagName).item(0).getParentNode()
                        .getNodeName());
    }

    @Test
    public void givenCCR_andEventTimestampIsOnFirstLevel_andEventTimeStampIsAfterServiceInfo_whenWeEncodeThisMessage_andWeDecodeIt_thenEventTimestampIsOnFirstLevel()
            throws Exception {
        String message = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/wireshark/givenCCR_andEventTimestampIsOnFirstLevel_andEventTimeStampIsAfterServiceInfo.wireshark")),
                StandardCharsets.UTF_8);
        encodeDecodeAndCheck(message, "Event-Timestamp", "CCR");
    }

    @SuppressWarnings("NonAsciiCharacters")
    @Test
    public void givenCCR_andUserEquipmentOnFirstLevel_andAVPBeforeEquipmentContainsEmptyData_whenWeEncodeThisMessage_andWeDecodeIt_thenEquipmentIsOnFirstLevel()
            throws Exception {
        String message = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/wireshark/givenCCR_andUserEquipmentOnFirstLevel_andAVPBeforeEquipmentContainsEmptyData.wireshark")),
                StandardCharsets.UTF_8);
        encodeDecodeAndCheck(message, "User-Equipment-Info", "CCR");
    }

    @Test
    public void givenCER_whenWeEncode_thenItConvertsToBytes() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/wireshark/CER.wireshark")),
                StandardCharsets.UTF_8);
        ByteBuffer encode = encoder.encode(message);
        byte[] actual = encode.array();
        String actualStr = new String(actual, StandardCharsets.UTF_8);
        byte[] expected = new byte[]{1, 0, 0, -76, -128, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 1, 8, 64, 0, 0, 36, 115, 111, 109, 101, 45, 97, 100, 100,
                114, 101, 115, 115, 46, 111, 117, 114, 45, 99, 111, 109, 112, 97, 110, 121, 46, 99,
                111, 109, 0, 0, 1, 40, 64, 0, 0, 23, 111, 117, 114, 45, 99, 111, 109, 112,
                97, 110, 121, 46, 99, 111, 109, 0, 0, 0, 1, 1, 64, 0, 0, 16, 48, 65,
                55, 48, 48, 55, 52, 57, 0, 0, 1, 10, 0, 0, 0, 12, 0, 0, 0, -63,
                0, 0, 1, 13, 0, 0, 0, 35, 68, 83, 67, 44, 32, 40, 99, 41, 32, 69,
                114, 105, 99, 115, 115, 111, 110, 32, 71, 109, 98, 72, 32, 50, 48, 49, 50, 0,
                0, 0, 1, 2, 64, 0, 0, 12, 0, 0, 0, 4, 0, 0, 1, 43, 64, 0,
                0, 12, 0, 0, 0, 0, 0, 0, 1, 11, 64, 0, 0, 12, 0, 0, 0, 1};
        String expectedStr = new String(expected, StandardCharsets.UTF_8);
        assertEquals(String.format("message \n %s \n does not contain value \n %s \n for AVP %s ",
                ArrayUtils.toString(actual), ArrayUtils.toString(expected), "SGSN"), 0,
                actualStr.indexOf(expectedStr));
    }
}
