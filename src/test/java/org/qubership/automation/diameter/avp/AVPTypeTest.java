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

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * diameter-transport
 */
class AVPTypeTest {

    @Test
    void testConvertUTF8() {
        String stringMessage = "123";
        byte[] byteMessage = stringMessage.getBytes();
        AVPType utf8String = AVPType.fromString("UTF8String");
        assertInOut(utf8String, stringMessage, byteMessage);
    }

    @Test
    void testConvertUnsigned32() {
        int intValue = 32;
        byte[] byteValue = ByteBuffer.allocate(4).putInt(intValue).array();
        AVPType unSIGNED32 = AVPType.fromString("UnSIGNED32");
        assertInOut(unSIGNED32, intValue, byteValue);
    }

    @Test
    void testConvertIPAddress() {
        AVPType avpType = AVPType.fromString("Address");
        String stringIp = "10.217.33.28";
        /*0,1 - it's marker that it's IPv4. 0,2 - it's IPv6*/
        byte[] bytesIp = new byte[]{0, 1, 10, -39, 33, 28};
        assertInOut(avpType, stringIp, bytesIp);
    }

    @Test
    void testConvertUnsigned64() {
        AVPType avpType = AVPType.fromString("Unsigned64");
        long longValue = 10;
        byte[] byteValue = ByteBuffer.allocate(8).putLong(longValue).array();
        assertInOut(avpType, String.valueOf(longValue), byteValue);
    }

    @Test
    void testConvertSigned32() {
        AVPType avpType = AVPType.fromString("Signed32");
        int intValue = 10;
        byte[] byteValue = ByteBuffer.allocate(4).putInt(intValue).array();
        assertInOut(avpType, String.valueOf(intValue), byteValue);
    }

    @Test
    void testConvertSigned64() {
        AVPType avpType = AVPType.fromString("Signed64");
        long longValue = System.currentTimeMillis();
        byte[] byteValue = ByteBuffer.allocate(8).putLong(longValue).array();
        assertInOut(avpType, String.valueOf(longValue), byteValue);
    }

    @Test
    void testConvertFloat64() {
        AVPType avpType = AVPType.fromString("Float64");
        long longValue = System.currentTimeMillis();
        byte[] byteValue = ByteBuffer.allocate(8).putLong(longValue).array();
        assertInOut(avpType, String.valueOf(longValue), byteValue);
    }

    private void assertInOut(AVPType avpType, String stringIp, byte[] bytesIp) {
        Assertions.assertArrayEquals(bytesIp, avpType.encode(stringIp));
        Assertions.assertEquals(stringIp, avpType.decode(bytesIp));
    }
    
    @Test
    void EncodeOneNumberAsStringToOctetStringforByte() {
        AVPType avpType = AVPType.fromString("OctetString");
        avpType.encode("Bx1");
        avpType.encode("Bx6");
    }

    @Test
    void givenNumber3332AndItHex_whenWeEncodeIt_thenWeHave32() {
        AVPType avpType = AVPType.fromString("OctetString");
        Assertions.assertArrayEquals(new byte[]{51, 50}, avpType.encode("0x3332"));
    }

    @Test
    void givenNumber32AndItHex_whenWeEncodeIt_thenWeHave32() {
        AVPType avpType = AVPType.fromString("OctetString");
        Assertions.assertArrayEquals(new byte[]{3, 2}, avpType.encode("0x0302"));
    }
    
    @Test
    void EncodeOneNumberAsStringToOctetStringForNumber() {
        AVPType avpType = AVPType.fromString("OctetString");
        Assertions.assertArrayEquals(new byte[]{51, 50}, avpType.encode("32"));
    }

    @Test
    void givenHex_andOctetType_WhenWeencodeIt_thenWeGetBytesArray() {
        AVPType avpType = AVPType.fromString("OctetString");
        Assertions.assertArrayEquals(new byte[]{50}, avpType.encode("0x32"));
    }

    @Test
    void EncodeOneNumberAsStringToOctetStringForText() {
        AVPType avpType = AVPType.fromString("OctetString");
        Assertions.assertArrayEquals(new byte[]{70, 100, 103, 114, 101}, avpType.encode("Fdgre"));
    }
    
    //@Test
    //TODO: Actualize me
    void givenOctetWithNumberAndText_whenDecode_thenItGivesText() {
        AVPType avpType = AVPType.fromString("OctetString");
        Assertions.assertTrue(avpType.decode(new byte[]{70, 100, 103, 114, 101}).toString().contains("Fdgre"));
    }
    
    //@Test
    //TODO: Actualize me
    void givenOctetWithNumber_whenDecode_thenItGivesNumbers() {
        AVPType avpType = AVPType.fromString("OctetString");
        Assertions.assertTrue(avpType.decode(new byte[]{0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x2c, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64}).toString().contains("hello, world"));
    }
    
    private void assertInOut(AVPType avpType, int stringIp, byte[] bytesIp) {
        Assertions.assertArrayEquals(bytesIp, avpType.encode(stringIp));
    }
}
