package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private Handler mHandler = new Handler();
    private int progressBarStatus = 0;

    @BindView(R.id.log_app_bar)
    Toolbar mToolBar;
    @BindView(R.id.tilEmail)
    EditText emailView;
    @BindView(R.id.tilPassword)
    EditText passwordView;
    @BindView(R.id.btnLogin)
    Button buttonLogin;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Login Account");

        mAuth = FirebaseAuth.getInstance();
        buttonLogin.setOnClickListener(this);
    }

    private void login(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                    Toast.makeText(getApplicationContext(), "Welcome !", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onClick(View view) {
        String email = emailView.getText().toString().trim();
        String password = passwordView.getText().toString().trim();

        if (!email.isEmpty()||!password.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (progressBarStatus < 100) {
                        progressBarStatus++;
                        android.os.SystemClock.sleep(50);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.VISIBLE);
                                progressBar.setProgress(progressBarStatus);
                            }
                        });
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            login(email, password);
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(getApplicationContext(), "Login Empty Credentials. ", Toast.LENGTH_SHORT).show();
        }
    }
}


