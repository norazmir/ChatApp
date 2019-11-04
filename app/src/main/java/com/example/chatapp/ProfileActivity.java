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
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

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
    @BindView(R.id.btnDeclineRequest)
    Button declineFriendRequest;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        final String user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        current_state = "not_friends";
        declineFriendRequest.setVisibility(View.INVISIBLE);
        declineFriendRequest.setEnabled(false);

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

                                declineFriendRequest.setVisibility(View.VISIBLE);
                                declineFriendRequest.setEnabled(true);
                            }
                            else if (req_type.equals("sent")){
                                current_state = "req_sent";
                                profileFriendRequest.setText("Cancel Friend Request");

                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                declineFriendRequest.setEnabled(false);
                            }
                        }
                        else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){
                                        current_state = "friends";
                                        profileFriendRequest.setText("Unfriend this person");

                                        declineFriendRequest.setVisibility(View.INVISIBLE);
                                        declineFriendRequest.setEnabled(false);
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
        //not friends state
        String user_id = getIntent().getStringExtra("user_id");
        if (current_state.equals("not_friends")){

            DatabaseReference newNotification = mRootRef.child("Notifications").child(user_id).push();
            String newNotificationId = newNotification.getKey();

            HashMap<String, String> notificationData = new HashMap<>();
            notificationData.put("from", mCurrentUser.getUid());
            notificationData.put("type", "request");

            Map requestMap = new HashMap();
            requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
            requestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");
            requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

            mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null){
                        Toast.makeText(getApplicationContext(), "There was some error sending request.", Toast.LENGTH_SHORT).show();
                    }
                    profileFriendRequest.setEnabled(true);
                    current_state = "req_sent";
                    profileFriendRequest.setText("Cancel Friend Request");
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
                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                declineFriendRequest.setEnabled(false);
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
                                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                                declineFriendRequest.setEnabled(false);
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
