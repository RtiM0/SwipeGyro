package com.mustafa.shakir.swipegyro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button buttonStart;
    private Button buttonStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);
        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == buttonStart) {
            //start the service here
            startService(new Intent(this, GyroService.class));
        } else if (view == buttonStop) {
            //stop the service here
            stopService(new Intent(this, GyroService.class));
        }
    }
}
