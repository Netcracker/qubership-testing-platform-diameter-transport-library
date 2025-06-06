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

import static org.qubership.automation.diameter.data.constants.CommandXmlTags.COMMAND_XML_TAGS;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.diameter.avp.AVPEntity;
import org.qubership.automation.diameter.dictionary.DiameterDictionary;
import org.qubership.automation.diameter.dictionary.DiameterDictionaryHolder;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.qubership.automation.diameter.exception.DiameterXmlFormatException;

public class AvpFormatter {

    /**
     * Pattern to identify XML tag.
     */
    private static final Pattern XML_TAG_PATTERN = Pattern.compile("<(.+?)>");

    /**
     * Pattern to identify 2 opened XML tags.
     */
    private static final Pattern TWO_OPEN_XML_TAG_PATTERN = Pattern.compile("<([a-zA-Z].+?)>(.*)<([a-zA-Z].+?)>");

    /**
     * Pattern to identify that row contains not only command.
     */
    private static final Pattern NOT_ONLY_COMMAND_IN_ROW_PATTERN = Pattern.compile("^<(.+?[^<>])>+[^.*]+$");

    /**
     * Constant for end of XML tag.
     */
    private static final char END = '>';

    /**
     * Constant for incorrect row format message (only opened/closed bracket in the row).
     */
    public static final String INCORRECT_FORMAT_ONLY_BRACKETS =
            "Row has incorrect xml format - only opened/closed bracket in the row: ";

    /**
     * Constant for incorrect row format message (many opened xml tags in 1 row).
     */
    public static final String INCORRECT_FORMAT_MANY_TAGS =
            "Row has incorrect xml format - many opened xml tags in 1 row: ";

    /**
     * Constant for incorrect row format message (avp tag doesn't have end bracket).
     */
    public static final String INCORRECT_FORMAT_NO_END_BRACKET =
            "Row has incorrect xml format - avp tag doesn't have end bracket. Row: ";

    /**
     * Constant for incorrect row format message (Command open tag has not closed bracket).
     */
    public static final String INCORRECT_FORMAT_NO_CLOSED_BRACKET =
            "Command open tag has not closed bracket. Row: ";

    /**
     * This method add code and vendor attribute from diameter dictionary (by path and type) for all opened avp tags
     * that xml template contains.
     *
     * @param templateContent  xml diameter template content (text)
     * @param dictionaryConfig {@link DictionaryConfig} that contains dictionary path,
     *                                                 parser Class extending DiameterParser, UUID.
     * @return return formatted content if all avp was found by name in dictionaries and content isn't contains wrong
     *         xml string format.
     * @throws DiameterXmlFormatException when xml string has wrong format.
     *                                    - two or more opened xml tags in one row
     *                                    - command or avp hasn't got closed bracket
     *                                    - row has got only one symbol - "&lt;" or "&gt;"
     *                                    - command has something before/after tag
     */
    public static String formatAvp(final String templateContent, final DictionaryConfig dictionaryConfig)
            throws DiameterXmlFormatException, IllegalArgumentException {
        String parserType = dictionaryConfig.getParserClass().getSimpleName();
        String dictionaryPath = dictionaryConfig.getDictionaryPath();
        DiameterDictionary dictionary = DiameterDictionaryHolder.getInstance().getDictionary(dictionaryConfig);
        if (Objects.isNull(dictionary)) {
            throw new IllegalStateException(
                    "Something went wrong... Diameter dictionary with [" + dictionaryPath + "] path and type ["
                            + parserType + "] wasn't found in dictionary holder.");
        }
        StringBuilder result = new StringBuilder();
        String[] messageRows = templateContent.split("\n");
        for (String row : messageRows) {
            validateRowAndFormatAvp(row, result, dictionary);
        }
        return String.valueOf(result);
    }

    private static void validateRowAndFormatAvp(final String row,
                                                final StringBuilder result,
                                                final DiameterDictionary dictionary)
            throws DiameterXmlFormatException, IllegalArgumentException {
        interruptIfRowHasOnlyBracket(row);
        interruptIfRowHasManyOpenedXmlTags(row);
        String commandTag = hasOpenedCorrectCommandTag(row);
        boolean isOpenedCommandTag = StringUtils.isNotEmpty(commandTag);
        if (isOpenedCommandTag) {
            result.append(row).append("\n");
            return;
        }
        AVPEntity avp = hasOpenedNotFormattedAvpTag(row, dictionary);
        boolean hasAvpTag = Objects.nonNull(avp);
        if (!hasAvpTag) {
            result.append(row).append("\n");
            return;
        }
        format(row, avp, result);
    }

    private static void format(final String row, final AVPEntity avpEntity, final StringBuilder result) {
        String avpName = avpEntity.getName();
        String s = row.replaceFirst(avpName,
                avpName + String.format(" code=\"%d\" vendor=\"%d\"", avpEntity.getId(), avpEntity.getVendorId()));
        result.append(s).append("\n");
    }

    /**
     * Format AVP without code and vendor attributes.
     *
     * @param templateContent  String to process
     * @return String AVP representation.
     */
    public static String reverseFormatAvp(final String templateContent) {
        return templateContent.replaceAll(" code=\"([0-9])+\" vendor=\"([0-9])+\"", "");
    }

