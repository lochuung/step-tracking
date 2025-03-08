package hcmute.edu.vn.huuloc.steptracking.repository;

import java.util.List;

import hcmute.edu.vn.huuloc.steptracking.model.StepData;

public interface StepDataRepository {
    void saveStepData(StepData stepData);

    void updateStepData(StepData stepData);

    void deleteStepData(Long id);

    StepData getStepData(Long id);

    List<StepData> getAllStepData();

    StepData getStepDataByDate(String date);

    int getTotalSteps();
}
