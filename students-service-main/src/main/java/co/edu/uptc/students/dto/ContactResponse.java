package co.edu.uptc.students.dto;

public record ContactResponse(
        Long id,
        Long studentId,
        String address,
        String phone,
        String emergencyContactName,
        String emergencyContactPhone) {
}