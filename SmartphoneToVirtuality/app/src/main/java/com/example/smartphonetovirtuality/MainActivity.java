package com.example.smartphonetovirtuality;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

/**
 * Main Android activity.
 * @author COGOLUEGNES Charles
 */
public class MainActivity extends AppCompatActivity {
    private final MainActivity $this = this;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch connectSwitch = (Switch) findViewById(R.id.connect_switch);
        connectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * When the switch button has changed.
             * Starts the SensorsService when the switch is on and stop the service if it is off.
             * @param buttonView switch button.
             * @param isChecked if the switch is toggle.
             */
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(intent == null) intent = new Intent($this, SensorsService.class);
                if(isChecked) {
                    EditText ip = (EditText) findViewById(R.id.ip_text);
                    EditText port = (EditText) findViewById(R.id.port_text);
                    intent.putExtra("ip", ip.getText().toString());
                    intent.putExtra("port", Integer.parseInt(port.getText().toString()));
                    startService(intent);
                }
                else stopService(intent);
            }
        });
    }

    /**
     * Stop the service if it exists.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(intent != null) stopService(intent);
    }
}