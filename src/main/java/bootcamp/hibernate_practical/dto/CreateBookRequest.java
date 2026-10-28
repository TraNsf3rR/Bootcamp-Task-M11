package bootcamp.hibernate_practical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateBookRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String author;

    private String genre;

    @NotNull
    private Integer publicationYear;
}
