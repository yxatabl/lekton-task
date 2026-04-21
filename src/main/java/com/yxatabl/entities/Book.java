package com.yxatabl.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Book {
    private final Integer id;
    private final String title;
    private final String author;
    private final Integer year;
}
