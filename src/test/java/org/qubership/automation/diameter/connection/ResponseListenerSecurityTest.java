package org.qubership.automation.diameter.connection;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.diameter.TestDataFactory;
import org.qubership.automation.diameter.data.Decoder;
import org.qubership.automation.diameter.data.XmlDecoder;
import org.qubership.automation.diameter.interceptor.Interceptor;

public class ResponseListenerSecurityTest {

    private ResponseListener responseListener;
    private Decoder decoder;
    private ExtraChannel channel;

    @BeforeEach
    void setUp() {
        responseListener = new ResponseListener();
        decoder = mock(XmlDecoder.class);
        channel = mock(ExtraChannel.class);
        responseListener.setDecoder(decoder);
        responseListener.setChannel(channel);
    }

    /**
     * Test 1: Valid full message should be processed w/o exceptions.
     */
    @Test
    void shouldProcessValidFullMessage() {
        // Given: Valid full message
        byte[] validMessage = TestDataFactory.createValidFullMessage();
        String expectedXml = "<CCR><Session-Id>valid</Session-Id></CCR>";
        when(decoder.decode(any(ByteBuffer.class))).thenReturn(expectedXml);

        Interceptor interceptor = mock(Interceptor.class);
        when(interceptor.onReceive(any(), any())).thenReturn(true);
        responseListener.addInterceptors(java.util.Set.of(interceptor));

        // When
        responseListener.processData(ByteBuffer.wrap(validMessage), validMessage.length);

        // Then
        verify(decoder).decode(any(ByteBuffer.class));
        verify(interceptor).onReceive(eq(expectedXml), any(ExtraChannel.class));
    }

    /**
     * Test 2: Message is received by parts. It should be composed correctly.
     */
    @Test
    void shouldProcessMessageSplitIntoParts() {
        // Given: message is separated into 2 parts
        byte[] firstPart = TestDataFactory.createFirstPart();
        byte[] secondPart = TestDataFactory.createSecondPart();
        String expectedXml = "<CCR><Session-Id>complete</Session-Id></CCR>";
        when(decoder.decode(any(ByteBuffer.class))).thenReturn(expectedXml);

        Interceptor interceptor = mock(Interceptor.class);
        when(interceptor.onReceive(any(), any())).thenReturn(true);
        responseListener.addInterceptors(java.util.Set.of(interceptor));

        // When: the 1st part is received
        responseListener.processData(ByteBuffer.wrap(firstPart), firstPart.length);

        // Then: message is not full yet, so Decoder isn't invoked
        verify(decoder, never()).decode(any(ByteBuffer.class));

        // When: the 2nd part is received
        responseListener.processData(ByteBuffer.wrap(secondPart), secondPart.length);

        // Then: message is processed, and result is expectedXml.
        verify(decoder).decode(any(ByteBuffer.class));
        verify(interceptor).onReceive(eq(expectedXml), any(ExtraChannel.class));
    }

    /**
     * Test 3: Some (2+) full messages are in one buffer. All messages should be processed.
     */
    @Test
    void shouldProcessMultipleMessagesInOneBuffer() {
        // Given: 2 full messages are in one buffer
        byte[] message1 = TestDataFactory.createValidFullMessage();
        byte[] message2 = TestDataFactory.createAnotherValidMessage();
        byte[] combined = new byte[message1.length + message2.length];
        System.arraycopy(message1, 0, combined, 0, message1.length);
        System.arraycopy(message2, 0, combined, message1.length, message2.length);

        String xml1 = "<CCR><Session-Id>msg1</Session-Id></CCR>";
        String xml2 = "<CCA><Session-Id>msg2</Session-Id></CCA>";
        when(decoder.decode(any(ByteBuffer.class)))
                .thenReturn(xml1)
                .thenReturn(xml2);

        AtomicBoolean received1 = new AtomicBoolean(false);
        AtomicBoolean received2 = new AtomicBoolean(false);

        Interceptor interceptor1 = mock(Interceptor.class);
        when(interceptor1.onReceive(any(), any())).thenAnswer(inv -> {
            String xml = inv.getArgument(0);
            if (xml1.equals(xml)) received1.set(true);
            return true;
        });
        Interceptor interceptor2 = mock(Interceptor.class);
        when(interceptor2.onReceive(any(), any())).thenAnswer(inv -> {
            String xml = inv.getArgument(0);
            if (xml2.equals(xml)) received2.set(true);
            return true;
        });

        responseListener.addInterceptors(java.util.Set.of(interceptor1, interceptor2));

        // When
        responseListener.processData(ByteBuffer.wrap(combined), combined.length);

        // Then: both messages are processed
        Assertions.assertTrue(received1.get());
        Assertions.assertTrue(received2.get());
        verify(decoder, times(2)).decode(any(ByteBuffer.class));
    }

    /**
     * Test 4: Buffer should be cleaned up correct way after processing.
     */
    @Test
    void shouldClearBufferAfterProcessing() {
        // Given
        byte[] validMessage = TestDataFactory.createValidFullMessage();
        when(decoder.decode(any(ByteBuffer.class))).thenReturn("<CCR></CCR>");

        Interceptor interceptor = mock(Interceptor.class);
        when(interceptor.onReceive(any(), any())).thenReturn(true);
        responseListener.addInterceptors(java.util.Set.of(interceptor));

        // When
        responseListener.processData(ByteBuffer.wrap(validMessage), validMessage.length);

        // Then: Buffer should be empty after processing
        Assertions.assertEquals(0, responseListener.getBuffer().length);
    }
}
