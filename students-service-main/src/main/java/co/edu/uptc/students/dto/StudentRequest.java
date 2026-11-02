package co.edu.uptc.students.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentRequest(

        @NotBlank(message = "firstName is mandatory")
        @Size(min = 2, max = 80, message = "firstName must be between 2 and 80 characters")
        String firstName,

        @NotBlank(message = "lastName is mandatory")
        @Size(min = 2, max = 80, message = "lastName must be between 2 and 80 characters")
        String lastName,

        @NotBlank(message = "email is mandatory")
        @Email(message = "email must have a valid format")
        String email,

        @NotNull(message = "programId is mandatory")
        Long programId) {
}