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

import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;
import org.qubership.automation.diameter.HexDumpReader;
import org.qubership.automation.diameter.MarbenConfigProvider;
import org.qubership.automation.diameter.data.Decoder;
import org.qubership.automation.diameter.data.XmlDecoder;

public class XMLDecoderWithMarbenConfigsTest extends MarbenConfigProvider {

    private static final String HEX_DUMP_RESOURCE = "/hexdump/";

    private XmlDecoder decoder;

    @Before
    public void setUp() {
        decoder = new XmlDecoder(DICTIONARY_CONFIG);
    }

    @Test
    public void testDecodeSNRFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/snr.bytes.txt")));
        validate(message, StringContains.containsString("<SNR>"), StringContains.containsString("</SNR>"));
    }

    @Test
    public void testDecodeSNRFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "snr.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<SNR>"));
        assertThat(message, StringContains.containsString("</SNR>"));
    }

    @Test
    public void testDecodeSNAFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "sna.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<SNA>"));
        assertThat(message, StringContains.containsString("</SNA>"));
    }

    @Test
    public void testDecodeSNAFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/sna.bytes.txt")));
        validate(message, StringContains.containsString("<SNA>"), StringContains.containsString("</SNA>"));
    }

    @Test
    public void testDecodeCCRFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/ccr.bytes.txt")));
        validate(message, StringContains.containsString("<CCR>"), StringContains.containsString("</CCR>"));
    }

    @Test
    public void testDecodeCCRFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "ccr.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<CCR>"));
        assertThat(message, StringContains.containsString("</CCR>"));
    }

    @Test
    public void testDecodeCCAFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/cca.bytes.txt")));
        validate(message, StringContains.containsString("<CCA>"), StringContains.containsString("</CCA>"));
    }

    @Test
    public void testDecodeCCAFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "cca.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<CCA>"));
        assertThat(message, StringContains.containsString("</CCA>"));
    }

    @Test
    public void testDecodeDWRFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/dwr.bytes.txt")));
        validate(message, StringContains.containsString("<DWR>"), StringContains.containsString("</DWR>"));
    }

    @Test
    public void testDecodeDWRFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "dwr.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<DWR>"));
        assertThat(message, StringContains.containsString("</DWR>"));
    }

    @Test
    public void testDecodeDWAFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/dwa.bytes.txt")));
        validate(message, StringContains.containsString("<DWA>"), StringContains.containsString("</DWA>"));
    }

    @Test
    public void testDecodeDWAFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "dwa.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<DWA>"));
        assertThat(message, StringContains.containsString("</DWA>"));
    }

    @Test
    public void testDecodeCERFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/cer.bytes.txt")));
        validate(message, StringContains.containsString("<CER>"), StringContains.containsString("</CER>"));
    }

    @Test
    public void testDecodeCERFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "cer.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<CER>"));
        assertThat(message, StringContains.containsString("</CER>"));
    }

    @Test
    public void testDecodeCEAFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/cea.bytes.txt")));
        validate(message, StringContains.containsString("<CEA>"), StringContains.containsString("</CEA>"));
    }

    @Test
    public void testDecodeCEAFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "cea.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<CEA>"));
        assertThat(message, StringContains.containsString("</CEA>"));
    }

    @Test
    public void testDecodeSTRFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/str.bytes.txt")));
        validate(message, StringContains.containsString("<STR>"), StringContains.containsString("</STR>"));
    }

    @Test
    public void testDecodeSTRFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "str.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<STR>"));
        assertThat(message, StringContains.containsString("</STR>"));
    }

    @Test
    public void testDecodeSLRFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/slr.bytes.txt")));
        validate(message, StringContains.containsString("<SLR>"), StringContains.containsString("</SLR>"));
    }

    @Test
    public void testDecodeSLRFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "slr.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<SLR>"));
        assertThat(message, StringContains.containsString("</SLR>"));
    }

    @Test
    public void testDecodeSLAFromBytesAndMarbenConfigs() throws Exception {
        String message = IOUtils.toString(
                Objects.requireNonNull(getClass().getResourceAsStream("/bytes/sla.bytes.txt")));
        validate(message, StringContains.containsString("<SLA>"), StringContains.containsString("</SLA>"));
    }

    @Test
    public void testDecodeSLAFromHexAndMarbenConfigs() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "sla.hexdump.txt");
        String message = decoder.decode(buffer);
        assertThat(message, StringContains.containsString("<SLA>"));
        assertThat(message, StringContains.containsString("</SLA>"));
    }

    private void validate(String string, Matcher<String> firstMatcher, Matcher<String> secondMatcher) {
        Decoder decoder = new XmlDecoder(DICTIONARY_CONFIG);
        byte[] buffer = getBytesFromString(string);
        String decode = decoder.decode(ByteBuffer.wrap(buffer));
        assertThat(decode, firstMatcher);
        assertThat(decode, secondMatcher);
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
