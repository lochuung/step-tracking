package hcmute.edu.vn.huuloc.steptracking.view.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StepTrackingViewModel extends ViewModel {

    // Create a singleton instance
    private static StepTrackingViewModel instance;
    private final MutableLiveData<Integer> stepCount = new MutableLiveData<>(0);
    private final MutableLiveData<Double> distance = new MutableLiveData<>(0.0);

    public static final double STRIDE_LENGTH = 0.762; // Độ dài sải chân trung bình (m)

    // Get singleton instance
    public static synchronized StepTrackingViewModel getInstance() {
        if (instance == null) {
            instance = new StepTrackingViewModel();
        }
        return instance;
    }

    public LiveData<Integer> getStepCount() {
        return stepCount;
    }

    public LiveData<Double> getDistance() {
        return distance;
    }

    public void updateStepCount(int newCount) {
        stepCount.setValue(newCount);
        updateDistance(newCount);
    }

    public void updateDistance(int steps) {
        double newDistance = (steps * STRIDE_LENGTH) / 1000; // Chuyển sang km
        distance.setValue(newDistance);
    }
}
