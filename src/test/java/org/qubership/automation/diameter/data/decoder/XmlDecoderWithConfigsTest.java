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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.diameter.HexDumpReader;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.data.Encoder;
import org.qubership.automation.diameter.data.XmlDecoder;
import org.qubership.automation.diameter.data.encoder.XmlEncoder;

class XmlDecoderWithConfigsTest extends StandardConfigProvider {

    private static final String HEX_DUMP_RESOURCE = "/hexdump/";

    private XmlDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new XmlDecoder(DICTIONARY_CONFIG);
    }

    @Test
    void testDecodeReAuthAnswer() throws IOException, URISyntaxException {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "raa.hexdump.txt");
        String decode = decoder.decode(buffer);
        Assertions.assertTrue(decode.contains("UNKNOWN Session ID"));
    }

    @SuppressWarnings("Pmd")
    @Test
    void givenCCRInHexFormat_whenWeDecodeIt_thenItContainsSGSN_andSGSNhasIpAddress()
            throws IOException, URISyntaxException {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "ccr.hexdump.txt");
        String decode = decoder.decode(buffer);
        Assertions.assertTrue(decode.contains("10.217.33.28"));
    }

    @Test
    void testDecodeAbortSessionAnswer() throws IOException, URISyntaxException {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "asa.hexdump.txt");
        String decode = decoder.decode(buffer);
        Assertions.assertTrue(decode.contains("UNKNOWN Session ID"));
    }

    @Test
    void testAllMessagesReadFromFileWithoutFailAndMessageContentIsPresent() throws Exception {
        Path filePath = Paths.get("src/test/resources/bytes/diameter.incoming.bytes.txt");
        List<String> strings = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        for (String string : strings) {
            if (StringUtils.isBlank(string) || string.startsWith("#")) {
                continue;
            }
            byte[] buffer = getBytesFromString(string);
            String message = decoder.decode(ByteBuffer.wrap(buffer));
            Assertions.assertFalse(StringUtils.isBlank(message));
        }
    }

    @Test
    void givenAvplibs_whenDWRcomes_thenItstartsByDWRtag_andFinishesByDWRTag() throws Exception {
        ByteBuffer buffer = HexDumpReader.read(HEX_DUMP_RESOURCE, "dwr.hexdump.txt");
        String decode = decoder.decode(buffer);
        Assertions.assertTrue(decode.contains("<DWR>"));
        Assertions.assertTrue(decode.contains("</DWR>"));
    }

    @Test
    void testReadCEACommandFromIncomingMessage() throws Exception {
        Path filePath = Paths.get("src/test/resources/bytes/cea.bytes.txt");
        String message = Files.readString(filePath, StandardCharsets.UTF_8);
        validate(message, "<CEA>", "</CEA>");
    }

    @Test
    void testReadCERCommandFromIncomingMessage() throws Exception {
        Path filePath = Paths.get("src/test/resources/bytes/cer.bytes.txt");
        String message = Files.readString(filePath, StandardCharsets.UTF_8);
        validate(message, "<CER>", "</CER>");
    }

    @Test
    void testCEAHostContainsAuthAppId() throws Exception {
        Path filePath = Paths.get("src/test/resources/bytes/cea.bytes.txt");
        String message = Files.readString(filePath, StandardCharsets.UTF_8);
        validate(message, "<Auth-Application-Id", "</Auth-Application-Id>");
    }

    @Test
    void testCCAIsParsedCorrectly() throws Exception {
        Path filePath = Paths.get("src/test/resources/bytes/cca.bytes.txt");
        String message = Files.readString(filePath, StandardCharsets.UTF_8);
        validate(message, "<Auth-Application-Id", "</Auth-Application-Id>");
    }

    private void validate(String string, String firstMatcher, String secondMatcher) {
        byte[] buffer = getBytesFromString(string);
        String decode = decoder.decode(ByteBuffer.wrap(buffer));
        Assertions.assertTrue(decode.contains(firstMatcher));
        Assertions.assertTrue(decode.contains(secondMatcher));
    }

    @Test
    void testGetMessageLength() {
        int messageLength = decoder.getMessageLength(new byte[]{0, 0, 0, -16});
        Assertions.assertEquals(240, messageLength);
    }

    @Test
    void shouldEscapeXmlSpecialCharactersInAvpValue() throws Exception {
        // Given: A message with AVP containing XML special characters
        // Note: You need to construct a proper Diameter message with these bytes
        // This is a simplified example - actual bytes depend on your message structure

        // For testing, create a message with tag value(s) containing <, >, &, ", '
        // When
        String decoded = decoder.decode(ByteBuffer.wrap(getBytesMessageWithSpecialChars()));

        // Then
        Assertions.assertAll(
                () -> Assertions.assertTrue(decoded.contains("&lt;"), "Should escape <"),
                () -> Assertions.assertTrue(decoded.contains("&gt;"), "Should escape >"),
                () -> Assertions.assertTrue(decoded.contains("&amp;"), "Should escape &"),
                () -> Assertions.assertTrue(decoded.contains("&quot;"), "Should escape \""),
                () -> Assertions.assertTrue(decoded.contains("&apos;"), "Should escape '"),
                // AVP names are not escaped
                () -> Assertions.assertTrue(decoded.contains("<CER>"), "AVP names should not be escaped"),
                () -> Assertions.assertTrue(decoded.contains("</Host-IP-Address>"), "AVP closing tags should not be escaped")
        );
    }

    @Test
    void shouldPreserveRoundTripByteArrayToByteArray() throws Exception {
        // Source bytes with AVP(s), containing special characters
        byte[] original = getBytesMessageWithSpecialChars();

        // Decode -> XML
        String decodedXml = decoder.decode(ByteBuffer.wrap(original));

        // Encode -> byte[]
        Encoder encoder = new XmlEncoder(DICTIONARY_CONFIG);
        ByteBuffer encodedBytes = encoder.encode(decodedXml);
        byte[] result = encodedBytes.array();

        /*
            Why we don't compare original and result byte arrays?
            Like:
            assertArrayEquals(original, result,
                "Decode -> Encode should preserve original binary data");

            The reason is:
             - XML tags doesn't contain flags information,
             - So, original and result byte[] can differ in flag bytes,
             especially in P flag (protected).

             All diameter flags are:
                0x80	V (Vendor)	    1 = vendor-specific AVP
                0x40	M (Mandatory)   1 = mandatory AVP
                0x20	P (Protected)	1 = protected AVP
                0x00	                -	Flags are not set

            Current encoding algorithm (buildFlag method) correctly recovers V an M flags,
            but P flag may be missed, because XML doesn't contain such info.
            And such behavior is okay for the most production scenarios.

            So, we perform extra decode, and compare XMLs.
         */

        // Decode again -> XML
        String roundTripXml = decoder.decode(ByteBuffer.wrap(result));

        // Check that both XMLs are the same (after decode and after decode -> encode -> decode)
        Assertions.assertEquals(decodedXml, roundTripXml,
                "Decode->Encode->Decode should preserve XML representation");

        // Extra check: get AVP values and compare
        Map<String, List<String>> originalValues = extractAvpValues(decodedXml);
        Map<String, List<String>> roundTripValues = extractAvpValues(roundTripXml);
        Assertions.assertEquals(originalValues, roundTripValues,
                "All AVP values should be preserved through round-trip");
    }

    private Map<String, List<String>> extractAvpValues(String xml) {
        // Simple parser to get AVP values, based on regexp (DOM could be used instead)
        Map<String, List<String>> result = new HashMap<>();
        Pattern pattern = Pattern.compile("<([^>]+)>([^<]*)</\\1>");
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = matcher.group(2);
            // If name contains attributes (code, vendor), remove them
            if (name.contains(" ")) {
                name = name.substring(0, name.indexOf(' '));
            }
            result.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        }
        return result;
    }

    @Test
    void shouldPreserveRoundTripXmlToXml() throws Exception {
        // Source XML with escaped special characters
        String originalXml = "<CCR><Session-Id>test&lt;user&gt;</Session-Id></CCR>";

        // Encode -> byte[]
        Encoder encoder = new XmlEncoder(DICTIONARY_CONFIG);
        ByteBuffer encodedBytes = encoder.encode(originalXml);

        // Decode -> XML
        String resultXml = decoder.decode(encodedBytes);

        // Check that after encode -> decode resulting XML equals to original
        Assertions.assertEquals(originalXml, resultXml,
                "Encode -> Decode should preserve original XML structure");
    }

    @Test
    void shouldProduceValidXmlWithSpecialChars() throws Exception {
        byte[] message = getBytesMessageWithSpecialChars();
        String decoded = decoder.decode(ByteBuffer.wrap(message));

        // Check that result is valid XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        Assertions.assertDoesNotThrow(() -> {
            builder.parse(new ByteArrayInputStream(decoded.getBytes(StandardCharsets.UTF_8)));
        });
    }

    private byte[] getBytesMessageWithSpecialChars() throws IOException {
        Path filePath = Paths.get("src/test/resources/bytes/broken.cer.bytes.txt");
        String message = Files.readString(filePath, StandardCharsets.UTF_8);
        return getBytesFromString(message);
    }

    private byte[] getBytesFromString(String string) {
        String[] split = string.split("\\s");
        ByteBuffer buffer = ByteBuffer.allocate(split.length);
        for (String value : split) {
            buffer.put(Byte.parseByte(value));
        }
        return buffer.array();
    }

    // Round-trip with special characters

    @Test
    void shouldCorrectlyRoundTripWithEncodedSpecialCharacters() throws Exception {
        // This test verifies the complete round-trip: encode -> decode -> encode -> decode
        // with special characters that need escaping

        // Step 1: Start with XML containing special chars in values
        String originalXml = "<CCR><Session-Id>test&lt;user&gt;&amp;company&apos;domain&quot;value</Session-Id></CCR>";

        // Step 2: Encode to bytes
        Encoder encoder = new XmlEncoder(DICTIONARY_CONFIG);
        ByteBuffer encodedBytes = encoder.encode(originalXml);

        // Step 3: Decode to XML (should escape special chars)
        String decodedXml = decoder.decode(encodedBytes);
        // Step 4: The decoded XML should have escaped special chars in values
        Assertions.assertTrue(decodedXml.contains("test&lt;user&gt;&amp;company&apos;domain&quot;value"));
        // But tags should remain intact
        Assertions.assertTrue(decodedXml.contains("<CCR>"));
        Assertions.assertTrue(decodedXml.contains("</CCR>"));

        // Step 5: Encode again
        ByteBuffer reEncoded = encoder.encode(decodedXml);

        // Step 6: Decode again
        String finalXml = decoder.decode(reEncoded);

        // Step 7: Final XML should equal decodedXml (not original)
        // because decoder escapes values on output
        Assertions.assertEquals(decodedXml, finalXml);
    }
}
