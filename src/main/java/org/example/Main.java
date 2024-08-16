package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.example.JsonHandler.getMethodData;
import static org.example.JsonHandler.saveJson;
import static org.example.JsonToCsvConverter.writeAllDataToCSV;

public class Main {
    public static String token;
    public static boolean includeAllErrorsAndParamsFlag;
    public static Map<String, Integer> statisticCounter = new HashMap<String, Integer>() {{
        put("emptyCounter", 0);
        put("warningCounter", 0);
    }};

    public static void main(String[] args) {
        loadProperties();
        boolean isTokenExpired = false;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Integer> blankMap = new HashMap<>(statisticCounter);
        Map<String, Integer> overallStatistic = new HashMap<>(statisticCounter);

        try {
            JsonNode rootNode = mapper.readTree(new File("src/main/resources/data/input.json"));
            Iterator<Map.Entry<String, JsonNode>> teamIterator = rootNode.fields();

            while (teamIterator.hasNext()) {
                if (isTokenExpired) {
                    break;
                }
                statisticCounter.putAll(blankMap);
                JsonNode categoryNode = teamIterator.next().getValue();
                Iterator<Map.Entry<String, JsonNode>> methodIterator = categoryNode.fields();
                while (methodIterator.hasNext()) {
                    Map.Entry<String, JsonNode> methodEntry = methodIterator.next();
                    String methodName = methodEntry.getKey();

                    // Получение информации о методе
                    ObjectNode methodInfo = getMethodData(methodName, token);
                    if (methodInfo == null) {
                        isTokenExpired = true;
                        break;
                    }

                    // Вставка информации о методе внутрь JSON
                    ((ObjectNode)categoryNode).set(methodName, methodInfo);

                }
                ((ObjectNode)categoryNode).put("empty", statisticCounter.get("emptyCounter"));
                ((ObjectNode)categoryNode).put("warning", statisticCounter.get("warningCounter"));
                for (Map.Entry<String, Integer> entry : statisticCounter.entrySet()) {
                    overallStatistic.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
            ((ObjectNode)rootNode).put("summary empty", overallStatistic.get("emptyCounter"));
            ((ObjectNode)rootNode).put("summary warning", overallStatistic.get("warningCounter"));
            if (!isTokenExpired) {
                saveJson(rootNode);
            }
            writeAllDataToCSV();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static void loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(input);
            token = properties.getProperty("token");
            includeAllErrorsAndParamsFlag = Boolean.parseBoolean(properties.getProperty("includeAllErrorsAndParamsFlag"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}