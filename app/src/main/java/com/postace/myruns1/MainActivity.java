package com.postace.myruns1v2;

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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // request code for sending intent
    private static final int REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA = 4;
    private static final int REQUEST_CODE_CROP_IMAGE = 5;

    private static final String IMAGE_UNSPECIFIED = "image/*";

    private Uri mImageCaptureUri;
    private ImageView mUserAvatarImage;
    private boolean isTakenFromCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUserAvatarImage = (ImageView) findViewById(R.id.user_avatar);
        // load user data and update UI
        loadData();
    }

    // handle data when got result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, getString(R.string.toast_unknown_error),
                    Toast.LENGTH_SHORT).show();
            return;     // something error -> nothing to do
        }

        switch (requestCode) {
            case REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA:
                // got picture, now crop it
                cropImage();
                isTakenFromCamera = true;
                break;
            case REQUEST_CODE_CROP_IMAGE:
                // Get photo data from intent
                Bundle extras = data.getExtras();
                // update user avatar in UI
                if (extras != null) {
                    Bitmap bm = extras.getParcelable("data");
                    mUserAvatarImage.setImageBitmap(bm);
                }
                // clear temporary image
                if (isTakenFromCamera) {
                    File f = new File(mImageCaptureUri.getPath());
                    if (f.exists())
                        f.delete();
                }
                break;
        }
    }

    // Checking if external storage is available for read/write
    public boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState());
    }

    // ***************** button click callbacks ***************** //
    public void onCaptureClicked(View v) {
        // Construct and intent to take photo
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Construct an temporary image file and path for storing photo
        // if external storage is not available, use internal storage
        if (!isExternalStorageWritable()) {
            mImageCaptureUri = Uri.fromFile(new File(
                    getFilesDir(), "tmp_" + System.currentTimeMillis() + ".jpg"));
        } else {    // otherwise, use external storage
            mImageCaptureUri = Uri.fromFile(new File(
                    Environment.getExternalStorageDirectory(),
                    "tmp_" + System.currentTimeMillis() + ".jpg"));
        }
        // set output for image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        intent.putExtra("return-data", true);
        try {
            // start camera to take photo
            startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO_FROM_CAMERA);
        } catch (ActivityNotFoundException ae) {
            Toast.makeText(this,
                    getString(R.string.toast_msg_no_activity), Toast.LENGTH_SHORT).show();
        }
    }

    public void onSaveClicked(View v) {
        // saving user data
        saveData();
        // notify user that data is saved
        Toast.makeText(this,
                getString(R.string.toast_data_saved), Toast.LENGTH_SHORT).show();
    }

    public void onCancelClicked(View v) {
        // exit application
        finish();
    }

    // ******************* private helper functions ******************* //

    // cropping image after taken photo from camera
    private void cropImage() {
        // Use existing activity for crop
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCaptureUri, IMAGE_UNSPECIFIED);

        // Specify image size
        intent.putExtra("outputX", 120);
        intent.putExtra("outputY", 120);

        // Specify image aspect ratio 1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);

        // start camera for cropping
        try {
            startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
        } catch (ActivityNotFoundException ae) {
            Toast.makeText(this,
                    getString(R.string.toast_msg_no_activity)
                    , Toast.LENGTH_SHORT).show();
        }
    }

    // saving user avatar to internal storage
    private void savePhoto() {
        mUserAvatarImage.buildDrawingCache();
        Bitmap bm = mUserAvatarImage.getDrawingCache();
        try {
            FileOutputStream fos = openFileOutput(
                    getString(R.string.user_avatar_photo_name), MODE_PRIVATE);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            // cannot save photo
            ioe.printStackTrace();
        }
    }

    // loading photo saved and update in UI
    private void loadPhoto() {
        try {
            FileInputStream fis = openFileInput(getString(R.string.user_avatar_photo_name));
            Bitmap bm = BitmapFactory.decodeStream(fis);
            mUserAvatarImage.setImageBitmap(bm);
            fis.close();
        } catch (IOException ioe) {
            // if no photo saved before, load default photo
            mUserAvatarImage.setImageResource(R.drawable.androidparty);
        }
    }

    // ******************* Shared Preferences ******************* //

    // load user data and update UI
    private void loadData() {
        // load photo avatar
        loadPhoto();
        // Get shared prefs object
        String mKey = getString(R.string.pref_user_data_key);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);

        // get name
        mKey = getString(R.string.pref_user_name_key);
        String mName = mPrefs.getString(mKey, " ");
        ((EditText) findViewById(R.id.user_name_input)).setText(mName);

        // get email
        mKey = getString(R.string.pref_user_email_key);
        String mEmail = mPrefs.getString(mKey, " ");
        ((EditText) findViewById(R.id.user_email_input)).setText(mEmail);

        // get phone number
        mKey = getString(R.string.pref_user_phone_key);
        String mPhone = mPrefs.getString(mKey, " ");
        ((EditText) findViewById(R.id.user_phone_input)).setText(mPhone);

        // get gender
        mKey = getString(R.string.pref_user_gender_key);
        int genId = mPrefs.getInt(mKey, -1);
        if (genId >= 0) {
            RadioButton radioBtn = (RadioButton)
                    ((RadioGroup) findViewById(R.id.user_gender)).getChildAt(genId);
            radioBtn.setChecked(true);
        }

        // get class
        mKey = getString(R.string.pref_user_class_key);
        String mClass = mPrefs.getString(mKey, " ");
        ((EditText) findViewById(R.id.user_class_input)).setText(mClass);

        // get major
        mKey = getString(R.string.pref_user_major_key);
        String mMajor = mPrefs.getString(mKey, " ");
        ((EditText) findViewById(R.id.user_major_input)).setText(mMajor);
    }

    // storing user information into shared preferences
    private void saveData() {
        // saving user avatar
        savePhoto();
        // Getting shared prefs object
        String mKey = getString(R.string.pref_user_data_key);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.clear();            // clear previous data

        // save name
        mKey = getString(R.string.pref_user_name_key);
        String mName = ((EditText) findViewById(R.id.user_name_input)).getText().toString();
        mEditor.putString(mKey, mName);

        // save email
        mKey = getString(R.string.pref_user_email_key);
        String mEmail = ((EditText) findViewById(R.id.user_email_input)).getText().toString();
        mEditor.putString(mKey, mEmail);

        // save phone
        mKey = getString(R.string.pref_user_phone_key);
        String mPhone = ((EditText) findViewById(R.id.user_phone_input)).getText().toString();
        mEditor.putString(mKey, mPhone);

        // save gender
        mKey = getString(R.string.pref_user_gender_key);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.user_gender);
        int genId = radioGroup.indexOfChild(findViewById(radioGroup.getCheckedRadioButtonId()));
        mEditor.putInt(mKey, genId);

        // save class
        mKey = getString(R.string.pref_user_class_key);
        String mclass = ((EditText) findViewById(R.id.user_class_input)).getText().toString();
        mEditor.putString(mKey, mclass);

        // save major
        mKey = getString(R.string.pref_user_major_key);
        String mMajor = ((EditText) findViewById(R.id.user_major_input)).getText().toString();
        mEditor.putString(mKey, mMajor);

        // commit all changes
        mEditor.commit();
    }
}
