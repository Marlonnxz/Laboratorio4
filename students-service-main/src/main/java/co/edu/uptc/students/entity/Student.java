package co.edu.uptc.students.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "photo_blob_name")
    private String photoBlobName;

    @Column(name = "photo_original_name")
    private String photoOriginalName;

    @Column(name = "photo_content_type")
    private String photoContentType;

    @Column(name = "photo_size")
    private Long photoSize;

    @Column(name = "photo_uploaded_at")
    private java.time.LocalDateTime photoUploadedAt;

    public Student() {
    }

    public Student(Long id, String firstName, String lastName, String email, Program program) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.program = program;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public String getPhotoBlobName() {
        return photoBlobName;
    }

    public void setPhotoBlobName(String photoBlobName) {
        this.photoBlobName = photoBlobName;
    }

    public String getPhotoOriginalName() {
        return photoOriginalName;
    }

    public void setPhotoOriginalName(String photoOriginalName) {
        this.photoOriginalName = photoOriginalName;
    }

    public String getPhotoContentType() {
        return photoContentType;
    }

    public void setPhotoContentType(String photoContentType) {
        this.photoContentType = photoContentType;
    }

    public Long getPhotoSize() {
        return photoSize;
    }

    public void setPhotoSize(Long photoSize) {
        this.photoSize = photoSize;
    }

    public java.time.LocalDateTime getPhotoUploadedAt() {
        return photoUploadedAt;
    }

    public void setPhotoUploadedAt(java.time.LocalDateTime photoUploadedAt) {
        this.photoUploadedAt = photoUploadedAt;
    }
}