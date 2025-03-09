package hcmute.edu.vn.huuloc.steptracking.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalDate;

import hcmute.edu.vn.huuloc.steptracking.util.DateConverter;

@Entity(tableName = "step_data")
@TypeConverters(DateConverter.class)
public class StepData {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Integer steps;
    private Double distance;
    private LocalDate date;
    private Integer calories;

    // Default constructor
    public StepData() {}

    // All-args constructor
    public StepData(Long id, Integer steps, Double distance, LocalDate date, Integer calories) {
        this.id = id;
        this.steps = steps;
        this.distance = distance;
        this.date = date;
        this.calories = calories;
    }

    // Builder static method (simplified version of Lombok's builder)
    public static StepDataBuilder builder() {
        return new StepDataBuilder();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    // Builder class
    public static class StepDataBuilder {
        private Long id;
        private Integer steps;
        private Double distance;
        private LocalDate date;
        private Integer calories;

        public StepDataBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public StepDataBuilder steps(Integer steps) {
            this.steps = steps;
            return this;
        }

        public StepDataBuilder distance(Double distance) {
            this.distance = distance;
            return this;
        }

        public StepDataBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public StepDataBuilder calories(Integer calories) {
            this.calories = calories;
            return this;
        }

        public StepData build() {
            return new StepData(id, steps, distance, date, calories);
        }
    }
}
