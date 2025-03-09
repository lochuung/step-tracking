package hcmute.edu.vn.huuloc.steptracking.repository;

import java.time.LocalDate;
import java.util.List;

import hcmute.edu.vn.huuloc.steptracking.model.entity.StepData;

public interface StepDataRepository {
    long saveStepData(StepData stepData);

    void updateStepData(StepData stepData);

    void deleteStepData(Long id);

    StepData getStepData(Long id);

    List<StepData> getAllStepData();

    StepData getStepDataByDate(LocalDate date);

    int getTotalSteps();
}
