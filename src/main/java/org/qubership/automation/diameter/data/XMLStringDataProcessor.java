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

package org.qubership.automation.diameter.data;

import org.qubership.automation.diameter.data.constants.CommandXmlTags;
import org.qubership.automation.diameter.interceptor.InterceptorTypes;

public class XMLStringDataProcessor {

    /**
     * get session id tag value from message that contains Session-Id xml tag.
     *
     * @param message message that contains Session-Id xml tag
     * @return String session id value from Session-Id xml tag
     */
    public static String getSessionId(final String message) {
        int sessionIdFirstTagIndex = message.indexOf("<Session-Id");
        int from = message.indexOf(">", sessionIdFirstTagIndex) + 1;
        int to = message.indexOf("</Session-Id>");
        return message.substring(from, to);
    }

    /**
     * Check command short name (request) that is SNR, RAR or ASR.
     *
     * @param stringMessage short diameter command name.
     * @return true if input string start with SNR, RAR or ASR.
     */
    public static boolean isNotificationRequest(final String stringMessage) {
        String message = stringMessage.trim();
        return message.startsWith(CommandXmlTags.SNR)
                || message.startsWith(CommandXmlTags.RAR)
                || message.startsWith(CommandXmlTags.ASR);
    }

    /**
     * Check command short name (answer) that is SNA, RAA or ASA.
     *
     * @param stringMessage short diameter command name.
     * @return true if input string start with SNA, RAA or ASA.
     */
    public static boolean isNotificationAnswer(final String stringMessage) {
        String message = stringMessage.trim();
        return message.startsWith(CommandXmlTags.SNA)
                || message.startsWith(CommandXmlTags.RAA)
                || message.startsWith(CommandXmlTags.ASA);
    }

    /**
     * Get interceptor type (string short name of interceptor type) from xml answer message.
     *
     * @param message string xml answer message
     * @return string short name of interceptor type
     */
    public static String getInterceptorTypeFromAnswerMessage(final String message) {
        if (message.startsWith(CommandXmlTags.SNA)) {
            return InterceptorTypes.SNR;
        } else if (message.startsWith(CommandXmlTags.RAA)) {
            return InterceptorTypes.RAR;
        } else if (message.startsWith(CommandXmlTags.ASA)) {
            return InterceptorTypes.ASR;
        }
        return "";
    }

    /**
     * Get interceptor type (string short name of interceptor type) from xml request message.
     *
     * @param message string xml request message
     * @return string short name of interceptor type
     */
    public static String getInterceptorTypeByRequestMessage(final String message) {
        if (message.startsWith(CommandXmlTags.SNR)) {
            return InterceptorTypes.SNR;
        } else if (message.startsWith(CommandXmlTags.RAR)) {
            return InterceptorTypes.RAR;
        } else if (message.startsWith(CommandXmlTags.ASR)) {
            return InterceptorTypes.ASR;
        }
        return "";
    }
}
