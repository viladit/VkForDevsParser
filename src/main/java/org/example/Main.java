package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.example.JsonHandler.getMethodData;
import static org.example.JsonHandler.saveJson;

public class Main {
    public static void main(String[] args) {
        String token = "YOUR TOKEN";
        ObjectMapper mapper = new ObjectMapper();
        boolean isTokenExpired = false;

        try {
            JsonNode rootNode = mapper.readTree(new File("data/input.json"));
            Iterator<Map.Entry<String, JsonNode>> categoryIterator = rootNode.fields();

            while (categoryIterator.hasNext()) {
                if (isTokenExpired) {
                    break;
                }
                JsonNode categoryNode = categoryIterator.next().getValue();
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