package org.qubership.automation.diameter.data.decoder;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.data.XmlDecoder;
import org.qubership.automation.diameter.exception.DecodeException;

/**
 * Security tests for XmlDecoder, checking validation of AVP length.
 */
public class XmlDecoderSecurityTest extends StandardConfigProvider {
    private XmlDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new XmlDecoder(DICTIONARY_CONFIG);
    }

    /**
     * Test 1: Valid message with valid AVP(s) should be decoded w/o errors.
     */
    @Test
    void shouldDecodeValidMessageWithCorrectAvpLengths() {
        // Given: Valid CER message with valid AVPs
        byte[] validMessage = createValidCerMessage();

        // When: Decode message
        String result = decoder.decode(ByteBuffer.wrap(validMessage));

        // Then: No exceptions. Successful parsing
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("<CER>"));
        Assertions.assertTrue(result.contains("</CER>"));
    }

    /**
     * Test 2: AVP with length < minimum (< 8 bytes).
     */
    @Test
    void shouldRejectAvpWithLengthLessThan8() {
        // Given: Message with AVP, which length = 4 (< 8)
        byte[] malformedMessage = createMessageWithAvpLength(4);

        // When/Then:
        String result = decoder.decode(ByteBuffer.wrap(malformedMessage));
        Assertions.assertNotNull(result);
        Assertions.assertEquals("<CER></CER>", result);
    }

    /**
     * Test 3: AVP with length = 0.
     */
    @Test
    void shouldRejectAvpWithLengthZero() {
        // Given: Message with AVP, which length = 0
        byte[] malformedMessage = createMessageWithAvpLength(0);

        // When/Then:
        String result = decoder.decode(ByteBuffer.wrap(malformedMessage));
        Assertions.assertNotNull(result);
        Assertions.assertEquals("<CER></CER>", result);
    }

    /**
     * Test 4: AVP with length > actual message length.
     */
    @Test
    void shouldRejectAvpWithLengthExceedingMessageLength() {
        // Given: Message with length = 100 bytes, but in the AVP length = 1000
        byte[] malformedMessage = createMessageWithAvpLengthExceedingMessage(1000);

        // When/Then: Should be DecodeException about incorrect length
        DecodeException exception = Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(malformedMessage)));

        // Then: There should be DecodeException about incorrect length in the cause
        Assertions.assertNotNull(exception);
        Assertions.assertTrue(exception.getMessage().contains("Failed parsing AVPs"));
        Throwable cause = exception.getCause();
        Assertions.assertNotNull(cause);
        Assertions.assertInstanceOf(DecodeException.class, cause);
        Assertions.assertEquals("AVP length exceeds message: 1000 > 80 (AVP code: 264)", cause.getMessage());
    }

    /**
     * Test 5: AVP with length = 16_000_000 (theoretical maximum).
     */
    @Test
    void shouldRejectAvpWithMaximumPossibleLength() {
        // Given: Message with AVP length = 16_000_000
        int maxLength = 16_000_000;
        byte[] malformedMessage = createMessageWithAvpLength(maxLength);

        // When/Then: Should be DecodeException
        DecodeException exception = Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(malformedMessage)));

        Assertions.assertNotNull(exception);
        Assertions.assertTrue(exception.getMessage().contains("Failed parsing AVPs"));
        Throwable cause = exception.getCause();
        Assertions.assertNotNull(cause);
        Assertions.assertInstanceOf(DecodeException.class, cause);
        Assertions.assertEquals("AVP size exceeds limit: 16000000 > 1048576 (AVP code: 264)",
                cause.getMessage());
    }

    /**
     * Test 6: AVP with length = 16_000_000, but it's the only AVP in the message,
     * and message length is greater than 16_000_000.
     */
    @Test
    void shouldRejectSingleAvpWithMaximumLength() {
        // Given: Message with 1 AVP, length = 16_000_000,
        // and message in fact has such length
        byte[] malformedMessage = createSingleAvpMessageWithLength(16_000_000);

        // When: Decode message, measure processing time
        long startTime = System.currentTimeMillis();
        Exception exception = Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(malformedMessage)));
        long duration = System.currentTimeMillis() - startTime;

        // Then: Should be quick, because exception should be thrown due to message size limit
        Assertions.assertTrue(duration < 1000, "Decode took " + duration + "ms, should be < 1000ms");

        Assertions.assertTrue(exception.getMessage().contains("Failed parsing AVPs"));
        Throwable cause = exception.getCause();
        Assertions.assertNotNull(cause);
        Assertions.assertInstanceOf(DecodeException.class, cause);
        Assertions.assertEquals("AVP size exceeds limit: 16000000 > 1048576 (AVP code: 264)",
                cause.getMessage());
    }

    /**
     * Test 7: AVP with length = 1_000_000 (below configured limit).
     */
    @Test
    void shouldAcceptAvpWithLengthWithinConfiguredLimit() {
        // Given: Message with AVP length = 1_000_000
        // It's valid in case MAX_AVP_SIZE >= 1_000_000
        byte[] validMessage = createMessageWithAvpLength(1_000_000);

        // When: Decode message
        String result = decoder.decode(ByteBuffer.wrap(validMessage));

        // Then: Should be processed successfully
        Assertions.assertNotNull(result);
    }

    /**
     * Test 9: Some (2+) AVPs, one of them has oversized length.
     */
    @Test
    void shouldStopProcessingOnFirstOversizedAvp() {
        // Given: Message with 2 AVPs. The 1st AVP is valid, the 2nd is oversized
        byte[] malformedMessage = createMessageWithMixedAvpLengths();

        // When/Then: Should be DecodeException
        Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(malformedMessage)));

        // Should be thrown on the 1st incorrect AVP
    }

    /**
     * Test 10: Vendor-Specific AVP with oversized length.
     */
    @Test
    void shouldRejectVendorSpecificAvpWithOversizedLength() {
        // Given: Vendor-Specific AVP (V bit=1) with length = 16_000_000
        byte[] malformedMessage = createVendorSpecificAvpWithLength(16_000_000);

        // When/Then: Should be DecodeException
        Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(malformedMessage)));
    }

    // ==================== Performance tests ==================

    /**
     * Performance test #1: attack with 16 Mb AVP should be processed quickly.
     */
    @Test
    void shouldNotHangOnOversizedAvp() {
        // Given: Message with AVP length = 16_000_000
        byte[] attackMessage = createMessageWithAvpLength(16_000_000);

        long startTime = System.currentTimeMillis();
        Exception exception = Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(attackMessage)));
        long duration = System.currentTimeMillis() - startTime;

        // Then: Should be quick (< 100 ms)
        Assertions.assertTrue(duration < 100,
                "Decode took " + duration + "ms, should be < 100ms");

        Assertions.assertTrue(exception.getMessage().contains("Failed parsing AVPs"));
        Throwable cause = exception.getCause();
        Assertions.assertNotNull(cause);
        Assertions.assertInstanceOf(DecodeException.class, cause);
        Assertions.assertEquals("AVP size exceeds limit: 16000000 > 1048576 (AVP code: 264)",
                cause.getMessage());
    }

    /**
     * Performance test #2: 2+ attacks in the row.
     */
    @Test
    void shouldHandleMultipleAttacksEfficiently() {
        int attackCount = 10;
        long totalTime = 0;

        for (int i = 0; i < attackCount; i++) {
            byte[] attackMessage = createMessageWithAvpLength(1_100_000);
            long startTime = System.currentTimeMillis();
            Assertions.assertThrows(DecodeException.class,
                    () -> decoder.decode(ByteBuffer.wrap(attackMessage)));
            totalTime += System.currentTimeMillis() - startTime;
        }

        // Then: Average timw < 50 ms per attack
        long averageTime = totalTime / attackCount;
        Assertions.assertTrue(averageTime < 50,
                "Average time " + averageTime + "ms should be < 50ms");
    }

    // ==================== Helper methods (create messages) ====================

    /**
     * Create valid CER message for positive test
     */
    private byte[] createValidCerMessage() {
        // Minimum valid CER message
        // CER: command code 257, Request flag=1
        return new byte[] {
                // Header (20 bytes)
                1, 0, 0, 20,     // Version=1, Length=20
                -0x80, 0, 1, 1,   // Flags=-0x80 (Request), Command=257 (CER)
                0, 0, 0, 0,      // Application-ID=0
                0, 0, 0, 1,      // Hop-by-Hop
                0, 0, 0, 1       // End-to-End
        };
    }

    /**
     * Create message with 1 AVP, which length is set from avpLength parameter.
     *
     * @param avpLength Length of AVP (can be incorrect)
     * @return Populated message.
     */
    private byte[] createMessageWithAvpLength(int avpLength) {
        // Create basic message with 1 AVP (Origin-Host)
        // AVP code = 264 (Origin-Host), type UTF8String

        int messageLength = 20 + roundLength(avpLength); // 20 bytes header + AVP
        byte[] message = new byte[messageLength];

        // Message header (CER)
        message[0] = 1;  // Version
        message[1] = (byte)((messageLength >> 16) & 0xFF);
        message[2] = (byte)((messageLength >> 8) & 0xFF);
        message[3] = (byte)(messageLength & 0xFF);
        message[4] = (byte) 0x80; // Request flag
        message[5] = 0x00;
        message[6] = 0x01;
        message[7] = 0x01; // Command=257 (CER)
        // Application-ID = 0
        message[8] = 0x00;
        message[9] = 0x00;
        message[10] = 0x00;
        message[11] = 0x00;
        // Hop-by-Hop = 1
        message[12] = 0x00;
        message[13] = 0x00;
        message[14] = 0x00;
        message[15] = 0x01;
        // End-to-End = 1
        message[16] = 0x00;
        message[17] = 0x00;
        message[18] = 0x00;
        message[19] = 0x01;

        // AVP header (8 bytes)
        int offset = 20;
        try {
            message[offset] = 0x00; // AVP Code (264 = 0x0108)
            message[offset + 1] = 0x00;
            message[offset + 2] = 0x01;
            message[offset + 3] = (byte) 0x08;
            message[offset + 4] = 0x40; // M bit=1
            message[offset + 5] = (byte) ((avpLength >> 16) & 0xFF);
            message[offset + 6] = (byte) ((avpLength >> 8) & 0xFF);
            message[offset + 7] = (byte) (avpLength & 0xFF);

            // AVP Body (if any)
            if (avpLength > 8) {
                int bodyLength = avpLength - 8;
                // Fill with test data
                for (int i = 0; i < bodyLength && i + offset + 8 < message.length; i++) {
                    message[offset + 8 + i] = (byte) 'x';
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("AVP length is too small: " + avpLength
                    + ". Message populating is broken. Exception: " + e);
        }
        return message;
    }

    /**
     * Create message, where AVP length > message length
     */
    private byte[] createMessageWithAvpLengthExceedingMessage(int fakeAvpLength) {
        // Create message with AVP length = fakeAvpLength
        // But real message length is small
        byte[] message = createMessageWithAvpLength(fakeAvpLength);
        // Truncate message to 100 bytes
        if (message.length > 100) {
            byte[] truncated = new byte[100];
            System.arraycopy(message, 0, truncated, 0, 100);
            // Change message length in the header
            truncated[1] = 0;
            truncated[2] = 0;
            truncated[3] = 100;
            return truncated;
        }
        return message;
    }

    /**
     * Create message with 1 big AVP
     */
    private byte[] createSingleAvpMessageWithLength(int avpLength) {
        // The same as createMessageWithAvpLength, but ensure that
        // message real length >= avpLength
        return createMessageWithAvpLength(avpLength);
    }

    /**
     * Create message with 2+ AVPs, where the 2nd AVP is oversized
     */
    private byte[] createMessageWithMixedAvpLengths() {
        // Create message with 2 AVPs:
        // 1. Valid AVP (Origin-Host)
        // 2. Oversized AVP (Session-Id)
        byte[] message = createMessageWithAvpLength(12); // Valid AVP
        // Add the 2nd AVP with length 1000 (but it's shorter in fact)
        byte[] extended = new byte[message.length + 20];
        System.arraycopy(message, 0, extended, 0, message.length);
        // Add AVP with length=1000
        int offset = message.length;
        extended[offset] = 0x00; // AVP Code (Session-Id = 263)
        extended[offset + 1] = 0x00;
        extended[offset + 2] = 0x01;
        extended[offset + 3] = 0x07;
        extended[offset + 4] = 0x40;
        extended[offset + 5] = 0x00;
        extended[offset + 6] = 0x03;
        extended[offset + 7] = (byte)0xE8; // 1000
        // Change message length
        int newLength = offset + 20;
        extended[1] = (byte)((newLength >> 16) & 0xFF);
        extended[2] = (byte)((newLength >> 8) & 0xFF);
        extended[3] = (byte)(newLength & 0xFF);
        return extended;
    }

    /**
     * Create Vendor-Specific AVP with length = avpLength
     */
    private byte[] createVendorSpecificAvpWithLength(int avpLength) {
        // AVP with V bit=1 (vendor-specific)
        byte[] message = createMessageWithAvpLength(avpLength);
        // Set V bit in AVP flags
        int offset = 20;
        message[offset + 4] = (byte)0xC0; // V bit=1, M bit=1
        // Add Vendor-ID (4 bytes)
        // Shift all data right to 4 bytes (simplified...)
        return message;
    }

    /**
     * Round length to 4*N bytes
     */
    private int roundLength(int length) {
        if (length % 4 != 0) {
            return (int) Math.ceil((double) length / 4) * 4;
        }
        return length;
    }
}
