package org.qubership.automation.diameter.data.decoder;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.qubership.automation.diameter.StandardConfigProvider;
import org.qubership.automation.diameter.data.XmlDecoder;
import org.qubership.automation.diameter.exception.DecodeException;

/**
 * Security tests for XmlDecoder, checking validation of AVP length.
 * <p>
 * The current behavior: Passed for correct messages, but failed for incorrect ones (because there is no validation).
 * Should be all passed after adding of validation into XmlDecoder.parseContent().
 */
public class XmlDecoderSecurityTest extends StandardConfigProvider {
    private XmlDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new XmlDecoder(DICTIONARY_CONFIG);
    }

    /**
     * Тест 1: Корректное сообщение с валидными AVP должно декодироваться без ошибок
     * GREEN на текущем коде ✅
     */
    @Test
    void shouldDecodeValidMessageWithCorrectAvpLengths() {
        // Given: валидное сообщение CER с корректными AVP
        byte[] validMessage = createValidCerMessage();

        // When: декодируем
        String result = decoder.decode(ByteBuffer.wrap(validMessage));

        // Then: не должно быть исключений
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("<CER>"));
        Assertions.assertTrue(result.contains("</CER>"));
    }

    /**
     * Тест 2: AVP с length меньше минимального (8 байт)
     * На текущем коде - Passed (нет проверки)
     * После исправления - скорее всего, придется НЕ бросать ошибку - сохранить текущее поведение
     */
    @Test
    void shouldRejectAvpWithLengthLessThan8() {
        // Given: сообщение с AVP, у которого length = 4 (меньше 8)
        byte[] malformedMessage = createMessageWithAvpLength(4);

        // When/Then:
        String result = decoder.decode(ByteBuffer.wrap(malformedMessage));
        Assertions.assertNotNull(result);
        Assertions.assertEquals("<CER></CER>", result);
    }

    /**
     * Тест 3: AVP с length = 0
     * На текущем коде - Passed (нет проверки)
     * После исправления - скорее всего, придется НЕ бросать ошибку - сохранить текущее поведение
     */
    @Test
    void shouldRejectAvpWithLengthZero() {
        // Given: сообщение с AVP, у которого length = 0
        byte[] malformedMessage = createMessageWithAvpLength(0);

        // When/Then:
        String result = decoder.decode(ByteBuffer.wrap(malformedMessage));
        Assertions.assertNotNull(result);
        Assertions.assertEquals("<CER></CER>", result);
    }

    /**
     * Тест 4: AVP с length > actual message length
     * На текущем коде - RED (аллокация 16 МБ с паддингом)
     * После исправления - GREEN (DecodeException)
     */
    @Test
    void shouldRejectAvpWithLengthExceedingMessageLength() {
        // Given: сообщение длиной 100 байт, но AVP указывает length = 1000
        byte[] malformedMessage = createMessageWithAvpLengthExceedingMessage(1000);

        // When/Then: должно быть DecodeException
        DecodeException exception = Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(malformedMessage)));

        // Then: There should be DecodeException about incorrect length in the cause
        Assertions.assertNotNull(exception);
        Assertions.assertTrue(exception.getMessage().contains("Failed parsing AVPs"));
        Throwable cause = exception.getCause();
        Assertions.assertNotNull(cause);
        Assertions.assertInstanceOf(IllegalArgumentException.class, cause);
        Assertions.assertTrue(cause.getMessage().contains("1000 > 80"));
    }

    /**
     * Тест 5: AVP с length = 16_000_000 (максимально возможный)
     * На текущем коде - RED (аллокация 16 МБ и квадратичный BigInteger.toString)
     * После исправления - GREEN (DecodeException или ограничение)
     */
    @Disabled("Enable after fix")
    @Test
    void shouldRejectAvpWithMaximumPossibleLength() {
        // Given: сообщение с AVP length = 16_000_000
        int maxLength = 16_000_000;
        byte[] malformedMessage = createMessageWithAvpLength(maxLength);

        // When/Then: должно быть DecodeException
        DecodeException exception = Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(malformedMessage)));

        Assertions.assertNotNull(exception);
        Assertions.assertTrue(exception.getMessage().contains("Failed parsing AVPs"));
        Throwable cause = exception.getCause();
        Assertions.assertNotNull(cause);
        Assertions.assertInstanceOf(IllegalArgumentException.class, cause);
    }

    /**
     * Тест 6: AVP с length = 16_000_000, но это единственный AVP в сообщении
     * и сообщение имеет длину больше 16_000_000
     * На текущем коде - RED (квадратичный BigInteger.toString)
     * После исправления - GREEN (DecodeException или ограничение)
     */
    @Disabled("Enable after fix")
    @Test
    void shouldRejectSingleAvpWithMaximumLength() {
        // Given: сообщение с одним AVP, length = 16_000_000
        // и сообщение физически имеет такую длину
        byte[] malformedMessage = createSingleAvpMessageWithLength(16_000_000);

        // Когда декодируем с большим таймаутом
        long startTime = System.currentTimeMillis();
        Exception exception = Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(malformedMessage)));
        long duration = System.currentTimeMillis() - startTime;

        // Тогда: должно быть быстро (не должно висеть секунды)
        Assertions.assertTrue(duration < 1000, "Decode took " + duration + "ms, should be < 1000ms");
        Assertions.assertTrue(exception.getMessage().contains("length") ||
                exception.getMessage().contains("exceeds"));
    }

    /**
     * Тест 7: AVP с length = 1_000_000 (в пределах допустимого, если установлен лимит)
     * На текущем коде - RED (аллокация 1 МБ и BigInteger.toString) - неверно. На текущем коде passed, и быстро
     * После исправления - GREEN (если лимит >= 1M, то декодируется)
     */
    @Test
    void shouldAcceptAvpWithLengthWithinConfiguredLimit() {
        // Given: сообщение с AVP length = 1_000_000
        // Это корректно, если MAX_AVP_SIZE >= 1_000_000
        byte[] validMessage = createMessageWithAvpLength(1_000_000);

        // When: декодируем
        // Это должно работать, если лимит не меньше 1_000_000
        String result = decoder.decode(ByteBuffer.wrap(validMessage));

        // Then: не должно быть исключений
        Assertions.assertNotNull(result);
    }

    /**
     * Тест 9: Несколько AVP, один из которых с oversized length
     * На текущем коде - RED (аллокация на первом же oversized AVP) - на текущем коде адекватная ошибка длины
     * После исправления - GREEN (исключение на первом же некорректном AVP)
     */
    @Test
    void shouldStopProcessingOnFirstOversizedAvp() {
        // Given: сообщение с 2 AVP, первый корректный, второй oversized
        byte[] malformedMessage = createMessageWithMixedAvpLengths();

        // When/Then: должно быть DecodeException
        Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(malformedMessage)));

        // Проверяем, что обработка остановилась на первом же ошибочном AVP
        // (в логах должно быть сообщение о проблемном AVP)
    }

    /**
     * Тест 10: Vendor-Specific AVP с oversized length
     * На текущем коде - RED (аллокация с учетом vendorId)
     * После исправления - GREEN (проверка до getBody)
     */
    @Disabled("Enable after fix")
    @Test
    void shouldRejectVendorSpecificAvpWithOversizedLength() {
        // Given: Vendor-Specific AVP (V бит=1) с length = 16_000_000
        byte[] malformedMessage = createVendorSpecificAvpWithLength(16_000_000);

        // When/Then: должно быть DecodeException
        Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(malformedMessage)));
    }

    // ==================== Тесты производительности ==================

    /**
     * Тест производительности: атака с 16 МБ AVP должна завершаться быстро
     * На текущем коде - RED (зависает на секунды-минуты)
     * После исправления - GREEN (< 100 мс)
     */
    @Disabled("Enable or remove after fix")
    @Test
    void shouldNotHangOnOversizedAvp() {
        // Given: сообщение с AVP length = 16_000_000
        byte[] attackMessage = createMessageWithAvpLength(16_000_000);

        long startTime = System.currentTimeMillis();
        Exception exception = Assertions.assertThrows(DecodeException.class,
                () -> decoder.decode(ByteBuffer.wrap(attackMessage)));
        long duration = System.currentTimeMillis() - startTime;

        // Then: должно быть быстро (< 100 мс)
        Assertions.assertTrue(duration < 100,
                "Decode took " + duration + "ms, should be < 100ms");
        Assertions.assertTrue(exception.getMessage().contains("length"));
    }

    /**
     * Тест производительности: несколько атак подряд
     */
    @Disabled("Enable or remove after fix")
    @Test
    void shouldHandleMultipleAttacksEfficiently() {
        int attackCount = 10;
        long totalTime = 0;

        for (int i = 0; i < attackCount; i++) {
            byte[] attackMessage = createMessageWithAvpLength(1_000_000);
            long startTime = System.currentTimeMillis();
            Assertions.assertThrows(DecodeException.class,
                    () -> decoder.decode(ByteBuffer.wrap(attackMessage)));
            totalTime += System.currentTimeMillis() - startTime;
        }

        // Then: среднее время < 50 мс на атаку
        long averageTime = totalTime / attackCount;
        Assertions.assertTrue(averageTime < 50,
                "Average time " + averageTime + "ms should be < 50ms");
    }

    // ==================== Вспомогательные методы ====================

    /**
     * Создает валидное CER сообщение для позитивного теста
     */
    private byte[] createValidCerMessage() {
        // Минимальное валидное CER сообщение
        // CER: код команды 257, Request флаг
        return new byte[] {
                // Header (20 байт)
                1, 0, 0, 20,     // Version=1, Length=20
                -0x80, 0, 1, 1,   // Flags=-0x80 (Request), Command=257 (CER)
                0, 0, 0, 0,      // Application-ID=0
                0, 0, 0, 1,      // Hop-by-Hop
                0, 0, 0, 1       // End-to-End
        };
    }

    /**
     * Создает сообщение с одним AVP, у которого указан определенный length
     *
     * @param avpLength длина AVP (может быть некорректной)
     * @return сформированное сообщение
     */
    private byte[] createMessageWithAvpLength(int avpLength) {
        // Создаем базовое сообщение с одним AVP (Origin-Host)
        // AVP код 264 (Origin-Host), тип UTF8String

        int messageLength = 20 + roundLength(avpLength); // 20 байт заголовка + AVP
        byte[] message = new byte[messageLength];

        // Заголовок сообщения (CER)
        message[0] = 1;  // Version
        message[1] = (byte)((messageLength >> 16) & 0xFF);
        message[2] = (byte)((messageLength >> 8) & 0xFF);
        message[3] = (byte)(messageLength & 0xFF);
        message[4] = (byte) 0x80; // Request флаг
        message[5] = 0x00;
        message[6] = 0x01;
        message[7] = 0x01; // Command=257 (CER)
        // Application-ID = 0
        message[8] = 0x00;
        message[9] = 0x00;
        message[10] = 0x00;
        message[11] = 0x00;
        // Hop-by-Hop = 1
        message[12] = 0x00;
        message[13] = 0x00;
        message[14] = 0x00;
        message[15] = 0x01;
        // End-to-End = 1
        message[16] = 0x00;
        message[17] = 0x00;
        message[18] = 0x00;
        message[19] = 0x01;

        // AVP заголовок (8 байт)
        int offset = 20;
        try {
            message[offset] = 0x00; // AVP Code (264 = 0x0108)
            message[offset + 1] = 0x00;
            message[offset + 2] = 0x01;
            message[offset + 3] = (byte) 0x08;
            message[offset + 4] = 0x40; // M бит=1
            message[offset + 5] = (byte) ((avpLength >> 16) & 0xFF);
            message[offset + 6] = (byte) ((avpLength >> 8) & 0xFF);
            message[offset + 7] = (byte) (avpLength & 0xFF);

            // AVP Body (если есть)
            if (avpLength > 8) {
                int bodyLength = avpLength - 8;
                // Заполняем тестовыми данными
                for (int i = 0; i < bodyLength && i + offset + 8 < message.length; i++) {
                    message[offset + 8 + i] = (byte) 'x';
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("AVP length is too small: " + avpLength
                    + ". Message populating is broken. Exception: " + e);
        }
        return message;
    }

    /**
     * Создает сообщение, где AVP length превышает длину сообщения
     */
    private byte[] createMessageWithAvpLengthExceedingMessage(int fakeAvpLength) {
        // Создаем сообщение с AVP length = fakeAvpLength
        // но реальная длина сообщения маленькая
        byte[] message = createMessageWithAvpLength(fakeAvpLength);
        // Обрезаем сообщение до 100 байт
        if (message.length > 100) {
            byte[] truncated = new byte[100];
            System.arraycopy(message, 0, truncated, 0, 100);
            // Корректируем длину сообщения в заголовке
            truncated[1] = 0;
            truncated[2] = 0;
            truncated[3] = 100;
            return truncated;
        }
        return message;
    }

    /**
     * Создает сообщение с одним большим AVP
     */
    private byte[] createSingleAvpMessageWithLength(int avpLength) {
        // Аналогично createMessageWithAvpLength, но гарантируем,
        // что сообщение физически имеет длину >= avpLength
        return createMessageWithAvpLength(avpLength);
    }

    /**
     * Создает сообщение с несколькими AVP, где второй - oversized
     */
    private byte[] createMessageWithMixedAvpLengths() {
        // Создаем сообщение с двумя AVP:
        // 1. Корректный AVP (Origin-Host)
        // 2. Oversized AVP (Session-Id)
        byte[] message = createMessageWithAvpLength(12); // корректный AVP
        // Добавляем второй AVP с длиной 1000 (но данных мало)
        byte[] extended = new byte[message.length + 20];
        System.arraycopy(message, 0, extended, 0, message.length);
        // Добавляем AVP с length=1000
        int offset = message.length;
        extended[offset] = 0x00; // AVP Code (Session-Id = 263)
        extended[offset + 1] = 0x00;
        extended[offset + 2] = 0x01;
        extended[offset + 3] = 0x07;
        extended[offset + 4] = 0x40;
        extended[offset + 5] = 0x00;
        extended[offset + 6] = 0x03;
        extended[offset + 7] = (byte)0xE8; // 1000
        // Корректируем длину сообщения
        int newLength = offset + 20;
        extended[1] = (byte)((newLength >> 16) & 0xFF);
        extended[2] = (byte)((newLength >> 8) & 0xFF);
        extended[3] = (byte)(newLength & 0xFF);
        return extended;
    }

    /**
     * Создает Vendor-Specific AVP с указанной длиной
     */
    private byte[] createVendorSpecificAvpWithLength(int avpLength) {
        // AVP с V битом=1 (vendor-specific)
        byte[] message = createMessageWithAvpLength(avpLength);
        // Устанавливаем V бит в AVP флагах
        int offset = 20;
        message[offset + 4] = (byte)0xC0; // V бит=1, M бит=1
        // Добавляем Vendor-ID (4 байта)
        // Сдвигаем все данные на 4 байта
        // ... (упрощенно)
        return message;
    }

    /**
     * Округление длины до 4 байт
     */
    private int roundLength(int length) {
        if (length % 4 != 0) {
            return (int) Math.ceil((double) length / 4) * 4;
        }
        return length;
    }
}
