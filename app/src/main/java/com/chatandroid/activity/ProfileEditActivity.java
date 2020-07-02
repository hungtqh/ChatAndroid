package com.chatandroid.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.chatandroid.R;
import com.chatandroid.databinding.ActivityProfileEditBinding;
import com.chatandroid.utils.Tools;
import com.google.android.gms.common.api.Status;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditActivity extends Authenticate implements DatePickerDialog.OnDateSetListener {
    private DatabaseReference usersRef;

    private ActivityProfileEditBinding binding;

    private CircularImageView image;
    private StorageReference userProfileImagesRef;
    private ProgressDialog loadingBar;
    private static final int GalleryPick = 1;
    private static final int REQUEST_CODE_LOCATION = 500;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        primaryMenu(savedInstanceState);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("profile_pictures");
        usersRef.keepSynced(true);
        initToolbar();

        retrieveUserInfo();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        binding.location.setOnClickListener(v -> openAutocompleteActivity(REQUEST_CODE_LOCATION));

        boolean nightMode = preference.getNightMode();
        toggleNightMode(view, nightMode);

        binding.updateProfile.setOnClickListener(v -> updateProfile());

        binding.dateOfBirth.setOnClickListener(v -> {
            showDatePicker();
        });

        binding.image.setOnClickListener(view1 -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GalleryPick);
        });
    }

    private void showDatePicker() {
        Calendar calendar;
        DatePickerDialog datePickerDialog;
        int Year, Month, Day;

        calendar = Calendar.getInstance();

        Year = calendar.get(Calendar.YEAR);
        Month = calendar.get(Calendar.MONTH);
        Day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = DatePickerDialog.newInstance(ProfileEditActivity.this, Year, Month, Day);
        datePickerDialog.setLocale(new Locale(selectedLocale));
        datePickerDialog.setThemeDark(false);

        datePickerDialog.showYearPickerFirst(false);

        datePickerDialog.setAccentColor(Color.parseColor("#0072BA"));

        datePickerDialog.setTitle(getString(R.string.select_birthday_date_picker));

        datePickerDialog.show(getSupportFragmentManager(), getString(R.string.date_picker_dialog));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                binding.location.setText(place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Snackbar.make(binding.parent, status.toString(), Snackbar.LENGTH_SHORT).show();
            }
        }

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle(getString(R.string.set_profile_image));
                loadingBar.setMessage(getString(R.string.wait_setting_image));
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                StorageReference filePath = userProfileImagesRef.child(currentUserID + ".jpg");
                filePath.putFile(imageUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadedUrl = uri.toString();

                            rootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadedUrl)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(ProfileEditActivity.this, R.string.image_saved, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    });
                        });
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                });
            }
        }
    }


    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.menu_profile));
        Tools.setSystemBarColorInt(this, getResources().getColor(R.color.default_status_color));
        loadingBar = new ProgressDialog(this);
    }

    private void retrieveUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                image = binding.image;
                String firtname = Tools.getRefValue(dataSnapshot.child("firstname"));
                String lastname = Tools.getRefValue(dataSnapshot.child("lastname"));
                String name = firtname + " " + lastname;
                String nickName = Tools.getRefValue(dataSnapshot.child("username"));
                String email = Tools.getRefValue(dataSnapshot.child("email"));
                String location = Tools.getRefValue(dataSnapshot.child("location"));
                String phonenumber = Tools.getRefValue(dataSnapshot.child("phonenumber"));
                String gender = Tools.getRefValue(dataSnapshot.child("gender"));
                String dateOfBirth = Tools.getRefValue(dataSnapshot.child("dateOfBirth"));

                if (dataSnapshot.child("image").exists()) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.mipmap.ic_launcher_round).into(image);
                }

                binding.profileName.setText(name);

                if (gender.equals("Male") || gender.equals("Nam")) {
                    binding.male.setChecked(true);
                } else {
                    binding.female.setChecked(true);
                }

                if (binding.dateOfBirth.getText().toString().isEmpty()) {
                    binding.dateOfBirth.setText(dateOfBirth);
                }

                if (binding.email.getText().toString().isEmpty()) {
                    binding.email.setText(email);
                }

                if (binding.usernameEdit.getText().toString().isEmpty()) {
                    binding.usernameEdit.setText(nickName);
                }

                if (binding.firstname.getText().toString().isEmpty()) {
                    binding.firstname.setText(firtname);
                }

                if (binding.lastname.getText().toString().isEmpty()) {
                    binding.lastname.setText(lastname);
                }

                if (mAuth.getCurrentUser() != null) {
                    if (!TextUtils.isEmpty(mAuth.getCurrentUser().getPhoneNumber())) {
                        phonenumber = mAuth.getCurrentUser().getPhoneNumber();
                        binding.phonenumber.setEnabled(false);
                    } else {
                        binding.email.setText(mAuth.getCurrentUser().getEmail());
                        binding.email.setEnabled(false);
                    }
                }

                if (binding.phonenumber.getText().toString().isEmpty()) {
                    binding.phonenumber.setText(phonenumber);
                }

                if (binding.location.getText().toString().isEmpty()) {
                    binding.location.setText(location);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateProfile() {
        String firstname = binding.firstname.getText().toString();
        String lastname = binding.lastname.getText().toString();
        String username = binding.usernameEdit.getText().toString();
        String location = binding.location.getText().toString();
        String phoneNumber = binding.phonenumber.getText().toString();
        String email = binding.email.getText().toString();
        int checkedGender = binding.gender.getCheckedRadioButtonId();
        String gender = (checkedGender == R.id.male) ? "Male" : "Female";
        String dateOfBirth = binding.dateOfBirth.getText().toString();

        if (TextUtils.isEmpty(lastname) ||
                TextUtils.isEmpty(firstname) ||
                TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(location) ||
                TextUtils.isEmpty(phoneNumber) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(dateOfBirth)) {
            Toast.makeText(this, R.string.enter_all_fields, Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("firstname", firstname);
            profileMap.put("lastname", lastname);
            profileMap.put("username", username);
            profileMap.put("gender", gender);
            profileMap.put("dateOfBirth", dateOfBirth);
            profileMap.put("location", location);
            profileMap.put("email", email);
            profileMap.put("phonenumber", phoneNumber);
            rootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileEditActivity.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
            onBackPressed();
        }
    }

    private void openAutocompleteActivity(int request_code) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);
        startActivityForResult(intent, request_code);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        monthOfYear++;
        String date = dayOfMonth + "-" + monthOfYear + "-" + year;
        binding.dateOfBirth.setText(date);
    }
}
