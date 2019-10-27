package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.btnRegister)
    Button buttonRegister;
    @BindView(R.id.btnLogin)
    Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        buttonRegister.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view == buttonRegister)
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        else if (view == buttonLogin)
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }
}
