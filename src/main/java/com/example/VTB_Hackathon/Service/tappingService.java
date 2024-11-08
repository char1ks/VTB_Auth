package com.example.VTB_Hackathon.Service;

import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class tappingService {
    public String analyzeSound(byte[] audioData) {
        StringBuilder morseCode = new StringBuilder();
        try {
            // Проверяем, что данные не пустые
            if (audioData == null || audioData.length == 0) {
                System.err.println("Received empty audio data.");
                return morseCode.toString();
            }

            // Получаем аудиопоток
            AudioInputStream audioInputStream = new AudioInputStream(
                    new ByteArrayInputStream(audioData),
                    new AudioFormat(16000, 16, 1, true, false), // Изменен на 16 бит и 1 канал
                    audioData.length / 2 // Длина аудиопотока
            );

            AudioFormat format = audioInputStream.getFormat();
            System.out.println("Audio format: " + format);
            byte[] audioBytes = new byte[audioData.length];
            audioInputStream.read(audioBytes);

            int sampleSizeInBytes = 2; // 16 бит = 2 байта
            int samplesPerSecond = (int) format.getFrameRate();
            int segmentDurationInMillis = 500; // 0.5 секунды
            int samplesPerSegment = (samplesPerSecond * segmentDurationInMillis) / 1000;

            // Вычисляем общее среднее значение громкости
            double totalVolume = 0;
            int totalSamples = audioBytes.length / sampleSizeInBytes;

            for (int i = 0; i < totalSamples; i++) {
                int sampleIndex = i * sampleSizeInBytes;
                if (sampleIndex + 1 < audioBytes.length) {
                    int sample = (audioBytes[sampleIndex + 1] << 8) | (audioBytes[sampleIndex] & 0xFF);
                    totalVolume += Math.abs(sample);
                }
            }

            double overallAverageVolume = totalVolume / totalSamples;

            // Обрабатываем сегменты
            boolean lastSound = false; // Флаг для отслеживания наличия звука
            int pauseCount = 0; // Счетчик пауз

            for (int i = 0; i < audioBytes.length; i += samplesPerSegment * sampleSizeInBytes) {
                double segmentVolume = 0;
                int segmentSamples = Math.min(samplesPerSegment, (totalSamples - (i / sampleSizeInBytes)));

                for (int j = 0; j < segmentSamples; j++) {
                    int sampleIndex = i + j * sampleSizeInBytes;
                    if (sampleIndex + 1 < audioBytes.length) {
                        int sample = (audioBytes[sampleIndex + 1] << 8) | (audioBytes[sampleIndex] & 0xFF);
                        segmentVolume += Math.abs(sample);
                    }
                }

                double averageSegmentVolume = segmentVolume / segmentSamples;

                // Если среднее значение сегмента больше общего среднего, добавляем точку
                if (averageSegmentVolume > overallAverageVolume) {
                    if (pauseCount > 0) {
                        for (int k = 0; k < pauseCount; k++) {
                            morseCode.append("_"); // Добавляем паузы
                        }
                        pauseCount = 0; // Сбрасываем счетчик пауз
                    }
                    morseCode.append("."); // Добавляем точку для стука
                    lastSound = true;
                } else {
                    if (lastSound) {
                        pauseCount++; // Увеличиваем счетчик пауз
                    }
                }
            }

            // В конце добавляем "_" для оставшейся паузы
            if (pauseCount > 0) {
                for (int k = 0; k < pauseCount; k++) {
                    morseCode.append("_"); // Добавляем оставшиеся паузы
                }
            }
        } catch (IOException e) {
            System.err.println("IO exception while processing audio: " + e.getMessage());
        }
        return morseCode.toString().trim();
    }
}
