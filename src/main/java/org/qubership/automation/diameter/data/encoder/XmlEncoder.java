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

package org.qubership.automation.diameter.data.encoder;

import static org.qubership.automation.diameter.data.Converter.intToBytes;
import static org.qubership.automation.diameter.data.XMLStringDataProcessor.isNotificationAnswer;
import static org.qubership.automation.diameter.data.constants.DiameterHeader.E2E;
import static org.qubership.automation.diameter.data.constants.DiameterHeader.HBH;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.diameter.avp.AVPDictionary;
import org.qubership.automation.diameter.avp.AVPEntity;
import org.qubership.automation.diameter.avp.AVPRule;
import org.qubership.automation.diameter.avp.AVPType;
import org.qubership.automation.diameter.command.Command;
import org.qubership.automation.diameter.command.CommandDictionary;
import org.qubership.automation.diameter.data.Encoder;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.qubership.automation.diameter.dictionary.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlEncoder extends Encoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlEncoder.class);
    private static final byte[] EMPTY_BYTES = new byte[0];
    private final byte[] zeroHbH = {0x00, 0x00, 0x00, 0x00};
    private final byte[] zeroE2E = {0x00, 0x00, 0x00, 0x00};
    private String previousSessionId = StringUtils.EMPTY;
    private int currentCommonHbH = 0;
    private int currentCommonE2E = 0;

    public XmlEncoder(@Nonnull DictionaryConfig dictionaryConfig) {
        this.dictionaryConfig = dictionaryConfig;
    }

    public ByteBuffer encode(String stringMessage) throws Exception {
        return encode(stringMessage, new HashMap<>());
    }

    /**
     * Encode xml string message to byte array (ByteBuffer).
     *
     * @param stringMessage xml string message.
     * @param headers       hbh and e2e diameter headers.
     * @return bytebuffer of string message.
     * @throws Exception if encoding is failed for message.
     */
    public ByteBuffer encode(String stringMessage, Map<String, Object> headers) throws Exception {
        boolean isNotificationAnswer = isNotificationAnswer(stringMessage);
        boolean isHbhAndE2eHeadersExist = isHbHAndE2eHeadersExist(headers);
        int avpLength = 0;
        int hbh = currentCommonHbH++;
        int e2e = currentCommonE2E++;
        int hbhLength = zeroHbH.length;
        int e2eLength = zeroE2E.length;
        if (isNotificationAnswer && isHbhAndE2eHeadersExist) {
            hbh = (int) headers.get(HBH);
            e2e = (int) headers.get(E2E);
            hbhLength = intToBytes(hbh).length;
            e2eLength = intToBytes(e2e).length;
        }
        Node nodes = getNodeFromString(stringMessage);
        String commandName = nodes.getNodeName();
        Command command = getCommand(commandName);
        byte[] applicationId = intToBytes(command.getApplicationId());
        byte[] code = command.convertToBytesAndSetFlags();
        byte[] avps = getContent(nodes, DictionaryService.getInstance().getAvpDictionary(dictionaryConfig));
        if (avps != null) {
            avpLength = avps.length;
        }
        int bufferSize = 4 + code.length + applicationId.length + hbhLength + e2eLength + avpLength;
        byte[] totalLength = intToBytes(bufferSize);
        totalLength[0] = 0x01; // This is really the version number [not involved in length]
        byte[] message = new byte[bufferSize];
        int currentIndex = 0;
        System.arraycopy(totalLength, 0, message, currentIndex, totalLength.length);
        currentIndex += totalLength.length;
        System.arraycopy(code, 0, message, currentIndex, code.length);
        currentIndex += code.length;
        System.arraycopy(applicationId, 0, message, currentIndex, applicationId.length);
        currentIndex += applicationId.length;
        System.arraycopy(intToBytes(hbh), 0, message, currentIndex, hbhLength);
        currentIndex += hbhLength;
        System.arraycopy(intToBytes(e2e), 0, message, currentIndex, e2eLength);
        currentIndex += e2eLength;
        if (avps != null) {
            System.arraycopy(avps, 0, message, currentIndex, avpLength);
        }
        return ByteBuffer.wrap(message);
    }

    private byte[] encode(Node childNode, AVPEntity entry) {
        Node firstChild = childNode.getFirstChild();
        if (firstChild == null) {
            return EMPTY_BYTES;
        }
        AVPType type = entry.getType();
        if (type == null) {
            return EMPTY_BYTES; //ENUMERATE AVP doesn't have message body
        }
        if (AVPType.ENUMERATE.equals(type)) {
            if (firstChild.hasChildNodes()) {
                return EMPTY_BYTES; //ENUMERATE doesn't have self body, there for return empty
            } else {
                return AVPType.SIGNED32.encode(firstChild.getNodeValue());
            }
        }
        return type.encode(firstChild.getNodeValue());
    }

    private Command getCommand(String commandName) {
        CommandDictionary commandDictionary = DictionaryService.getInstance().getCommandDictionary(dictionaryConfig);
        if (commandDictionary.isRequest(commandName)) {
            return commandDictionary.getRequest(commandName);
        }
        return commandDictionary.getAnswer(commandName);
    }

    //TODO need refactoring... can't understand what the method doing...
    private byte[] getContent(Node nodes, AVPDictionary dictionary) throws NumberFormatException {
        byte[] groupedAvp;
        byte[] local;
        Node childNode;
        NodeList nodeList;
        byte[] temp = new byte[0];
        if (nodes.hasChildNodes()) {
            nodeList = nodes.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                childNode = nodeList.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    AVPEntity entry = determineAvp(childNode, dictionary);
                    if (AVPType.GROUPED.equals(entry.getType()) || entry.getType() == null) {
                        if (childNode.hasChildNodes()) {
                            groupedAvp = getContent(childNode, dictionary);
                            if (groupedAvp == null) {
                                throw new UnsupportedOperationException(
                                        "Invalid command " + "'" + childNode.getNodeName() + "'");
                            }
                            temp = concatenate(temp,
                                    concatenate(getGroupAvp(entry, entry.getVendorId(), groupedAvp.length),
                                            groupedAvp));
                        } else {
                            byte[] b = new byte[0]; // No value
                            local = getAvp(entry, entry.getMandatory(), entry.getProtect(), entry.getVendorId(), b);
                            temp = concatenate(temp, local);
                        }
                    } else {
                        if (childNode.getFirstChild() == null && entry.getMandatory() == AVPRule.MUST) {
                            throw new IllegalStateException(String.format(
                                    "AVP node '%s' with type '%s' is configured wrong: it should contain body.",
                                    childNode.getNodeName(), entry.getType().toString()));
                        }
                        try {
                            local = getAvp(entry, entry.getMandatory(), entry.getProtect(), entry.getVendorId(),
                                    encode(childNode, entry));
                        } catch (Exception ex) {
                            throw new RuntimeException(String.format(
                                    "Error while encoding of AVP id='%s' for vendor '%s' and name '%s'",
                                    entry.getId(), entry.getVendorId(), entry.getName()), ex);
                        }
                        temp = concatenate(temp, local);
                    }
                }
            }
        }
        return temp;
    }

    private AVPEntity determineAvp(Node thisNode, AVPDictionary dictionary) {
        AVPEntity entry;
        NamedNodeMap attributes = thisNode.getAttributes();
        if (attributes == null || attributes.getLength() == 0) {
            // no attributes ==> search by name
            entry = dictionary.getByName(thisNode.getNodeName());
        } else {
            Node codeAttribute = attributes.getNamedItem("code");
            if (codeAttribute == null) {
                // no 'code' attribute ==> search by name
                entry = dictionary.getByName(thisNode.getNodeName());
            } else {
                // Decide, search by code or by (vendor, code)
                Node vendorAttribute = attributes.getNamedItem("vendor");
                if (vendorAttribute == null) {
                    entry = dictionary.getByIdGlobal(Integer.valueOf(codeAttribute.getNodeValue()));
                } else {
                    int vendorId = Integer.parseInt(vendorAttribute.getNodeValue());
                    if (vendorId == 0) {
                        entry = dictionary.getById(Integer.valueOf(codeAttribute.getNodeValue()));
                    } else {
                        entry = dictionary.getVendor(vendorId).getById(Integer.valueOf(codeAttribute.getNodeValue()));
                    }
                }
            }
        }
        if (entry == null) {
            throw new IllegalArgumentException(String.format(
                    "Unknown AVP command '%s' - AVP is not defined in the dictionary (Keep in mind, parser is case "
                            + "sensitive).", thisNode.getNodeName()));
        }
        return entry;
    }

    private byte[] concatenate(byte[] temp, byte[] local) {
        return ArrayUtils.addAll(temp, local);
    }

    private Node getNodeFromString(String xmlMessage) {
        xmlMessage = xmlMessage.replace(Character.MIN_VALUE, ' ');

        /*
            According docs, neither DocumentBuilderFactory nor DocumentBuilder are thread-safe.
            So, we create new instances here.
         */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlMessage.getBytes()));
            return doc.getFirstChild();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(String.format("Encoding is failed for message: %s", xmlMessage), e);
        }
    }

    private byte[] getAvp(AVPEntity entity, AVPRule mandatory, AVPRule protect, int vendorId, byte[] value) {
        byte[] b = null;
        byte[] avpCode;
        byte[] flagsLength;
        byte[] vendor;
        int padLength;
        int currentIndex = 0;
        byte flags = 0x00;
        int bufferSize;
        try {
            // Will copy initial session id if the current session id is a subset of the initial.
            value = checkForCarryOverSessionId(entity.getName(), value);
            flags = buildFlag(mandatory, protect, flags);
            if (vendorId == 0) {
                bufferSize = 8;
            } else {
                bufferSize = 12;
                flags += (byte) 0x80;
            }
            /*The content must be divisible by 4
            (it's necessary to check every 4 bytes to find AVP Code, after that to search message of this AVP)
            for example, if message is [1,2,3] then we need to append to message some empty byte, to get divisible by
             4 message
            |0|0|1|1| - AVP Code
            |49|50|51|0| - AVP message - [1,2,3] in ASCII it is [49,50,51] but it's not divisible, therefore we added
             the 0 and got [49,50,51,0]
            */
            bufferSize += value.length;
            padLength = Utils.getPadLength(bufferSize);
            bufferSize += padLength;
            b = new byte[bufferSize];
            avpCode = intToBytes(entity.getId());
            System.arraycopy(avpCode, 0, b, currentIndex, avpCode.length);
            currentIndex += avpCode.length;
            flagsLength = intToBytes(bufferSize - padLength);
            flagsLength[0] = flags;
            System.arraycopy(flagsLength, 0, b, currentIndex, flagsLength.length);
            currentIndex += flagsLength.length;
            if (vendorId != 0) {
                vendor = intToBytes(vendorId);
                System.arraycopy(vendor, 0, b, currentIndex, vendor.length);
                currentIndex += vendor.length;
            }
            System.arraycopy(value, 0, b, currentIndex, value.length);
        } catch (Exception ex) {
            LOGGER.error("Error getAVP id = {}; mandatory = {}; protect = {}; vendorId = {}",
                    entity.getId(), mandatory, protect, vendorId, ex);
        }
        return b;
    }

    private byte buildFlag(AVPRule mandatory, AVPRule protect, byte flags) {
        if (AVPRule.MUSTNOT.equals(mandatory)) {
            flags = AVPRule.MUSTNOT.flag();
            return flags;
        }
        if (AVPRule.MUST.equals(mandatory)) {
            flags += AVPRule.MUST.flag();
        }
        if (AVPRule.MUST.equals(protect)) { //TODO it's not friendly that we check must and set may.
            flags += AVPRule.MAY.flag();
        }
        return flags;
    }

    /*
     * This method will check is it "Session-Id" AVP or not,
     * in case it is, then set current session as previous
     * @param avpId - AVP avpId
     * @param value - session avpId, it
     * @return if @value is "Session-Id" then you'll get "Session-Id".getByes() else the @value
     */
    private byte[] checkForCarryOverSessionId(String name, byte[] value) {
        if ("Session-Id".equals(name)) {
            String currentSessionId = new String(value);
            //bytesCurrentSessionId = value; // Used for RAR
            if (!previousSessionId.startsWith(currentSessionId)) {
                previousSessionId = currentSessionId;
            } else {
                value = previousSessionId.getBytes();
            }
        }
        return value;
    }

    private byte[] getGroupAvp(AVPEntity avp, int vendor, int groupLength) {
        byte[] b;
        int groupAvpLength = 8;
        byte flags = 0x00;
        flags = buildFlag(avp.getMandatory(), avp.getProtect(), flags);
        if (vendor != 0) {
            groupAvpLength = 12;
            flags += (byte) 0x80;
        }
        b = new byte[groupAvpLength];
        System.arraycopy(intToBytes(avp.getId()), 0, b, 0, 4);
        byte[] flagLength = intToBytes(groupAvpLength + groupLength);
        flagLength[0] = flags;
        System.arraycopy(flagLength, 0, b, 4, 4);
        if (vendor != 0) {
            System.arraycopy(intToBytes(vendor), 0, b, 8, 4);
        }
        return b;
    }

    private boolean isHbHAndE2eHeadersExist(Map<String, Object> headers) {
        return isHeaderExist(headers, HBH) && isHeaderExist(headers, E2E);
    }

    private boolean isHeaderExist(Map<String, Object> headers, String headerName) {
        return headers.containsKey(headerName);
    }
}
