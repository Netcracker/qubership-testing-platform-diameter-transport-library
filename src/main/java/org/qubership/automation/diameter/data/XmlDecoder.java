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

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.diameter.avp.AVPDictionary;
import org.qubership.automation.diameter.avp.AVPEntity;
import org.qubership.automation.diameter.avp.AVPProvider;
import org.qubership.automation.diameter.avp.AVPType;
import org.qubership.automation.diameter.command.CommandDictionary;
import org.qubership.automation.diameter.command.CommandTypeChecker;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.qubership.automation.diameter.dictionary.DictionaryService;
import org.qubership.automation.diameter.exception.DecodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlDecoder extends Decoder {

    private static final char BEGIN = '<';
    private static final char END = '>';
    private static final String CLOSE = "</";
    private static final int REQUEST_FLAG = -128;
    private static final int REQUEST_AND_ERROR_FLAGS = -96;
    private static final int REQUEST_AND_PROXIABLE_FLAGS = -64;
    private static final int REQUEST_AND_PROXIABLE_AND_ERROR_FLAGS = -32;
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlDecoder.class);
    private static boolean APPEND_AVPCODE = Boolean.parseBoolean(
            System.getProperty("diameter.xmldecoder.appendAvpcode", "false"));
    private static boolean APPEND_AVPVENDOR = Boolean.parseBoolean(
            System.getProperty("diameter.xmldecoder.appendAvpvendor", "false"));

    /**
     * Constructor.
     *
     * @param dictionaryConfig DictionaryConfig object.
     */
    public XmlDecoder(DictionaryConfig dictionaryConfig) {
        super(dictionaryConfig);
    }

    /**
     * Set flag if AVP code should be appended to AVP String representation or not.
     *
     * @param value boolean value to set.
     */
    public void setAppendAvpcode(boolean value) {
        APPEND_AVPCODE = value;
    }

    /**
     * Set flag if AVP vendor ID should be appended to AVP String representation or not.
     *
     * @param value boolean value to set.
     */
    public void setAppendAvpvendor(boolean value) {
        APPEND_AVPVENDOR = value;
    }

    /**
     * Decode byte array to xml message.
     *
     * @param data ByteBuffer diameter message
     * @return decoded string xml message
     */
    public String decode(final ByteBuffer data) {
        byte[] message = data.array();
        //get message length
        int messageLength = getMessageLength(message);
        //remove bytes which describes message length
        message = slice(message, QUARTER, messageLength);
        //get command from next 4 bytes
        String command = getCommand(message);
        LOGGER.debug("Decode command {} with length {}", command, messageLength);
        //remove command description
        message = slice(message, QUARTER, message.length);
        message = cutApplicationId(message, command);
        message = cutHopByHopAndEndToEnd(message);

        StringBuilder decodedMessage = new StringBuilder();
        decodedMessage.append(BEGIN).append(command).append(END);
        parseContent(message, decodedMessage);
        decodedMessage.append(CLOSE).append(command).append(END);
        return decodedMessage.toString();
    }

    private Object decode(final AVPEntity avp, final byte[] avpBody, final StringBuilder decodedMessage) {
        if (avpBody.length == 0) {
            return 0;
        }
        AVPType type = avp.getType();
        if (type == null) {
            throw new IllegalStateException(avp + " doesn't have described the 'simple type'.");
        }
        if (AVPType.GROUPED.equals(type)) {
            parseContent(avpBody, decodedMessage);
            return StringUtils.EMPTY;
        }
        if (AVPType.ENUMERATE.equals(type)) {
            return avp.getEnumerated(Converter.bytesToInt(avpBody));
        }
        return type.decode(avpBody);
    }

    private byte[] cutApplicationId(byte[] message, final String command) {
        boolean connection = CommandTypeChecker.isConnectionCommand(command);
        boolean creditControl = CommandTypeChecker.isCreditControlCommand(command);
        boolean spendingLimit = CommandTypeChecker.isSpendingLimitCommand(command);
        boolean session = CommandTypeChecker.isSessionCommand(command);
        boolean statusNotification = CommandTypeChecker.isStatusNotificationCommand(command);
        if (connection || creditControl || spendingLimit || session || statusNotification) {
            message = slice(message, 4, message.length);
        }
        return message;
    }

    private byte[] cutHopByHopAndEndToEnd(byte[] message) {
        message = slice(message, 8, message.length);
        return message;
    }

    private void parseContent(byte[] message, final StringBuilder decodedMessage) {
        int avpId;
        int length;
        int vendorId;
        try {
            while (message.length > 8) {
                byte[] avpCodeBytes = Arrays.copyOfRange(message, 0, QUARTER);
                avpCodeBytes[0] = 0x0; //we don't need flags in getting AVP code.
                byte[] avpLengthBytes = getAvpLengthBytes(message, QUARTER, QUARTER * 2);
                avpId = ByteBuffer.wrap(avpCodeBytes).getInt();
                length = ByteBuffer.wrap(avpLengthBytes).getInt();
                vendorId = isVendorSpecificFlag(message, QUARTER, QUARTER * 2) ? getVendorId(message) : 0;
                AVPEntity avp = getAvp(avpId, vendorId);
                byte[] avpBody = getBody(avp, message, length);
                if (avpBody.length != 0) {
                    decodedMessage.append(BEGIN).append(avp.getName());
                    if (APPEND_AVPCODE) {
                        decodedMessage.append(" code=\"").append(avp.getId()).append("\"");
                        if (APPEND_AVPVENDOR && avp.getVendorId() != 0) {
                            decodedMessage.append(" vendor=\"").append(avp.getVendorId()).append("\"");
                        }
                    }
                    decodedMessage
                            .append(END).append(decode(avp, avpBody, decodedMessage))
                            .append(CLOSE).append(avp.getName())
                            .append(END);
                }
                message = slice(message, roundLength(length), message.length);
            }
        } catch (Exception e) {
            throw new DecodeException("Failed parsing AVPs: ", decodedMessage.toString(), message, e);
        }
    }

    private int getVendorId(final byte[] message) {
        byte[] avpVendor = Arrays.copyOfRange(message, QUARTER * 2, QUARTER * 3);
        return ByteBuffer.wrap(avpVendor).getInt();
    }

    private int roundLength(final int length) {
        if (length % QUARTER != 0) {
            return (int) Math.ceil((double) length / QUARTER) * QUARTER;
        }
        return length;
    }

    @SuppressWarnings("SameParameterValue")
    private byte[] getAvpLengthBytes(final byte[] message, final int quarter, final int to) {
        byte[] bytes = Arrays.copyOfRange(message, quarter  /* +1 ignore the flag*/, to/*again this flag*/);
        bytes[0] = 0; //let's ignore the flag
        return bytes;
    }

    /**
     * The Vendor-ID field is present in message if the 'V' bit is set in the AVP Flags field. (RFC 6733 Diameter
     * Base Protocol Page 41)
     *
     * @param message input byte array
     * @param quarter starting four bytes
     * @param to      ending four bytes
     * @return true if vendor id is specified (not 0)
     */
    @SuppressWarnings("SameParameterValue")
    private boolean isVendorSpecificFlag(final byte[] message, final int quarter, final int to) {
        byte[] bytes = Arrays.copyOfRange(message, quarter, to);
        byte avpFlags = bytes[0]; //getting the byte of AVP flags
        return (avpFlags & (1 << 7)) != 0; //checking the bit #7 ('V' bit - flag for vendor)
    }

    private byte[] getBody(final AVPEntity avp, final byte[] message, final int length) {
        if (!avp.getName().toLowerCase().endsWith("vendor-id") && avp.getVendorId() > 0) {
            return Arrays.copyOfRange(message, QUARTER * 3, length);
        }
        return Arrays.copyOfRange(message, QUARTER * 2, length);
    }

    private AVPEntity getAvp(final int avpId, final int vendorId) {
        AVPDictionary avpDictionary = DictionaryService.getInstance().getAvpDictionary(dictionaryConfig);
        AVPProvider avpProvider = avpDictionary;
        if (avpDictionary.isVendorExist(vendorId)) {
            avpProvider = avpDictionary.getVendor(vendorId);
        }
        return avpProvider.getById(avpId);
    }

    private String getCommand(final byte[] message) {
        byte[] slice = slice(message, 0, 4);
        byte flags = slice[0];
        boolean request = isRequest(flags);
        slice[0] = 0;
        int commandId = ByteBuffer.wrap(slice).getInt();
        CommandDictionary commandDictionary = DictionaryService.getInstance().getCommandDictionary(dictionaryConfig);
        if (request) {
            return commandDictionary.getRequest(commandId).getShortName();
        } else {
            return commandDictionary.getAnswer(commandId).getShortName();
        }
    }

    private boolean isRequest(final byte flags) {
        switch (flags) {
            case REQUEST_FLAG:
            case REQUEST_AND_ERROR_FLAGS:
            case REQUEST_AND_PROXIABLE_FLAGS:
            case REQUEST_AND_PROXIABLE_AND_ERROR_FLAGS:
                return true;
            default:
                return false;
        }
    }

    private byte[] slice(final byte[] message, final int from, final int to) {
        return Arrays.copyOfRange(message, from, to);
    }

    /**
     * Getting diameter message length from specific bytes in message.
     *
     * @param message part of whole diameter message (byte array)
     *                that contains specific byte and responsible to length of message.
     * @return int length of diameter message.
     */
    public int getMessageLength(final byte[] message) {
        byte[] bytes = Arrays.copyOfRange(message, 0, 4);
        bytes[0] = 0;
        return ByteBuffer.wrap(bytes).getInt();
    }
}
