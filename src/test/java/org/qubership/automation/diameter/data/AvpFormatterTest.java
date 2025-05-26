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

package org.qubership.automation.diameter.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.qubership.automation.diameter.MarbenConfigProvider;
import org.qubership.automation.diameter.data.encoder.XMLMessages;
import org.qubership.automation.diameter.dictionary.DiameterDictionary;
import org.qubership.automation.diameter.dictionary.DiameterDictionaryHolder;
import org.qubership.automation.diameter.exception.DiameterXmlFormatException;

public class AvpFormatterTest extends MarbenConfigProvider {

    DiameterDictionary dictionary;

    String OPENED_CCR_TAG = "<CCR>";
    String OPENED_CCR_TAG_WITH_DESCRIPTION =
            "<CCR Description=\"CCR-I\" Retransmit=\"true\" " + "Service" + "=\"To_DiameterPeer_User_dp_user2\">";
    String OPENED_CCR_TAG_WITH_SOMETHING_BEFORE = "ddd><CCR>";
    String OPENED_CCR_TAG_WITH_SOMETHING_AFTER = "<CCR>ddd>";
    String CLOSED_CCR_TAG = "</CCR>";
    String WRONG_CCC_COMMAND_TAG = "<CCC>";
    String OPENED_CCR_TAG_WITHOUT_END_BRACKET = "<CCR";
    String OPENED_CCR_TAG_WITH_SOMETHING_BEFORE_AND_NOT_CLOSED_BRACKET = "ddd><CCR";
    String NO_XML_TAG = "abs<5";

    String OPENED_ORIGIN_HOST_AVP_TAG = "<Origin-Host>";
    String OPENED_ORIGIN_HOST_AVP_TAG_WITH_ATTRIBUTES = "<Origin-Host code=\"264\" vendor=\"0\">";
    String OPENED_ORIGIN_HOST_AVP_WITH_ATTRIBUTES_AND_VALUE_AND_CLOSED_TAG =
            "<Origin-Host code=\"264\" " + "vendor" + "=\"0\">cube.tlnt-env.our-company.com</Origin-Host>\n";
    String OPENED_ORIGIN_HOST_AVP_WITH_VALUE_AND_CLOSED_TAG =
            "<Origin-Host>cube.tlnt-env.our-company.com</Origin" + "-Host>\n";
    String OPENED_ORIGIN_HOST_AVP_TAG_WITH_SOMETHING_BEFORE = "ddd><Origin-Host>";
    String OPENED_ORIGIN_HOST_AVP_TAG_WITH_SOMETHING_AFTER = "<Origin-Host>ddd>";
    String CLOSED_ORIGIN_HOST_AVP_TAG = "</Origin-Host>";
    String WRONG_CCC_AVP_TAG = "<CCC>";
    String OPENED_ORIGIN_HOST_AVP_TAG_WITHOUT_END_BRACKET = "<Origin-Host";
    String OPENED_ORIGIN_HOST_AVP_TAG_WITHOUT_END_BRACKET_AND_TAB_SYMBOL_AT_THE_END = "<Origin-Host\t";
    String OPENED_ORIGIN_HOST_AVP_TAG_WITHOUT_END_BRACKET_AND_RETURN_CARET_SYMBOL_AT_THE_END = "<Origin-Host\r";
    String OPENED_ORIGIN_HOST_AVP_TAG_WITH_SOMETHING_BEFORE_AND_NOT_CLOSED_BRACKET = "ddd><Origin-Host";

    @Before
    public void setUp() {
        dictionary = DiameterDictionaryHolder.getInstance().getDictionary(DICTIONARY_CONFIG);
    }

