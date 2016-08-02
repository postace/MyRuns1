package com.postace.myruns1;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // For photo
    private static final int REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA = 0;
    private static final int REQUEST_CODE_CROP_PHOTO = 2;

    private static final String IMAGE_UNSPECIFIED = "image/*";

    private Uri mImageCaptureUri;
    private ImageView mImageView;
    boolean isTakenFromCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.user_photo);
        // loading user data to screen from shared preferences
        loadUserData();
        // loading user profile photo
        loadSnap();

    }

    // ****************** onClick callbacks ****************** //
    public void onSnapClicked(View v) {
        // Construct intent with action MediaStore.ACTION_IMAGE_CAPTURE
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Construct temporary image path and name to save the taken photo
        mImageCaptureUri = Uri.fromFile(new File(Environment
                                .getExternalStorageDirectory(), "tmp_" +
                                System.currentTimeMillis() + ".jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        intent.putExtra("return-data", true);
        try {
            // start a camera capturing activity
            startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA);
        } catch (ActivityNotFoundException ae) {
            ae.printStackTrace();
        }
    }

    public void onSaveClicked(View v) {
        // save all user information from the screen into a 'shared preferences'
        saveUserData();
        // notify user that data is saved
        Toast.makeText(this, getString(R.string.toast_saved),
                Toast.LENGTH_SHORT).show();
    }

    public void onCancelClicked(View v) {
        // kill activity and exist application
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;     // error, nothing to do
        }

        switch (requestCode) {
            case REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA:
                // cropping the image
                cropImage();
                isTakenFromCamera = true;
                break;
            case REQUEST_CODE_CROP_PHOTO:
                // update image view after crop image
                Bundle bundle = data.getExtras();
                // set the picture image in UI
                if (bundle != null) {
                    mImageView.setImageBitmap((Bitmap) bundle.getParcelable("data"));
                }
                // Delete temporary image taken from camera after crop
                if (isTakenFromCamera) {
                    File f = new File(mImageCaptureUri.getPath());
                    if (f.exists())
                        f.delete();
                }
                // save photo's profile
                saveSnap();

                break;
        }
    }

    // ****************** private helper functions ****************** //

    // load the user data from shared preferences if there is no data, make sure that
    // we set it to something reasonable
    private void loadUserData() {
        // Get the shared preferences, create or update the activity pref object
        String mKey = getString(R.string.pref_user_key);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);
        // load the user name, update text_view
        mKey = getString(R.string.pref_user_name_key);
        String mUsername = mPrefs.getString(mKey, " ");
        ((TextView) findViewById(R.id.text_name)).setText(mUsername);

        // load the user email
        mKey = getString(R.string.pref_user_email_key);
        String mUserEmail = mPrefs.getString(mKey, " ");
        ((TextView) findViewById(R.id.text_email)).setText(mUserEmail);

        // load the user phone
        mKey = getString(R.string.pref_user_phone_key);
        String mUserPhone = mPrefs.getString(mKey, " ");
        ((TextView) findViewById(R.id.text_phone_number)).setText(mUserPhone);

        // load the user gender
        mKey = getString(R.string.pref_user_gender_key);
        // default gender -1 mean no radio button was set.
        int genderValue = mPrefs.getInt(mKey, -1);
        if (genderValue >= 0) {
            // Find the button that should be checked
            RadioButton radioBtn = (RadioButton) ((RadioGroup)
                    findViewById(R.id.radioGender)).getChildAt(genderValue);
            // Check the button
            radioBtn.setChecked(true);
        }

        // load the user class
        mKey = getString(R.string.pref_user_class_key);
        String mUserClass = mPrefs.getString(mKey, " ");
        ((TextView) findViewById(R.id.text_class)).setText(mUserClass);

        // load the user major
        mKey = getString(R.string.pref_user_major_key);
        String mUserMajor = mPrefs.getString(mKey, " ");
        ((TextView) findViewById(R.id.text_major)).setText(mUserMajor);
    }

    // saving user data in to shared preferences
    private void saveUserData() {
        // Getting the shared preferences editor
        String mKey = getString(R.string.pref_user_key);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);

        SharedPreferences.Editor mEditor = mPrefs.edit();
        // clear previous data
        mEditor.clear();

        // save the user's information
        // save user name
        mKey = getString(R.string.pref_user_name_key);
        String mUsername = ((TextView) findViewById(R.id.text_name)).getText().toString();
        mEditor.putString(mKey, mUsername);
        // save user email
        mKey = getString(R.string.pref_user_email_key);
        String mUserEmail = ((TextView) findViewById(R.id.text_email)).getText().toString();
        mEditor.putString(mKey, mUserEmail);

        // save user phone
        mKey = getString(R.string.pref_user_phone_key);
        String mUserPhone = ((TextView) findViewById(R.id.text_phone_number)).getText().toString();
        mEditor.putString(mKey, mUserPhone);

        // Read which radio index is checked
        mKey = getString(R.string.pref_user_gender_key);
        RadioGroup radioGender = (RadioGroup) findViewById(R.id.radioGender);
        int genValue = radioGender.indexOfChild(findViewById(
                        radioGender.getCheckedRadioButtonId()));
        mEditor.putInt(mKey, genValue);

        // save user class
        mKey = getString(R.string.pref_user_class_key);
        String mUserClass = ((TextView) findViewById(R.id.text_class)).getText().toString();
        mEditor.putString(mKey, mUserClass);
        // save user major
        mKey = getString(R.string.pref_user_major_key);
        String mUserMajor = ((TextView) findViewById(R.id.text_major)).getText().toString();
        mEditor.putString(mKey, mUserMajor);

        // commit all changes
        mEditor.commit();
    }

    // save picture to internal storage
    private void saveSnap() {
        // Commit all changes into preferences file
        // Save user profile photo into internal storage
        mImageView.buildDrawingCache();
        Bitmap bm = mImageView.getDrawingCache();
        try {
            FileOutputStream fos = openFileOutput(
                                    getString(R.string.user_photo_file_name), MODE_PRIVATE);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // load user-picture profile
    private void loadSnap() {
        // load the photo from internal storage
        try {
            FileInputStream fis = openFileInput(getString(R.string.user_photo_file_name));
            Bitmap bm = BitmapFactory.decodeStream(fis);
            mImageView.setImageBitmap(bm);
            fis.close();        // remember to close file
        } catch (FileNotFoundException fe) {
            // Default profile photo if no photo saved before
            mImageView.setImageResource(R.drawable.sample);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // Crop and resize image for the profile
    private void cropImage() {
        // Use existing crop activity.
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCaptureUri, IMAGE_UNSPECIFIED);

        // Specify image size
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);

        // Specify aspect ratio, 1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
    }
}
