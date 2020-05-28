package com.chatandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.chatandroid.databinding.ActivityProfileEditBinding;
import com.chatandroid.utils.Tools;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ProfileEditActivity extends Authenticate
{
    private DatabaseReference UserRef;

    private ActivityProfileEditBinding binding;

    private CircularImageView image;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;
    private static final int GalleryPick = 1;
    private static final int REQUEST_CODE_LOCATION = 500;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding =  ActivityProfileEditBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        primaryMenu(savedInstanceState);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("profile_pictures");
        UserRef.keepSynced(true);
        initToolbar();

        RetrieveUserInfo();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        binding.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAutocompleteActivity(REQUEST_CODE_LOCATION);
            }
        });


        binding.updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        binding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                LatLng latLng = place.getLatLng();
                binding.location.setText(place.getName());
//                binding.latitude.setText(String.valueOf(latLng.latitude));
//                binding.longitude.setText(String.valueOf(latLng.longitude));
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Snackbar.make(binding.parent, status.toString(), Snackbar.LENGTH_SHORT).show();
            }
        }

        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();


                StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ProfileEditActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                            final String downloaedUrl = task.getResult().getMetadata().getName().toString();

                            RootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloaedUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(ProfileEditActivity.this, "Image saved Successfully...", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            String message = task.getException().toString();
                            loadingBar.dismiss();
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                });
            }
        }


    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
        loadingBar = new ProgressDialog(this);
    }

    private void RetrieveUserInfo()
    {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                image = (CircularImageView) binding.image;
                String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                String name = firtname + " " + lastname;
                String username = Tools.getRefValue(dataSnapshot.child("username"));
                String location = Tools.getRefValue(dataSnapshot.child("location"));
                String latitude = Tools.getRefValue(dataSnapshot.child("latitude"));
                String longitude = Tools.getRefValue(dataSnapshot.child("longitude"));
                String phonenumber = Tools.getRefValue(dataSnapshot.child("phonenumber"));

                if (dataSnapshot.child("image").exists())
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.mipmap.ic_launcher_round).into(image);

                }

                binding.profileName.setText(name);
                if (mAuth.getCurrentUser() != null) {
                    binding.username.setText(mAuth.getCurrentUser().getEmail());
                }
                binding.usernameEdit.setText(username);
                binding.firstname.setText(firtname);
                binding.lastname.setText(lastname);
                binding.longitude.setText(longitude);
                binding.latitude.setText(latitude);
                binding.phonenumber.setText(phonenumber);
                if(!location.isEmpty()){
                    binding.location.setText(location);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateProfile()
    {
        String firstname = binding.firstname.getText().toString();
        String lastname = binding.lastname.getText().toString();
        String username = binding.usernameEdit.getText().toString();

        if (TextUtils.isEmpty(lastname))
        {
            Toast.makeText(this, "Please write your first name.", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(firstname))
        {
            Toast.makeText(this, "Please write your lastname", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("firstname", firstname);
            profileMap.put("lastname", lastname);
            profileMap.put("username", username);
            profileMap.put("location", binding.location.getText().toString());
            profileMap.put("phonenumber",binding.phonenumber.getText().toString());
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(ProfileEditActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void openAutocompleteActivity(int request_code) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);
        startActivityForResult(intent, request_code);
    }
}
