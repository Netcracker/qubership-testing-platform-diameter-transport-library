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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.core.StringContains;
import org.junit.Test;
import org.qubership.automation.diameter.HexDumpReader;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.data.Decoder;
import org.qubership.automation.diameter.data.XmlDecoder;

import com.google.common.io.Files;

public class XmlDecoderWithConfigsTest extends StandardConfigProvider {

    private static final String HEX_DUMP_RESOURCE = "/hexdump/";

    @Test
    public void testDecodeReAuthAnswer() throws IOException, URISyntaxException {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "raa.hexdump.txt");
        XmlDecoder decoder = new XmlDecoder(DICTIONARY_CONFIG);
        String decode = decoder.decode(buffer);
        assertThat(decode, StringContains.containsString("UNKNOWN Session ID"));
    }

    @SuppressWarnings("Pmd")
    @Test
    public void givenCCRInHexFormat_whenWeDecodeIt_thenItContainsSGSN_andSGSNhasIpAddress()
            throws IOException, URISyntaxException {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "ccr.hexdump.txt");
        XmlDecoder decoder = new XmlDecoder(DICTIONARY_CONFIG);
        String decode = decoder.decode(buffer);
        assertThat(decode, StringContains.containsString("10.217.33.28"));
    }

    @Test
    public void testDecodeAbortSessionAnswer() throws IOException, URISyntaxException {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "asa.hexdump.txt");
        XmlDecoder decoder = new XmlDecoder(DICTIONARY_CONFIG);
        String decode = decoder.decode(buffer);
        assertThat(decode, StringContains.containsString("UNKNOWN Session ID"));
    }

    @Test
    public void testAllMessagesReadFromFileWithoutFailAndMessageContentIsPresent() throws Exception {
        Decoder decoder = new XmlDecoder(DICTIONARY_CONFIG);
        File file = new File("src/test/resources/bytes/diameter.incoming.bytes.txt");
        @SuppressWarnings("UnstableApiUsage")
        List<String> strings = Files.readLines(file, Charset.defaultCharset());
        for (String string : strings) {
            if (StringUtils.isBlank(string) || string.startsWith("#")) {
                continue;
            }
            byte[] buffer = getBytesFromString(string);
            String message = decoder.decode(ByteBuffer.wrap(buffer));
            assertFalse(StringUtils.isBlank(message));
        }
    }

    @Test
    public void givenAvplibs_whenDWRcomes_thenItstartsByDWRtag_andFinishesByDWRTag() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "dwr.hexdump.txt");
        XmlDecoder decoder = new XmlDecoder(DICTIONARY_CONFIG);
        String decode = decoder.decode(buffer);
        assertThat(decode, StringContains.containsString("<DWR>"));
        assertThat(decode, StringContains.containsString("</DWR>"));
    }

    @Test
    public void testReadCEACommandFromIncomingMessage() throws Exception {
        @SuppressWarnings("UnstableApiUsage")
        String message = Files.toString(new File("src/test/resources/bytes/cea.bytes.txt"), Charset.defaultCharset());
        validate(message, StringContains.containsString("<CEA>"), StringContains.containsString("</CEA>"));
    }

    @Test
    public void testReadCERCommandFromIncomingMessage() throws Exception {
        @SuppressWarnings("UnstableApiUsage")
        String message = Files.toString(new File("src/test/resources/bytes/cer.bytes.txt"), Charset.defaultCharset());
        validate(message, StringContains.containsString("<CER>"), StringContains.containsString("</CER>"));
    }

    @Test
    public void testCEAHostContainsAuthAppId() throws Exception {
        @SuppressWarnings("UnstableApiUsage")
        String message = Files.toString(new File("src/test/resources/bytes/cea.bytes.txt"), Charset.defaultCharset());
        validate(message, StringContains.containsString("<Auth-Application-Id"),
                StringContains.containsString("</Auth-Application-Id>"));
    }

    @Test
    public void testCCAIsParsedCorrectly() throws Exception {
        @SuppressWarnings("UnstableApiUsage")
        String message = Files.toString(new File("src/test/resources/bytes/cca.bytes.txt"), Charset.defaultCharset());
        validate(message, StringContains.containsString("<Auth-Application-Id"),
                StringContains.containsString("</Auth-Application-Id>"));
    }

    private void validate(String string, Matcher<String> firstMatcher, Matcher<String> secondMatcher) {
        Decoder decoder = new XmlDecoder(DICTIONARY_CONFIG);
        byte[] buffer = getBytesFromString(string);
        String decode = decoder.decode(ByteBuffer.wrap(buffer));
        assertThat(decode, firstMatcher);
        assertThat(decode, secondMatcher);
    }

    @Test
    public void testGetMessageLength() {
        Decoder decoder = new XmlDecoder(DICTIONARY_CONFIG);
        int messageLength = decoder.getMessageLength(new byte[]{0, 0, 0, -16});
        assertEquals(240, messageLength);
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
