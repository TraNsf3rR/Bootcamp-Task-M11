package bootcamp.hibernate_practical.controller;

import bootcamp.hibernate_practical.dto.BookResponse;
import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.dto.UpdateBookRequest;
import bootcamp.hibernate_practical.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void requiresValidBasicAuthForGetRequests() throws Exception {
        given(bookService.getAllBooks()).willReturn(List.of(sampleBook(true)));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/books")
                        .header("Authorization", basicAuth("user", "wrong")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/books")
                        .header("Authorization", basicAuth("user", "user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));
        mockMvc.perform(get("/api/books")
                        .header("Authorization", basicAuth("admin", "admin")))
                .andExpect(status().isOk());

        verify(bookService, times(2)).getAllBooks();
    }

    @Test
    void deniesUserForEveryNonGetEndpoint() throws Exception {
        String authorization = basicAuth("user", "user");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"The Hobbit","author":"J.R.R. Tolkien","genre":"Fantasy","publicationYear":1937}
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/books")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"The Hobbit","author":"J.R.R. Tolkien","genre":"Fantasy","publicationYear":1937}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/books/1")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"The Hobbit","author":"J.R.R. Tolkien","genre":"Fantasy","publicationYear":1937,"available":true}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/books/1/borrow")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/books/1/return")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/books/1")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    void allowsAdminWritesWithoutCsrfTokens() throws Exception {
        String authorization = basicAuth("admin", "admin");
        given(bookService.createBook(any(CreateBookRequest.class))).willReturn(sampleBook(true));
        given(bookService.updateBook(eq(1L), any(UpdateBookRequest.class))).willReturn(sampleBook(true));
        given(bookService.borrowBook(1L)).willReturn(sampleBook(false));
        given(bookService.returnBook(1L)).willReturn(sampleBook(true));

        mockMvc.perform(post("/api/books")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"The Hobbit","author":"J.R.R. Tolkien","genre":"Fantasy","publicationYear":1937}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/books/1")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"The Hobbit","author":"J.R.R. Tolkien","genre":"Fantasy","publicationYear":1937,"available":true}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/books/1/borrow")
                        .header("Authorization", authorization))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/books/1/return")
                        .header("Authorization", authorization))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/books/1")
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());

        verify(bookService).createBook(any(CreateBookRequest.class));
        verify(bookService).updateBook(eq(1L), any(UpdateBookRequest.class));
        verify(bookService).borrowBook(1L);
        verify(bookService).returnBook(1L);
        verify(bookService).deleteBook(1L);
    }

    private static String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private static BookResponse sampleBook(boolean available) {
        return new BookResponse(1L, "The Hobbit", "J.R.R. Tolkien", "Fantasy", 1937, available, !available);
    }
}
