package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonToCsvConverter {

    public static void writeAllDataToCSV() {
        String directoryPath = "src/main/resources/data";
        String csvFilePath = "src/main/resources/data/summary.csv";

        try {
            // Инициализация структуры данных
            Map<String, List<String>> dataMap = new LinkedHashMap<>();
            List<String> dates = new ArrayList<>();

            // Получение списка JSON файлов из директории
            Files.newDirectoryStream(Paths.get(directoryPath), path -> path.toString().endsWith(".json"))
                    .forEach(jsonFilePath -> {
                        try {
                            // Извлечение даты из имени файла
                            String fileName = new File(jsonFilePath.toString()).getName();
                            String dateStr = fileName.substring(0, fileName.indexOf('.'));
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

                            // Пропускаем файл, если он не соответствует формату
                            Date date;
                            try {
                                date = sdf.parse(dateStr);
                            } catch (ParseException e) {
                                System.out.println("Файл пропущен: " + fileName + " (не соответствует формату даты)");
                                return; // пропускаем этот файл
                            }

                            // Сохранение даты в формате yyyy-MM-dd
                            SimpleDateFormat outputSdf = new SimpleDateFormat("yyyy-MM-dd");
                            String formattedDate = outputSdf.format(date);
                            dates.add(formattedDate);

                            // Чтение и парсинг каждого JSON файла
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode rootNode = mapper.readTree(new File(jsonFilePath.toString()));

                            // Обработка каждого блока команды
                            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
                            while (fields.hasNext()) {
                                Map.Entry<String, JsonNode> entry = fields.next();
                                String command = entry.getKey();
                                JsonNode commandNode = entry.getValue();

                                if (command.equals("summary empty")) {
                                    // Если это суммарные счетчики
                                    updateDataMap(dataMap, "summary.empty", rootNode.get("summary empty").asInt(), formattedDate);
                                } else if (command.equals("summary warning")) {
                                    updateDataMap(dataMap, "summary.warning", rootNode.get("summary warning").asInt(), formattedDate);
                                } else {
                                    // Извлечение значений empty и warning для каждой команды
                                    int emptyCount = commandNode.has("empty") ? commandNode.get("empty").asInt() : 0;
                                    int warningCount = commandNode.has("warning") ? commandNode.get("warning").asInt() : 0;

                                    // Заполнение данных в map
                                    updateDataMap(dataMap, command + ".empty", emptyCount, formattedDate);
                                    updateDataMap(dataMap, command + ".warning", warningCount, formattedDate);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

            // Запись в CSV
            writeDataToCSV(csvFilePath, dataMap, dates);

            System.out.println("Данные успешно записаны в CSV файл.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateDataMap(Map<String, List<String>> dataMap, String key, int value, String date) {
        dataMap.computeIfAbsent(key, k -> new ArrayList<>()).add(String.valueOf(value));
    }

    private static void writeDataToCSV(String csvFilePath, Map<String, List<String>> dataMap, List<String> dates) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {
            // Запись заголовков
            List<String> headers = new ArrayList<>();
            headers.add("Command");
            headers.addAll(dates);
            writer.writeNext(headers.toArray(new String[0]));

            // Запись данных
            for (Map.Entry<String, List<String>> entry : dataMap.entrySet()) {
                List<String> row = new ArrayList<>();
                row.add(entry.getKey());
                row.addAll(entry.getValue());
                writer.writeNext(row.toArray(new String[0]));
            }
        }
    }
}
