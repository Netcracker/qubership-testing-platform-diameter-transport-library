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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.diameter.MarbenConfigProvider;
import org.qubership.automation.diameter.avp.AVPDictionary;
import org.qubership.automation.diameter.avp.AVPEntity;
import org.qubership.automation.diameter.avp.AVPRule;
import org.qubership.automation.diameter.avp.AVPType;
import org.qubership.automation.diameter.command.Command;
import org.qubership.automation.diameter.command.CommandDictionary;
import org.qubership.automation.diameter.dictionary.DictionaryService;

public class MarbenParserTest extends MarbenConfigProvider {

    private AVPDictionary avpDictionary;
    private CommandDictionary commandDictionary;

    @BeforeEach
    public void setUp() {
        avpDictionary = DictionaryService.getInstance().getAvpDictionary(DICTIONARY_CONFIG);
        commandDictionary = DictionaryService.getInstance().getCommandDictionary(DICTIONARY_CONFIG);
    }

    @Test
    void testParseCommands() {
        Command request = commandDictionary.getRequest(272);
        Assertions.assertNotNull(request);
        Assertions.assertEquals(272, request.getId());
        Assertions.assertEquals("CCR", request.getShortName());
        Assertions.assertEquals(request, commandDictionary.getRequest("CCR"));
        Command answer = commandDictionary.getAnswer(272);
        Assertions.assertNotNull(answer);
        Assertions.assertEquals("CCA", answer.getShortName());
        Assertions.assertEquals(answer, commandDictionary.getAnswer("CCA"));
    }

    @Test
    void testParseAvpsWithVendorId() {
        AVPEntity avp = avpDictionary.getVendor(10415).getById(2);
        Assertions.assertNotNull(avp);
        Assertions.assertEquals(2, avp.getId());
        Assertions.assertEquals(AVPRule.MUSTNOT, avp.getMandatory());
        Assertions.assertEquals(AVPRule.MAY, avp.getProtect());
        Assertions.assertEquals(10415, avp.getVendorId());
        Assertions.assertEquals("TGPP-Charging-Id", avp.getName());
    }

    @Test
    void testParseAvpWithEnumeratedType() {
        AVPEntity avp = avpDictionary.getById(295);
        Assertions.assertEquals(AVPType.ENUMERATE, avp.getType());
        Assertions.assertEquals("LOST_SERVICE".toLowerCase(), avp.getEnumerated(13).toLowerCase());
    }

    @Test
    void testParseAvpWithGroupedType() {
        AVPEntity avp = avpDictionary.getById(458);
        Assertions.assertEquals(AVPType.GROUPED, avp.getType());
    }

    @Test
    void testParseUnsigned32Type() {
        AVPEntity avp = avpDictionary.getVendor(10415).getById(869);
        Assertions.assertEquals(AVPType.UNSIGNED32, avp.getType());
    }

    @Test
    void testParseOctetString() {
        AVPEntity avp = avpDictionary.getVendor(10415).getById(2);
        Assertions.assertEquals(AVPType.OCTET_STRING, avp.getType());
    }

    @Test
    void testParseApplicationId() {
        Command request = commandDictionary.getRequest(272);
        Assertions.assertEquals(4, request.getApplicationId());
    }
}
