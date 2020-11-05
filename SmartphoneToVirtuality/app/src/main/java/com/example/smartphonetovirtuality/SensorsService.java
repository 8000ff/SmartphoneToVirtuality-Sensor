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

/**
 * An Android service aiming to send data of different sensors to a server.
 * When the service is on it has to be able to send sensors data in any condition (ex: screen lock).
 * @author COGOLUEGNES Charles
 */
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

    /**
     * Binds the sensors.
     * Creates a notification on foreground.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        bindSensors();
        startForeground(4269, createNotification());
    }

    /**
     * Acquire a lock on the CPU.
     * Gets the ip and port of the server device.
     * @param intent intent.
     * @param flags flags.
     * @param startId id.
     * @return a start id.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        acquireLock();
        ip = intent.getStringExtra("ip");
        port = intent.getIntExtra("port", 0);
        running = true;
        return Service.START_STICKY;
    }

    /**
     * Release the lock and stop the notification.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        wakeLock.release();
        stopForeground(true);
        stopSelf();
    }

    /**
     * Triggers a change when a sensor data has changed.
     * @param event the data of the sensor.
     */
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

    /**
     * Creates a listener for every used sensor.
     * Gets the min and max proximity.
     */
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

    /**
     * Normalize the given proximity with a max and a min proximity of the device.
     * @param proximity the given proximity.
     * @return a string which is the normalize proximity.
     */
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

    /**
     * Acquires a lock on the CPU.
     */
    private void acquireLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);
    }

    /**
     * Triggers a background task to send sensors data.
     * @param sensor the sensor which has changed.
     * @param data the coordinates or value of the sensor.
     */
    public void trigger(int sensor, String data) {
        if(running) new SensorsTask().execute(sensor+"_"+data);
    }

    private static class SensorsTask extends AsyncTask<String, Void, Void> {

        /**
         * Sends the data via UDP to the server.
         * @param args the data.
         * @return Void.
         */
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
