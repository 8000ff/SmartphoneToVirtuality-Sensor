package com.example.smartphonetovirtuality;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {
    private final MainActivity $this = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch connectSwitch = (Switch) findViewById(R.id.connect_switch);
        connectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent($this, SensorsService.class);
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
}