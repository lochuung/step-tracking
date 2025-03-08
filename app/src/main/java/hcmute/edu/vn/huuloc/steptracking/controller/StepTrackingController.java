package hcmute.edu.vn.huuloc.steptracking.controller;

import android.content.Context;

import hcmute.edu.vn.huuloc.steptracking.listener.StepTracker;
import hcmute.edu.vn.huuloc.steptracking.model.StepData;
import hcmute.edu.vn.huuloc.steptracking.repository.StepDataRepository;
import hcmute.edu.vn.huuloc.steptracking.repository.StepDataRepositoryImpl;
import lombok.Setter;

public class StepTrackingController implements StepTracker.StepUpdateListener {
    private Context context;
    private final StepTracker stepTracker;
    private final StepDataRepository stepDataRepository;
    @Setter
    private StepUpdateListener stepUpdateListener;

    public interface StepUpdateListener {
        void onStepCountUpdated(int stepCount);
    }

    public StepTrackingController(Context context) {
        this.context = context;
        this.stepDataRepository = new StepDataRepositoryImpl(context);
        this.stepTracker = new StepTracker(context, this);
    }

    public void startTracking() {
        stepTracker.start();
    }

    public void stopTracking() {
        stepTracker.stop();
        saveStepsToDatabase(stepTracker.getStepCount());
    }

    public void resetSteps() {
        stepTracker.resetSteps();
    }

    public int getCurrentStepCount() {
        return stepTracker.getStepCount();
    }

    @Override
    public void onStepChanged(int stepCount) {
        if (stepUpdateListener != null) {
            stepUpdateListener.onStepCountUpdated(stepCount);
        }
    }

    private void saveStepsToDatabase(int steps) {
        if (steps > 0) {
            StepData stepData = StepData.builder()
                    .steps(steps)
                    .build();
            stepDataRepository.saveStepData(stepData);
        }
    }

    public int getTotalSteps() {
        return stepDataRepository.getTotalSteps();
    }
}
