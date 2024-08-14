package org.example;
import static org.example.Main.statisticCounter;

public class StatisticCounter {
    public static void increaseEmptyCounter() {
        statisticCounter.put("emptyCounter", statisticCounter.get("emptyCounter") + 1);
    }
    public static void increaseWarningCounter() {
        statisticCounter.put("warningCounter", statisticCounter.get("warningCounter") + 1);
    }
}
