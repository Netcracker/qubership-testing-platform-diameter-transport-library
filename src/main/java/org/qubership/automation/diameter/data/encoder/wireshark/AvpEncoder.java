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

import static java.lang.System.arraycopy;
import static org.qubership.automation.diameter.data.Converter.intToBytes;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.qubership.automation.diameter.avp.AVPDictionary;
import org.qubership.automation.diameter.avp.AVPEntity;
import org.qubership.automation.diameter.avp.AVPType;
import org.qubership.automation.diameter.data.encoder.Utils;

public class AvpEncoder {

    private static final char DASH = '-';
    public static final int QUARTER = 4;
    public static final byte[] EMPTY = {};
    private final AVPDictionary avpDictionary;
    private final Pattern digestPattern = Pattern.compile(".+\\((\\d+)\\)");
    private List<Pair<Integer, AvpRecord>> records;
    private int currentIndex = -1;

    public AvpEncoder(AVPDictionary avpDictionary) {
        this.avpDictionary = avpDictionary;
    }

    public byte[] encode(List<Pair<Integer, AvpRecord>> avpRecords) {
        this.records = avpRecords;
        return getAvps();
    }

    private byte[] getAvps() {
        byte[] grouped;
        byte[] temp = new byte[]{};
        byte[] local;
        while (has(currentIndex)) {
            Pair<Integer, AvpRecord> pair = next(currentIndex);
            AvpRecord record = pair.getValue();
            AVPEntity entity = getEntity(record);
            if (entity.getType() == AVPType.GROUPED) {
                if (hasChilds(currentIndex, pair.getKey())) {
                    currentIndex++;
                    grouped = getAvps();
                    temp = concatenate(temp, concatenate(getGroupAvp(entity, record, grouped.length), grouped));
                    if (!back(currentIndex, pair.getKey())) {
                        return temp;
                    }
                } else {
                    local = buildContent(record);
                    temp = concatenate(temp, local);
                    if (!back(currentIndex, pair.getKey())) {
                        return temp;
                    }
                }
            } else {
                local = buildContent(record);
                temp = concatenate(temp, local);
                if (!back(currentIndex, pair.getKey())) {
                    return temp;
                }
            }
            currentIndex++;
        }
        return temp;
    }

    private boolean back(int currentIndex, Integer lvl) {
        if (has(currentIndex + 1)) {
            Pair<Integer, AvpRecord> next = next(currentIndex + 1);
            return next.getKey().equals(lvl);
        }
        return false;
    }

    private boolean hasChilds(int currentIndex, Integer lvl) {
        if (has(currentIndex + 1)) {
            Pair<Integer, AvpRecord> next = next(currentIndex + 1);
            return next.getKey() > lvl;
        }
        return false;
    }

    private Pair<Integer, AvpRecord> next(int currentIndex) {
        return records.get(currentIndex + 1);
    }

    private boolean has(int currentIndex) {
        return records.size() > currentIndex + 1;
    }

    private AVPEntity getEntity(AvpRecord record) {
        String vendor = record.getVendor();
        if (vendor == null) {
            return avpDictionary.getById(Utils.parseInt(record.getCode()));
        }
        return avpDictionary.getVendor(Utils.parseInt(vendor)).getById(Utils.parseInt(record.getCode()));
    }

    private byte[] buildContent(AvpRecord record) {
        Integer avpId = Utils.parseInt(record.getCode());
        AVPType avpType;
        int vendorId = -1;
        int size = 8;
        if (record.getVendor() == null) {
            avpType = avpDictionary.getById(avpId).getType();
        } else {
            size += 4;
            vendorId = Utils.parseInt(record.getVendor());
            avpType = avpDictionary.getVendor(vendorId).getById(avpId).getType();
        }
        byte[] avpCode = intToBytes(avpId);
        byte flag = getAvpFlags(record.getFlags());
        byte[] vendor = {};

        if (vendorId > 0) {
            vendor = intToBytes(vendorId);
        }
        byte[] value = getValue(record, avpType);
        size += value.length;
        return concatValues(vendorId, size, avpCode, flag, vendor, value);
    }

    private byte[] concatValues(Integer vendorId, int size, byte[] avpCode, byte flag, byte[] vendor, byte[] value) {
        int padding = Utils.getPadLength(size);
        byte[] result = new byte[size + padding];
        int destPos = 0;
        arraycopy(avpCode, 0, result, destPos, 4);
        destPos += QUARTER;
        arraycopy(intToBytes(size), 0, result, destPos, 4);
        destPos += QUARTER;
        result[4] = flag;
        if (vendorId > 0) {
            arraycopy(vendor, 0, result, destPos, 4);
            destPos += QUARTER;
        }
        arraycopy(value, 0, result, destPos, value.length);
        return result;
    }

    private byte[] getValue(AvpRecord record, AVPType avpType) {
        if (avpType == AVPType.INTEGER32 || avpType == AVPType.INTEGER64
                || avpType == AVPType.SIGNED32 || avpType == AVPType.SIGNED64
                || avpType == AVPType.UNSIGNED32 || avpType == AVPType.UNSIGNED64) {
            return avpType.encode(getNumber(record));
        }
        switch (avpType) {
            case ENUMERATE :
                return AVPType.SIGNED32.encode(getNumber(record));
            case GROUPED :
                return EMPTY;
            case ADDRESS :
            case OCTET_STRING :
                return avpType.encode("0x" + record.getHexValue());
            default :
                return avpType.encode(record.getValue());
        }
    }

    private String getNumber(AvpRecord record) {
        String value = record.getValue();
        if (value.matches(".+\\(\\d+\\)")) {
            Matcher matcher = digestPattern.matcher(value);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return value;
    }

    /**
     * Get flags of AVP combined in one byte.
     *
     * @param text String fragment containing 3 first bytes of AVP flags.
     * @return byte with AVP flags combined.
     */
    public byte getAvpFlags(String text) {
        char vendorSpecified = text.charAt(0);
        char mandatoryByte = text.charAt(1);
        char protectedByte = text.charAt(2);
        byte result = 0;
        result += increaseIfNotDash(vendorSpecified, (byte) 0x80);
        result += increaseIfNotDash(mandatoryByte, (byte) 0x40);
        result += increaseIfNotDash(protectedByte, (byte) 0x20);
        return result;
    }

    private byte increaseIfNotDash(char vendorSpecified, byte result) {
        if (vendorSpecified != DASH) {
            return result;
        }
        return 0x0;
    }

    private byte[] getGroupAvp(AVPEntity avp, AvpRecord record, int groupLength) {
        int vendor = StringUtils.isBlank(record.getVendor()) ? 0 : Utils.parseInt(record.getVendor());
        int groupAvpLength = 8;
        byte flags = 0x00;
        flags = getAvpFlags(record.getFlags());
        if (vendor != 0) {
            groupAvpLength = 12;
        }
        byte[] b = new byte[groupAvpLength];
        arraycopy(intToBytes(avp.getId()), 0, b, 0, 4);
        byte[] flagLength = intToBytes(groupAvpLength + groupLength);
        flagLength[0] = flags;
        arraycopy(flagLength, 0, b, 4, 4);
        if (vendor != 0) {
            arraycopy(intToBytes(vendor), 0, b, 8, 4);
        }
        return b;
    }

    private byte[] concatenate(byte[] temp, byte[] local) {
        return ArrayUtils.addAll(temp, local);
    }

}