    @Test
    public void formatAvp() {
        assertEquals(XMLMessages.CCA_WITH_VELOCITY_AFTER_FORMAT_AVP,
                AvpFormatter.formatAvp(XMLMessages.CCA_WITH_VELOCITY_AND_FORMATTED_SESSION_ID_AVP, DICTIONARY_CONFIG));
        try {
            AvpFormatter.formatAvp(XMLMessages.CCR_WITH_DESCRIPTION_ATTR_AND_WITHOUT_END_BRACKET, DICTIONARY_CONFIG);
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_NO_CLOSED_BRACKET
                    + "&lt;CCR Description=\"CCR-I\" Retransmit"
                    + "=\"true\" Service=\"To_DiameterPeer_User_dp_user2\"", e.getMessage());
        }
        try {
            AvpFormatter.formatAvp(XMLMessages.AVP_WITH_ATTR_WITHOUT_END_BRACKET, DICTIONARY_CONFIG);
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_NO_END_BRACKET
                    + "&lt;Session-Id code=\"123\"", e.getMessage());
        }
        try {
            AvpFormatter.formatAvp(XMLMessages.SESSION_ID_AVP_WITHOUT_END_BRACKET_AND_NEW_LINE, DICTIONARY_CONFIG);
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_NO_END_BRACKET + "\t&lt;Session-Id", e.getMessage());
        }
        try {
            AvpFormatter.formatAvp(XMLMessages.SESSION_ID_AVP_WITHOUT_END_BRACKET_AND_RETURN_CARET, DICTIONARY_CONFIG);
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_NO_END_BRACKET + "\t&lt;Session-Id", e.getMessage());
        }
        try {
            AvpFormatter.formatAvp(XMLMessages.SESSION_ID_AVP_WITHOUT_END_BRACKET_AND_TAB_AT_THE_END, DICTIONARY_CONFIG);
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_NO_END_BRACKET + "\t&lt;Session-Id", e.getMessage());
        }
        assertEquals("<fff<bbb\n" + "    >$SessionId</Session-Id>\n",
                AvpFormatter.formatAvp(XMLMessages.NOT_AVP_WITHOUT_END_BRACKET_SOME_CHARS_INTO_AND_OPENED_BRACKET_AFTER,
                        DICTIONARY_CONFIG));
        assertEquals("\t<Session-Id code=\"263\" vendor=\"0\" >\n" + "    $SessionId</Session-Id>\n",
                AvpFormatter.formatAvp(XMLMessages.SESSION_ID_AVP_WITH_SPACE_AFTER, DICTIONARY_CONFIG));
        assertEquals("\t< Session-Id code=\"263\" vendor=\"0\" >\n" + "    $SessionId</Session-Id>\n",
                AvpFormatter.formatAvp(XMLMessages.SESSION_ID_AVP_WITH_SPACE_BEFORE_AND_AFTER, DICTIONARY_CONFIG));
        try {
            AvpFormatter.formatAvp(XMLMessages.SESSION_ID_AVP_WITH_SPACE_AFTER_AND_WITHOUT_END_BRACKET,
                    DICTIONARY_CONFIG);
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_NO_END_BRACKET + "\t&lt;Session-Id ",
                    e.getMessage());
        }
        assertEquals("\t<Session-Id code=\"263\" vendor=\"0\"\t>    $SessionId</Session-Id>\n",
                AvpFormatter.formatAvp(XMLMessages.SESSION_ID_AVP_WITH_TAB_INTO_TAG, DICTIONARY_CONFIG));
        assertEquals("\t<Session-Id code=\"263\" vendor=\"0\">\t    $SessionId</Session-Id>\n",
                AvpFormatter.formatAvp(XMLMessages.SESSION_ID_AVP_WITH_TAB_AFTER_TAG, DICTIONARY_CONFIG));
    }

    @Test
    public void reverseFormatAvp() {
        assertEquals(XMLMessages.CCA_WITHOUT_AVP_ATTRIBUTES,
                AvpFormatter.reverseFormatAvp(XMLMessages.CCA_FORMATTED_TEMPLATE));
    }

    @Test
    public void negativeTestFormatAVP() {
        try {
            AvpFormatter.formatAvp(XMLMessages.TEMPLATE_WITH_NOT_EXISTING_AVP, DICTIONARY_CONFIG);
        } catch (IllegalArgumentException e) {
            assertEquals("AVP not found by name Trigger1212121212", e.getMessage());
        }
    }

    @Test
    public void hasOpenedCorrectCommandTag() {
        assertEquals("ccr", AvpFormatter.hasOpenedCorrectCommandTag(OPENED_CCR_TAG));
        assertEquals("ccr", AvpFormatter.hasOpenedCorrectCommandTag(OPENED_CCR_TAG_WITH_DESCRIPTION));
        assertEquals("", AvpFormatter.hasOpenedCorrectCommandTag(WRONG_CCC_COMMAND_TAG));
        try {
            assertEquals("ccr", AvpFormatter.hasOpenedCorrectCommandTag(OPENED_CCR_TAG_WITH_SOMETHING_BEFORE));
        } catch (DiameterXmlFormatException e) {
            assertEquals("Command CCR has some chars before/after. Row: ddd&gt;&lt;CCR&gt;", e.getMessage());
        }
        try {
            assertEquals("ccr", AvpFormatter.hasOpenedCorrectCommandTag(OPENED_CCR_TAG_WITH_SOMETHING_AFTER));
        } catch (DiameterXmlFormatException e) {
            assertEquals("Command CCR has some chars before/after. Row: &lt;CCR&gt;ddd&gt;", e.getMessage());
        }
        assertEquals("", AvpFormatter.hasOpenedCorrectCommandTag(CLOSED_CCR_TAG));
        try {
            assertEquals("", AvpFormatter.hasOpenedCorrectCommandTag(
                    OPENED_CCR_TAG_WITH_SOMETHING_BEFORE_AND_NOT_CLOSED_BRACKET));
        } catch (DiameterXmlFormatException e) {
            assertEquals("Command open tag has not closed bracket. Row: ddd&gt;&lt;CCR", e.getMessage());
        }
        try {
            assertEquals("",
                    AvpFormatter.hasOpenedCorrectCommandTag(XMLMessages.CCR_WITH_DESCRIPTION_ATTR_AND_OPENED_AVP_TAG));
        } catch (DiameterXmlFormatException e) {
            assertEquals("Command CCR has some chars before/after. Row: &lt;CCR Description=\"CCR-I\" " + "Retransmit"
                    + "=\"true\"" + " Service=\"To_DiameterPeer_User_dp_user2\"&gt;\t&lt;" + "Session-Id&gt;"
                    + "$SessionId", e.getMessage());
        }
        try {
            assertEquals("", AvpFormatter.hasOpenedCorrectCommandTag(
                    XMLMessages.CCR_WITH_DESCRIPTION_ATTR_AND_OPENED_AVP_TAG_WITH_WHITESPACE));
        } catch (DiameterXmlFormatException e) {
            assertEquals("Command CCR has some chars before/after. Row: &lt;CCR Description=\"CCR-I\" " + "Retransmit"
                    + "=\"true\"" + " Service=\"To_DiameterPeer_User_dp_user2\"&gt; &lt;" + "Session-Id&gt;"
                    + "$SessionId", e.getMessage());
        }
        try {
            assertEquals("", AvpFormatter.hasOpenedCorrectCommandTag(
                    XMLMessages.CCR_WITH_DESCRIPTION_ATTR_AND_WITHOUT_END_BRACKET));
        } catch (DiameterXmlFormatException e) {
            assertEquals(
                    "Command open tag has not closed bracket. Row: &lt;CCR Description=\"CCR-I\" Retransmit=\"true\" "
                            + "Service=\"To_DiameterPeer_User_dp_user2\"\n" + "&gt;&lt;Session-Id&gt;$SessionId&lt;"
                            + "/Session-Id&gt;", e.getMessage());
        }
    }

    @Test
    public void rowHasXmlTag() {
        assertNotNull(AvpFormatter.rowHasCorrectOpenedXmlTag(OPENED_CCR_TAG));
        assertNotNull(AvpFormatter.rowHasCorrectOpenedXmlTag(OPENED_CCR_TAG_WITH_DESCRIPTION));
        assertNull(AvpFormatter.rowHasCorrectOpenedXmlTag(NO_XML_TAG));
        assertNull(AvpFormatter.rowHasCorrectOpenedXmlTag(OPENED_CCR_TAG_WITHOUT_END_BRACKET));
    }

    @Test
    public void interruptIfRowHasOnlyBracket() {
        try {
            AvpFormatter.interruptIfRowHasOnlyBracket("<");
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_ONLY_BRACKETS + "&lt;", e.getMessage());
        }
        try {
            AvpFormatter.interruptIfRowHasOnlyBracket(">");
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_ONLY_BRACKETS + "&gt;", e.getMessage());
        }
        AvpFormatter.interruptIfRowHasOnlyBracket("hfdh<");
    }

    @Test
    public void interruptIfRowHasManyOpenedXmlTags() {
        try {
            AvpFormatter.interruptIfRowHasManyOpenedXmlTags(OPENED_CCR_TAG + OPENED_CCR_TAG);
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_MANY_TAGS + "&lt;CCR&gt;&lt;CCR&gt;",
                    e.getMessage());
        }
        AvpFormatter.interruptIfRowHasManyOpenedXmlTags(OPENED_CCR_TAG + CLOSED_CCR_TAG);
        AvpFormatter.interruptIfRowHasManyOpenedXmlTags(OPENED_CCR_TAG);
        AvpFormatter.interruptIfRowHasManyOpenedXmlTags("");
        AvpFormatter.interruptIfRowHasManyOpenedXmlTags("<><>");
        AvpFormatter.interruptIfRowHasManyOpenedXmlTags(XMLMessages.CCR_WITH_DESCRIPTION_ATTR_AND_OPENED_AVP_TAG);
    }

    @Test
    public void hasOpenedNotFormattedAvpTag() {
        assertNotNull(AvpFormatter.hasOpenedNotFormattedAvpTag(OPENED_ORIGIN_HOST_AVP_TAG, dictionary));
        assertNull(AvpFormatter.hasOpenedNotFormattedAvpTag(CLOSED_ORIGIN_HOST_AVP_TAG, dictionary));
        assertNull(AvpFormatter.hasOpenedNotFormattedAvpTag(OPENED_ORIGIN_HOST_AVP_TAG_WITH_ATTRIBUTES, dictionary));
        assertNotNull(
                AvpFormatter.hasOpenedNotFormattedAvpTag(OPENED_ORIGIN_HOST_AVP_TAG_WITH_SOMETHING_AFTER, dictionary));
        assertNotNull(
                AvpFormatter.hasOpenedNotFormattedAvpTag(OPENED_ORIGIN_HOST_AVP_TAG_WITH_SOMETHING_BEFORE, dictionary));
        try {
            assertNull(AvpFormatter.hasOpenedNotFormattedAvpTag(WRONG_CCC_AVP_TAG, dictionary));
        } catch (IllegalArgumentException e) {
            assertEquals("AVP not found by name CCC", e.getMessage());
        }
        assertNull(AvpFormatter.hasOpenedNotFormattedAvpTag(
                OPENED_ORIGIN_HOST_AVP_WITH_ATTRIBUTES_AND_VALUE_AND_CLOSED_TAG, dictionary));
        assertNotNull(
                AvpFormatter.hasOpenedNotFormattedAvpTag(OPENED_ORIGIN_HOST_AVP_WITH_VALUE_AND_CLOSED_TAG, dictionary));
        try {
            AvpFormatter.hasOpenedNotFormattedAvpTag(OPENED_ORIGIN_HOST_AVP_TAG_WITHOUT_END_BRACKET, dictionary);
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_NO_END_BRACKET + "&lt;Origin-Host", e.getMessage());
        }
        try {
            AvpFormatter.hasOpenedNotFormattedAvpTag(
                    OPENED_ORIGIN_HOST_AVP_TAG_WITHOUT_END_BRACKET_AND_TAB_SYMBOL_AT_THE_END, dictionary);
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_NO_END_BRACKET + "&lt;Origin-Host\t", e.getMessage());
        }
        try {
            AvpFormatter.hasOpenedNotFormattedAvpTag(
                    OPENED_ORIGIN_HOST_AVP_TAG_WITHOUT_END_BRACKET_AND_RETURN_CARET_SYMBOL_AT_THE_END, dictionary);
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_NO_END_BRACKET + "&lt;Origin-Host\r", e.getMessage());
        }
        try {
            assertNull(AvpFormatter.hasOpenedNotFormattedAvpTag(
                    OPENED_ORIGIN_HOST_AVP_TAG_WITH_SOMETHING_BEFORE_AND_NOT_CLOSED_BRACKET, dictionary));
        } catch (DiameterXmlFormatException e) {
            assertEquals(AvpFormatter.INCORRECT_FORMAT_NO_END_BRACKET + "ddd&gt;&lt;Origin-Host",
                    e.getMessage());
        }
    }
}
