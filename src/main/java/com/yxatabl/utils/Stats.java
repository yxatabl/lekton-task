package com.yxatabl.utils;

import java.util.List;

import com.yxatabl.entities.Book;

public record Stats(Integer count, Book oldestBook, Book newestBook, List<AuthorStats> topAuthors) {}
