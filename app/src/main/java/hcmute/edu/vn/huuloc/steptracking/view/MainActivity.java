package hcmute.edu.vn.huuloc.steptracking.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import hcmute.edu.vn.huuloc.steptracking.R;
import hcmute.edu.vn.huuloc.steptracking.controller.StepTrackingController;
import hcmute.edu.vn.huuloc.steptracking.controller.StepTrackingService;
import hcmute.edu.vn.huuloc.steptracking.repository.StepDataRepository;
import hcmute.edu.vn.huuloc.steptracking.repository.StepDataRepositoryImpl;

public class MainActivity extends AppCompatActivity implements StepTrackingController.StepUpdateListener {
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 100;
    private static final int PERMISSION_REQUEST_POST_NOTIFICATION = 101;

    private TextView textViewStepCount;
    private TextView textViewStatus;
    private TextView textViewTotalSteps;
    private TextView textViewTodayTotal;
    private MaterialButton buttonStartTracking;
    private MaterialButton buttonStopTracking;
    private MaterialButton buttonReset;

    private StepTrackingController stepTrackingController;
    private StepDataRepository stepDataRepository;
    private boolean isTracking = false;

    // Average stride length in meters - you can make this configurable
    private static final double STRIDE_LENGTH = 0.762;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize UI components
        textViewStepCount = findViewById(R.id.textViewStepCount);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewTotalSteps = findViewById(R.id.textViewTotalSteps);
        textViewTodayTotal = findViewById(R.id.textViewTodayTotal);
        buttonStartTracking = findViewById(R.id.buttonStartTracking);
        buttonStopTracking = findViewById(R.id.buttonStopTracking);
        buttonReset = findViewById(R.id.buttonReset);

        // Initialize database helper
        stepDataRepository = new StepDataRepositoryImpl(this);

        // Initialize controller
        stepTrackingController = new StepTrackingController(this);
        stepTrackingController.setStepUpdateListener(this);

        // Update UI with any saved steps
        updateStatsUI();

        // Check if service is already running
        isTracking = StepTrackingService.isServiceRunning();
        updateTrackingUI(isTracking);

        // Set up button click listeners
        setupButtonListeners();

        // Request necessary permissions
        checkAndRequestPermissions();
    }

    private void setupButtonListeners() {
        buttonStartTracking.setOnClickListener(v -> startTracking());

        buttonStopTracking.setOnClickListener(v -> stopTracking());

        buttonReset.setOnClickListener(v -> {
            stepTrackingController.resetSteps();
            updateStepCountUI(0);
        });
    }

    private void startTracking() {
        if (!isTracking) {
            // Start the foreground service for step tracking
            Intent serviceIntent = new Intent(this, StepTrackingService.class);
            serviceIntent.setAction(StepTrackingService.ACTION_START_FOREGROUND_SERVICE);

            startForegroundService(serviceIntent);

            isTracking = true;
            updateTrackingUI(true);
            Toast.makeText(this, "Step tracking started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopTracking() {
        if (isTracking) {
            // Stop the foreground service
            Intent serviceIntent = new Intent(this, StepTrackingService.class);
            serviceIntent.setAction(StepTrackingService.ACTION_STOP_FOREGROUND_SERVICE);

            startForegroundService(serviceIntent);

            isTracking = false;
            updateTrackingUI(false);
            Toast.makeText(this, "Step tracking stopped", Toast.LENGTH_SHORT).show();

            // Update stats after stopping
            updateStatsUI();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateTrackingUI(boolean tracking) {
        buttonStartTracking.setEnabled(!tracking);
        buttonStopTracking.setEnabled(tracking);

        if (tracking) {
            textViewStatus.setText("TRACKING");
            textViewStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.status_background_active));
        } else {
            textViewStatus.setText("NOT TRACKING");
            textViewStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.status_background_inactive));
        }
    }

    @Override
    public void onStepCountUpdated(int stepCount) {
        updateStepCountUI(stepCount);
        updateDistanceUI(stepCount);
    }

    private void updateStepCountUI(int stepCount) {
        textViewStepCount.setText(String.valueOf(stepCount));
    }

    private void updateStatsUI() {
        int totalSteps = stepDataRepository.getTotalSteps();
        textViewTotalSteps.setText(String.valueOf(totalSteps));

        // Update distance calculation (km = steps * stride length in meters / 1000)
        updateDistanceUI(totalSteps);
    }

    @SuppressLint("DefaultLocale")
    private void updateDistanceUI(int steps) {
        double distanceInKm = (steps * STRIDE_LENGTH) / 1000;
        textViewTodayTotal.setText(String.format("%.2f", distanceInKm));
    }

    private void checkAndRequestPermissions() {
        // Check and request ACTIVITY_RECOGNITION permission for step counter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
            }
        }

        // Check and request POST_NOTIFICATION permission for foreground service on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_POST_NOTIFICATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Step tracking permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Step tracking permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_POST_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Service may not work properly.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if service status changed
        boolean serviceRunning = StepTrackingService.isServiceRunning();
        if (isTracking != serviceRunning) {
            isTracking = serviceRunning;
            updateTrackingUI(isTracking);
        }

        // Update step count from service if it's running
        if (isTracking) {
            int currentSteps = StepTrackingService.getCurrentStepCount();
            updateStepCountUI(currentSteps);
            updateDistanceUI(currentSteps);
        }

        // Update stats
        updateStatsUI();
    }
}