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

package org.qubership.automation.diameter.connection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.junit.Test;
import org.qubership.automation.diameter.data.Decoder;
import org.qubership.automation.diameter.interceptor.Interceptor;

import com.google.common.collect.Sets;

public class ResponseListenerTest {
    private final byte[] notFullMessage = {1, 0, 2, 20, 64, 0, 1, 16, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 7, 64, 0, 0, 20, 52, 52, 54, 54, 53, 55, 48, 51, 49, 52, 50, 55, 0, 0, 1, -97, 64, 0, 0, 12, 0, 0, 0, 0, 0, 0, 1, 22, 64, 0, 0, 12, 0, 0, 0, 1, 0, 0, 1, 26, 64, 0, 0, 66, 54, 51, 51, 50, 50, 100, 51, 49, 51, 48, 50, 100, 51, 50, 51, 49, 51, 55, 50, 100, 51, 51, 51, 50, 50, 100, 51, 50, 50, 100, 54, 99, 54, 50, 54, 53, 55, 48, 54, 55, 51, 50, 51, 48, 51, 49, 50, 101, 55, 54, 54, 99, 50, 101, 54, 51, 54, 49, 0, 0, 0, 0, 1, -85, 64, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 55, 64, 0, 0, 12, -34, -112, 46, 88, 0, 0, 1, 12, 64, 0, 0, 12, 0, 0, 7, -47, 0, 0, 1, 2, 64, 0, 0, 12, 0, 0, 0, 4, 0, 0, 1, -96, 64, 0, 0, 12, 0, 0, 0, 1, 0, 0, 1, -94, 64, 0, 0, 12, 0, 0, 0, 1, 0, 0, 1, -56, 64, 0, 1, 16, 0, 0, 1, 12, 64, 0, 0, 12, 0, 0, 7, -47, 0, 0, 3, 101, -64, 0, 0, 16, 0, 0, 40, -81, 0, 80, 4, 24, 0, 0, 1, -64, 64, 0, 0, 12, 0, 0, 1, 44, 0, 0, 1, -80, 64, 0, 0, 12, 0, 0, 0, 100, 0, 0, 1, -81, 64, 0, 0, 40, 0, 0, 1, -100, 64, 0, 0, 16, 0, 0, 0, 0, 1, -32, 0, 0, 0, 0, 1, -98, 64, 0, 0, 16, 0, 0, 0, 0, 1, -32, 0, 0, 0, 0, 4, -16, -64, 0, 0, -84, 0, 0, 40, -81, 0, 0, 3, 102, -64, 0, 0, 16, 0, 0, 40, -81, 0, 0, 0, 1, 0, 0, 3, 102, -64, 0, 0, 16, 0, 0, 40, -81, 0, 0, 0, 2, 0, 0, 3, 102, -64, 0, 0, 16, 0, 0, 40, -81, 0, 0, 0, 3, 0, 0, 3, 102, -64, 0, 0, 16, 0, 0, 40, -81, 0, 0, 0, 4, 0, 0, 3, 102, -64, 0, 0, 16, 0, 0, 40, -81, 0, 0, 0, 30, 0, 0, 3, 102, -64, 0, 0, 16, 0, 0, 40, -81, 0, 0, 0, 31, 0, 0, 3, 102, -64, 0, 0, 16, 0, 0, 40, -81, 0, 0, 0, 32, 0, 0, 3, 102, -64, 0, 0, 16, 0, 0, 40, -81, 0, 0, 0, 33, 0, 0, 3, 102, -64, 0, 0, 16, 0, 0, 40, -81, 0, 0, 0, 60, 0, 0, 3, 102, -64, 0, 0, 16, 0, 0, 40, -81, 0, 0, 0, 61, 0, 0, 1, 8, 64, 0, 0, 31, 100, 100, 114, 115, 49, 103, 121, 98, 116, 46, 118, 105, 100, 101, 111, 116, 114, 111, 110, 46, 99, 111, 109, 0, 0, 0, 1, 40};

