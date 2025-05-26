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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.diameter.avp.AVPType;
import org.qubership.automation.diameter.data.encoder.Utils;

@SuppressWarnings("checkstyle:MagicNumber")
public class Converter {

    /**
     * Constant for default timezone.
     */
    public static String DEFAULT_TIMEZONE = "Etc/GMT+0";

    /**
     * Maximum unsigned 64-bit value.
     */
    private static final BigInteger MAX_UNSIGNED_64 = new BigInteger("18446744073709551615");

    /* Unused, commented. Not deleted, for possible future use
    private static final byte[] IPV_4_FAMILY = {0x00, 0x01};
    private static final byte[] IPV_6_FAMILY = {0x00, 0x02};
     */

    /**
     * Main date time format String.
     */
    private static final String DATE_TIME_FORMAT_MAIN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Secondary date time format String.
     */
    private static final String DATE_TIME_FORMAT_SECONDARY = "MMM dd, yyyy HH:mm:ss";

    /**
     * UTC time zone constant.
     */
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    /**
     * Date time zero point in the main format.
     */
    private static final String DATE_TIME_ZERO_POINT_MAIN_FORMAT = "1900-01-01 00:00:00";

    /**
     * Date time zero point in the secondary format.
     */
    private static final String DATE_TIME_ZERO_POINT_SECONDARY_FORMAT = "Jan 01, 1900 00:00:00";

    /**
     * Convert byte[] to int.
     *
     * @param bytesArray byte[] to convert
     * @return int value of conversion.
     */
    public static int bytesToInt(final byte[] bytesArray) {
        ByteBuffer bb = ByteBuffer.wrap(bytesArray);
        return bb.getInt();
    }

