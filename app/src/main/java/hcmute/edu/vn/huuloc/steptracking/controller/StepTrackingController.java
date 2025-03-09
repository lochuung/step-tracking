package hcmute.edu.vn.huuloc.steptracking.controller;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;

import hcmute.edu.vn.huuloc.steptracking.listener.StepTracker;
import hcmute.edu.vn.huuloc.steptracking.model.entity.StepData;
import hcmute.edu.vn.huuloc.steptracking.repository.StepDataRepository;
import hcmute.edu.vn.huuloc.steptracking.repository.StepDataRepositoryImpl;
import lombok.Setter;

public class StepTrackingController {
    private static final String PREFS_NAME = "StepTrackingPrefs";
    private static final String CURRENT_STEP_COUNT = "current_step_count";
    // Constants for calculations
    private static final double STEP_LENGTH_METERS = 0.762; // Average step length in meters
    private static final double CALORIES_PER_STEP = 0.04; // Average calories burned per step

    private final StepTracker stepTracker;
    private final StepDataRepository stepDataRepository;
    private final Context context;
    @Setter
    private StepTracker.StepUpdateListener stepUpdateListener;

    public StepTrackingController(Context context) {
        this.context = context;
        this.stepDataRepository = new StepDataRepositoryImpl(context);

        // Create the step tracker with our custom listener
        this.stepTracker = new StepTracker(context, stepCount -> {
            // Save current step count to preferences for persistence
            saveStepCountToPrefs(stepCount);

            // Forward to any external listeners (like the service)
            if (stepUpdateListener != null) {
                stepUpdateListener.onStepChanged(stepCount);
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

    public int getCurrentStepCount() {
        return stepTracker.getStepCount();
    }

    public void setStepCount(int steps) {
        stepTracker.setStepCount(steps);
    }

    private void saveStepsToDatabase(int steps) {
        if (steps <= 0) return;

        double distance = calculateDistance(steps);
        int calories = calculateCalories(steps);
        StepData stepData = stepDataRepository.getStepDataByDate(LocalDate.now());

        if (stepData == null) {
            stepData = StepData.builder()
                    .steps(steps)
                    .distance(distance)
                    .date(LocalDate.now())
                    .calories(calories)
                    .build();
            stepDataRepository.saveStepData(stepData);
        } else {
            stepData.setSteps(steps);
            stepData.setDistance(distance);
            stepData.setCalories(calories);
            stepDataRepository.updateStepData(stepData);
        }
    }

    // Calculate distance in kilometers
    private double calculateDistance(int steps) {
        return (steps * STEP_LENGTH_METERS) / 1000.0;
    }

    // Calculate calories burned
    private int calculateCalories(int steps) {
        return (int) (steps * CALORIES_PER_STEP);
    }

    private void saveStepCountToPrefs(int count) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(CURRENT_STEP_COUNT, count).apply();
    }

    public int getSavedStepCount() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(CURRENT_STEP_COUNT, 0);
    }

    public StepData getTodayStepData() {
        return stepDataRepository.getStepDataByDate(LocalDate.now());
    }
}
