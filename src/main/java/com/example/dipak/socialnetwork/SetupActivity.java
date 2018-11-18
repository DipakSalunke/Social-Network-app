package com.example.dipak.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog LoadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserID;
    final static int Gallery_Pick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = (EditText) findViewById(R.id.setup_user_name);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        CountryName = (EditText) findViewById(R.id.setup_country_name);
        SaveInformationButton = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        LoadingBar = new ProgressDialog(this);

        SaveInformationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SaveAccountSetupInformation();

            }
        });
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent galleryIntent = new Intent();
               galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
               galleryIntent.setType("image/*");
               startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String image = dataSnapshot.child("profileimage").getValue().toString();


                    Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result= CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                LoadingBar.setTitle("Profile Image");
                LoadingBar.setMessage("Please wait, while we are Updating Your Profile Image....");
                LoadingBar.show();
                LoadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SetupActivity.this, "Profile image Saved to the firebase storage successfully...", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UsersRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Intent setupIntent = new Intent(SetupActivity.this,SetupActivity.class);
                                                startActivity(setupIntent);

                                                Toast.makeText(SetupActivity.this, "Profile Image Stored To the Firebase Database successfully...", Toast.LENGTH_SHORT).show();
                                                LoadingBar.dismiss();
                                            }
                                            else{
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "Error Occured: "+ message, Toast.LENGTH_SHORT).show();
                                                LoadingBar.dismiss();
                                            }

                                        }
                                    });
                        }
                    }
                });
            }
            else{
                Toast.makeText(this, "Error Occured: Image Can't be Cropped, Try again..", Toast.LENGTH_SHORT).show();
                LoadingBar.dismiss();
            }
        }
    }

    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();
        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please enter your username...", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "Please enter your full name...", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please enter your country...", Toast.LENGTH_SHORT).show();
        }else{
            LoadingBar.setTitle("Saving Information");
            LoadingBar.setMessage("Please wait, while we are saving your information....");
            LoadingBar.show();
            LoadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("country",country);
            userMap.put("status","Hey there, i am using SpaceX Network, developed by Dipak.");
            userMap.put("gender","none");
            userMap.put("dob","none");
            userMap.put("relationshipstatus","none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {    SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your information is saved Successfully..", Toast.LENGTH_LONG).show();
                    LoadingBar.dismiss();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occured: "+message, Toast.LENGTH_SHORT).show();
                    LoadingBar.dismiss();
                    }
                }
            });
        }


    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
