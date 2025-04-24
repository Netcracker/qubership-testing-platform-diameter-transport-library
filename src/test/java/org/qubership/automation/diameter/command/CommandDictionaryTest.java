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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * diameter-transport
 */
public class CommandDictionaryTest {
    private Command ccr;
    private Command raa;
    private CommandDictionary commandDictionary;

    @Before
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
    public void testCommandDictionaryReturnsCommandById() {
        Command command = commandDictionary.getRequest(272);
        assertEquals(ccr, command);
    }

    @Test
    public void testCommandDictionaryReturnsCommandByName() {
        Command command = commandDictionary.getRequest("CCR");
        assertEquals(ccr, command);
    }

    @Test
    public void giveRAACommandByName() {
        Command command = commandDictionary.getRequest("RAA");
        assertTrue(!command.isRequest());
        assertEquals(raa, command);
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void testCommandDictionaryThrowsException() throws Exception {
        commandDictionary.getRequest("Exception");
    }

    @Test
    public void testCommandCCRIsConvertedToBytesValid() {
        Command command = commandDictionary.getRequest(272);
        assertArrayEquals(new byte[]{-128, 0, 1, 16}, command.convertToBytesAndSetFlags());
    }
}
