package edu.uptc.enrollments.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "period", nullable = false, length = 20)
    private String period;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EnrollmentStatus status;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Grade> grades = new ArrayList<>();

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnrollmentStatusHistory> statusHistory = new ArrayList<>();

    public Enrollment() {
    }

    public Enrollment(Long studentId, Long courseId, String period, EnrollmentStatus status, LocalDate enrollmentDate) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.period = period;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
    }

    public Enrollment(Long id, Long studentId, Long courseId, String period, EnrollmentStatus status, LocalDate enrollmentDate) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.period = period;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
    }

    public void addGrade(Grade grade) {
        this.grades.add(grade);
        grade.setEnrollment(this);
    }

    public void removeGrade(Grade grade) {
        this.grades.remove(grade);
        grade.setEnrollment(null);
    }

    public void addStatusHistory(EnrollmentStatusHistory history) {
        this.statusHistory.add(history);
        history.setEnrollment(this);
    }

    public void removeStatusHistory(EnrollmentStatusHistory history) {
        this.statusHistory.remove(history);
        history.setEnrollment(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public List<Grade> getGrades() {
        return grades;
    }

    public void setGrades(List<Grade> grades) {
        this.grades = grades;
    }

    public List<EnrollmentStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<EnrollmentStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", period='" + period + '\'' +
                ", status=" + status +
                ", enrollmentDate=" + enrollmentDate +
                '}';
    }
}
