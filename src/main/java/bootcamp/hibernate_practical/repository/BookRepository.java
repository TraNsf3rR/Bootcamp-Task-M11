package bootcamp.hibernate_practical.repository;

import bootcamp.hibernate_practical.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByAuthor(String author);
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByPublicationYearGreaterThan(int publicationYear);

    List<Book> findByBorrowedStatusFalse();
}