    /**
     * Return command name if one of COMMAND_XML_TAGS contains in the input string.
     *
     * @param row - string message
     * @return command short name
     */
    public static String hasOpenedCorrectCommandTag(final String row) {
        String result = null;
        for (String commandTag : COMMAND_XML_TAGS) {
            if (row != null && !row.isEmpty() && row.trim().toLowerCase().contains(commandTag)) {
                interruptIfWrongRowWithCommandTag(row, commandTag);
                result = commandTag.substring(1);
                break;
            }
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * This method validate that row contains closed bracket after diameter command xml tag
     * in the input row.
     *
     * @param row string that can contains diameter xml tags
     * @param command String command to parse
     * @return true - if xml tag has closed bracket.
     */
    private static boolean hasCommandClosedBracket(final String row, final String command) {
        int indexOfStartCommandBracket = row.toLowerCase().indexOf(command.toLowerCase());
        int indexOfEndCommandBracket = row.indexOf(END, indexOfStartCommandBracket);
        if (indexOfEndCommandBracket != -1) {
            boolean rowHasNewlineIntoCommandTag = row.substring(indexOfStartCommandBracket, indexOfEndCommandBracket)
                    .contains("\n");
            return !rowHasNewlineIntoCommandTag;
        }
        return false;
    }

    /**
     * Method checks that input string contains xml tag.
     *
     * @param row string that can contains xml tag.
     * @return Matcher object if xml tag was found.
     */
    public static Matcher rowHasCorrectOpenedXmlTag(final String row) {
        Matcher matcher = XML_TAG_PATTERN.matcher(row);
        boolean found = matcher.find();
        return found ? matcher : null;
    }

    /**
     * This method throw exception if row contains only "&lt;" or "&gt;" symbol.
     *
     * @param row string that you need to validate
     * @throws DiameterXmlFormatException if input string contains only "&lt;" or "&gt;" symbol.
     */
    protected static void interruptIfRowHasOnlyBracket(final String row) {
        if (row.trim().equals("<") || row.trim().equals(">")) {
            throw new DiameterXmlFormatException(INCORRECT_FORMAT_ONLY_BRACKETS + escapeXmlBrackets(row));
        }
    }

    protected static void interruptIfRowHasManyOpenedXmlTags(final String row) {
        boolean hasTwoOrManyOpenedTags = row.trim().matches(TWO_OPEN_XML_TAG_PATTERN.pattern());
        if (hasTwoOrManyOpenedTags) {
            throw new DiameterXmlFormatException(INCORRECT_FORMAT_MANY_TAGS + escapeXmlBrackets(row));
        }
    }

    protected static AVPEntity hasOpenedNotFormattedAvpTag(final String row,
                                                           final DiameterDictionary dictionary)
            throws IllegalArgumentException {
        AVPEntity result = null;
        Matcher matcher = rowHasCorrectOpenedXmlTag(row);
        boolean hasCorrectOpenedXmlTag = Objects.nonNull(matcher);
        if (hasCorrectOpenedXmlTag) {
            String tagContent = matcher.group(1);
            if (!isAlreadyFormatted(tagContent) && !isEndXmlTag(tagContent)) {
                tagContent = trimRow(tagContent);
                result = dictionary.getAvpDictionary().getByName(tagContent);
            }
        } else {
            if (hasAvpTagWithoutEndBracket(row, dictionary)) {
                throw new DiameterXmlFormatException(INCORRECT_FORMAT_NO_END_BRACKET + escapeXmlBrackets(row));
            }
        }
        return result;
    }

    private static boolean hasAvpTagWithoutEndBracket(final String inputRow,
                                                      final DiameterDictionary dictionary) {
        String[] split = inputRow.split("<");
        for (String splitRow : split) {
            splitRow = trimRow(splitRow);
            if (splitRow.isEmpty()) {
                continue;
            }
            int endIndex = findIndexBeforeSpace(splitRow);
            String avpName = splitRow.substring(0, endIndex);
            try {
                dictionary.getAvpDictionary().getByName(avpName);
                return true;
            } catch (IllegalArgumentException ignored) {
                //noinspection UnnecessaryContinue
                continue;
            }
        }
        return false;
    }

    private static int findIndexBeforeSpace(final String row) {
        int spaceIndex = row.indexOf(" ");
        return spaceIndex == -1
                ? row.length()
                : spaceIndex;
    }

    private static String trimRow(final String row) {
        return row.replaceAll("\\t", "")
                .replaceAll("\\r", "")
                .trim();
    }

    private static void interruptIfWrongRowWithCommandTag(final String row, final String commandTag) {
        boolean hasCommandClosedBracket = hasCommandClosedBracket(row, commandTag);
        if (!hasCommandClosedBracket) {
            throw new DiameterXmlFormatException(INCORRECT_FORMAT_NO_CLOSED_BRACKET + escapeXmlBrackets(row));
        }
        if (rowHasNotOnlyCommandTag(row, commandTag)) {
            throw new DiameterXmlFormatException(
                    "Command " + commandTag.substring(1).toUpperCase() + " has some chars before/after. Row: "
                            + escapeXmlBrackets(row));
        }
    }

    private static boolean rowHasNotOnlyCommandTag(final String row, final String commandTag) {
        boolean startWithCorrectCommandTag = row.trim().toLowerCase().startsWith(commandTag);
        if (!startWithCorrectCommandTag) {
            return true;
        }
        return row.trim().matches(NOT_ONLY_COMMAND_IN_ROW_PATTERN.pattern());
    }

    private static boolean isAlreadyFormatted(final String string) {
        return string.contains("code=") || string.contains("vendor=");
    }

    private static boolean isEndXmlTag(final String xmlTag) {
        return xmlTag.contains("/");
    }

    private static String escapeXmlBrackets(final String row) {
        return row.replaceAll(">", "&gt;").replaceAll("<", "&lt;");
    }
}