    private final byte[] fullMessage = {1, 0, 2, 20, 64, 0, 1, 16, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 7, 64, 0, 0, 20, 52, 50, 52, 48, 49, 55, 49, 50, 48, 56, 51, 54, 0, 0, 1, (byte) 159, 64, 0, 0, 12, 0, 0, 0, 0, 0, 0, 1, 22, 64, 0, 0, 12, 0, 0, 0, 1, 0, 0, 1, 26, 64, 0, 0, 66, 54, 51, 51, 50, 50, 100, 51, 49, 51, 48, 50, 100, 51, 50, 51, 49, 51, 55, 50, 100, 51, 51, 51, 50, 50, 100, 51, 50, 50, 100, 54, 99, 54, 50, 54, 53, 55, 48, 54, 55, 51, 50, 51, 48, 51, 49, 50, 101, 55, 54, 54, 99, 50, 101, 54, 51, 54, 49, 0, 0, 0, 0, 1, (byte) 171, 64, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 55, 64, 0, 0, 12, (byte) 222, (byte) 144, 32, (byte) 186, 0, 0, 1, 12, 64, 0, 0, 12, 0, 0, 7, (byte) 209, 0, 0, 1, 2, 64, 0, 0, 12, 0, 0, 0, 4, 0, 0, 1, (byte) 160, 64, 0, 0, 12, 0, 0, 0, 1, 0, 0, 1, (byte) 162, 64, 0, 0, 12, 0, 0, 0, 1, 0, 0, 1, (byte) 200, 64, 0, 1, 16, 0, 0, 1, 12, 64, 0, 0, 12, 0, 0, 7, (byte) 209, 0, 0, 3, 101, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 80, 4, 24, 0, 0, 1, (byte) 192, 64, 0, 0, 12, 0, 0, 1, 44, 0, 0, 1, (byte) 176, 64, 0, 0, 12, 0, 0, 0, 100, 0, 0, 1, (byte) 175, 64, 0, 0, 40, 0, 0, 1, (byte) 156, 64, 0, 0, 16, 0, 0, 0, 0, 1, (byte) 224, 0, 0, 0, 0, 1, (byte) 158, 64, 0, 0, 16, 0, 0, 0, 0, 1, (byte) 224, 0, 0, 0, 0, 4, (byte) 240, (byte) 192, 0, 0, (byte) 172, 0, 0, 40, (byte) 175, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 1, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 2, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 3, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 4, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 30, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) (byte) 175, 0, 0, 0, 31, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 32, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 33, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 60, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 61, 0, 0, 1, 8, 64, 0, 0, 31, 100, 100, 114, 115, 49, 103, 121, 98, 116, 46, 118, 105, 100, 101, 111, 116, 114, 111, 110, 46, 99, 111, 109, 0, 0, 0, 1, 40, 64, 0, 0, 21, 118, 105, 100, 101, 111, 116, 114, 111, 110, 46, 99, 111, 109, 0, 0, 0};


    private final byte[] firstPart = {1, 0, 2, 20, 64, 0, 1, 16, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 7, 64, 0, 0, 20, 52, 50, 52, 48, 49, 55, 49, 50, 48, 56, 51, 54, 0, 0, 1, (byte) 159, 64, 0, 0, 12, 0, 0, 0, 0, 0, 0, 1, 22, 64, 0, 0, 12, 0, 0, 0, 1, 0, 0, 1, 26, 64, 0, 0, 66, 54, 51, 51, 50, 50, 100, 51, 49, 51, 48, 50, 100, 51, 50, 51, 49, 51, 55, 50, 100, 51, 51, 51, 50, 50, 100, 51, 50, 50, 100, 54, 99, 54, 50, 54, 53, 55, 48, 54, 55, 51, 50, 51, 48, 51, 49, 50, 101, 55, 54, 54, 99, 50, 101, 54, 51, 54, 49, 0, 0, 0, 0, 1, (byte) 171, 64, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 55, 64, 0, 0, 12, (byte) 222, (byte) 144, 32, (byte) 186, 0, 0, 1, 12, 64, 0, 0, 12, 0, 0, 7, (byte) 209, 0, 0, 1, 2, 64, 0, 0, 12, 0, 0, 0, 4, 0, 0, 1, (byte) 160, 64, 0, 0, 12, 0, 0, 0, 1, 0, 0, 1, (byte) 162, 64, 0, 0, 12, 0, 0, 0, 1, 0, 0, 1, (byte) 200, 64, 0, 1, 16, 0, 0, 1, 12, 64, 0, 0, 12, 0, 0, 7, (byte) 209, 0, 0, 3, 101, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 80, 4, 24, 0, 0, 1, (byte) 192, 64, 0, 0, 12, 0, 0, 1, 44, 0, 0, 1, (byte) 176, 64, 0, 0, 12, 0, 0, 0, 100, 0, 0, 1, (byte) 175, 64, 0, 0, 40, 0, 0, 1, (byte) 156, 64, 0, 0, 16, 0, 0, 0, 0, 1, (byte) 224, 0, 0, 0, 0, 1, (byte) 158, 64, 0, 0, 16, 0, 0};

    private final byte[] secondPart = {0, 0, 1, (byte) 224, 0, 0, 0, 0, 4, (byte) 240, (byte) 192, 0, 0, (byte) 172, 0, 0, 40, (byte) 175, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 1, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 2, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 3, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 4, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 30, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) (byte) 175, 0, 0, 0, 31, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 32, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 33, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 60, 0, 0, 3, 102, (byte) 192, 0, 0, 16, 0, 0, 40, (byte) 175, 0, 0, 0, 61, 0, 0, 1, 8, 64, 0, 0, 31, 100, 100, 114, 115, 49, 103, 121, 98, 116, 46, 118, 105, 100, 101, 111, 116, 114, 111, 110, 46, 99, 111, 109, 0, 0, 0, 1, 40, 64, 0, 0, 21, 118, 105, 100, 101, 111, 116, 114, 111, 110, 46, 99, 111, 109, 0, 0, 0};

    @Test
    public void testMessageIsNotParsedWhenMessageIsNotCompleted() {
        ResponseListener responseListener = new ResponseListener();
        responseListener.appendBuffer(notFullMessage);
        ByteBuffer[] messages = responseListener.getMessages(responseListener.getBuffer());
        assertEquals(0, messages.length);
    }

    @Test
    public void testMessageIsFullParsedCorrect() {
        ResponseListener responseListener = new ResponseListener();
        responseListener.appendBuffer(fullMessage);
        ByteBuffer[] messages = responseListener.getMessages(responseListener.getBuffer());
        assertEquals(1, messages.length);
    }

    @Test
    public void testConcatMessage() {
        ResponseListener responseListener = new ResponseListener();
        responseListener.appendBuffer(firstPart);
        ByteBuffer[] messages = responseListener.getMessages(responseListener.getBuffer());
        assertEquals(0, messages.length);
        responseListener.appendBuffer(secondPart);
        messages = responseListener.getMessages(responseListener.getBuffer());
        assertEquals(1, messages.length);
        responseListener.appendBuffer(firstPart);
        responseListener.appendBuffer(secondPart);
        messages = responseListener.getMessages(responseListener.getBuffer());
        assertEquals(1, messages.length);
    }

    @Test
    public void testFallOutMessageWhenNotCompletedMessageWereSent() {
        ResponseListener responseListener = new ResponseListener();
        responseListener.cleanupBuffer(firstPart);
        responseListener.appendBuffer(firstPart);
        ByteBuffer[] messages = responseListener.getMessages(responseListener.getBuffer());
        assertEquals(0, messages.length);
        responseListener.cleanupBuffer(firstPart);
        responseListener.appendBuffer(firstPart);//after this message, first part must fall out.
        messages = responseListener.getMessages(responseListener.getBuffer());
        assertEquals(0, messages.length);
        responseListener.cleanupBuffer(secondPart);
        responseListener.appendBuffer(secondPart);
        messages = responseListener.getMessages(responseListener.getBuffer());
        assertEquals(1, messages.length);
    }

    @Test
    public void testInterceptorProcessed() {
        ResponseListener responseListener = new ResponseListener();
        Interceptor interceptor = mock(Interceptor.class);
        when(interceptor.onReceive(any(), any())).thenReturn(true);
        responseListener.addInterceptors(Sets.newHashSet(interceptor));
        Decoder decoder = mock(Decoder.class);
        String cca = "<CCA></CCA>";
        when(decoder.decode(any(ByteBuffer.class))).thenReturn(cca);

        responseListener.setDecoder(decoder);
        responseListener.processData(ByteBuffer.wrap(firstPart), firstPart.length);
        responseListener.processData(ByteBuffer.wrap(secondPart), secondPart.length);
        verify(interceptor).onReceive(eq(cca), any(ExtraChannel.class));
    }

    //@Test
    //TODO: Actualize me
    public void testErrorIsProcessedAsMessage() {
        ResponseListener listener = new ResponseListener();
        Interceptor interceptor = mock(Interceptor.class);
        Decoder decoder = mock(Decoder.class);
        listener.setDecoder(decoder);
        listener.addInterceptors(Sets.newHashSet(interceptor));
        IllegalArgumentException ex = new IllegalArgumentException("AVP configured wrong");
        when(decoder.decode(any(ByteBuffer.class))).thenThrow(ex);
        listener.processData(ByteBuffer.wrap(firstPart), firstPart.length);
        listener.processData(ByteBuffer.wrap(secondPart), secondPart.length);
        verify(interceptor).setError(eq("Failed parsing diameter message"), any());
    }

    @Test
    public void test() {
        ResponseListener listener = new ResponseListener();
        TestInterceptor interceptor0 = new TestInterceptor();
        interceptor0.setId("1");
        TestInterceptor interceptor1 = new TestInterceptor();
        interceptor1.setId("2");
        TestInterceptor interceptor2 = new TestInterceptor();
        interceptor2.setId("1");
        listener.addInterceptors(Sets.newHashSet(interceptor0));
        listener.addInterceptors(Sets.newHashSet(interceptor1));
        listener.addInterceptors(Sets.newHashSet(interceptor2));
        System.out.println(listener);
    }

    private static class TestInterceptor extends Interceptor{

        private String id;

        @Override
        @SuppressWarnings("MethodName")
        protected boolean _onReceive(String message, ExtraChannel channel) {
            return false;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object object) {
//            if (super.equals(object)) return true;
            if (object instanceof TestInterceptor) {
                return Objects.equals(this.getId(), ((TestInterceptor) object).getId());
            } else {
                return false;
            }
        }
    }
}
