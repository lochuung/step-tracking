package hcmute.edu.vn.huuloc.steptracking.controller;

import android.content.Context;
import android.content.SharedPreferences;

import hcmute.edu.vn.huuloc.steptracking.listener.StepTracker;
import hcmute.edu.vn.huuloc.steptracking.model.StepData;
import hcmute.edu.vn.huuloc.steptracking.repository.StepDataRepository;
import hcmute.edu.vn.huuloc.steptracking.repository.StepDataRepositoryImpl;
import lombok.Setter;

public class StepTrackingController {
    private static final String PREFS_NAME = "StepTrackingPrefs";
    private static final String CURRENT_STEP_COUNT = "current_step_count";

    private final StepTracker stepTracker;
    private final StepDataRepository stepDataRepository;
    private final Context context;
    @Setter
    private StepTracker.StepUpdateListener stepUpdateListener;

    public StepTrackingController(Context context) {
        this.context = context;
        this.stepDataRepository = new StepDataRepositoryImpl(context);
        
        // Create the step tracker with our custom listener
        this.stepTracker = new StepTracker(context, new StepTracker.StepUpdateListener() {
            @Override
            public void onStepChanged(int stepCount) {
                // Save current step count to preferences for persistence
                saveStepCountToPrefs(stepCount);
                
                // Forward to any external listeners (like the service)
                if (stepUpdateListener != null) {
                    stepUpdateListener.onStepChanged(stepCount);
                }
            }
        });
        
        // Restore any saved step count
        int savedSteps = getSavedStepCount();
        if (savedSteps > 0) {
            stepTracker.setStepCount(savedSteps);
        }
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
        saveStepCountToPrefs(0);
    }

    public int getCurrentStepCount() {
        return stepTracker.getStepCount();
    }

    public void setStepCount(int steps) {
        stepTracker.setStepCount(steps);
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
    
    private void saveStepCountToPrefs(int count) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(CURRENT_STEP_COUNT, count).apply();
    }
    
    public int getSavedStepCount() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(CURRENT_STEP_COUNT, 0);
    }
}
