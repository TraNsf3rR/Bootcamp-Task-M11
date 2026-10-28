package bootcamp.hibernate_practical.repository;

import bootcamp.hibernate_practical.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;

    @Test
    void findsBooksByExactAuthor() {
        Book matchingBook = bookRepository.save(
                new Book("Pride and Prejudice", "Jane Austen", "Classic", 1813, true)
        );
        bookRepository.save(new Book("Another Book", "Jane Austen Jr", "Fiction", 2020, true));

        var results = bookRepository.findByAuthor("Jane Austen");

        assertEquals(
                Set.of(matchingBook.getId()),
                results.stream().map(Book::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void findsBooksByCaseInsensitivePartialTitle() {
        Book matchingBook = bookRepository.save(
                new Book("The Hobbit", "J.R.R. Tolkien", "Fantasy", 1937, true)
        );
        Book anotherMatchingBook = bookRepository.save(
                new Book("HOBBIT: There and Back Again", "J.R.R. Tolkien", "Fantasy", 1940, true)
        );
        bookRepository.save(new Book("Dune", "Frank Herbert", "Science Fiction", 1965, true));

        var results = bookRepository.findByTitleContainingIgnoreCase("hobbit");

        assertEquals(
                Set.of(matchingBook.getId(), anotherMatchingBook.getId()),
                results.stream().map(Book::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void findsBooksPublishedStrictlyAfterTheGivenYear() {
        bookRepository.save(new Book("Older Book", "Author One", "Fiction", 1999, true));
        bookRepository.save(new Book("Boundary Book", "Author Two", "Fiction", 2000, true));
        Book newerBook = bookRepository.save(
                new Book("Newer Book", "Author Three", "Fiction", 2001, true)
        );
        Book newestBook = bookRepository.save(
                new Book("Newest Book", "Author Four", "Fiction", 2020, true)
        );

        var results = bookRepository.findByPublicationYearGreaterThan(2000);

        assertEquals(
                Set.of(newerBook.getId(), newestBook.getId()),
                results.stream().map(Book::getId).collect(Collectors.toSet())
        );
    }
}
