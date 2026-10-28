package bootcamp.hibernate_practical.service;

import bootcamp.hibernate_practical.dto.BookResponse;
import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.dto.UpdateBookRequest;
import bootcamp.hibernate_practical.entity.Book;
import bootcamp.hibernate_practical.exception.BookNotFoundException;
import bootcamp.hibernate_practical.exception.BookStateConflictException;
import bootcamp.hibernate_practical.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse createBook(CreateBookRequest request) {
        Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getGenre(),
                request.getPublicationYear(),
                true
        );
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BookResponse getBookById(Long id) {
        return mapToResponse(findBookById(id));
    }

    public BookResponse updateBook(Long id, UpdateBookRequest request) {
        Book book = findBookById(id);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setGenre(request.getGenre());
        book.setPublicationYear(request.getPublicationYear());
        book.setBorrowedStatus(!request.getAvailable());

        return mapToResponse(bookRepository.save(book));
    }

    public BookResponse borrowBook(Long id) {
        Book book = findBookById(id);
        if (book.isBorrowedStatus()) {
            throw new BookStateConflictException("Book with ID " + id + " is already borrowed");
        }

        book.setBorrowedStatus(true);
        return mapToResponse(bookRepository.save(book));
    }

    public BookResponse returnBook(Long id) {
        Book book = findBookById(id);
        if (!book.isBorrowedStatus()) {
            throw new BookStateConflictException("Book with ID " + id + " is not borrowed");
        }

        book.setBorrowedStatus(false);
        return mapToResponse(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        bookRepository.delete(findBookById(id));
    }

    public List<BookResponse> findByAuthor(String author) {
        return bookRepository.findByAuthor(author).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title.strip()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> findAvailableBooks() {
        return bookRepository.findByBorrowedStatusFalse().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> findBooksPublishedAfter(int year) {
        return bookRepository.findByPublicationYearGreaterThan(year).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public long countBooks() {
        return bookRepository.count();
    }

    private BookResponse mapToResponse(Book book) {
        boolean borrowedStatus = book.isBorrowedStatus();
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getPublicationYear(),
                !borrowedStatus,
                borrowedStatus
        );
    }

    private Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }
}
