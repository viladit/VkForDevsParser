package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
            // description
            ObjectNode descriptionNode = outputNode.putObject("description");
            descriptionNode.put("value", inputContentsNode.path("description").asText());
            if (inputContentsNode.path("description").asText() == "") {
                descriptionNode.put("status", "EMPTY");
            } else {
                descriptionNode.put("status", "OK");
            }

            // result_description
            ObjectNode resultDescriptionNode = outputNode.putObject("result_description");
            resultDescriptionNode.put("value", inputContentsNode.path("result_description").asText());
            if (inputContentsNode.path("result_description").asText() == "") {
                resultDescriptionNode.put("status", "EMPTY");
            } else {
                resultDescriptionNode.put("status", "OK");
            }

            // errors
            ObjectNode errorsNode = outputNode.putObject("errors");
            if (inputContentsNode.path("errors").size() == 0) {
                errorsNode.put("status", "EMPTY");
            } else if (inputContentsNode.path("errors").size() <= 1) {
                errorsNode.put("status", "WARNING");
            } else if (inputContentsNode.path("errors").size() > 1) {
                errorsNode.put("status", "OK");
            }

            // params
            ObjectNode paramsNode = outputNode.putObject("params");
            // TODO
            int noDescriptionParamsCount = 0;
            boolean allParamsHaveDescription = true;
            for (JsonNode paramNode : inputContentsNode.path("params")) {
                if (paramNode.path("description").asText() == "") {
                    allParamsHaveDescription = false;
                    noDescriptionParamsCount++;
                }
            }

            if(originalNode.path("response").path("page").path("contents").path("params").size() == 0) {
                paramsNode.put("status", "EMPTY");
                paramsNode.put("empty counter", noDescriptionParamsCount);
            } else if (!allParamsHaveDescription) {
                paramsNode.put("status", "WARNING");
                paramsNode.put("empty counter", noDescriptionParamsCount);
            } else {
                paramsNode.put("status", "OK");
                paramsNode.put("empty counter", noDescriptionParamsCount);
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