    /**
     * Convert address to byte array.
     *
     * @param address - String address to convert,
     * @return byte array converted result.
     */
    public static byte[] addressToBytes(final String address) {
        /*
            As we discussed with Maxim Paramonov, address must be converted to octet string.
            https://developer.opencloud.com/devportal/devportal/apis/diameter/diameter-connectivity-pack/2.0/
            diameter-connectivity-pack-2.0-javadoc/org/jainslee/resources/diameter/base/types/Address.html
         */
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException("Address is empty, can not encode it");
        }
        if (address.toLowerCase().startsWith("0x")) {
            return AVPType.OCTET_STRING.encode(address);
        }
        return ipToHexBytes(address);
    }

    /**
     * Convert String representation of int number into byte array.
     *
     * @param intValueAsString - String value to convert.
     * @return - byte array - converted result.
     */
    public static byte[] intToBytes(final String intValueAsString) {
        int intValue = Utils.parseInt(intValueAsString.trim());
        return intToBytes(intValue);
    }

    /**
     * Convert int parameter into byte array.
     *
     * @param intValue - int value to convert.
     * @return - byte array - converted result.
     */
    public static byte[] intToBytes(final int intValue) {
        ByteBuffer bb = ByteBuffer.allocate(4); // Integer takes 4 bytes
        bb.putInt(intValue);
        return bb.array();
    }

    /**
     * Convert [0;len-1] part of string parameter (containing hex digits) to bytes array.
     *
     * @param string - String containing hex digits,
     * @param len - length from the beginning of the string - how many characters to convert.
     * @return - byte array - converted result.
     */
    public static byte[] hexToBytes(final String string, final int len) {
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            char current = string.charAt(i);
            char next = string.charAt(i + 1);
            data[i / 2] = (byte) ((Character.digit(string.charAt(current), 16) << 4)
                    + Character.digit(string.charAt(next), 16));
        }
        return data;
    }

    /**
     * Convert byte array to String address.
     *
     * @param message - byte array to convert.
     * @return - String ip-address representation.
     */
    public static String bytesToAddress(final byte[] message) {
        try {
            StringBuilder builder = new StringBuilder();
            byte[] bytes;
            bytes = Arrays.copyOfRange(message, 4, message.length);
            try {
                bytes = DatatypeConverter.parseHexBinary(new String(bytes));
            } catch (Exception e) {
                bytes = Arrays.copyOfRange(message, 2, message.length);
            }
            //start from second as first two bytes is a family id 01, 02, 03 etc
            for (int index = 0; index < bytes.length; index++) {
                builder.append(Byte.toUnsignedInt(bytes[index]));
                if (index < bytes.length - 1) {
                    builder.append('.');
                }
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse host address from message "
                    + Arrays.toString(message), e);
        }
    }

    public static byte[] longToBytes(final long longValue) {
        return ByteBuffer.allocate(8).putLong(longValue).array();
    }

    /**
     * Convert byte array to long value.
     *
     * @param message - byte array to convert.
     * @return long value got from message.
     */
    public static Long bytesToLong(final byte[] message) {
        if (message.length != 8) {
            byte[] result = new byte[8];
            System.arraycopy(message, 0, result, result.length - message.length, message.length);
            return ByteBuffer.wrap(result).getLong();
        }
        return ByteBuffer.wrap(message).getLong();
    }

    /**
     * Convert byte array to Linux Date.
     *
     * @param message - byte array to convert.
     * @return String representation of date in default timezone.
     */
    public static String bytesToLinuxDate(final byte[] message) {
        try {
            return ntpToString(bytesToLong(message));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse date from value " + Arrays.toString(message));
        }
    }

    /**
     * Convert date in Linux Date String representation into byte array.
     *
     * @param message - Linux Date String representation,
     * @return - byte array - converted result.
     */
    public static byte[] linuxDateToBytes(final String message) {
        try {
            return intToBytes((int) stringToNtp(message));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse date from value " + message
                    + ". Probably invalid date format. Required: " + DATE_TIME_FORMAT_MAIN, e);
        }
    }

    private static long stringToNtp(final String s) throws ParseException {
        Date date2;
        DateFormat formatter;
        if (!s.matches("^\\d.+")) {
            //Sep  7, 2017 06:14:13.000000000 UTC
            formatter = new SimpleDateFormat(DATE_TIME_FORMAT_SECONDARY, Locale.ENGLISH);
            formatter.setTimeZone(UTC_TIME_ZONE);
            date2 = formatter.parse(DATE_TIME_ZERO_POINT_SECONDARY_FORMAT);
        } else {
            formatter = new SimpleDateFormat(DATE_TIME_FORMAT_MAIN, Locale.ENGLISH);
            formatter.setTimeZone(UTC_TIME_ZONE);
            date2 = formatter.parse(DATE_TIME_ZERO_POINT_MAIN_FORMAT);
        }
        formatter.setTimeZone(TimeZone.getTimeZone(Converter.DEFAULT_TIMEZONE));
        Date date = formatter.parse(s);
        return (date.getTime() - date2.getTime()) / 1000;
    }

    private static String ntpToString(final long l) throws ParseException {
        DateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT_MAIN);
        formatter.setTimeZone(UTC_TIME_ZONE);
        Date date2 = formatter.parse(DATE_TIME_ZERO_POINT_MAIN_FORMAT);
        Date date = new Date(l);
        int offset = TimeZone.getTimeZone(Converter.DEFAULT_TIMEZONE).getRawOffset();
        return formatter.format(new Date(date.getTime() * 1000 + offset + date2.getTime()));
    }

    /**
     * Convert String (as unsigned 32-bit) into byte array.
     *
     * @param value - String value to convert.
     * @return - byte array - converted result.
     */
    public static byte[] unsigned32ToBytes(final String value) {
        return unsigned32ToBytes(Long.parseLong(value.trim()));
    }

    /**
     * Convert long (as unsigned 32-bit) into byte array.
     *
     * @param value - long value to convert.
     * @return - byte array - converted result.
     */
    public static byte[] unsigned32ToBytes(final long value) {
        if (value <= 4294967295L && value >= 0) {
            byte[] bytes = longToBytes(value);
            byte[] range = Arrays.copyOfRange(bytes, 4, bytes.length);
            return getBytes(4, range);
        }
        throw new IllegalArgumentException("Unsigned32 is out of range 0<=" + value + "<=4294967295L");
    }

    /**
     * Convert String (as unsigned 64-bit) into byte array.
     *
     * @param value - String value to convert.
     * @return - byte array - converted result.
     */
    public static byte[] unsigned64ToBytes(final String value) {
        BigInteger val = new BigInteger(value);
        int upperRange = MAX_UNSIGNED_64.compareTo(val);
        int loverRange = BigInteger.ZERO.compareTo(val);
        if (upperRange >= 0 && loverRange <= 0) {
            byte[] bytes = val.toByteArray();
            int offset = getNotEmptyFirstOffset(bytes);
            byte[] range = Arrays.copyOfRange(bytes, offset, bytes.length);
            return getBytes(8, range);
        }
        throw new IllegalArgumentException("Unsigned64 is out of range 0<=" + value + "<=18446744073709551615");
    }

    public static BigInteger bytesToUnsigned(final byte[] bytes) {
        return new BigInteger(bytes);
    }

    private static byte[] getBytes(final int length, final byte[] range) {
        byte[] result = new byte[length];
        System.arraycopy(range, 0, result, length - range.length, range.length);
        return result;
    }

    private static int getNotEmptyFirstOffset(final byte[] result) {
        int offset = 0;
        for (byte b : result) {
            if (b == 0) {
                offset++;
            } else {
                break;
            }
        }
        return offset;
    }

    private static byte[] ipToHexBytes(final String ip) {
        StringBuilder hex = new StringBuilder();
        String[] part = ip.split("[.,]");
        if (part.length < 4) {
            return "00000000".getBytes();
        }
        for (int i = 0; i < 4; i++) {
            int decimal = Integer.parseInt(part[i]);
            if (decimal < 16) {
                hex.append('0').append(String.format("%01X", decimal));
            } else {
                hex.append(String.format("%01X", decimal));
            }
        }
        if (hex.length() == 8) {
            hex.insert(0, "0001");
        } else {
            hex.insert(0, "0002");
        }
        return DatatypeConverter.parseHexBinary(hex.toString());
    }

    /**
     * Convert String into byte array containing hex string representation of each character.
     *
     * @param message String message to convert.
     * @return - byte array - converted result.
     */
    public static byte[] textToHex(final String message) {
        StringBuilder builder = new StringBuilder();
        for (char ch : message.toCharArray()) {
            builder.append(Integer.toHexString(ch));
        }
        return builder.toString().getBytes();
    }

    /**
     * Array of hex digit characters.
     */
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Convert bytes array into String representation of hex digits.
     *
     * @param bytes - bytes array.
     * @return - String representation converted.
     */
    public static String bytesHexToText(final byte[] bytes) {
        StringBuilder output = new StringBuilder();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        String hexStr = new String(hexChars);
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

}
