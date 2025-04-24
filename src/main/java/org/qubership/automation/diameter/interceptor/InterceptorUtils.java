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

package org.qubership.automation.diameter.interceptor;

import static org.qubership.automation.diameter.data.XMLStringDataProcessor.getSessionId;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.qubership.automation.diameter.data.Converter;
import org.qubership.automation.diameter.data.constants.DiameterHeader;

public class InterceptorUtils {

    /**
     * Set hopByHop and end2End bytes in the byte buffer.
     *
     * @param encode - byte buffer to set hopByHop and end2End bytes,
     * @param hopByHop - int hopByHop value to set in buffer,
     * @param end2End - int end2End value to set in buffer,
     * @return - byte buffer updated.
     */
    public static ByteBuffer setHbHAndE2E(ByteBuffer encode, int hopByHop, int end2End) {
        byte[] array = encode.array();
        int index = 12;
        update(array, index, Converter.intToBytes(hopByHop));
        index = 16;
        update(array, index, Converter.intToBytes(end2End));
        return ByteBuffer.wrap(array);
    }

    private static void update(byte[] array, int index, byte[] data) {
        for (byte b : data) {
            array[index] = b;
            index++;
        }
    }

    /**
     * Set HBh and E2e to Interceptor properties - for all interceptors filtered by type and session id.
     *
     * @param message - String message under processing,
     * @param content - byte array containing HBh and E2e data,
     * @param interceptorType - type of interceptors for filtering,
     * @param interceptors - set of interceptors.
     */
    public static void saveHBhAndE2eToInterceptor(String message, byte[] content, String interceptorType,
                                                  Set<Interceptor> interceptors) {
        String sessionId = getSessionId(message);
        for (Interceptor interceptor : interceptors) {
            if (interceptorType.equals(interceptor.getType()) && sessionId.equals(interceptor.getSessionId())) {
                interceptor.setHopByHop(Arrays.copyOfRange(content, 12, 16));
                interceptor.setEnd2End(Arrays.copyOfRange(content, 16, 20));
            }
        }
    }

    /**
     * Add HBH and E2E headers to headers map.
     *
     * @param sessionId - diameter session id,
     * @param headers - headers map,
     * @param interceptorType - type of interceptors for filtering,
     * @param interceptors - set of interceptors.
     */
    public static void addHbhAndE2eHeaders(String sessionId, Map<String, Object> headers, String interceptorType,
                                           Set<Interceptor> interceptors) {
        for (Interceptor interceptor : interceptors) {
            if (interceptorType.equals(interceptor.getType()) && sessionId.equals(interceptor.getSessionId())) {
                headers.put(DiameterHeader.HBH, interceptor.getHopByHop());
                headers.put(DiameterHeader.E2E, interceptor.getEnd2End());
            }
        }
    }
}
