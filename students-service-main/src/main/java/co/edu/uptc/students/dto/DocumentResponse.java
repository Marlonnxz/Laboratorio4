package co.edu.uptc.students.dto;

import java.time.LocalDate;

public record DocumentResponse(
        Long id,
        Long studentId,
        String type,
        String number,
        LocalDate issueDate) {
}