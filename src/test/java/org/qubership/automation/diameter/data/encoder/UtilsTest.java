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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTest {
    @Test
    void testParseInt() {
        Assertions.assertEquals(3, Utils.parseInt("3"));
    }

    @Test
    void testParseInt16() {
        Assertions.assertEquals(255, Utils.parseInt("Ff", 16));
    }

    @Test
    void testParseInt16NumberWithoutHex() {
        Assertions.assertEquals(5, Utils.parseInt("5", 16));
    }

    @Test
    void testParseIntFailIfValueIsNotNumber() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Utils.parseInt(" "));
    }

    @Test
    void testParseInt16FailIfValueIsNotNumber() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Utils.parseInt("X"));
    }
}
