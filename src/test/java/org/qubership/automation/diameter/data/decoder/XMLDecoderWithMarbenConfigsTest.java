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

package org.qubership.automation.diameter.data.decoder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.diameter.HexDumpReader;
import org.qubership.automation.diameter.MarbenConfigProvider;
import org.qubership.automation.diameter.data.Decoder;
import org.qubership.automation.diameter.data.XmlDecoder;

public class XMLDecoderWithMarbenConfigsTest extends MarbenConfigProvider {

    private static final String HEX_DUMP_RESOURCE = "/hexdump/";

    private XmlDecoder decoder;

    @BeforeEach
    public void setUp() {
        decoder = new XmlDecoder(DICTIONARY_CONFIG);
    }

    @Test
    public void testDecodeSNRFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/snr.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<SNR>", "</SNR>");
    }

    @Test
    public void testDecodeSNRFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "snr.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<SNR>"));
        Assertions.assertTrue(message.contains("</SNR>"));
    }

    @Test
    public void testDecodeSNAFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "sna.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<SNA>"));
        Assertions.assertTrue(message.contains("</SNA>"));
    }

    @Test
    public void testDecodeSNAFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/sna.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<SNA>", "</SNA>");
    }

    @Test
    public void testDecodeCCRFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/ccr.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<CCR>", "</CCR>");
    }

    @Test
    public void testDecodeCCRFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "ccr.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<CCR>"));
        Assertions.assertTrue(message.contains("</CCR>"));
    }

    @Test
    public void testDecodeCCAFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/cca.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<CCA>", "</CCA>");
    }

    @Test
    public void testDecodeCCAFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "cca.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<CCA>"));
        Assertions.assertTrue(message.contains("</CCA>"));
    }

    @Test
    public void testDecodeDWRFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/dwr.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<DWR>", "</DWR>");
    }

    @Test
    public void testDecodeDWRFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "dwr.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<DWR>"));
        Assertions.assertTrue(message.contains("</DWR>"));
    }

    @Test
    public void testDecodeDWAFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/dwa.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<DWA>", "</DWA>");
    }

    @Test
    public void testDecodeDWAFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "dwa.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<DWA>"));
        Assertions.assertTrue(message.contains("</DWA>"));
    }

    @Test
    public void testDecodeCERFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/cer.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<CER>", "</CER>");
    }

    @Test
    public void testDecodeCERFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "cer.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<CER>"));
        Assertions.assertTrue(message.contains("</CER>"));
    }

    @Test
    public void testDecodeCEAFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/cea.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<CEA>", "</CEA>");
    }

    @Test
    public void testDecodeCEAFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "cea.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<CEA>"));
        Assertions.assertTrue(message.contains("</CEA>"));
    }

    @Test
    public void testDecodeSTRFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/str.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<STR>", "</STR>");
    }

    @Test
    public void testDecodeSTRFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "str.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<STR>"));
        Assertions.assertTrue(message.contains("</STR>"));
    }

    @Test
    public void testDecodeSLRFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/slr.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<SLR>", "</SLR>");
    }

    @Test
    public void testDecodeSLRFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "slr.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<SLR>"));
        Assertions.assertTrue(message.contains("</SLR>"));
    }

    @Test
    public void testDecodeSLAFromBytesAndMarbenConfigs() throws Exception {
        String message = new String(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/sla.bytes.txt")).readAllBytes(),
                StandardCharsets.UTF_8
        );
        validate(message, "<SLA>", "</SLA>");
    }

    @Test
    public void testDecodeSLAFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "sla.hexdump.txt");
        String message = decoder.decode(buffer);
        Assertions.assertTrue(message.contains("<SLA>"));
        Assertions.assertTrue(message.contains("</SLA>"));
    }

    private void validate(String string, String firstMatcher, String secondMatcher) {
        Decoder decoder = new XmlDecoder(DICTIONARY_CONFIG);
        byte[] buffer = getBytesFromString(string);
        String decode = decoder.decode(ByteBuffer.wrap(buffer));
        Assertions.assertTrue(decode.contains(firstMatcher));
        Assertions.assertTrue(decode.contains(secondMatcher));
    }

    private byte[] getBytesFromString(String string) {
        String[] split = string.split("\\s");
        ByteBuffer buffer = ByteBuffer.allocate(split.length);
        for (String value : split) {
            buffer.put(Byte.parseByte(value));
        }
        return buffer.array();
    }
}
