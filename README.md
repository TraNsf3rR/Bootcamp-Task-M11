# Practical Task: Library Book Tracker API

Starter project for Data Architecture practical task: Build a REST API for managing books in a library using Spring Boot, Spring Data JPA and an H2 database.

## Task
Complete all of the 'TODO' portions of the code. The finished application should compile and all of the controller endpoints should be functional.

## Behavioural Requirements
- Books can be created
- All books can be retrieved
- A single book can be retrieved by ID
- Books can be deleted
- Books can be filtered and searched by their fields.
- Controller delegates to Service, which delegates to Repository
- DTOs are used for requests and responses

## API Endpoints

| Method | Path | Behavior |
| --- | --- | --- |
| `POST` | `/api/books` | Creates a book and returns `201 Created`; the database generates its ID. |
| `GET` | `/api/books` | Returns all books. |
| `GET` | `/api/books/{id}` | Returns a book, or `404 Not Found` if the ID does not exist. |
| `PUT` | `/api/books/{id}` | Updates a book, or returns `404 Not Found` if the ID does not exist. |
| `POST` | `/api/books/{id}/borrow` | Borrows an available book and returns the updated book; returns `404 Not Found` if missing or `409 Conflict` if already borrowed. |
| `POST` | `/api/books/{id}/return` | Returns a borrowed book and returns the updated book; returns `404 Not Found` if missing or `409 Conflict` if already available. |
| `DELETE` | `/api/books/{id}` | Deletes a book and returns `204 No Content`, or `404 Not Found` if the ID does not exist. |
| `GET` | `/api/books/author/{author}` | Returns books with an exact author match. |
| `GET` | `/api/books/search?title={query}` | Returns books whose titles contain the query, ignoring case. |
| `GET` | `/api/books/available` | Returns available books. |
| `GET` | `/api/books/published-after/{year}` | Returns books published strictly after the given year. |
| `GET` | `/api/books/count` | Returns the total number of books. |

Create and update requests require nonblank titles and authors, as well as a publication year. Update requests must also include `available`; `false` is a valid value. The genre is optional. Invalid request bodies return `400 Bad Request`.

The entity stores `borrowedStatus`; `available` in responses is its inverse. New books start available and not borrowed.
# Bootcamp-Task-M11
