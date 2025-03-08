package hcmute.edu.vn.huuloc.steptracking.controller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.huuloc.steptracking.R;
import hcmute.edu.vn.huuloc.steptracking.listener.StepTracker;
import hcmute.edu.vn.huuloc.steptracking.view.MainActivity;
import lombok.Getter;


public class StepTrackingService extends Service implements StepTracker.StepUpdateListener {
    private static final String CHANNEL_ID = "FitnessAppStepTracking";
    private static final int NOTIFICATION_ID = 1;

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FROM_NOTIFICATION = "ACTION_STOP_FROM_NOTIFICATION";

    @Getter
    private static boolean isServiceRunning = false;
    @Getter
    private static int currentStepCount = 0;

    private StepTrackingController stepTrackingController;

    private final StepTrackingController.StepUpdateListener bridgeListener = this::onStepChanged;
    private NotificationManager notificationManager;

    // Average stride length in meters
    private static final double STRIDE_LENGTH = 0.762;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        stepTrackingController = new StepTrackingController(this);

        stepTrackingController.setStepUpdateListener(bridgeListener);

        // Create notification channel for Android O and above
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    startForegroundService();
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                case ACTION_STOP_FROM_NOTIFICATION:
                    stopForegroundService();
                    break;
            }
        }

        // If service is killed, restart it
        return START_STICKY;
    }

    private void startForegroundService() {
        // Start step tracking
        stepTrackingController.startTracking();

        // Start foreground service with notification
        startForeground(NOTIFICATION_ID, buildNotification(0));

        isServiceRunning = true;
    }

    private void stopForegroundService() {
        // Stop step tracking
        stepTrackingController.stopTracking();

        // Stop foreground service
        stopForeground(true);
        stopSelf();

        isServiceRunning = false;
    }

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
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setColor(getResources().getColor(R.color.colorPrimary));

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Fitness App Step Tracking",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Shows the current step count");
            channel.enableVibration(false);
            channel.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onStepChanged(int stepCount) {
        currentStepCount = stepCount;
        // Update notification with new step count
        notificationManager.notify(NOTIFICATION_ID, buildNotification(stepCount));
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