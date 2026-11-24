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

package org.qubership.automation.diameter.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Diameter-transport test of Command Dictionary
 */
class CommandDictionaryTest {
    private Command ccr;
    private Command raa;
    private CommandDictionary commandDictionary;

    @BeforeEach
    public void setUp() {
        this.commandDictionary = new CommandDictionary();
        ccr = new Command(272, 4);
        ccr.setShortName("CCR");
        ccr.setRequest(true);
        ccr.setRequestTag(true);

        raa = new Command(258, 4);
        raa.setShortName("RAA");
        raa.setRequest(false);
        raa.setRequestTag(true);

        commandDictionary.add(ccr);
        commandDictionary.add(raa);
    }

    @Test
    void testCommandDictionaryReturnsCommandById() {
        Command command = commandDictionary.getRequest(272);
        Assertions.assertEquals(ccr, command);
    }

    @Test
    void testCommandDictionaryReturnsCommandByName() {
        Command command = commandDictionary.getRequest("CCR");
        Assertions.assertEquals(ccr, command);
    }

    @Test
    void giveRAACommandByName() {
        Command command = commandDictionary.getRequest("RAA");
        Assertions.assertFalse(command.isRequest());
        Assertions.assertEquals(raa, command);
    }

    @Test
    void testCommandDictionaryThrowsException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> commandDictionary.getRequest("Exception"));
    }

    @Test
    void testCommandCCRIsConvertedToBytesValid() {
        Command command = commandDictionary.getRequest(272);
        Assertions.assertArrayEquals(new byte[]{-128, 0, 1, 16}, command.convertToBytesAndSetFlags());
    }
}
