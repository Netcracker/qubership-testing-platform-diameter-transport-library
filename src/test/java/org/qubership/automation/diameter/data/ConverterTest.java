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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

/**
 * diameter-transport
 */
public class ConverterTest {

    @Test
    public void testDateConvert() {
        assertEquals("2017-08-07 13:06:40", Converter.bytesToLinuxDate(ByteBuffer.allocate(8).putLong(3711100000L).array()));
    }

    @Test
    public void givenDataWithFormatYYYYMMDD_whenWeConvertIt2BytesAnd2LinuxData_thenItGivesTheSameDateAndTime() {
        Converter.DEFAULT_TIMEZONE = "America/New_York";
        //Converter.DEFAULT_TIMEZONE = "Etc/GMT+0";
        assertEquals("2019-02-08 12:00:00", /*timezone*/
                Converter.bytesToLinuxDate(
                        Converter.linuxDateToBytes("2019-02-08 12:00:00")
                )
        );
    }

    @Test
    public void givenDataWithWireSharkFormatMMMDD_whenWeConvertIt2BytesAnd2LinuxData_thenItGivesTheSameDateAndTime() {
        assertEquals("2018-01-09 19:02:57", /*timezone*/
                Converter.bytesToLinuxDate(
                        Converter.linuxDateToBytes("Jan 9, 2018 19:02:57 Russia TZ 3 Standard Time")
                )
        );
    }

    @Test
    public void testBytesToLongConvertsIntValue() {
        long aLong = Converter.bytesToLong(new byte[]{0, 0, 0, 4});
        assertEquals(4L, aLong);
    }

    @Test
    public void testUnsigned32() {
        byte[] bytes = Converter.unsigned32ToBytes(2868904937L);
        assertEquals(-85, bytes[0]);
        assertEquals(-23, bytes[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOutOfRange() {
        Converter.unsigned32ToBytes(5294967295L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOutOfRangeLessZero() {
        byte[] bytes = Converter.unsigned32ToBytes("0");
        assertEquals(0, bytes[bytes.length - 1]);
        Converter.unsigned64ToBytes("-1");
    }

    @Test
    public void testUnsigned64() {
        byte[] bytes = Converter.unsigned64ToBytes("2868904937");
        assertEquals(-85, bytes[4]);
        assertEquals(-23, bytes[7]);
    }

    @Test
    public void testUnsigned64SmallValue() {
        byte[] bytes = Converter.unsigned64ToBytes("10");
        assertEquals(10, bytes[7]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOutOfRange64() {
        Converter.unsigned64ToBytes("18446744073709551616");
    }

    @Test
    public void testUnsigned64Test() {
        byte[] bytes = Converter.unsigned64ToBytes("2560000");
        assertEquals(2560000, Converter.bytesToUnsigned(bytes).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOutOfRange64LessZero() {
        byte[] bytes = Converter.unsigned64ToBytes("0");
        assertEquals(0, bytes[bytes.length - 1]);
        Converter.unsigned64ToBytes("-1");
    }

    @Test
    public void testBytesToUnsigned() {
        ByteBuffer buffer = ByteBuffer.allocate(4).putInt(4);
        assertEquals(4, Converter.bytesToUnsigned(buffer.array()).intValue());
    }

    @Test
    public void testConvertIpHex() {
        byte[] bytes = Converter.addressToBytes("10.232.4.144");
        assertArrayEquals(new byte[]{0, 1, 10, -24, 4, -112}, bytes);
    }

    @Test
    public void testBytesToAddress() {
        String address = Converter.bytesToAddress(new byte[]{0, 1, 10, -24, 4, -112});
        assertEquals("10.232.4.144", address);
    }

    @Test
    public void given2number_whenWeIncodeToHex_thenItGivesArrayWith3Numbers() {
        byte[] bytes = Converter.textToHex("32");
        assertArrayEquals(new byte[]{51, 51, 51, 50}, bytes);
    }
    
    @Test
    public void given1Symbol_whenWeIncodeToHex_thenItGivesArrayWith2Numbers() {
        byte[] bytes = Converter.textToHex("F");
        assertArrayEquals(new byte[]{52, 54}, bytes);
    }
    
    
    @Test
    public void testConvertBytesHexToString() {
        String result = Converter.bytesHexToText(new byte[]{52, 54});
        assertEquals("46", result);
    }

    @Test
    public void testConvertAddressToBytes() {
        byte[] address = Converter.addressToBytes("0x00016469603E");
        assertArrayEquals(new byte[]{0, 1, 100, 105, 96, 62}, address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertAddressWithEmptyValueThrowsError() {
        Converter.addressToBytes("");
    }
}
