package bootcamp.hibernate_practical.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BookResponse {
    public Long id;
    public String title;
    public String author;
    public String genre;
    public int publicationYear;
    public boolean available;
    public boolean borrowedStatus;
}
