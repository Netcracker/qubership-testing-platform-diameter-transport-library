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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.qubership.automation.diameter.avp.AVPDictionary;
import org.qubership.automation.diameter.data.Converter;
import org.qubership.automation.diameter.data.Encoder;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.qubership.automation.diameter.dictionary.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WireSharkEncoder extends Encoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(WireSharkEncoder.class);

    public WireSharkEncoder(final DictionaryConfig dictionaryConfig) {
        this.dictionaryConfig = dictionaryConfig;
    }

    @Override
    public ByteBuffer encode(final String strMessage, final Map<String, Object> headers) throws Exception {
        WireSharkMessageParser wireSharkMessageParser = new WireSharkMessageParser();
        WireSharkMessage shMessage = wireSharkMessageParser.parse(strMessage);
        byte version = parseByte(shMessage.getVersion());
        byte messageFlag = parseByte(shMessage.getFlag());
        byte[] commandCode = parseValue(shMessage.getCommand());
        byte[] applicationId = parseValue(shMessage.getApplicationId());
        byte[] message = buildMessageHeader(version, messageFlag, commandCode, applicationId);
        byte[] content = getAvps(shMessage.getAvpRecords());
        message = ArrayUtils.addAll(message, content);
        int size = message.length;
        arraycopy(Converter.intToBytes(size), 1, message, 1, 3);
        LOGGER.info("Message size: {}", size);
        LOGGER.info(Arrays.toString(message));
        return ByteBuffer.wrap(message);
    }

    @Override
    public ByteBuffer encode(final String message) throws Exception {
        return encode(message, new HashMap<>());
    }

    private byte[] getAvps(final List<Pair<Integer, AvpRecord>> avpRecords) {
        AVPDictionary avpDictionary = DictionaryService.getInstance().getAvpDictionary(dictionaryConfig);
        AvpEncoder avpEncoder = new AvpEncoder(avpDictionary);
        return avpEncoder.encode(avpRecords);
    }

    private byte[] buildMessageHeader(final byte version,
                                      final byte messageFlag,
                                      final byte[] commandCode,
                                      final byte[] applicationId) {
        //version + length = 4
        //request flag + message code = 8
        //applicationId = 12
        //hbh + e2e = 20
        byte[] message = new byte[20];
        message[0] = version; //set message version
        message[4] = messageFlag; //Request or response
        arraycopy(commandCode, 1, message, 5, 3); //add command code
        arraycopy(applicationId, 0, message, 8, 4); //add application id
        return message;
    }

    public byte[] parseValue(final String flag) {
        return Converter.intToBytes(flag);
    }

    private byte parseByte(final String flag) {
        @SuppressWarnings("StringOperationCanBeSimplified")
        String hex = flag.substring(2, flag.length());
        return DatatypeConverter.parseHexBinary(hex)[0];
    }
}
