package co.edu.uptc.students.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentRequest(

        @NotNull(message = "studentId is mandatory")
        Long studentId,

        @NotBlank(message = "type is mandatory")
        String type,

        @NotBlank(message = "number is mandatory")
        String number,

        @NotNull(message = "issueDate is mandatory")
        LocalDate issueDate) {
}