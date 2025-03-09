package hcmute.edu.vn.huuloc.steptracking.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import hcmute.edu.vn.huuloc.steptracking.model.entity.StepData;
import hcmute.edu.vn.huuloc.steptracking.view.service.StepTrackingService;
import hcmute.edu.vn.huuloc.steptracking.view.viewmodel.StepTrackingViewModel;

public class MainActivity extends AppCompatActivity {
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
    private StepTrackingViewModel viewModel;
    private boolean isTracking = false;

    @SuppressLint("DefaultLocale")
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

        // Initialize controller
        stepTrackingController = new StepTrackingController(this);

        // Initialize ViewModel
        viewModel = StepTrackingViewModel.getInstance();

        // Check if service is already running and get stored step count
        isTracking = StepTrackingService.isServiceRunning();
        int currentSteps = stepTrackingController.getSavedStepCount();
        if (currentSteps > 0) {
            viewModel.updateStepCount(currentSteps);
            viewModel.updateDistance(currentSteps);
        }

        // Update UI with any saved steps
        updateStatsUI();
        updateTrackingUI(isTracking);

        // Lắng nghe khi bước chân thay đổi
        viewModel.getStepCount().observe(this, stepCount -> {
            textViewStepCount.setText(String.valueOf(stepCount));
        });

        // Lắng nghe khi quãng đường thay đổi
        viewModel.getDistance().observe(this, distance -> {
            textViewTodayTotal.setText(String.format("%.2f km", distance));
        });

        // Set up button click listeners
        setupButtonListeners();

        // Request necessary permissions
        checkAndRequestPermissions();
    }

    private void setupButtonListeners() {
        buttonStartTracking.setOnClickListener(v -> startTracking());

        buttonStopTracking.setOnClickListener(v -> stopTracking());
    }

    private void startTracking() {
        if (!isTracking) {
            // Get current step count from controller
            int currentSteps = stepTrackingController.getCurrentStepCount();

            // Start the foreground service for step tracking
            Intent serviceIntent = new Intent(this, StepTrackingService.class);
            serviceIntent.setAction(StepTrackingService.ACTION_START_FOREGROUND_SERVICE);
            serviceIntent.putExtra(StepTrackingService.EXTRA_STEP_COUNT, currentSteps);

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

    @SuppressLint("DefaultLocale")
    private void updateStatsUI() {
        StepData stepData = stepTrackingController.getTodayStepData();
        textViewTotalSteps.setText(String.valueOf(stepData.getSteps()));
        textViewTodayTotal.setText(String.format("%.2f km", stepData.getDistance()));
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

        // Update stats
        updateStatsUI();
    }
}