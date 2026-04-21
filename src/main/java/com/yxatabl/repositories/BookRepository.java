package com.yxatabl.repositories;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.yxatabl.entities.Book;

public class BookRepository {
    private final Map<Integer, Book> books;

    public BookRepository() {
        this.books = new HashMap<>();
    }

    public Book createBook(String title, String author, Integer year) {
        Book book = new Book(books.size(), title, author, year);
        books.put(books.size(), book);
        return book;
    }

    public void removeBook(Integer id) {
        books.remove(id);
    }

    public Collection<Book> getAllBooks() {
        Collection<Book> result = books.values();
        return result;
    }
}
