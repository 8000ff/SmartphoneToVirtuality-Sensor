package com.example.smartphonetovirtuality;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class SensorsService extends Service implements SensorEventListener {
    private static String ip;
    private static int port;
    private boolean running;
    private WakeLock wakeLock;
    private float max_proximity, min_proximity;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        bindSensors();
        startForeground(4269, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        acquireLock();
        ip = intent.getStringExtra("ip");
        port = intent.getIntExtra("port", 0);
        running = true;
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        wakeLock.release();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                trigger(Sensor.TYPE_ACCELEROMETER, event.values[0]+" "+event.values[1]+" "+event.values[2]);
                break;
            case Sensor.TYPE_GYROSCOPE:
                trigger(Sensor.TYPE_GYROSCOPE, event.values[0]+" "+event.values[1]+" "+event.values[2]);
                break;
            case Sensor.TYPE_PROXIMITY:
                trigger(Sensor.TYPE_PROXIMITY, normalizeProximity(event.values[0]));
                break;
            default:
                System.out.println("Unhandled sensor.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void bindSensors() {
        SensorManager manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);

        Sensor proximity = manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        max_proximity = proximity.getMaximumRange();
        min_proximity = proximity.getMinDelay();
        manager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private String normalizeProximity(float proximity) {
        return ((proximity - min_proximity) / (max_proximity - min_proximity))+"";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification createNotification() {
        String channelId = "STVService";
        NotificationChannel channel = new NotificationChannel(channelId, "STV Service", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("STV")
                .setContentText("Running")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
    }

    private void acquireLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);
    }

    public void trigger(int sensor, String data) {
        if(running) new SensorsTask().execute(sensor+"_"+data);
    }

    private static class SensorsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... args) {
            new Client().sendUDP(args[0], ip, port);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
