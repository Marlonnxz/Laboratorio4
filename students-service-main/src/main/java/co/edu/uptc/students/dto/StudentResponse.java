package co.edu.uptc.students.dto;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Long programId,
        String photoUrl,
        String photoOriginalName) {

    public StudentResponse(Long id, String firstName, String lastName, String email, Long programId) {
        this(id, firstName, lastName, email, programId, null, null);
    }
}
