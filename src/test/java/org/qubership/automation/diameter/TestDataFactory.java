package org.qubership.automation.diameter;

import java.nio.ByteBuffer;

public class TestDataFactory {
    /**
     * Create valid full CCR message
     */
    public static byte[] createValidFullMessage() {
        // Minimum correct Diameter message (CCR)
        // Version: 1, Length: 20 bytes, Flags: 0x40, Command: 272 (CCR)
        return new byte[] {
                1, 0, 0, 20,    // Version=1, Length=20
                0x40, 0, 1, 16, // Flags=0x40 (Request), Command=272 (0x0110)
                0, 0, 0, 4,     // Application-ID=4
                0, 0, 0, 1,     // Hop-by-Hop
                0, 0, 0, 1      // End-to-End
        };
    }

    /**
     * Create message with oversized length in the header
     */
    public static byte[] createMessageWithOversizedLength(int fakeLength) {
        byte[] message = createValidFullMessage();

        // Overwrite Length field (bytes 1-3)
        ByteBuffer.wrap(message, 1, 3).putInt(fakeLength);
        return message;
    }

    /**
     * Create packet with invalid version
     */
    public static byte[] createInvalidVersionPacket() {
        byte[] packet = new byte[20];
        packet[0] = 0; // Invalid version
        packet[1] = 0;
        packet[2] = 0;
        packet[3] = 20;
        return packet;
    }

    /**
     * Create incomplete message with length field in the header > length of the message
     */
    public static byte[] createIncompleteMessageWithLargeLength() {
        byte[] message = new byte[20];
        message[0] = 1;
        message[1] = (byte)0x10; // Length = 4096 (instead of 20)
        message[2] = 0x00;
        message[3] = 0x00;
        // All remaining can be filled with any minimal data
        return message;
    }

    /**
     * Create message with oversized header length
     */
    public static byte[] createMessageWithOversizedHeaderLength(int headerLength) {
        byte[] message = new byte[20];
        message[0] = 1;
        message[1] = (byte)((headerLength >> 16) & 0xFF);
        message[2] = (byte)((headerLength >> 8) & 0xFF);
        message[3] = (byte)(headerLength & 0xFF);
        // All remaining can be filled with any minimal data
        return message;
    }

    /**
     * Create message with oversized AVP length
     */
    public static byte[] createMessageWithOversizedAvpLength(int avpLength) {
        // Minimal message with one AVP
        byte[] message = new byte[100]; // Small message

        // Minimal valid CER
        message[0] = 1;  // Version
        message[1] = 0;
        message[2] = 0;
        message[3] = 60; // Message length ~ 60 bytes

        message[4] = 0x40; // Request flag
        message[5] = 0x00;
        message[6] = 0x01;
        message[7] = 0x01; // CER

        message[8] = 0x00;
        message[9] = 0x00;
        message[10] = 0x00;
        message[11] = 0x00; // Application-ID=0

        // Add AVP with oversized length
        int offset = 20;
        // AVP Code = 264 (Origin-Host)
        message[offset] = 0x00;
        message[offset + 1] = 0x00;
        message[offset + 2] = 0x01;
        message[offset + 3] = 0x08;
        // AVP Flags
        message[offset + 4] = 0x40;
        // AVP Length = avpLength (oversized!)
        message[offset + 5] = (byte)((avpLength >> 16) & 0xFF);
        message[offset + 6] = (byte)((avpLength >> 8) & 0xFF);
        message[offset + 7] = (byte)(avpLength & 0xFF);

        // Small body of AVP
        message[offset + 8] = 't';
        message[offset + 9] = 'e';
        message[offset + 10] = 's';
        message[offset + 11] = 't';

        return message;
    }

    public static byte[] createFirstPart() {
        // The 1st part of the message
        byte[] full = createValidFullMessage();
        int half = full.length / 2;
        return java.util.Arrays.copyOf(full, half);
    }

    public static byte[] createSecondPart() {
        // The 2nd part of the message
        byte[] full = createValidFullMessage();
        int half = full.length / 2;
        return java.util.Arrays.copyOfRange(full, half, full.length);
    }

    public static byte[] createAnotherValidMessage() {
        // CCA message
        return new byte[] {
                1, 0, 0, 20,
                0x40, 0, 1, 17,  // CCA = 273
                0, 0, 0, 4,
                0, 0, 0, 2,
                0, 0, 0, 2
        };
    }

    /**
     * Create valid CER message
     */
    public static byte[] createValidCerMessage() {
        return new byte[] {
                1, 0, 0, 20,     // Version=1, Length=20
                0x40, 0, 1, 1,   // CER
                0, 0, 0, 0,      // Application-ID=0
                0, 0, 0, 1,      // Hop-by-Hop
                0, 0, 0, 1       // End-to-End
        };
    }
}
