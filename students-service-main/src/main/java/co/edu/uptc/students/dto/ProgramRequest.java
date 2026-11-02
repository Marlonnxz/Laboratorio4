package co.edu.uptc.students.dto;

import jakarta.validation.constraints.NotBlank;

public record ProgramRequest(

        @NotBlank(message = "name is mandatory")
        String name,

        @NotBlank(message = "code is mandatory")
        String code) {
}