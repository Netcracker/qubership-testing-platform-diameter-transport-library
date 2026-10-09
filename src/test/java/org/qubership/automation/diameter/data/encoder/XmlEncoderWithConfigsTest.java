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

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.data.XmlDecoder;
import org.xml.sax.SAXParseException;

public class XmlEncoderWithConfigsTest extends StandardConfigProvider {

    private XmlEncoder encoder;
    private XmlDecoder decoder;

    @BeforeEach
    void setUp() {
        encoder = new XmlEncoder(DICTIONARY_CONFIG);
        decoder = new XmlDecoder(DICTIONARY_CONFIG);
    }

    @Test
    void testEncodeCommandCER() throws Exception {
        ByteBuffer encode = encoder.encode(XMLMessages.CER);
        Assertions.assertEquals(204, encode.array().length);
    }

    @Test
    void testEncodeCommandRAA() throws Exception {
        ByteBuffer encode = encoder.encode(XMLMessages.RAA);
        Assertions.assertEquals(124, encode.array().length);
    }

    @Test
    void shouldUnescapeXmlEntitiesDuringEncoding() throws Exception {
        // XML with escaped special characters in AVP value
        String xmlWithEscaped = "<CCR>\n  <Session-Id>test&lt;user&gt;&amp;company</Session-Id>\n</CCR>";

        ByteBuffer encoded = encoder.encode(xmlWithEscaped);

        // Decode back to verify unescape worked
        String decoded = decoder.decode(encoded);

        // The decoded XML should have the original unescaped values properly escaped again
        // (because decoder escapes values on output)
        Assertions.assertTrue(decoded.contains("test&lt;user&gt;&amp;company"));
        // Should not contain double-escaped values
        Assertions.assertFalse(decoded.contains("test&amp;lt;user&amp;gt;&amp;amp;company"));
    }

    @Test
    void shouldHandleMultipleEscapedEntitiesInAvpValue() throws Exception {
        String expectedOriginHost = "host&amp;domain&lt;test&gt; and 'plain name'";
        String expectedSessionId = "session \"Test\" with 'Sample text'";
        String xmlWithEscaped = "<CCR>"
                        + "<Origin-Host>" + expectedOriginHost + "</Origin-Host>"
                        + "<Session-Id>" + expectedSessionId + "</Session-Id>"
                        + "</CCR>";

        ByteBuffer encoded = encoder.encode(xmlWithEscaped);
        String decoded = decoder.decode(encoded);

        // All entities should be properly handled
        Assertions.assertTrue(decoded.contains(expectedOriginHost));
        Assertions.assertTrue(decoded.contains(expectedSessionId));
    }

    @Test
    void shouldHandleNonEscapedSpecialCharactersByThrowingException() {
        // XML with raw special characters is invalid
        String invalidXml = "<CCR><Origin-Host>host&domain<<test></Origin-Host></CCR>";

        Assertions.assertThrows(RuntimeException.class, () -> encoder.encode(invalidXml));
    }

    @Test
    void shouldPreserveNonEscapedValuesDuringEncodeDecode() throws Exception {
        // XML with normal (non-escaped) values
        String originalXml = "<CCR><Session-Id>normal-value-123</Session-Id></CCR>";

        ByteBuffer encoded = encoder.encode(originalXml);
        String decoded = decoder.decode(encoded);

        // Normal values should be preserved
        Assertions.assertEquals(originalXml, decoded);
    }

    @Test
    void shouldThrowExceptionWhenEncodingInvalidXml() {
        String invalidXml = "<CCR><Session-Id>unclosed";

        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> encoder.encode(invalidXml)
        );
        Assertions.assertTrue(exception.getMessage().contains("Encoding is failed for message"));
    }

    @Test
    void shouldPreserveRoundTripForXmlWithEscapedValues() throws Exception {
        // Original XML with escaped values
        String originalXml = "<CCR><Session-Id>test&lt;user&gt;</Session-Id></CCR>";

        // Encode -> decode
        ByteBuffer encoded = encoder.encode(originalXml);
        String decoded = decoder.decode(encoded);

        // Should be identical to original
        Assertions.assertEquals(originalXml, decoded);
    }

    @Test
    void shouldThrowExceptionWhenAvpValueIsEmptyAndRequired() {
        // Session-Id is mandatory and should not be empty
        String invalidXml = "<CCR><Session-Id></Session-Id></CCR>";

        Assertions.assertThrows(Exception.class, () -> encoder.encode(invalidXml));
    }

    @Test
    void shouldHandleWhitespaceInAvpValue() throws Exception {
        String xmlWithWhitespace = "<CCR><Session-Id>  value with spaces  </Session-Id></CCR>";

        ByteBuffer encoded = encoder.encode(xmlWithWhitespace);
        String decoded = decoder.decode(encoded);

        // Whitespace should be preserved
        Assertions.assertTrue(decoded.contains("  value with spaces  "));
    }

    // Defense against XXE - tests

    @Test
    void shouldBlockDoctypeDeclaration() {
        String maliciousXml = """
                <?xml version="1.0"?>
                <!DOCTYPE root [
                  <!ENTITY test SYSTEM "file:///etc/passwd">
                ]>
                <CCR>
                  <Session-Id>&test;</Session-Id>
                </CCR>""";

        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> encoder.encode(maliciousXml)
        );
        Assertions.assertInstanceOf(SAXParseException.class, exception.getCause());
        Assertions.assertTrue(exception.getCause().getMessage().contains("DOCTYPE is disallowed"));
    }

    @Test
    void shouldAllowValidXmlWithoutDoctype() {
        String validXml = """
                <CCR>
                  <Session-Id>test123</Session-Id>
                  <Origin-Host>host.com</Origin-Host>
                </CCR>""";

        Assertions.assertDoesNotThrow(() -> encoder.encode(validXml));
    }
}
