package org.qubership.automation.diameter.connection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.TestDataFactory;
import org.qubership.automation.diameter.data.Decoder;
import org.qubership.automation.diameter.data.XmlDecoder;
import org.qubership.automation.diameter.exception.DecodeException;
import org.qubership.automation.diameter.interceptor.Interceptor;

/**
 * Security tests of ResponseListener.
 * <p>
 * To test invalid messages processing, especially messages with
 * incorrect (too big) length field in the header.
 */
public class ResponseListenerAttackTest extends StandardConfigProvider {
    private ResponseListener responseListener;
    private Decoder decoder;
    private ExtraChannel channel;

    @BeforeEach
    void setUp() {
        responseListener = new ResponseListener();

        // Use real XmlDecoder, not a mock - to test real behavior
        decoder = new XmlDecoder(DICTIONARY_CONFIG);
        responseListener.setDecoder(decoder);

        channel = mock(ExtraChannel.class);
        when(channel.isOpen()).thenReturn(true);
        responseListener.setChannel(channel);
    }

    /**
     * Test 5: Attack with oversized AVP length
     * <p>
     * The current behavior:
     *   - Allocation of 16 Mbs via Arrays.copyOfRange
     *   - O^2 resource consuming BigInteger.toString()
     *   - Durable execution (even minutes)
     * <p>
     * Expected behavior after fix:
     *   - DecodeException is thrown BEFORE allocation
     *   - Execution time < 100 ms (it's arbitrary threshold. At least, it shouldn't be seconds-minutes)
     */
    @Test
    void shouldRejectAvpWithOversizedLength() {
        // Given: message with AVP having length = 16_000_000
        // (Real message size is ~100 bytes)
        byte[] attackMessage = TestDataFactory.createMessageWithOversizedAvpLength(16_000_000);

        // When: try to decode the message
        DecodeException exception = Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(attackMessage)));

        // Then: There should be DecodeException about incorrect length in the cause
        Assertions.assertNotNull(exception);
        Assertions.assertTrue(exception.getMessage().contains("Failed parsing AVPs"));
        Throwable cause = exception.getCause();
        Assertions.assertNotNull(cause);
        Assertions.assertInstanceOf(DecodeException.class, cause);
        Assertions.assertEquals("AVP length exceeds message: 16000000 > 40 (AVP code: 264)",
                cause.getMessage());
    }

    /**
     * Test 7: Attack with incomplete message, where length > data length
     * <p>
     * The current behavior:
     *   - getMessages returns EMPTY
     *   - So, buffer isn't truncated
     *   - Buffer size increases
     * <p>
     * Expected behavior after fix:
     *   - Buffer is truncated or limited
     *   - No memory leaks
     */
    @Test
    void shouldHandleIncompleteMessageWithLargeLengthSafely() {
        // Given: incomplete message with length in the header > length of message
        byte[] incomplete = TestDataFactory.createIncompleteMessageWithLargeLength();

        // When: try to process
        responseListener.processData(ByteBuffer.wrap(incomplete), incomplete.length);

        // Then:
        // 1. Buffer shouldn't grow greater than message length
        Assertions.assertTrue(responseListener.getBuffer().length <= incomplete.length * 2,
                "Buffer grew to " + responseListener.getBuffer().length +
                        ", expected <= " + incomplete.length * 2);

        // 2. Buffer size shouldn't be greater than some limit (TBD, 1_048_576 hardcoded in the test now)
        Assertions.assertTrue(responseListener.getBuffer().length < 1_048_576,
                "Buffer too large: " + responseListener.getBuffer().length);
    }

    /**
     * Test 8: Message with incorrect length in the header
     * <p>
     * The current behavior:
     *   - getMessages attempts to create buffer of length bytes
     *   - Allocation of huge array is performed
     *   In fact, test isn't completed in proper time.
     * <p>
     * Expected behavior after fix:
     *   - Length is validated before allocation
     *   - In case incorrect length, exception is thrown or connection is closed
     */
    @Test
    void shouldRejectMessageWithInvalidHeaderLength() {
        // Given: message with length = 16_000_000 in the header
        byte[] attackMessage = TestDataFactory.createMessageWithOversizedHeaderLength(16_000_000);

        // When/Then: processing shouldn't allocate 16 Mbs.
        // Check it indirect way - via processing duration check.
        long startTime = System.currentTimeMillis();
        responseListener.processData(ByteBuffer.wrap(attackMessage), attackMessage.length);
        long duration = System.currentTimeMillis() - startTime;

        // Then: processing shouldn't allocate 16 Mbs (indicator is 'duration < 100 ms')
        Assertions.assertTrue(duration < 100,
                "Processing took " + duration + "ms, should be < 100ms (no 16MB allocation)");
    }

    /**
     * Test 9: After attack, a correct message should be processed successfully.
     * <p>
     * Test that getMessages correctly truncates buffer after processing
     * of invalid packet, and then subsequent valid message is processed successfully.
     */
    @Test
    void shouldProcessValidMessageAfterInvalidPacket() {
        // Given: invalid packet (version=0)
        byte[] attackPacket = TestDataFactory.createInvalidVersionPacket();
        responseListener.processData(ByteBuffer.wrap(attackPacket), attackPacket.length);

        // After getMessages buffer should be empty (truncated)
        Assertions.assertEquals(0, responseListener.getBuffer().length,
                "Buffer should be empty after processing invalid packet");

        // Given: valid message
        Interceptor interceptor = mock(Interceptor.class);
        when(interceptor.onReceive(any(), any())).thenReturn(true);
        responseListener.addInterceptors(java.util.Set.of(interceptor));

        // Create valid message
        byte[] validMessage = TestDataFactory.createValidFullMessage();

        // When: process valid message. There shouldn't be exceptions.
        Assertions.assertDoesNotThrow(
                () -> responseListener.processData(ByteBuffer.wrap(validMessage), validMessage.length));

        // Then: valid message should be processed
        // (check that interceptor was invoked)
        verify(interceptor, timeout(1000)).onReceive(any(), any());
    }

    /**
     * Test 10: Stress-test - many attacks of different types.
     * Please note:
     *  Primarily, the array contained extra attack:
     *      - TestDataFactory.createMessageWithOversizedAvpLength(16_000_000)
     *  But it was removed, because (see Test 'shouldRejectAvpWithOversizedLength'):
     *      - exception is thrown during processing, so buffer isn't truncated,
     *      and processed again - infinite loop (should be fixed too!)
     */
    @Test
    void shouldHandleMultipleAttackTypes() {
        // Given: array of different attacks
        byte[][] attacks = {
                TestDataFactory.createInvalidVersionPacket(), /* quickly Passed */
                TestDataFactory.createIncompleteMessageWithLargeLength(), /* Failed after 1+ sec, due to duration */
                TestDataFactory.createMessageWithOversizedHeaderLength(16_000_000) /* Failed after 1m 20 sec, due to duration */
        };

        long totalTime = 0;
        int attacksProcessed = 0;

        // When: process all attacks in the loop
        for (byte[] attack : attacks) {
            System.out.println("Processing attack " + attacksProcessed + "...");
            long startTime = System.currentTimeMillis();
            responseListener.processData(ByteBuffer.wrap(attack), attack.length);
            totalTime += System.currentTimeMillis() - startTime;
            System.out.println("Processing attack " + (attacksProcessed++) + " is completed.");

            // Buffer shouldn't grow above limit
            Assertions.assertTrue(responseListener.getBuffer().length < 1_048_576,
                    "Buffer too large after " + attacksProcessed + " attacks");
        }

        // Then: all attacks are processed quickly
        long averageTime = totalTime / attacksProcessed;
        Assertions.assertTrue(averageTime < 100,
                "Average attack processing time " + averageTime + "ms should be < 100ms");
    }
}
