package com.retinavision.app.retinavisionapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.retinavision.app.AppManager;

public class SettingsActivity extends BaseActivity {

    private EditText serviceurlEditor;
    private EditText echourlEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        serviceurlEditor = (EditText) findViewById(R.id.settings_serviceurl);
        echourlEditor = (EditText) findViewById(R.id.settings_echourl);
        Button defaultButton = (Button) findViewById(R.id.settings_default);
        Button saveButton = (Button) findViewById(R.id.settings_save);
        Button gobackButton = (Button) findViewById(R.id.settings_goback);
        Button exitButton = (Button) findViewById(R.id.settings_exit);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.saveSettings();
                SettingsActivity.this.finish();
            }
        });

        defaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceurlEditor.setText("http://<Hostname>:<Port>/retina");
                echourlEditor.setText("http://<Hostname>:<Port>/echo");
            }
        });

        gobackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.finish();
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishAllActivity();
            }
        });


        loadSettings();
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        String serviceurl = sharedPreferences.getString("serviceurl", "");
        String echourl = sharedPreferences.getString("echourl", "");
        serviceurlEditor.setText(serviceurl);
        echourlEditor.setText(echourl);
    }

    private void saveSettings(){
        SharedPreferences sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("serviceurl",serviceurlEditor.getText().toString());
        editor.putString("echourl",echourlEditor.getText().toString());
        editor.apply();

    }
}
