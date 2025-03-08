package hcmute.edu.vn.huuloc.steptracking.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import lombok.Getter;

public class StepTracker implements SensorEventListener {
    private static final String TAG = "StepTracker";
    private static final int ACCEL_RING_SIZE = 50;
    private static final int STEP_THRESHOLD = 12;

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    @Getter
    private int stepCount = 0;
    private final double[] accelRingX = new double[ACCEL_RING_SIZE];
    private final double[] accelRingY = new double[ACCEL_RING_SIZE];
    private final double[] accelRingZ = new double[ACCEL_RING_SIZE];
    private int accelRingCounter = 0;
    private double lastMagnitude = 0;
    private boolean firstStep = true;

    // Kalman filter variables
    private double kalmanEstimate = 0; // Current state estimate
    private double kalmanErrorCovariance = 1; // Current error covariance
    private double kalmanProcessNoise = 0.01; // Process noise variance
    private double kalmanMeasurementNoise = 0.1; // Measurement noise variance
    private double kalmanGain = 0; // Kalman gain

    private final StepUpdateListener stepUpdateListener;

    public interface StepUpdateListener {
        void onStepChanged(int stepCount);
    }

    public StepTracker(Context context) {
        this(context, null);
    }

    public StepTracker(Context context, StepUpdateListener listener) {
        this.stepUpdateListener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Initialize Kalman filter with default values
        initKalmanFilter();
    }

    private void initKalmanFilter() {
        kalmanEstimate = 0;
        kalmanErrorCovariance = 1;
    }

    public void start() {
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Step tracking started");
        } else {
            Log.e(TAG, "Accelerometer sensor not available");
        }
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Step tracking stopped");
        }
    }

    public void resetSteps() {
        stepCount = 0;
        notifyStepUpdate();
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
        notifyStepUpdate();
    }

    private void notifyStepUpdate() {
        if (stepUpdateListener != null) {
            stepUpdateListener.onStepChanged(stepCount);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Add latest acceleration to ring buffer
        accelRingX[accelRingCounter] = x;
        accelRingY[accelRingCounter] = y;
        accelRingZ[accelRingCounter] = z;
        accelRingCounter = (accelRingCounter + 1) % ACCEL_RING_SIZE;

        // Calculate acceleration vector magnitude
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        
        // Apply Kalman filter to the magnitude
        double filteredMagnitude = applyKalmanFilter(magnitude);

        // Calculate average magnitude for smoothing
        double averageMagnitude = calculateAverageMagnitude();

        // Step detection logic based on filtered magnitude changes
        if (detectStep(filteredMagnitude, averageMagnitude)) {
            stepCount++;
            notifyStepUpdate();
        }

        lastMagnitude = filteredMagnitude;
    }

    private double applyKalmanFilter(double measurement) {
        // Prediction step
        // State prediction (no control input, so just use previous state)
        double predictedEstimate = kalmanEstimate;
        
        // Error covariance prediction
        double predictedErrorCovariance = kalmanErrorCovariance + kalmanProcessNoise;
        
        // Update step
        // Calculate Kalman gain
        kalmanGain = predictedErrorCovariance / (predictedErrorCovariance + kalmanMeasurementNoise);
        
        // Update estimate with measurement
        kalmanEstimate = predictedEstimate + kalmanGain * (measurement - predictedEstimate);
        
        // Update error covariance
        kalmanErrorCovariance = (1 - kalmanGain) * predictedErrorCovariance;
        
        return kalmanEstimate;
    }

    private double calculateAverageMagnitude() {
        double sum = 0;
        for (int i = 0; i < ACCEL_RING_SIZE; i++) {
            sum += Math.sqrt(accelRingX[i] * accelRingX[i] +
                    accelRingY[i] * accelRingY[i] +
                    accelRingZ[i] * accelRingZ[i]);
        }
        return sum / ACCEL_RING_SIZE;
    }

    private boolean detectStep(double currentMagnitude, double averageMagnitude) {
        if (firstStep) {
            firstStep = false;
            return false;
        }

        // Step is detected when filtered magnitude exceeds the average by threshold
        // and the previous magnitude was lower
        boolean isStep = currentMagnitude > averageMagnitude + STEP_THRESHOLD &&
                lastMagnitude <= averageMagnitude + STEP_THRESHOLD;

        if (isStep) {
            Log.d(TAG, "Step detected with filtered magnitude: " + currentMagnitude);
        }

        return isStep;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }
}