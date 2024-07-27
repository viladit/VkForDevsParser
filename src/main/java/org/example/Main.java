package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import static org.example.JsonHandler.getMethodData;
import static org.example.JsonHandler.saveJson;

public class Main {
    public static boolean includeAllErrorsAndParamsFlag = false;
    public static Map<String, Integer> statisticCounter = new HashMap<String, Integer>() {{
        put("emptyDescriptions", 0);
        put("emptyResultDescriptions", 0);
        put("warningErrors", 0);
        put("emptyErrors", 0);
        put("warningParams", 0);
        put("emptyParams", 0);
    }};

    public static void main(String[] args) {
        String token = "vk1.a.---Kcfeg";
        ObjectMapper mapper = new ObjectMapper();
        boolean isTokenExpired = false;
        Map<String, Integer> blankMap = new HashMap<>(statisticCounter);
        Map<String, Integer> overallStatistic = new HashMap<>(statisticCounter);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Включить список ВСЕХ ошибок и параметров в вывод? 1 = да, 0 - нет");
        while (true) {
            String line = scanner.nextLine();
            if (line.equals("1")) {
                includeAllErrorsAndParamsFlag = true;
                scanner.close();
                break;
            } else if (line.equals("0")) {
                includeAllErrorsAndParamsFlag = false;
                scanner.close();
                break;
            } else {
                System.out.println("Неверный ввод, повторите еще раз!");
            }
        }

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
                ((ObjectNode)categoryNode).put("empty descriptions", statisticCounter.get("emptyDescriptions"));
                ((ObjectNode)categoryNode).put("empty result_descriptions", statisticCounter.get("emptyResultDescriptions"));
                ((ObjectNode)categoryNode).put("warning errors", statisticCounter.get("warningErrors"));
                ((ObjectNode)categoryNode).put("empty errors", statisticCounter.get("emptyErrors"));
                ((ObjectNode)categoryNode).put("warning params", statisticCounter.get("warningParams"));
                ((ObjectNode)categoryNode).put("empty params", statisticCounter.get("emptyParams"));
                for (Map.Entry<String, Integer> entry : statisticCounter.entrySet()) {
                    overallStatistic.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
            ((ObjectNode)rootNode).put("summary empty descriptions", overallStatistic.get("emptyDescriptions"));
            ((ObjectNode)rootNode).put("summary empty result_descriptions", overallStatistic.get("emptyResultDescriptions"));
            ((ObjectNode)rootNode).put("summary warning errors", overallStatistic.get("warningErrors"));
            ((ObjectNode)rootNode).put("summary empty errors", overallStatistic.get("emptyErrors"));
            ((ObjectNode)rootNode).put("summary warning params", overallStatistic.get("warningParams"));
            ((ObjectNode)rootNode).put("summary empty params", overallStatistic.get("emptyParams"));

            if (!isTokenExpired) {
                saveJson(rootNode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}