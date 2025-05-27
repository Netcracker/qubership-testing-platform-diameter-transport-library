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

import org.apache.commons.lang3.BooleanUtils;

public class Utils {

    /**
     * Pattern to identify hex number.
     */
    private static final String HEX_PATTERN = "-?[0-9a-fA-F]+";

    /**
     * Pattern to identify non-hex number.
     */
    private static final String NONHEX_PATTERN = "-?[0-9]+";

    /**
     * Determine, how many characters should be added to buffer to pad the current length.
     *
     * @param i - total current length of the buffer,
     * @return - count of characters to be padded.
     */
    public static int getPadLength(final int i) {
        int intResult = 0;
        if (i % 4 != 0) {
            intResult = 4 - (i % 4);
        }
        return intResult;
    }

    /**
     * Parse String into int with 10 radix.
     *
     * @param input String to parse
     * @return int result.
     */
    public static int parseInt(final String input) {
        return parseInt(input, 10);
    }

    /**
     * Parse String representation of integer (with radix given) into int value.
     *
     * @param input - String representation of integer,
     * @param radix - radix value,
     * @return - int value parsed.
     */
    public static int parseInt(final String input, final int radix) {
        if (radix != 16 && (input == null || !input.matches(NONHEX_PATTERN))) {
            throw new IllegalArgumentException("Can't convert non-numeric value to int: '" + input + '\'');
        } else if (radix == 16 && (input == null || !input.matches(HEX_PATTERN))) {
            throw new IllegalArgumentException("Can't convert hex input to int, it isn't hex value: '" + input + '\'');
        }
        return Integer.parseInt(input, radix);
    }

    /**
     * Parse String to boolean.
     *
     * @param input String to parse
     * @return boolean result.
     */
    public static boolean parseBoolean(final String input) {
        return BooleanUtils.toBoolean(input);
    }
}
