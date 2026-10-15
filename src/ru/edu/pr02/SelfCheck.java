package ru.edu.pr02;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class SelfCheck {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        runUnitChecks();
        runDataDriven("data/sample.csv");
        runInteractive();
    }

    // ===== Режим 1: unit-проверки =====
    private static void runUnitChecks() {
        System.out.println("=== Unit-проверки ===");

        // 1. parseHours — корректная строка (вариант 26: 5 значений часов + ставка)
        try {
            int[] r = TimesheetAnalyzer.parseHours("8;9;10;8;8;500");
            check("1 parseHours correct", r.length == 5 && r[0] == 8 && r[1] == 9 && r[2] == 10 && r[3] == 8 && r[4] == 8);
        } catch (Exception e) {
            check("1 parseHours correct: " + e.getMessage(), false);
        }

        // 2. parseHours — пустая строка
        expectError("2 parseHours empty", "", "структура");

        // 3. parseHours — значение 17
        expectError("3 parseHours 17", "8;8;17;8;8;500", "позиция 3");

        // 4. parseHours — не число
        expectError("4 parseHours abc", "8;abc;8;8;8;500", "позиция 2");

        // 5. parseHours — 4 значения часов + ставка (5 полей вместо 6)
        expectError("5 parseHours 4 values", "8;8;8;8;500", "структура");

        // 6. parseHours — лишняя ; в конце (7 полей)
        expectError("6 parseHours trailing", "8;8;8;8;8;500;", "структура");

        // 7. parseHours — ведущий ноль
        expectError("7 parseHours leading zero", "08;8;8;8;8;500", "позиция 1");

        // 8. total — типовое
        try {
            check("8 total typical", TimesheetAnalyzer.total(new int[]{8, 9, 10, 8, 8}) == 43);
        } catch (Exception e) {
            check("8 total typical: " + e.getMessage(), false);
        }

        // 9. total — нижняя граница
        try {
            check("9 total min", TimesheetAnalyzer.total(new int[]{0, 0, 0, 0, 0}) == 0);
        } catch (Exception e) {
            check("9 total min: " + e.getMessage(), false);
        }

        // 10. total — верхняя граница
        try {
            check("10 total max", TimesheetAnalyzer.total(new int[]{16, 16, 16, 16, 16}) == 80);
        } catch (Exception e) {
            check("10 total max: " + e.getMessage(), false);
        }

        // 11. average — типовое
        try {
            check("11 average typical", TimesheetAnalyzer.average(new int[]{8, 9, 10, 8, 8}) == 8.6);
        } catch (Exception e) {
            check("11 average typical: " + e.getMessage(), false);
        }

        // 12. average — целое
        try {
            check("12 average integer", TimesheetAnalyzer.average(new int[]{8, 8, 8, 8, 8}) == 8.0);
        } catch (Exception e) {
            check("12 average integer: " + e.getMessage(), false);
        }

        // 13. average — дробное
        try {
            check("13 average fractional", TimesheetAnalyzer.average(new int[]{7, 8, 9, 8, 7}) == 7.8);
        } catch (Exception e) {
            check("13 average fractional: " + e.getMessage(), false);
        }

        // 14. overtimeDays — нет переработки
        try {
            check("14 overtimeDays none", TimesheetAnalyzer.overtimeDays(new int[]{8, 8, 8, 8, 8}) == 0);
        } catch (Exception e) {
            check("14 overtimeDays none: " + e.getMessage(), false);
        }

        // 15. overtimeDays — два дня
        try {
            check("15 overtimeDays two", TimesheetAnalyzer.overtimeDays(new int[]{9, 8, 7, 10, 8}) == 2);
        } catch (Exception e) {
            check("15 overtimeDays two: " + e.getMessage(), false);
        }

        // 16. overtimeDays — граница 8 (8 не переработка)
        try {
            check("16 overtimeDays boundary", TimesheetAnalyzer.overtimeDays(new int[]{8, 9, 8, 8, 8}) == 1);
        } catch (Exception e) {
            check("16 overtimeDays boundary: " + e.getMessage(), false);
        }

        // 17. weeklyOvertime — сумма 40
        try {
            check("17 weeklyOvertime 40", TimesheetAnalyzer.weeklyOvertime(new int[]{8, 8, 8, 8, 8}) == 0);
        } catch (Exception e) {
            check("17 weeklyOvertime 40: " + e.getMessage(), false);
        }

        // 18. weeklyOvertime — сумма 50
        try {
            check("18 weeklyOvertime 50", TimesheetAnalyzer.weeklyOvertime(new int[]{10, 10, 10, 10, 10}) == 10);
        } catch (Exception e) {
            check("18 weeklyOvertime 50: " + e.getMessage(), false);
        }

        // 19. weeklyOvertime — сумма меньше 40
        try {
            check("19 weeklyOvertime under 40", TimesheetAnalyzer.weeklyOvertime(new int[]{6, 6, 6, 6, 6}) == 0);
        } catch (Exception e) {
            check("19 weeklyOvertime under 40: " + e.getMessage(), false);
        }

        // 20. Вариант 26, часть А — 5 значений без ставки теперь ошибка структуры
        expectError("20 variant A: 5 values without rate", "8;8;8;8;8", "ожидалось 6 значений");

        // 21. Вариант 26, часть А — ставка не число (позиция 6)
        expectError("21 variant A: rate abc", "8;8;8;8;8;abc", "позиция 6");

        // 22. Вариант 26, часть А — ставка 0 недопустима
        expectError("22 variant A: rate 0", "8;8;8;8;8;0", "позиция 6");

        // 23. Вариант 26, часть А — дробная ставка читается из строки
        try {
            check("23 variant A: parseRate 250.5", TimesheetAnalyzer.parseRate("8;9;10;8;8;250.5") == 250.5);
        } catch (Exception e) {
            check("23 variant A: parseRate 250.5: " + e.getMessage(), false);
        }

        // 24. Вариант 26, часть Б — без переработки: 40 × 500 = 20000
        try {
            check("24 variant B: pay no overtime", TimesheetAnalyzer.addPayForWeek(new int[]{8, 8, 8, 8, 8}, 500) == 20000.0);
        } catch (Exception e) {
            check("24 variant B: pay no overtime: " + e.getMessage(), false);
        }

        // 25. Вариант 26, часть Б — с переработкой: 50 × 300 + 10 × 300 × 1.5 = 19500
        try {
            check("25 variant B: pay with overtime", TimesheetAnalyzer.addPayForWeek(new int[]{10, 10, 10, 10, 10}, 300) == 19500.0);
        } catch (Exception e) {
            check("25 variant B: pay with overtime: " + e.getMessage(), false);
        }

        // 26. Вариант 26, часть Б — дробная ставка: 43 × 250.5 + 3 × 250.5 × 1.5 = 11898.75
        try {
            check("26 variant B: pay fractional rate", TimesheetAnalyzer.addPayForWeek(new int[]{8, 9, 10, 8, 8}, 250.5) == 11898.75);
        } catch (Exception e) {
            check("26 variant B: pay fractional rate: " + e.getMessage(), false);
        }

        // 27. Вариант 26, часть Б — отрицательная ставка → IllegalArgumentException
        try {
            TimesheetAnalyzer.addPayForWeek(new int[]{8, 8, 8, 8, 8}, -1);
            check("27 variant B: negative rate", false);
        } catch (IllegalArgumentException e) {
            check("27 variant B: negative rate", true);
        }

        // 28. total — null → IllegalArgumentException
        try {
            TimesheetAnalyzer.total(null);
            check("28 total null", false);
        } catch (IllegalArgumentException e) {
            check("28 total null", e.getMessage().equals("hours is null"));
        }

        // 29. average — длина != 5 → IllegalArgumentException
        try {
            TimesheetAnalyzer.average(new int[]{8, 8, 8, 8});
            check("29 average length 4", false);
        } catch (IllegalArgumentException e) {
            check("29 average length 4", e.getMessage().contains("got 4"));
        }

        // 30. parseHours — surrounding-пробел
        expectError("30 parseHours space", "8; 9;10;8;8;500", "позиция 2");

        // 31. parseHours — пустое поле
        expectError("31 parseHours empty field", "8;;9;10;8;500", "позиция 2");

        // 32. parseHours — дробное значение часов
        expectError("32 parseHours 8.5", "8;8.5;8;8;8;500", "не является целым числом");

        System.out.println("Unit: " + passed + "/" + (passed + failed) + " passed");
    }

    // ===== Режим 2: data-driven из sample.csv =====
    private static void runDataDriven(String path) {
        System.out.println();
        System.out.println("=== Data-driven: " + path + " ===");

        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(path));
        } catch (Exception e) {
            System.out.println("FAIL reading sample.csv: " + e.getMessage());
            return;
        }

        int ok = 0;
        int count = 0;

        for (String row : lines) {
            if (row.isEmpty() || row.startsWith("#")) {
                continue;
            }
            count++;

            String[] parts = row.split("\\|", -1);
            String input = parts[0].equals("EMPTY") ? "" : parts[0];
            String expected = parts[1];
            String comment = parts.length > 2 ? parts[2] : "";

            String actual = evaluate(input);
            boolean match = expected.startsWith("OK:") ? actual.equals(expected) : actual.contains(expected.substring(6));

            if (match) {
                ok++;
                System.out.println("PASS data/" + count + " " + comment);
            } else {
                System.out.println("FAIL data/" + count + " " + comment + ": ожидалось " + expected + ", получено " + actual);
            }
        }

        System.out.println("Data-driven: " + ok + "/" + count + " passed");
    }

    private static String evaluate(String input) {
        try {
            int[] hours = TimesheetAnalyzer.parseHours(input);
            double rate = TimesheetAnalyzer.parseRate(input);

            return "OK:sum=" + TimesheetAnalyzer.total(hours)
                    + ",avg=" + String.format(Locale.US, "%.2f", TimesheetAnalyzer.average(hours))
                    + ",od=" + TimesheetAnalyzer.overtimeDays(hours)
                    + ",wo=" + TimesheetAnalyzer.weeklyOvertime(hours)
                    + ",pay=" + String.format(Locale.US, "%.2f", TimesheetAnalyzer.addPayForWeek(hours, rate));
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    // ===== Режим 3: интерактивный =====
    private static void runInteractive() {
        System.out.println();
        System.out.println("=== Интерактивный режим ===");
        System.out.println("Введите строку вида 8;9;10;8;8;500 (5 значений часов и ставка) или exit для выхода:");

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.equals("exit") || line.isEmpty()) {
                break;
            }

            try {
                int[] hours = TimesheetAnalyzer.parseHours(line);
                double rate = TimesheetAnalyzer.parseRate(line);

                System.out.println("Сумма: " + TimesheetAnalyzer.total(hours));
                System.out.printf(Locale.US, "Среднее: %.2f%n", TimesheetAnalyzer.average(hours));
                System.out.println("Дней с переработкой: " + TimesheetAnalyzer.overtimeDays(hours));
                System.out.println("Недельная переработка: " + TimesheetAnalyzer.weeklyOvertime(hours));
                System.out.printf(Locale.US, "Оплата за неделю: %.2f руб.%n", TimesheetAnalyzer.addPayForWeek(hours, rate));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }

            System.out.println();
            System.out.println("Введите следующую строку или exit:");
        }

        scanner.close();
    }

    // ===== Вспомогательные методы =====
    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    private static void expectError(String name, String line, String fragment) {
        try {
            TimesheetAnalyzer.parseHours(line);
            check(name + " (исключение не выброшено)", false);
        } catch (IllegalArgumentException e) {
            check(name, e.getMessage().contains(fragment));
        }
    }
}
