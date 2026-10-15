# Практическая работа №5

Строки, массивы, циклы и методы обработки данных. Вариант 26.

Формат входной строки (вариант 26): 5 значений часов (пн–пт) и почасовая ставка через `;`, например `8;9;10;8;8;500`.

## Сборка и запуск

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr02.SelfCheck
```

или `./run_selfcheck.sh` (Windows: `run_selfcheck.bat`).

SelfCheck по очереди выполняет:
1. unit-проверки (32 проверки);
2. data-driven проверки из `data/sample.csv`;
3. интерактивный режим — ввод строк с клавиатуры, выход по `exit` или пустой строке.

## Диаграммы

```bash
dot -Tpng diagrams/activity.dot -o diagrams/activity.png
dot -Tpng diagrams/class.dot -o diagrams/class.png
```
