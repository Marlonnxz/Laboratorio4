package edu.uptc.enrollments.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Column(name = "value", nullable = false, precision = 3, scale = 2)
    private BigDecimal value;

    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    public Grade() {
    }

    public Grade(Enrollment enrollment, String type, BigDecimal value, BigDecimal percentage) {
        this.enrollment = enrollment;
        this.type = type;
        this.value = value;
        this.percentage = percentage;
    }

    public Grade(Long id, Enrollment enrollment, String type, BigDecimal value, BigDecimal percentage) {
        this.id = id;
        this.enrollment = enrollment;
        this.type = type;
        this.value = value;
        this.percentage = percentage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grade grade = (Grade) o;
        return Objects.equals(id, grade.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Grade{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", value=" + value +
                ", percentage=" + percentage +
                '}';
    }
}
