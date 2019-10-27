package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SettingsActivity";
    private DatabaseReference mDatabaseReference;
    private StorageReference mImageStorage;
    private FirebaseUser mCurrentUser;
    private static final int GALLERY_PICK = 1;

    @BindView(R.id.tvStatus)
    TextView displayStatus;
    @BindView(R.id.tvName)
    TextView displayName;
    @BindView(R.id.btnChangeStatus)
    Button buttonChangeStatus;
    @BindView(R.id.btnChangeImage)
    Button buttonChangeImage;
    @BindView(R.id.ivProfile)
    CircleImageView displayImageProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mImageStorage = FirebaseStorage.getInstance().getReference();

        buttonChangeStatus.setOnClickListener(this);
        buttonChangeImage.setOnClickListener(this);

        displayProfile();
    }

    @Override
    public void onClick(View view) {
        if (view == buttonChangeStatus){
            String status_value = displayStatus.getText().toString();
            Intent intentStatus = new Intent(getApplicationContext(), StatusActivity.class);
            intentStatus.putExtra("status", status_value);
            startActivity(intentStatus);
        }
        else if (view == buttonChangeImage){
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
    }

    private void displayProfile() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "Data " + dataSnapshot.child("name"));
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                Log.i(TAG, "Data " + image);
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                Log.i(TAG, "Name " + name);

                displayName.setText(name);
                displayStatus.setText(status);
                if (!image.equals("default")){
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(displayImageProfile, new Callback() {
                        @Override
                        public void onSuccess() {

                        }
                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.avatar).into(displayImageProfile);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                String current_user_id = mCurrentUser.getUid();
                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());
                Bitmap thumb_bitmap = null;
                try {
                     thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] thumb_byte = baos.toByteArray();


                StorageReference filePath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");

                StorageReference thumb_filePathStorage = mImageStorage.child("profile_images").child("thumbs").child(current_user_id + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

//                        final String download_url = task.getResult().getStorage().getDownloadUrl().toString();
                        if (task.isSuccessful()){
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String thumb_downloadURL = uri.toString();
                                    if (task.isSuccessful()){
                                        UploadTask uploadTask = thumb_filePathStorage.putBytes(thumb_byte);
                                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    String download_url = uri.toString();
                                                    Map updateHashMap = new HashMap();
                                                    updateHashMap.put("image", download_url);
                                                    updateHashMap.put("thumb_image", thumb_downloadURL);
                                                    mDatabaseReference.updateChildren(updateHashMap);
                                                    Toast.makeText(getApplicationContext(), "Upload Success.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
