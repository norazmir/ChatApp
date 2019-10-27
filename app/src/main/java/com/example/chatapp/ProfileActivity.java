package com.example.chatapp;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private FirebaseUser mCurrentUser;
    private String current_state;

    @BindView(R.id.displayProfileName)
    TextView profileNameView;
    @BindView(R.id.profile_image)
    ImageView profileImageView;
    @BindView(R.id.displayProfileStatus)
    TextView profileStatusView;
    @BindView(R.id.profile_total_friends)
    TextView profileTotalFriendsView;
    @BindView(R.id.btnFriendRequest)
    Button profileFriendRequest;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        String user_id = getIntent().getStringExtra("user_id");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        current_state = "not_friends";

        profileFriendRequest.setOnClickListener(this);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String displayStatus = dataSnapshot.child("status").getValue().toString();
                String displayImage = dataSnapshot.child("image").getValue().toString();

                profileNameView.setText(displayName);
                profileStatusView.setText(displayStatus);
                Picasso.get().load(displayImage).placeholder(R.drawable.avatar).into(profileImageView);

                //FRIEND LIST/REQUEST FEATURE
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")){
                                current_state = "req_received";
                                profileFriendRequest.setText("Accept Friend Request");
                            }
                            else if (req_type.equals("sent")){
                                current_state = "req_sent";
                                profileFriendRequest.setText("Cancel Friend Request");
                            }
                        }
                        else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){
                                        current_state = "friends";
                                        profileFriendRequest.setText("Unfriend this person");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {

        profileFriendRequest.setEnabled(false);
        String user_id = getIntent().getStringExtra("user_id");
        if (current_state.equals("not_friends")){
            mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                profileFriendRequest.setEnabled(true);
                                current_state = "req_sent";
                                profileFriendRequest.setText("Cancel Friend Request");
                                Toast.makeText(getApplicationContext(),"Request Sent.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else
                        Toast.makeText(getApplicationContext(),"Failed Sending Request.", Toast.LENGTH_SHORT).show();
                }
            });
        }


        if (current_state.equals("req_sent")){
            mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                profileFriendRequest.setEnabled(true);
                                current_state = "not_friends";
                                profileFriendRequest.setText("Sent Friend Request");
                                Toast.makeText(getApplicationContext(),"Friend Request Cancelled.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            });
        }


        if (current_state.equals("req_received")){
            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
            mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                profileFriendRequest.setEnabled(true);
                                                current_state = "friends";
                                                profileFriendRequest.setText("Unfriend this person");
                                                Toast.makeText(getApplicationContext(), "Added Friend.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
    }
}
