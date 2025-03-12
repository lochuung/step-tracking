package hcmute.edu.vn.huuloc.steptracking.view.service;

import static hcmute.edu.vn.huuloc.steptracking.view.viewmodel.StepTrackingViewModel.STRIDE_LENGTH;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.huuloc.steptracking.R;
import hcmute.edu.vn.huuloc.steptracking.controller.StepTrackingController;
import hcmute.edu.vn.huuloc.steptracking.listener.StepTracker;
import hcmute.edu.vn.huuloc.steptracking.view.activity.MainActivity;
import hcmute.edu.vn.huuloc.steptracking.view.viewmodel.StepTrackingViewModel;
import lombok.Getter;

import java.time.LocalDate;

public class StepTrackingService extends Service implements StepTracker.StepUpdateListener {
    private static final String CHANNEL_ID = "FitnessAppStepTracking";
    private static final int NOTIFICATION_ID = 1;

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FROM_NOTIFICATION = "ACTION_STOP_FROM_NOTIFICATION";
    public static final String EXTRA_STEP_COUNT = "EXTRA_STEP_COUNT";

    @Getter
    private static boolean isServiceRunning = false;

    private StepTrackingController stepTrackingController;
    private NotificationManager notificationManager;
    private int currentStepCount = 0;

    // Average stride length in meters
    private int lastMilestoneSteps = 0;
    private static final int MILESTONE_STEP_THRESHOLD = 1000; // Save data every 1000 steps

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        stepTrackingController = new StepTrackingController(this);
        stepTrackingController.setStepUpdateListener(this);

        // Create notification channel for Android O and above
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (action != null) {
                switch (action) {
                    case ACTION_START_FOREGROUND_SERVICE:
                        // Check if there's a step count to restore
                        if (intent.hasExtra(EXTRA_STEP_COUNT)) {
                            int stepCount = intent.getIntExtra(EXTRA_STEP_COUNT, 0);
                            stepTrackingController.setStepCount(stepCount);
                            currentStepCount = stepCount;
                        } else {
                            // Otherwise, try to restore from saved preferences
                            currentStepCount = stepTrackingController.getSavedStepCount();
                        }
                        startForegroundService();
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                    case ACTION_STOP_FROM_NOTIFICATION:
                        stopForegroundService();
                        break;
                }
            }
        }

        // If service is killed, restart it
        return START_STICKY;
    }

    private void startForegroundService() {
        // Start step tracking
        stepTrackingController.startTracking();

        // Start foreground service with notification
        startForeground(NOTIFICATION_ID, buildNotification(currentStepCount));

        isServiceRunning = true;

        // Update the ViewModel with current steps
        updateViewModel(currentStepCount);
    }

    private void stopForegroundService() {
        // Save current step count before stopping
        currentStepCount = stepTrackingController.getCurrentStepCount();

        // Stop step tracking
        stepTrackingController.stopTracking();

        // Stop foreground service
        stopForeground(true);
        stopSelf();

        isServiceRunning = false;
    }

    @SuppressLint("DefaultLocale")
    private Notification buildNotification(int stepCount) {
        // Calculate distance
        double distanceInKm = (stepCount * STRIDE_LENGTH) / 1000;

        // Create intent for notification click
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Create intent for stop action
        Intent stopIntent = new Intent(this, StepTrackingService.class);
        stopIntent.setAction(ACTION_STOP_FROM_NOTIFICATION);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        // Create custom notification layout
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_layout);
        notificationLayout.setTextViewText(R.id.notification_title, "Step Tracker (Active)");
        notificationLayout.setTextViewText(R.id.notification_text,
                stepCount + " steps • " + String.format("%.2f", distanceInKm) + " km");
        notificationLayout.setOnClickPendingIntent(R.id.notification_stop, stopPendingIntent);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_footsteps)
                .setContentTitle("Step Tracker")
                .setContentText(stepCount + " steps • " + String.format("%.2f", distanceInKm) + " km")
                .setContentIntent(pendingIntent)
                .setCustomContentView(notificationLayout)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setColor(getResources().getColor(R.color.colorPrimary));

        return builder.build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Fitness App Step Tracking",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Shows the current step count");
        channel.enableVibration(false);
        channel.setShowBadge(true);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onStepChanged(int stepCount) {
        // Store current step count
        currentStepCount = stepCount;

        // Update notification with new step count
        notificationManager.notify(NOTIFICATION_ID, buildNotification(stepCount));

        // Update the ViewModel
        updateViewModel(stepCount);

        // Check if we've reached another milestone (e.g., every 1000 steps)
        if (stepCount - lastMilestoneSteps >= MILESTONE_STEP_THRESHOLD) {
            lastMilestoneSteps = stepCount;
        }
    }

    private void updateViewModel(int stepCount) {
        StepTrackingViewModel stepTrackingViewModel = StepTrackingViewModel.getInstance();
        stepTrackingViewModel.updateStepCount(stepCount);
        stepTrackingViewModel.updateDistance(stepCount);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
    }
}