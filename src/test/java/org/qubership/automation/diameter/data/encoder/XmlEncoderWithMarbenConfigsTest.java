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

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.diameter.MarbenConfigProvider;

public class XmlEncoderWithMarbenConfigsTest extends MarbenConfigProvider {

    XmlEncoder encoder;

    @BeforeEach
    public void setUp() {
        encoder = new XmlEncoder(DICTIONARY_CONFIG);
    }

    @Test
    public void testEncodeCommandCER() throws Exception {
        ByteBuffer encode = encoder.encode(XMLMessages.CER);
        Assertions.assertEquals(204, encode.array().length);
    }

    @Test
    public void testEncodeCommandCCR() throws Exception {
        ByteBuffer encode = encoder.encode(XMLMessages.CCR);
        Assertions.assertEquals(-128, encode.array()[4]);
        Assertions.assertEquals(876, encode.array().length);
    }

    @Test
    public void testEncodeCommandSNA() throws Exception {
        ByteBuffer encode = encoder.encode(XMLMessages.SNA);
        Assertions.assertEquals(0, encode.array()[4]);
    }

    @Test
    public void testEncodeCommandSLR() throws Exception {
        ByteBuffer encode = encoder.encode(XMLMessages.SLR);
        Assertions.assertEquals(-128, encode.array()[4]);
        Assertions.assertEquals(256, encode.array().length);
    }

    @Test
    public void testEncodeCommandSTR() throws Exception {
        ByteBuffer encode = encoder.encode(XMLMessages.STR);
        Assertions.assertEquals(-128, encode.array()[4]);
        Assertions.assertEquals(184, encode.array().length);
    }

    @Test
    public void testEncodeCommandDWA() throws Exception {
        ByteBuffer encode = encoder.encode(XMLMessages.DWA);
        Assertions.assertEquals(0, encode.array()[4]);
        Assertions.assertEquals(88, encode.array().length);
    }
}
