package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatusActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StatusActivity";
    private DatabaseReference statusReference;
    private FirebaseUser currentUser;

    @BindView(R.id.status_app_bar)
    Toolbar toolbar;
    @BindView(R.id.edtStatus)
    EditText editTextStatus;
    @BindView(R.id.btnStatus)
    Button buttonStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String status_value = getIntent().getStringExtra("status");
        editTextStatus.setText(status_value);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = currentUser.getUid();
        statusReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        buttonStatus.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String status = editTextStatus.getText().toString().trim();

        statusReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Status Changed", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                    Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
