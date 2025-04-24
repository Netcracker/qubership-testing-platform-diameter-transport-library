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

package org.qubership.automation.diameter.data.encoder.wireshark;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Splitter;

public class WireSharkMessageParser {

    private static final String VERSION = "Version";
    private static final String FLAGS = "Flags";
    private static final String COMMAND_CODE = "Command Code";
    private static final String APPLICATION_ID = "ApplicationId";
    private static final String DIGEST_PATTERN = "\\d+";
    private static final String BYTE_PATTERN = "0x" + "[0-9a-fA-F]+";
    private static final String AVP = "AVP:";
    private static final String AVP_VENDOR_ID = "AVP Vendor Id";
    private static final Pattern AVP_NAME_PATTERN = Pattern.compile("AVP: (.*?)\\(");
    private static final Pattern AVP_LENGTH_PATTERN = Pattern.compile("l=(\\d+)");
    private static final Pattern AVP_CODE_PATTERN = Pattern.compile("\\((\\d+)\\)");
    private static final Pattern AVP_VALUE_PATTERN = Pattern.compile("val=(.+)");
    private static final Pattern AVP_FLAGS_PATTERN = Pattern.compile("f=([^\\s]+)");
    private static final Pattern SPACES = Pattern.compile("^\\s+");
    private WireSharkMessage wireSharkMessage;
    private AvpRecord record = null;

    /**
     * Parse String message into WireSharkMessage.
     *
     * @param strMessage - message to parse.
     * @return - WireSharkMessage parsed.
     */
    public WireSharkMessage parse(String strMessage) {
        wireSharkMessage = new WireSharkMessage();
        Iterable<String> strings = Splitter.on("\n").split(strMessage);
        strings.forEach(value -> {
            String trim = value.trim();
            parse(wireSharkMessage, trim, value);
        });
        return wireSharkMessage;
    }

    private void parse(WireSharkMessage message, String trimmed, String source) {
        if (trimmed.startsWith(VERSION)) {
            message.setVersion(getValue(VERSION, trimmed, BYTE_PATTERN));
        } else if (trimmed.startsWith(FLAGS)) {
            message.setFlag(getValue(FLAGS, trimmed, BYTE_PATTERN));
        } else if (trimmed.startsWith(COMMAND_CODE)) {
            message.setCommand(getValue(COMMAND_CODE, trimmed, DIGEST_PATTERN));
        } else if (trimmed.startsWith(APPLICATION_ID)) {
            message.setApplicationId(getValue(APPLICATION_ID, trimmed, DIGEST_PATTERN));
        } else if (trimmed.startsWith(AVP)) {
            message.getAvpRecords().add(parseAvp(trimmed, source));
        } else if (record != null && trimmed.startsWith(record.getName() + ":")) {
            record.setHexValue(getValue(record.getName(), trimmed, ":(.*?)$").replaceFirst(": ", ""));
        } else if (trimmed.startsWith(AVP_VENDOR_ID)) {
            List<Pair<Integer, AvpRecord>> records = message.getAvpRecords();
            records.get(records.size() - 1).getValue().setVendor(getVendor(trimmed));
        }
    }

    private String getVendor(String trimmed) {
        Matcher matcher = AVP_CODE_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Parse String representation of Avp into level + AvpRecord pair.
     *
     * @param trimmed - String to parse (trimmed spaces),
     * @param source - source String,
     * @return - pair of level + AvpRecord.
     */
    public Pair<Integer, AvpRecord> parseAvp(String trimmed, String source) {
        Matcher matcher = AVP_LENGTH_PATTERN.matcher(trimmed);
        record = new AvpRecord();
        if (matcher.find()) {
            record.setLength(matcher.group(1));
        }
        matcher = AVP_NAME_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            record.setName(matcher.group(1));
        }
        matcher = AVP_CODE_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            record.setCode(matcher.group(1));
        }
        matcher = AVP_VALUE_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            record.setValue(matcher.group(1));
        }
        matcher = AVP_FLAGS_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            record.setFlags(matcher.group(1));
        }
        matcher = SPACES.matcher(source);
        int level = 0;
        if (matcher.find()) {
            level = matcher.end();
        }
        return Pair.of(level, record);
    }

    private String getValue(String property, String trim, String pattern) {
        Pattern compile = Pattern.compile(pattern);
        Matcher matcher = compile.matcher(trim);
        if (matcher.find()) {
            return trim.substring(matcher.start(), matcher.end());
        }
        throw new IllegalArgumentException(String.format("Unable to parse property '%s' (input '%s', pattern '%s')",
                property, trim, pattern));
    }
}
