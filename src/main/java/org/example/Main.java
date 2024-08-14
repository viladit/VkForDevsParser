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
        put("emptyCounter", 0);
        put("warningCounter", 0);
    }};

    public static void main(String[] args) {
        String token = "vk1.a.cGqL5la1d72x0O4KUjc3vfm0uJ1cVefIyj9kqytisF70jnjyBy0rznkzt4buR89zkrss5fP9g9rcM3F90pDBlSSr7ECttjR8rII6lrT583nNpJPwDVeFeOBksMOMS9CKS3ZwBti1uooQXvYNV4Lo-AY80fKCtjVpIlQ6QGqHW7mncXth8MDTpZUm_Eonq0MmwfIuJRAq1kS8F7Jn1XN30w";
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

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}