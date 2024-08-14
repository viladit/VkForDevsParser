package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.example.Main.includeAllErrorsAndParamsFlag;
import static org.example.StatisticCounter.*;


public class JsonHandler {

    public static ObjectNode getMethodData(String methodName, String token) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            String response = RequestsHandler.getMethodInfo(methodName, token);
            JsonNode originalNode = mapper.readTree(response);
            ObjectNode outputNode = mapper.createObjectNode();

            if (response.contains("\"error_code\":104")) {
                ObjectNode descriptionNode = outputNode.putObject("description");
                descriptionNode.put("value", "");
                descriptionNode.put("status", "EMPTY");
                increaseEmptyCounter();

                // result_description
                ObjectNode resultDescriptionNode = outputNode.putObject("result_description");
                resultDescriptionNode.put("value", "");
                resultDescriptionNode.put("status", "EMPTY");
                increaseEmptyCounter();

                // errors
                ObjectNode errorsNode = outputNode.putObject("errors");
                errorsNode.put("status", "EMPTY");
                increaseEmptyCounter();

                // params
                ObjectNode paramsNode = outputNode.putObject("params");
                paramsNode.put("value", "");
                paramsNode.put("status", "EMPTY");
                increaseEmptyCounter();

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
                increaseEmptyCounter();
            } else {
                descriptionNode.put("status", "OK");
            }

            // result_description
            ObjectNode resultDescriptionNode = outputNode.putObject("result_description");
            resultDescriptionNode.put("value", inputContentsNode.path("result_description").asText());
            if (inputContentsNode.path("result_description").asText() == "") {
                resultDescriptionNode.put("status", "EMPTY");
                increaseEmptyCounter();
            } else {
                resultDescriptionNode.put("status", "OK");
            }

            // errors
            ObjectNode errorsNode = outputNode.putObject("errors");
            if (inputContentsNode.path("errors").size() == 0) {
                errorsNode.put("status", "EMPTY");
                increaseEmptyCounter();
            } else if (inputContentsNode.path("errors").size() <= 1) {
                errorsNode.put("status", "WARNING");
                increaseWarningCounter();
            } else if (inputContentsNode.path("errors").size() > 1) {
                errorsNode.put("status", "OK");
            }
            if (includeAllErrorsAndParamsFlag) {
                ArrayNode errorsArrayNode = errorsNode.putArray("errors");
                for (JsonNode errorNode : inputContentsNode.path("errors")) {
                    ObjectNode newErrorNode = errorsArrayNode.addObject();
                    newErrorNode.put("title", errorNode.path("title").asText());
                    newErrorNode.put("description", errorNode.path("description").asText());
                }
            }

            // params
            ObjectNode paramsNode = outputNode.putObject("params");
            int noDescriptionParamsCount = 0;
            boolean allParamsHaveDescription = true;
            for (JsonNode paramNode : inputContentsNode.path("params")) {
                if (paramNode.path("description").asText() == "") {
                    allParamsHaveDescription = false;
                    noDescriptionParamsCount++;
                }
            }
            if(inputContentsNode.path("params").size() == 0) {
                paramsNode.put("status", "EMPTY");
                paramsNode.put("empty counter", noDescriptionParamsCount);
                increaseEmptyCounter();
            } else if (!allParamsHaveDescription) {
                paramsNode.put("status", "WARNING");
                paramsNode.put("empty counter", noDescriptionParamsCount);
                increaseWarningCounter();
            } else {
                paramsNode.put("status", "OK");
                paramsNode.put("empty counter", noDescriptionParamsCount);

            }
            if (includeAllErrorsAndParamsFlag) {
                ArrayNode paramsArrayNode = paramsNode.putArray("params");
                for (JsonNode paramNode : inputContentsNode.path("params")) {
                    ObjectNode newErrorNode = paramsArrayNode.addObject();
                    newErrorNode.put("name", paramNode.path("name").asText());
                    newErrorNode.put("description", paramNode.path("description").asText());
                }
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

        File jsonFile = new File("src/main/resources/data/"+filename);
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, jsonNode);

        System.out.println("JSON записан в файл: src/main/resources/data/" + filename);
    }
}
