package bootcamp.hibernate_practical.controller;

import bootcamp.hibernate_practical.dto.BookResponse;
import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.dto.UpdateBookRequest;
import bootcamp.hibernate_practical.exception.BookNotFoundException;
import bootcamp.hibernate_practical.exception.BookExceptionHandler;
import bootcamp.hibernate_practical.exception.BookStateConflictException;
import bootcamp.hibernate_practical.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(BookExceptionHandler.class)
class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void createsBookWithCreatedStatusAndRequestDto() throws Exception {
        BookResponse response = sampleBook(true);
        given(bookService.createBook(any(CreateBookRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"The Hobbit","author":"J.R.R. Tolkien","genre":"Fantasy","publicationYear":1937}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The Hobbit"));

        ArgumentCaptor<CreateBookRequest> requestCaptor = ArgumentCaptor.forClass(CreateBookRequest.class);
        verify(bookService).createBook(requestCaptor.capture());
        assertEquals("The Hobbit", requestCaptor.getValue().getTitle());
        assertEquals(Integer.valueOf(1937), requestCaptor.getValue().getPublicationYear());
    }

    @Test
    void retrievesAllBooksAndOneBookById() throws Exception {
        BookResponse response = sampleBook(true);
        given(bookService.getAllBooks()).willReturn(List.of(response));
        given(bookService.getBookById(1L)).willReturn(response);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"));
    }

    @Test
    void updatesBookAndReturnsUpdatedDto() throws Exception {
        given(bookService.updateBook(eq(1L), any(UpdateBookRequest.class))).willReturn(sampleBook(false));

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"The Hobbit","author":"J.R.R. Tolkien","genre":"Fantasy","publicationYear":1937,"available":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.borrowedStatus").value(true));

        ArgumentCaptor<UpdateBookRequest> requestCaptor = ArgumentCaptor.forClass(UpdateBookRequest.class);
        verify(bookService).updateBook(eq(1L), requestCaptor.capture());
        assertFalse(requestCaptor.getValue().getAvailable());
    }

    @Test
    void deletesBookWithNoContentStatus() throws Exception {
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(bookService).deleteBook(1L);
    }

    @Test
    void borrowsAndReturnsBooks() throws Exception {
        given(bookService.borrowBook(1L)).willReturn(sampleBook(false));
        given(bookService.returnBook(1L)).willReturn(sampleBook(true));

        mockMvc.perform(post("/api/books/1/borrow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrowedStatus").value(true))
                .andExpect(jsonPath("$.available").value(false));
        mockMvc.perform(post("/api/books/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrowedStatus").value(false))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void routesAuthorAvailabilityYearAndCountQueries() throws Exception {
        BookResponse response = sampleBook(true);
        given(bookService.findByAuthor("JaneAusten")).willReturn(List.of(response));
        given(bookService.findAvailableBooks()).willReturn(List.of(response));
        given(bookService.findBooksPublishedAfter(2000)).willReturn(List.of(response));
        given(bookService.countBooks()).willReturn(3L);

        mockMvc.perform(get("/api/books/author/JaneAusten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(get("/api/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(true));
        mockMvc.perform(get("/api/books/published-after/2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(get("/api/books/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void searchesByPartialTitleAndRejectsBlankSearchTerms() throws Exception {
        given(bookService.searchBooksByTitle("hobbit")).willReturn(List.of(sampleBook(true)));

        mockMvc.perform(get("/api/books/search").param("title", "hobbit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));
        mockMvc.perform(get("/api/books/search").param("title", "  "))
                .andExpect(status().isBadRequest());

        verify(bookService).searchBooksByTitle("hobbit");
    }

    @Test
    void mapsMissingBooksToNotFoundForGetUpdateAndDelete() throws Exception {
        given(bookService.getBookById(999L)).willThrow(new BookNotFoundException(999L));
        given(bookService.updateBook(eq(999L), any(UpdateBookRequest.class)))
                .willThrow(new BookNotFoundException(999L));
        given(bookService.borrowBook(999L)).willThrow(new BookNotFoundException(999L));
        given(bookService.returnBook(999L)).willThrow(new BookNotFoundException(999L));
        doThrow(new BookNotFoundException(999L)).when(bookService).deleteBook(999L);

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Title","author":"Author","genre":"Genre","publicationYear":2024,"available":true}
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/books/999/borrow"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/books/999/return"))
                .andExpect(status().isNotFound());
    }

    @Test
    void mapsInvalidBorrowAndReturnTransitionsToConflict() throws Exception {
        given(bookService.borrowBook(1L))
                .willThrow(new BookStateConflictException("Book with ID 1 is already borrowed"));
        given(bookService.returnBook(2L))
                .willThrow(new BookStateConflictException("Book with ID 2 is not borrowed"));

        mockMvc.perform(post("/api/books/1/borrow"))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/books/2/return"))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidCreateRequests() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","author":"Author","publicationYear":2020}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Title","author":"","publicationYear":2020}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Title","author":"Author"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void rejectsUpdateRequestsWithMissingRequiredValues() throws Exception {
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Title","author":"Author","publicationYear":2024}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Title","author":"Author","available":false}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    private static BookResponse sampleBook(boolean available) {
        return new BookResponse(1L, "The Hobbit", "J.R.R. Tolkien", "Fantasy", 1937, available, !available);
    }
}
