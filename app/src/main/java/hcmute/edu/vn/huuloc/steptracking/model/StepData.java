package hcmute.edu.vn.huuloc.steptracking.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepData {
    private Long id;
    private Integer steps;
    private Double distance;
    private String date;
}
