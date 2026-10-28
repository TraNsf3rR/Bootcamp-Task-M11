package bootcamp.hibernate_practical.controller;

import bootcamp.hibernate_practical.dto.BookResponse;
import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.dto.UpdateBookRequest;
import bootcamp.hibernate_practical.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody CreateBookRequest createBookRequest) {
        return bookService.createBook(createBookRequest);
    }

    @GetMapping
    public List<BookResponse> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public BookResponse getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest updateBookRequest
    ) {
        return bookService.updateBook(id, updateBookRequest);
    }

    @PostMapping("/{id}/borrow")
    public BookResponse borrowBook(@PathVariable Long id) {
        return bookService.borrowBook(id);
    }

    @PostMapping("/{id}/return")
    public BookResponse returnBook(@PathVariable Long id) {
        return bookService.returnBook(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    @GetMapping("/author/{author}")
    public List<BookResponse> getBooksByAuthor(@PathVariable String author) {
        return bookService.findByAuthor(author);
    }

    @GetMapping("/search")
    public List<BookResponse> searchBooksByTitle(@RequestParam @NotBlank String title) {
        return bookService.searchBooksByTitle(title);
    }

    @GetMapping("/available")
    public List<BookResponse> getAvailableBooks() {
        return bookService.findAvailableBooks();
    }

    @GetMapping("/published-after/{year}")
    public List<BookResponse> getBooksPublishedAfter(@PathVariable int year) {
        return bookService.findBooksPublishedAfter(year);
    }

    @GetMapping("/count")
    public long getBookCount() {
        return bookService.countBooks();
    }
}
