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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.command.Command;
import org.qubership.automation.diameter.dictionary.DiameterDictionaryHolder;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;

public class ConfigReaderTest extends StandardConfigProvider {

    @Test
    public void givenConfigForStandardFormat_whenWeReadItAndParse_thenConfigIsReadAndItGeneratesCommandById() {
        Command request = DiameterDictionaryHolder.getInstance().getDictionary(DICTIONARY_CONFIG).getCommandDictionary()
                .getRequest(257);
        assertNotNull(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfigIsNotReadAndThrowsException() throws Exception {
        String path = "src/test/resources/avp_base.xml";
        DictionaryConfig dictionaryConfig = new DictionaryConfig(path, StandardParser.class, null);
        ConfigReader.read(dictionaryConfig, false);
    }
}
