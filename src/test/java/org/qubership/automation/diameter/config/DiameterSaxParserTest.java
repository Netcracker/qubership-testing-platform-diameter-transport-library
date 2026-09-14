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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.avp.AVPDictionary;
import org.qubership.automation.diameter.avp.AVPEntity;
import org.qubership.automation.diameter.avp.AVPType;
import org.qubership.automation.diameter.command.Command;
import org.qubership.automation.diameter.command.CommandDictionary;
import org.qubership.automation.diameter.dictionary.DiameterDictionaryHolder;

public class DiameterSaxParserTest extends StandardConfigProvider {

    @Test
    public void testCommand() {
        CommandDictionary commandDictionary = DiameterDictionaryHolder.getInstance().getDictionary(DICTIONARY_CONFIG)
                .getCommandDictionary();
        Command command = commandDictionary.getRequest("CCR");
        Assertions.assertNotNull(command);
        Assertions.assertEquals(272, command.getId());
        command = commandDictionary.getAnswer("CEA");
        Assertions.assertNotNull(command);
        Assertions.assertEquals(257, command.getId());
        Assertions.assertEquals(0, command.getApplicationId());
        command = commandDictionary.getAnswer(282);
        Assertions.assertNotNull(command);
        Assertions.assertEquals("DPA", command.getShortName());
    }

    @Test
    public void testRAACommand() {
        CommandDictionary commandDictionary = DiameterDictionaryHolder.getInstance().getDictionary(DICTIONARY_CONFIG)
                .getCommandDictionary();
        Command command = commandDictionary.getRequest("RAA");
        Assertions.assertNotNull(command);
        Assertions.assertEquals(258, command.getId());
    }

    @Test
    public void testDDRSParser() {
        AVPDictionary avpDictionary = DiameterDictionaryHolder.getInstance().getDictionary(DICTIONARY_CONFIG)
                .getAvpDictionary();
        AVPEntity avp = avpDictionary.getVendor(3868).getById(50001);
        Assertions.assertNotNull(avp);
        Assertions.assertEquals("CVG-Event-Source", avp.getName());
        Assertions.assertEquals(3868, avp.getVendorId());
        Assertions.assertEquals(50001, avp.getId());
        Assertions.assertEquals(AVPType.UNSIGNED32, avp.getType());
    }

    @Test
    public void testParseDDRSEnumeratedParsed() {
        AVPDictionary avpDictionary = DiameterDictionaryHolder.getInstance().getDictionary(DICTIONARY_CONFIG)
                .getAvpDictionary();
        AVPEntity avp = avpDictionary.getById(416);
        Assertions.assertNotNull(avp);
        Assertions.assertEquals("CC-Request-Type", avp.getName());
        Assertions.assertEquals(416, avp.getId());
        Assertions.assertEquals(AVPType.ENUMERATE, avp.getType());
    }
}
