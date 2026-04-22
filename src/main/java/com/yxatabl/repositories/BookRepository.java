package com.yxatabl.repositories;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.yxatabl.entities.Book;
import com.yxatabl.exceptions.AddingExistingBookException;
import com.yxatabl.exceptions.RemovingNonExistingBookException;
import com.yxatabl.utils.AuthorStats;
import com.yxatabl.utils.Stats;

public class BookRepository {
    private final Map<Integer, Book> books;

    private boolean bookExists(String title, String author) {
        return books.values().stream()
            .anyMatch(book -> book.author().equals(author) && book.title().equals(title));
    }

    private Comparator<Book> getComparator(String sortBy) {
        if (sortBy == null) {
            return Comparator.comparing(Book::id);
        }

        return switch (sortBy.toLowerCase()) {
            case "title" -> Comparator.comparing(Book::title);
            case "author" -> Comparator.comparing(Book::author);
            case "year" -> Comparator.comparing(Book::year);
            default -> Comparator.comparing(Book::id);
        };
    }

    public BookRepository() {
        this.books = new HashMap<>();
    }

    public Book createBook(String title, String author, Integer year) throws AddingExistingBookException {
        if (bookExists(title, author)) {
            throw new AddingExistingBookException("Данная книга уже добавлена");
        }

        Book book = new Book(books.size(), title, author, year);
        books.put(books.size(), book);

        return book;
    }

    public Book removeBook(Integer id) throws RemovingNonExistingBookException {
        Book book = books.remove(id);
        
        if (book == null) {
            throw new RemovingNonExistingBookException("Попытка удалить несуществующую книгу");
        }

        return book;
    }

    public Collection<Book> getAllBooks(String sortBy) {
        Collection<Book> result = books.values();

        return result.stream()
            .sorted(getComparator(sortBy))
            .collect(Collectors.toList());
    }

    public List<Book> findBooks(String query) {
        String[] terms = query.toLowerCase().trim().split("\\s+");

        return books.values().stream()
            .filter(book -> {
                String searchable = (book.author() + " " + book.title()).toLowerCase();

                return Arrays.stream(terms).allMatch(searchable::contains);
            })
            .collect(Collectors.toList());
    }

    public Stats getStats() {
        if (books.isEmpty()) {
            return new Stats(0, null, null, Collections.emptyList());
        }

        Collection<Book> allBooks = books.values();

        Book newestBook = null;
        Book oldestBook = null;
        Map<String, Integer> authorCounts = new HashMap<>();

        for (Book book : allBooks) {
            if (oldestBook == null || book.year() < oldestBook.year()) {
                oldestBook = book;
            }

            if (newestBook == null || book.year() > newestBook.year()) {
                newestBook = book;
            }

            authorCounts.merge(book.author(), 1, Integer::sum);
        }

        List<AuthorStats> topAuthors = authorCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(3)
            .map(entry -> new AuthorStats(entry.getKey(), Integer.valueOf(entry.getValue())))
            .collect(Collectors.toList());
        
        return new Stats(allBooks.size(), oldestBook, newestBook, topAuthors);
    }
}
