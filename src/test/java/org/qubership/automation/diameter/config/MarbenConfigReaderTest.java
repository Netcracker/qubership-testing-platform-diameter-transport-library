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

package org.qubership.automation.diameter.config;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.qubership.automation.diameter.command.Command;
import org.qubership.automation.diameter.command.CommandDictionary;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.qubership.automation.diameter.dictionary.DictionaryService;

public class MarbenConfigReaderTest {

    private static final DictionaryConfig DICTIONARY_CONFIG = new DictionaryConfig("src/test/resources/marben",
            MarbenParser.class, null);
    private CommandDictionary commandDictionary;

    @Before
    public void setUp() throws Exception {
        ConfigReader.read(DICTIONARY_CONFIG, false);
        commandDictionary = DictionaryService.getInstance().getCommandDictionary(DICTIONARY_CONFIG);
    }

    @Test
    public void testReadingMarbenDictionaryTwiceAndCheckCCRApplicationId() throws Exception {
        Command request = commandDictionary.getRequest(272);
        assertEquals(4, request.getApplicationId());
        ConfigReader.read(DICTIONARY_CONFIG, true);
        Command request1 = commandDictionary.getRequest(272);
        assertEquals(4, request1.getApplicationId());
    }

    @Test
    public void testReadingMarbenDictionaryToCheckSNRApplicationId() {
        Command request = commandDictionary.getRequest(8388636);
        assertEquals(16777302, request.getApplicationId());
        assertEquals("SNR", request.getShortName());
    }
}
