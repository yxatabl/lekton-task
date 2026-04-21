package com.yxatabl;

import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.util.*;
import com.yxatabl.repositories.BookRepository;
import com.yxatabl.entities.Book;
import com.yxatabl.exceptions.AddingExistingBookException;
import com.yxatabl.exceptions.RemovingNonExistingBookException;
import com.yxatabl.utils.Stats;

public class CLI {
    private final BookRepository repository;
    private final Terminal terminal;
    private final LineReader reader;
    
    private static final List<String> COMMANDS = Arrays.asList(
        "ADD", "REMOVE", "LIST", "FIND", "STATS", "EXIT", "HELP"
    );

    public CLI() throws Exception {
        this.repository = new BookRepository();
        this.terminal = TerminalBuilder.builder()
            .system(true)
            .build();
        
        this.reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .completer(new CommandsCompleter())
            .parser(new DefaultParser())
            .build();
    }

    public void start() {
        printWelcome();
        
        while (true) {
            try {
                String line = reader.readLine();
                if (line == null || line.trim().isEmpty()) continue;
                
                line = line.trim();
                String[] parts = line.split("\\s+", 2);
                String command = parts[0].toUpperCase();
                String args = parts.length > 1 ? parts[1] : "";

                if (command.equals("EXIT")) {
                    terminal.writer().println("До свидания!");
                    break;
                }

                executeCommand(command, args);
            } catch (UserInterruptException e) {
                terminal.writer().println("^C");
            } catch (EndOfFileException e) {
                terminal.writer().println("\nДо свидания!");
                break;
            } catch (Exception e) {
                terminal.writer().println("Ошибка: " + e.getMessage());
            }
            terminal.flush();
        }
    }

    private void printWelcome() {
        terminal.writer().println("Система управления библиотекой");
        terminal.writer().println("Доступные команды: ADD, REMOVE, LIST, FIND, STATS, EXIT");
        terminal.writer().println("Введите HELP для подробной справки");
        terminal.writer().println("Нажмите TAB для автодополнения, ↑↓ для истории\n");
        terminal.flush();
    }

    private void executeCommand(String command, String args) {
        try {
            switch (command) {
                case "HELP":
                    showHelp();
                    break;
                case "ADD":
                    handleAdd(args);
                    break;
                case "REMOVE":
                    handleRemove(args);
                    break;
                case "LIST":
                    handleList();
                    break;
                case "FIND":
                    handleFind(args);
                    break;
                case "STATS":
                    handleStats();
                    break;
                default:
                    terminal.writer().println("Неизвестная команда. Введите HELP для справки.");
            }
        } catch (AddingExistingBookException e) {
            terminal.writer().println("Ошибка: " + e.getMessage());
        } catch (RemovingNonExistingBookException e) {
            terminal.writer().println("Ошибка: " + e.getMessage());
        } catch (NumberFormatException e) {
            terminal.writer().println("Ошибка: Некорректный формат числа");
        } catch (IllegalArgumentException e) {
            terminal.writer().println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            terminal.writer().println("Ошибка: Некорректный формат команды");
        }
    }

    private void showHelp() {
        terminal.writer().println("\nСПРАВКА ПО КОМАНДАМ");
        terminal.writer().println("ADD <название>;<автор>;<год>");
        terminal.writer().println("    Пример: ADD Война и мир;Лев Толстой;1869");
        terminal.writer().println("    Все поля обязательны. Нельзя добавить дубликат.");
        terminal.writer().println();
        terminal.writer().println("REMOVE <id>");
        terminal.writer().println("    Пример: REMOVE 1");
        terminal.writer().println("    Удаляет книгу по ID и показывает информацию о ней.");
        terminal.writer().println();
        terminal.writer().println("LIST");
        terminal.writer().println("    Показывает все книги в порядке добавления.");
        terminal.writer().println();
        terminal.writer().println("FIND <запрос>");
        terminal.writer().println("    Пример: FIND Толстой");
        terminal.writer().println("    Поиск по названию и автору (регистронезависимо).");
        terminal.writer().println();
        terminal.writer().println("STATS");
        terminal.writer().println("    Показывает статистику: всего книг, самая старая/новая, топ-3 авторов.");
        terminal.writer().println();
        terminal.writer().println("EXIT");
        terminal.writer().println("    Завершает программу.");
        terminal.writer().println();
    }

    private void handleAdd(String args) throws AddingExistingBookException {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Отсутствуют аргументы. Формат: ADD <название>;<автор>;<год>");
        }

        String[] parts = args.split(";");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Неверный формат. Ожидается: <название>;<автор>;<год>");
        }

        String title = parts[0].trim();
        String author = parts[1].trim();
        Integer year;

        try {
            year = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Год должен быть числом");
        }

        if (title.isEmpty() || author.isEmpty()) {
            throw new IllegalArgumentException("Название и автор не могут быть пустыми");
        }

        Book book = repository.createBook(title, author, year);
        terminal.writer().println("Книга добавлена: " + book);
    }

    private void handleRemove(String args) throws RemovingNonExistingBookException {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Отсутствует ID. Формат: REMOVE <id>");
        }

        Integer id;
        try {
            id = Integer.parseInt(args.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID должен быть числом");
        }

        Book removed = repository.removeBook(id);
        terminal.writer().println("Книга удалена: " + removed);
    }

    private void handleList() {
        var books = repository.getAllBooks();
        if (books.isEmpty()) {
            terminal.writer().println("Библиотека пуста");
        } else {
            terminal.writer().println("Всего книг: " + books.size());
            books.forEach(book -> terminal.writer().println("  " + book));
        }
    }

    private void handleFind(String query) {
        if (query.isEmpty()) {
            throw new IllegalArgumentException("Отсутствует поисковый запрос. Формат: FIND <запрос>");
        }

        var results = repository.findBooks(query);
        if (results.isEmpty()) {
            terminal.writer().println("Книги не найдены");
        } else {
            terminal.writer().println("Найдено " + results.size() + " книг:");
            results.forEach(book -> terminal.writer().println("  " + book));
        }
    }

    private void handleStats() {
        Stats stats = repository.getStats();
        
        terminal.writer().println("\nСТАТИСТИКА БИБЛИОТЕКИ");
        terminal.writer().println("Общее количество книг: " + stats.count());
        
        if (stats.oldestBook() != null) {
            terminal.writer().println("Самая старая книга: " + stats.oldestBook());
            terminal.writer().println("Самая новая книга: " + stats.newestBook());
        } else {
            terminal.writer().println("Самая старая книга: отсутствует");
            terminal.writer().println("Самая новая книга: отсутствует");
        }
        
        terminal.writer().println("\nТоп-3 авторов по количеству книг:");
        var topAuthors = stats.topAuthors();
        if (topAuthors.isEmpty()) {
            terminal.writer().println("  Нет данных");
        } else {
            int rank = 1;
            for (var author : topAuthors) {
                terminal.writer().printf("  %d. %s: %d книг(а)%n", 
                    rank++, author.author(), author.bookCount());
            }
        }
        terminal.writer().println();
    }

    private static class CommandsCompleter implements Completer {
        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            String word = line.word().toUpperCase();
            
            if (line.wordIndex() == 0) {
                COMMANDS.stream()
                    .filter(cmd -> cmd.startsWith(word))
                    .forEach(cmd -> candidates.add(new Candidate(cmd)));
            }
        }
    }
}