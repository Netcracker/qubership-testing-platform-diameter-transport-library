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
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void setUp() {
        avpDictionary = DictionaryService.getInstance().getAvpDictionary(DICTIONARY_CONFIG);
        commandDictionary = DictionaryService.getInstance().getCommandDictionary(DICTIONARY_CONFIG);
    }

    @Test
    public void testParseCommands() {
        Command request = commandDictionary.getRequest(272);
        assertNotNull(request);
        assertEquals(272, request.getId());
        assertEquals("CCR", request.getShortName());
        assertEquals(request, commandDictionary.getRequest("CCR"));
        Command answer = commandDictionary.getAnswer(272);
        assertNotNull(answer);
        assertEquals("CCA", answer.getShortName());
        assertEquals(answer, commandDictionary.getAnswer("CCA"));
    }

    @Test
    public void testParseAvpsWithVendorId() {
        AVPEntity avp = avpDictionary.getVendor(10415).getById(2);
        assertNotNull(avp);
        assertEquals(2, avp.getId());
        assertEquals(AVPRule.MUSTNOT, avp.getMandatory());
        assertEquals(AVPRule.MAY, avp.getProtect());
        assertEquals(10415, avp.getVendorId());
        assertEquals("TGPP-Charging-Id", avp.getName());
    }

    @Test
    public void testParseAvpWithEnumeratedType() {
        AVPEntity avp = avpDictionary.getById(295);
        assertEquals(AVPType.ENUMERATE, avp.getType());
        assertEquals("LOST_SERVICE".toLowerCase(), avp.getEnumerated(13).toLowerCase());
    }

    @Test
    public void testParseAvpWithGroupedType() {
        AVPEntity avp = avpDictionary.getById(458);
        assertEquals(AVPType.GROUPED, avp.getType());
    }

    @Test
    public void testParseUnsigned32Type() {
        AVPEntity avp = avpDictionary.getVendor(10415).getById(869);
        assertEquals(AVPType.UNSIGNED32, avp.getType());
    }

    @Test
    public void testParseOctetString() {
        AVPEntity avp = avpDictionary.getVendor(10415).getById(2);
        assertEquals(AVPType.OCTET_STRING, avp.getType());
    }

    @Test
    public void testParseApplicationId() {
        Command request = commandDictionary.getRequest(272);
        assertEquals(4, request.getApplicationId());
    }
}
