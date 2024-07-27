package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import static org.example.JsonHandler.getMethodData;
import static org.example.JsonHandler.saveJson;

public class Main {
    public static boolean includeAllErrorsAndParamsFlag = false;

    public static void main(String[] args) {
        String token = "vk1.a.---";
        ObjectMapper mapper = new ObjectMapper();
        boolean isTokenExpired = false;


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
            JsonNode rootNode = mapper.readTree(new File("data/input.json"));
            Iterator<Map.Entry<String, JsonNode>> teamIterator = rootNode.fields();

            while (teamIterator.hasNext()) {
                if (isTokenExpired) {
                    break;
                }
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
            }

            if (!isTokenExpired) {
                saveJson(rootNode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}