package ru.edu.pr02;

public class TimesheetAnalyzer {

    private static final int DAYS = 5;
    private static final int FIELDS = 6;             // вариант 26: 5 значений часов + ставка
    private static final int MAX_HOURS = 16;
    private static final int DAILY_NORMAL_HOURS = 8;
    private static final int WEEKLY_NORMAL_HOURS = 40;
    private static final double OVERTIME_RATE = 1.5;
    private static final String HOURS_REGEX = "^(0|[1-9][0-9]*)$";
    private static final String RATE_REGEX = "^(0|[1-9][0-9]*)(\\.[0-9]+)?$";

    // Вариант 26, часть А: строка из 6 значений (5 значений часов + ставка), возвращает int[5]
    public static int[] parseHours(String line) {
        String[] parts = splitLine(line);

        int[] result = new int[DAYS];

        for (int i = 0; i < DAYS; i++) {
            result[i] = parseHourValue(parts[i], i + 1);
        }

        parseRateValue(parts[DAYS]);

        return result;
    }

    // Ставка из той же строки (позиция 6). Строка проверяется целиком через parseHours
    public static double parseRate(String line) {
        parseHours(line);
        return parseRateValue(splitLine(line)[DAYS]);
    }

    public static int total(int[] hours) {
        checkHours(hours);

        int sum = 0;

        for (int i = 0; i < hours.length; i++) {
            sum += hours[i];
        }

        return sum;
    }

    public static double average(int[] hours) {
        checkHours(hours);

        double value = total(hours) / (double) DAYS;

        return Math.round(value * 100.0) / 100.0;
    }

    public static int overtimeDays(int[] hours) {
        checkHours(hours);

        int count = 0;

        for (int i = 0; i < hours.length; i++) {
            if (hours[i] > DAILY_NORMAL_HOURS) {
                count++;
            }
        }

        return count;
    }

    public static int weeklyOvertime(int[] hours) {
        checkHours(hours);

        return Math.max(0, total(hours) - WEEKLY_NORMAL_HOURS);
    }

    // Вариант 26, часть Б: обычная оплата + оплата переработки с коэффициентом 1.5
    public static double addPayForWeek(int[] hours, double rate) {
        checkHours(hours);

        if (rate <= 0) {
            throw new IllegalArgumentException("rate must be > 0, got " + rate);
        }

        double regularPay = total(hours) * rate;
        double overtimePay = weeklyOvertime(hours) * rate * OVERTIME_RATE;

        return Math.round((regularPay + overtimePay) * 100.0) / 100.0;
    }

    private static String[] splitLine(String line) {
        if (line == null) {
            throw new IllegalArgumentException(
                    "Ошибка: структура – ожидалось " + FIELDS + " значений, получено 0"
            );
        }

        String[] parts = line.split(";", -1);

        if (parts.length != FIELDS) {
            throw new IllegalArgumentException(
                    "Ошибка: структура – ожидалось " + FIELDS + " значений, получено " + parts.length
            );
        }

        return parts;
    }

    private static int parseHourValue(String part, int position) {
        if (!part.matches(HOURS_REGEX)) {
            throw new IllegalArgumentException(
                    "Ошибка: позиция " + position + " – значение \"" + part + "\" не является целым числом"
            );
        }

        int value = Integer.parseInt(part);

        if (value < 0 || value > MAX_HOURS) {
            throw new IllegalArgumentException(
                    "Ошибка: позиция " + position + " – значение " + value + " вне диапазона [0, " + MAX_HOURS + "]"
            );
        }

        return value;
    }

    private static double parseRateValue(String part) {
        if (!part.matches(RATE_REGEX)) {
            throw new IllegalArgumentException(
                    "Ошибка: позиция " + FIELDS + " – ставка \"" + part + "\" не является числом"
            );
        }

        double rate = Double.parseDouble(part);

        if (rate <= 0) {
            throw new IllegalArgumentException(
                    "Ошибка: позиция " + FIELDS + " – ставка должна быть больше 0"
            );
        }

        return rate;
    }

    private static void checkHours(int[] hours) {
        if (hours == null) {
            throw new IllegalArgumentException("hours is null");
        }

        if (hours.length != DAYS) {
            throw new IllegalArgumentException("hours length must be " + DAYS + ", got " + hours.length);
        }
    }
}
