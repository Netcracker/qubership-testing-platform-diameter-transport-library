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

package org.qubership.automation.diameter.exception;

import java.util.Arrays;

public class DecodeException extends RuntimeException {

    private static final int MAX_BYTES_IN_ERROR = Integer.parseInt(
            System.getProperty("diameter.decode.maxBytesInError", "1024")
    );

    public DecodeException(final String message) {
        super(message);
    }

    public DecodeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DecodeException(final String message,
                           final String parsedContent,
                           final byte[] data,
                           final Throwable cause) {
        super(buildErrorMessage(message, parsedContent, data), cause);
    }

    private static String buildErrorMessage(final String message,
                                            final String parsedContent,
                                            final byte[] data) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append("\nParsed Content: ").append(parsedContent);

        if (data == null) {
            sb.append("\nNot parsed byte data: null");
        } else if (data.length == 0) {
            sb.append("\nNo unparsed data left");
        } else if (data.length <= MAX_BYTES_IN_ERROR) {
            sb.append("\nNot parsed byte data (total size=")
                    .append(data.length)
                    .append("): ")
                    .append(Arrays.toString(data));
        } else {
            // Truncate data to MAX_BYTES_IN_ERROR
            sb.append("\nNot parsed byte data (truncated, total size=")
                    .append(data.length)
                    .append("): ")
                    .append(Arrays.toString(Arrays.copyOf(data, MAX_BYTES_IN_ERROR)))
                    .append("...");
        }
        return sb.toString();
    }
}
