package org.example;
import static org.example.Main.statisticCounter;

public class StatisticCounter {
    public static void increaseEmptyDescriptions() {
        statisticCounter.put("emptyDescriptions", statisticCounter.get("emptyDescriptions") + 1);
    }
    public static void increaseEmptyResultDescriptions() {
        statisticCounter.put("emptyResultDescriptions", statisticCounter.get("emptyResultDescriptions") + 1);
    }
    public static void increaseWarningErrors() {
        statisticCounter.put("warningErrors", statisticCounter.get("warningErrors") + 1);
    }
    public static void increaseEmptyErrors() {
        statisticCounter.put("emptyErrors", statisticCounter.get("emptyErrors") + 1);
    }
    public static void increaseWarningParams() {
        statisticCounter.put("warningParams", statisticCounter.get("warningParams") + 1);
    }
    public static void increaseEmptyParams() {
        statisticCounter.put("emptyParams", statisticCounter.get("emptyParams") + 1);
    }

}
