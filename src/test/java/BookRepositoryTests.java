import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.yxatabl.entities.Book;
import com.yxatabl.repositories.BookRepository;

public class BookRepositoryTests {
    private BookRepository repository;

    @BeforeEach
    public void beforeEach() {
        repository = new BookRepository();
    }

    @Test
    public void checkBookCreate() {
        Book book = repository.createBook("Title", "Author", 2026);
        assertTrue(repository.getAllBooks().contains(book));
    }

    @Test
    public void checkRemoveBook() {
        Book book = repository.createBook("Title", "Author", 2026);
        repository.removeBook(book.getId());
        assertFalse(repository.getAllBooks().contains(book));
    }

    @Test
    public void checkGetAllBooks() {
        Book book1 = repository.createBook("Title1", "Author1", 2026);
        Book book2 = repository.createBook("Title2", "Author2", 2026);
        
        Collection<Book> result = repository.getAllBooks();
        assertTrue(result.contains(book1) && result.contains(book2));
    }
}
