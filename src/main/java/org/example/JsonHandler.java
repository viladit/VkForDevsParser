package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonHandler {

    public static ObjectNode getMethodData(String methodName, String token) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            String response = RequestsHandler.getMethodInfo(methodName, token);
            JsonNode originalNode = mapper.readTree(response);
            ObjectNode outputNode = mapper.createObjectNode();

            if (response.contains("\"error_code\":104")) {
                System.out.println("метода нет!!!");
                outputNode.put("error_code", 104);
                return outputNode;
            }
            if (response.contains("\"error_code\":5")) {
                System.out.println("Token has expired!");
                return null;
            }

            // Извлечение узла "contents"
            JsonNode inputContentsNode = originalNode.path("response")
                    .path("page")
                    .path("contents");

            // Копируем нужные поля в новый обьект
            outputNode.put("title", inputContentsNode.path("title").asText());
            outputNode.put("description", inputContentsNode.path("description").asText());
            outputNode.put("result_description", inputContentsNode.path("result_description").asText());

            int errorsCount = inputContentsNode.path("errors").size();
            if (errorsCount == 0) {
                outputNode.put("errors", "0 BAD");
            } else if (errorsCount <= 2) {
                outputNode.put("errors", "1-2 NORMAL");
            } else if (errorsCount > 2) {
                outputNode.put("errors", "2+ GOOD");
            }

            int paramsCount = inputContentsNode.path("params").size();
            if (paramsCount == 0) {
                outputNode.put("params", "0 BAD");
            } else if (paramsCount <= 2) {
                outputNode.put("params", "1-2 NORMAL");
            } else if (paramsCount > 2) {
                outputNode.put("params", "2+ GOOD");
            }

            return outputNode;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveJson(JsonNode jsonNode) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = LocalDateTime.now().format(formatter);
        String filename = formattedDateTime + ".json";

        File jsonFile = new File("data/"+filename);
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, jsonNode);

        System.out.println("JSON записан в файл: data/" + filename);

    }
}
