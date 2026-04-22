import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.yxatabl.entities.Book;
import com.yxatabl.exceptions.AddingExistingBookException;
import com.yxatabl.exceptions.RemovingNonExistingBookException;
import com.yxatabl.repositories.BookRepository;
import com.yxatabl.utils.Stats;

public class BookRepositoryTests {
    private BookRepository repository;

    @BeforeEach
    public void beforeEach() {
        repository = new BookRepository();
    }

    @Test
    public void checkBookCreate() throws AddingExistingBookException {
        Book book = repository.createBook("Title", "Author", 2026);
        
        assertTrue(repository.getAllBooks(null).contains(book));
    }

    @Test
    public void checkBookCreateBad() throws AddingExistingBookException {
        repository.createBook("Title", "Author", 2026);

        assertThrows(AddingExistingBookException.class, () -> {
            repository.createBook("Title", "Author", 2026);
        });
    }

    @Test
    public void checkRemoveBook() throws AddingExistingBookException, RemovingNonExistingBookException {
        Book book = repository.createBook("Title", "Author", 2026);
        repository.removeBook(book.id());
        
        assertFalse(repository.getAllBooks(null).contains(book));
    }

    @Test
    public void checkRemoveBookBad() throws AddingExistingBookException {
        Book book = repository.createBook("Title", "Author", 2026);
        
        assertThrows(RemovingNonExistingBookException.class, () -> {
            repository.removeBook(book.id()+1);
        });
    }

    @Test
    public void checkGetAllBooks() throws AddingExistingBookException {
        Book book1 = repository.createBook("Title1", "Author1", 2026);
        Book book2 = repository.createBook("Title2", "Author2", 2026);
        
        Collection<Book> result = repository.getAllBooks(null);
        assertTrue(result.contains(book1) && result.contains(book2));
    }

    @Test
    public void checkFindBooks() throws AddingExistingBookException {
        repository.createBook("Super Book", "Lev Tolstoy", 2026);
        repository.createBook("Good One", "Lev", 2027);
        repository.createBook("Amazing book", "Nikolay Tolstoy", 2028);
        repository.createBook("Regular", "Nikolay", 2029);

        List<Book> result1 = repository.findBooks("Super Book Lev Tolstoy");
        List<Book> result2 = repository.findBooks("Book");
        List<Book> result3 = repository.findBooks("Nikolay");

        assertEquals(0, result1.get(0).id());
        
        assertEquals(0, result2.get(0).id());
        assertEquals(2, result2.get(1).id());
        
        assertEquals(2, result3.get(0).id());
        assertEquals(3, result3.get(1).id());
    }
    
    @Test
    public void checkStats() throws AddingExistingBookException {
        Book book1 = repository.createBook("Title1", "Author1", 2026);
        repository.createBook("Title2", "Author1", 2027);
        repository.createBook("Title1", "Author2", 2028);
        repository.createBook("Title2", "Author2", 2029);
        repository.createBook("TitleX", "Author3", 2030);
        Book book6 = repository.createBook("Title3", "Author2", 2031);

        Stats stats = repository.getStats();

        assertEquals(6, stats.count());
        assertEquals(book6, stats.newestBook());
        assertEquals(book1, stats.oldestBook());
        assertEquals("Author2", stats.topAuthors().get(0).author());
    }
}
