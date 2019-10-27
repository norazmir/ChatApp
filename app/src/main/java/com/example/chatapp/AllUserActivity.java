package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chatapp.Model.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class AllUserActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;

    @BindView(R.id.user_app_bar)
    Toolbar toolbar;
    @BindView(R.id.rvUser)
    RecyclerView userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userList.setHasFixedSize(true);
        userList.setLayoutManager(new LinearLayoutManager(this));

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mDatabaseReference, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int position, @NonNull Users users) {
                usersViewHolder.setDisplayName(users.getName());
                usersViewHolder.setDisplayStatus(users.getStatus());
                usersViewHolder.setDisplayImage(users.getThumb_image());

                String user_id = getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_layout, parent, false);

                return new UsersViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        userList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView){
            super(itemView);

            mView = itemView;
        }

        public void setDisplayName(String name){
            TextView userNameView = mView.findViewById(R.id.tvSingleUserName);
            userNameView.setText(name);
        }
        public void setDisplayStatus(String status){
            TextView userStatusView = mView.findViewById(R.id.tvSingleUserStatus);
            userStatusView.setText(status);
        }
        public void setDisplayImage(String thumb_image){
            CircleImageView userImageView = mView.findViewById(R.id.ivSingleUser);
            Picasso.get().load(thumb_image).placeholder(R.drawable.avatar).into(userImageView);
        }
    }
}
