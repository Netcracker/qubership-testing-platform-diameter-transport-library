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

package org.qubership.automation.diameter.avp;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.diameter.data.Converter;

@SuppressWarnings({"AbbreviationAsWordInName",
        "checkstyle:JavadocVariable",
        "checkstyle:MagicNumber"})
public enum AVPType {

    UTF8_STRING("UTF8String") {
        public byte[] encode(final String message) {
            return message.getBytes(StandardCharsets.UTF_8);
        }

        public String decode(final byte[] message) {
            return new String(message, StandardCharsets.UTF_8);
        }
    },

    INTEGER32("Integer32") {
        @Override
        public byte[] encode(final String message) {
            return Converter.intToBytes(message);
        }

        @Override
        public byte[] encode(final int message) {
            return Converter.intToBytes(message);
        }

        @Override
        public Integer decode(final byte[] message) {
            return Converter.bytesToInt(message);
        }
    },

    UNSIGNED32("Unsigned32") {
        @Override
        public byte[] encode(final String message) {
            if (message.contains(HEX_PREFIX)) {
                return Converter.hexToBytes(message.replaceFirst(HEX_PREFIX, StringUtils.EMPTY), 4);
            }
            return Converter.unsigned32ToBytes(message);
        }

        @Override
        public byte[] encode(final int message) {
            return Converter.unsigned32ToBytes(message);
        }

        @Override
        public String decode(final byte[] message) {
            return Converter.bytesToUnsigned(message).toString();
        }
    },

    ADDRESS("Address") {
        @Override
        public byte[] encode(final String message) {
            return Converter.addressToBytes(message);
        }

        @Override
        public String decode(final byte[] message) {
            return Converter.bytesToAddress(message);
        }
    },

    INTEGER64("Integer64") {
        @Override
        public byte[] encode(final String message) {
            return Converter.longToBytes(Long.parseLong(message));
        }

        @Override
        public String decode(final byte[] message) {
            return Converter.bytesToUnsigned(message).toString();
        }
    },

    UNSIGNED64("Unsigned64") {
        @Override
        public byte[] encode(final String message) {
            return Converter.unsigned64ToBytes(message);
        }

        @Override
        public String decode(final byte[] message) {
            return Converter.bytesToUnsigned(message).toString();
        }
    },

    SIGNED32("Signed32") {
        @Override
        public byte[] encode(final String message) {
            return Converter.intToBytes(message);
        }

        @Override
        public String decode(final byte[] message) {
            return String.valueOf(Converter.bytesToInt(message));
        }
    },

    SIGNED64("Signed64") {
        @Override
        public byte[] encode(final String message) {
            return Converter.longToBytes(Long.parseLong(message));
        }

        @Override
        public String decode(final byte[] message) {
            return Converter.bytesToLong(message).toString();
        }
    },

    DIAMETER_IDENTITY("DiameterIdentity") {
        @Override
        public byte[] encode(final String message) {
            return message.getBytes();
        }

        @Override
        public String decode(final byte[] message) {
            return new String(message);
        }
    },
    DIAMETER_URI("DiameterURI") {
        @Override
        public byte[] encode(final String message) {
            return message.getBytes();
        }

        @Override
        public String decode(final byte[] message) {
            return new String(message);
        }
    },
    IP_FILTER_RULE("IPFilterRule") {
        @Override
        public byte[] encode(final String message) {
            return message.getBytes();
        }

        @Override
        public String decode(final byte[] message) {
            return new String(message);
        }
    },
    OCTET_STRING("OctetString") {
        @Override
        public byte[] encode(String message) {
            if (message.startsWith(HEX_PREFIX)) {
                message = message.replaceFirst(HEX_PREFIX, StringUtils.EMPTY);
                int len = message.length();
                byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                    data[i / 2] = (byte) ((Character.digit(message.charAt(i), 16) << 4)
                            + Character.digit(message.charAt(i + 1), 16));
                }
                return data;
            }
            if (message.startsWith(BYTES_PREFIX)) {
                message = message.replaceFirst(BYTES_PREFIX, StringUtils.EMPTY);
                int number = Integer.decode(message);
                int groupSize = 8;
                int groups = 1;
                StringBuilder result = new StringBuilder();

                for (int i = groupSize * groups - 1; i >= 0; i--) {
                    int mask = 1 << i;
                    result.append((number & mask) != 0 ? "1" : "0");
                    if (i % groupSize == 0) {
                        result.append(" ");
                    }
                }
                result.replace(result.length() - 1, result.length(), "");
                return result.toString().getBytes();
            }
            /*
                According to project requirements, octet string must be converted as simple text.
            */
            return message.getBytes();
        }

        @Override
        public String decode(final byte[] message) {
            return Converter.bytesHexToText(message);
        }
    },
    TIME("Time") {
        @Override
        public byte[] encode(final String message) {
            return Converter.linuxDateToBytes(message);
        }

        @Override
        public String decode(final byte[] message) {
            return Converter.bytesToLinuxDate(message);
        }
    },
    FLOAT64("Float64") {
        @Override
        public byte[] encode(final String message) {
            return Converter.longToBytes(Long.parseLong(message));
        }

        @Override
        public String decode(final byte[] message) {
            return Converter.bytesToLong(message).toString();
        }
    },
    GROUPED("Grouped") {
        @Override
        public byte[] encode(final String message) {
            throw new NotImplementedException("Grouped can't be encoded through the type");
        }

        @Override
        public String decode(final byte[] message) {
            throw new NotImplementedException("Grouped can't be decoded through the type");
        }
    },
    ENUMERATE("Enumerated") {
        @Override
        public byte[] encode(final String message) {
            throw new NotImplementedException("Enumerated can't be encoded through the type");
        }

        @Override
        public String decode(final byte[] message) {
            throw new NotImplementedException("Enumerated can't be decoded through the type");
        }
    };

    private static final String HEX_PREFIX = "0x";
    private static final String BYTES_PREFIX = "Bx";
    private final String avpType;

    AVPType(final String avpType) {
        this.avpType = avpType;
    }

    /**
     * Get AVPType by name (equalsIgnoreCase).
     *
     * @param value - type name to search,
     * @return - AVPType found; IllegalArgumentException otherwise.
     */
    public static AVPType fromString(final String value) {
        for (AVPType type : AVPType.class.getEnumConstants()) {
            if (type.getAvpType().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown avp type: " + value);
    }

    public String getAvpType() {
        return avpType;
    }

    public abstract byte[] encode(String message);

    public byte[] encode(final int message) {
        throw new NotImplementedException("Method is not allowed for this AVP type");
    }

    public abstract <T> T decode(byte[] message);
}
