package co.edu.uptc.students.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContactRequest(

        @NotNull(message = "studentId is mandatory")
        Long studentId,

        @NotBlank(message = "address is mandatory")
        String address,

        @NotBlank(message = "phone is mandatory")
        String phone,

        @NotBlank(message = "emergencyContactName is mandatory")
        String emergencyContactName,

        @NotBlank(message = "emergencyContactPhone is mandatory")
        String emergencyContactPhone) {
}