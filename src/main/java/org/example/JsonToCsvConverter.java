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
            Map<String, List<String>> dataMap = new LinkedHashMap<>();
            List<String> dates = new ArrayList<>();

            Files.newDirectoryStream(Paths.get(directoryPath), path -> path.toString().endsWith(".json"))
                    .forEach(jsonFilePath -> {
                        try {
                            // Извлечение даты из имени файла
                            String fileName = new File(jsonFilePath.toString()).getName();
                            String dateStr = fileName.substring(0, fileName.indexOf('.'));
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

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

                            // Подсчет для summary по текущей дате
                            final int[] dateTotalCount = {0};
                            final int[] dateErrorCount = {0};

                            // Чтение и парсинг каждого JSON файла
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode rootNode = mapper.readTree(new File(jsonFilePath.toString()));

                            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
                            while (fields.hasNext()) {
                                Map.Entry<String, JsonNode> entry = fields.next();
                                String command = entry.getKey();

                                // Пропускаем summary empty и summary warning
                                if (command.startsWith("summary")) {
                                    continue;
                                }

                                JsonNode commandNode = entry.getValue();

                                // Подсчет общего количества методов и методов с ошибками или предупреждениями
                                int totalMethods = 0;
                                int methodsWithErrors = 0;

                                Iterator<Map.Entry<String, JsonNode>> methods = commandNode.fields();
                                while (methods.hasNext()) {
                                    Map.Entry<String, JsonNode> methodEntry = methods.next();
                                    JsonNode methodNode = methodEntry.getValue();

                                    if (methodNode.isObject()) {
                                        totalMethods++;

                                        boolean hasErrorOrWarning = checkForErrorsOrWarnings(methodNode);
                                        if (hasErrorOrWarning) {
                                            methodsWithErrors++;
                                        }
                                    }
                                }

                                // Суммируем для summary по дате
                                dateTotalCount[0] += totalMethods;
                                dateErrorCount[0] += methodsWithErrors;

                                // Запись в dataMap: всего методов и методов с ошибками
                                updateDataMap(dataMap, command + ".total", totalMethods, formattedDate);
                                updateDataMap(dataMap, command + ".errors", methodsWithErrors, formattedDate);
                            }

                            // Запись в dataMap для summary по текущей дате
                            updateDataMap(dataMap, "summary.total", dateTotalCount[0], formattedDate);
                            updateDataMap(dataMap, "summary.errors", dateErrorCount[0], formattedDate);

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

    private static boolean checkForErrorsOrWarnings(JsonNode methodNode) {
        // Проверяем наличие ошибок или предупреждений в ключевых полях
        String[] keysToCheck = {"errors", "params", "description", "result_description"};
        for (String key : keysToCheck) {
            if (methodNode.has(key)) {
                JsonNode statusNode = methodNode.get(key).get("status");
                if (statusNode != null) {
                    String status = statusNode.asText();
                    if ("EMPTY".equals(status) || "WARNING".equals(status)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
