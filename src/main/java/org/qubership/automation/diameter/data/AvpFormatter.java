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

    private static final Pattern XML_TAG_PATTERN = Pattern.compile("<(.+?)>");
    private static final Pattern TWO_OPEN_XML_TAG_PATTERN = Pattern.compile("<([a-zA-Z].+?)>(.*)<([a-zA-Z].+?)>");
    private static final Pattern NOT_ONLY_COMMAND_IN_ROW_PATTERN = Pattern.compile("^<(.+?[^<>])>+[^.*]+$");
    private static final char END = '>';

    /**
     * This method add code and vendor attribute from diameter dictionary (by path and type) for all opened avp tags
     * that xml template contains.
     *
     * @param templateContent  xml diameter template content (text)
     * @param dictionaryConfig {@link DictionaryConfig} that contains dictionary path, parser Class<? extends
     *                         DiameterParser>, UUID.
     * @return return formatted content if all avp was found by name in dictionaries and content isn't contains wrong
     *         xml string format.
     * @throws DiameterXmlFormatException when xml string has wrong format.
     *                                    - two or more opened xml tags in one row
     *                                    - command or avp hasn't got closed bracket
     *                                    - row has got only one symbol - "<" or ">"
     *                                    - command has something before/after tag
     */
    public static String formatAvp(String templateContent, DictionaryConfig dictionaryConfig)
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

    private static void validateRowAndFormatAvp(String row, StringBuilder result, DiameterDictionary dictionary)
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

    private static void format(String row, AVPEntity avpEntity, StringBuilder result) {
        String avpName = avpEntity.getName();
        String s = row.replaceFirst(avpName,
                avpName + String.format(" code=\"%d\" vendor=\"%d\"", avpEntity.getId(), avpEntity.getVendorId()));
        result.append(s).append("\n");
    }

    public static String reverseFormatAvp(String templateContent) {
        return templateContent.replaceAll(" code=\"([0-9])+\" vendor=\"([0-9])+\"", "");
    }

    /**
     * Return command name if one of COMMAND_XML_TAGS contains in the input string.
     *
     * @param row - string message
     * @return command short name
     */
    public static String hasOpenedCorrectCommandTag(String row) {
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
     * @param row string that can contains diameter xml tags.
     * @return true - if xml tag has closed bracket.
     */
    private static boolean hasCommandClosedBracket(String row, String command) {
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
    public static Matcher rowHasCorrectOpenedXmlTag(String row) {
        Matcher matcher = XML_TAG_PATTERN.matcher(row);
        boolean found = matcher.find();
        return found
                ? matcher
                : null;
    }

    /**
     * This method throw exception if row contains only "<" or ">" symbol.
     *
     * @param row string that you need to validate
     * @throws DiameterXmlFormatException if input string contains only "<" or ">" symbol.
     */
    protected static void interruptIfRowHasOnlyBracket(String row) {
        if (row.trim().equals("<") || row.trim().equals(">")) {
            throw new DiameterXmlFormatException(
                    "Row has incorrect xml format - only opened/closed bracket in the row: " + escapeXmlBrackets(row));
        }
    }

    protected static void interruptIfRowHasManyOpenedXmlTags(String row) {
        boolean hasTwoOrManyOpenedTags = row.trim().matches(TWO_OPEN_XML_TAG_PATTERN.pattern());
        if (hasTwoOrManyOpenedTags) {
            throw new DiameterXmlFormatException(
                    "Row has incorrect xml format - many opened xml tags in 1 row:" + escapeXmlBrackets(row));
        }
    }

    protected static AVPEntity hasOpenedNotFormattedAvpTag(String row, DiameterDictionary dictionary)
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
                throw new DiameterXmlFormatException(
                        "Row has incorrect xml format - avp tag hasn't got end bracket. " + "Row: " + escapeXmlBrackets(
                                row));
            }
        }
        return result;
    }

    private static boolean hasAvpTagWithoutEndBracket(String inputRow, DiameterDictionary dictionary) {
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

    private static int findIndexBeforeSpace(String row) {
        int spaceIndex = row.indexOf(" ");
        return spaceIndex == -1
                ? row.length()
                : spaceIndex;
    }

    private static String trimRow(String row) {
        row = row.replaceAll("\\t", "");
        row = row.replaceAll("\\r", "");
        return row.trim();
    }

    private static void interruptIfWrongRowWithCommandTag(String row, String commandTag) {
        boolean hasCommandClosedBracket = hasCommandClosedBracket(row, commandTag);
        if (!hasCommandClosedBracket) {
            throw new DiameterXmlFormatException(
                    "Command open tag has not closed bracket. Row: " + escapeXmlBrackets(row));
        }
        if (rowHasNotOnlyCommandTag(row, commandTag)) {
            throw new DiameterXmlFormatException(
                    "Command " + commandTag.substring(1).toUpperCase() + " has some chars before/after.. Row: "
                            + escapeXmlBrackets(row));
        }
    }

    private static boolean rowHasNotOnlyCommandTag(String row, String commandTag) {
        boolean startWithCorrectCommandTag = row.trim().toLowerCase().startsWith(commandTag);
        if (!startWithCorrectCommandTag) {
            return true;
        }
        return row.trim().matches(NOT_ONLY_COMMAND_IN_ROW_PATTERN.pattern());
    }

    private static boolean isAlreadyFormatted(String string) {
        return string.contains("code=") || string.contains("vendor=");
    }

    private static boolean isEndXmlTag(String xmlTag) {
        return xmlTag.contains("/");
    }

    private static String escapeXmlBrackets(String row) {
        return row.replaceAll(">", "&gt;").replaceAll("<", "&lt;");
    }
}
