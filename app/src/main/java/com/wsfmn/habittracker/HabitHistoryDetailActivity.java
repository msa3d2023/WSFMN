package com.wsfmn.habittracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.wsfmn.habit.HabitEventCommentTooLongException;
import com.wsfmn.habit.HabitEventNameException;
import com.wsfmn.habitcontroller.HabitHistoryController;
import com.wsfmn.habitcontroller.HabitListController;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.wsfmn.habittracker.HabitEventActivity.REQUEST_TAKE_PHOTO;

public class HabitHistoryDetailActivity extends AppCompatActivity {

    EditText nameEvent;
    Button addHabit;
    TextView habitName;
    Button addPicture;
    EditText comment;
    Button viewImage;
    Button confirm;
    TextView date;
    TextView T_address;
    int position2;
    int i;
    Button B_changeLocation;
    static final int CHANGE_LOCATION_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_history_detail);

        nameEvent = (EditText)findViewById(R.id.nameEvent2);
        addHabit = (Button)findViewById(R.id.addHabit2);
        habitName = (TextView) findViewById(R.id.habitName2);
        addPicture = (Button)findViewById(R.id.Picture2);
        comment = (EditText)findViewById(R.id.Comment2);
        viewImage = (Button)findViewById(R.id.ViewImg2);
        confirm = (Button)findViewById(R.id.confirmButton2);
        date = (TextView)findViewById(R.id.dateDetail);
        B_changeLocation = (Button)findViewById(R.id.B_changeLocation);
        T_address = (TextView)findViewById(R.id.T_C_showAddress);

        Intent intent = getIntent();
        Bundle b = getIntent().getExtras();
        try {
            position2 = b.getInt("position");
        }catch (NullPointerException e){

        }
        HabitHistoryController control = HabitHistoryController.getInstance();
        try {
            nameEvent.setText(control.get(position2).getHabitEventTitle());
            habitName.setText(control.get(position2).getHabitFromEvent().getTitle());
            comment.setText(control.get(position2).getComment());
            date.setText(control.get(position2).getDate());
        }catch (HabitEventNameException e) {
            Toast.makeText(HabitHistoryDetailActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (HabitEventCommentTooLongException e) {
            Toast.makeText(HabitHistoryDetailActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }catch(IndexOutOfBoundsException e){

        }

        Button B_ = (Button) findViewById(R.id.B_changeLocation);
        B_changeLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            //https://developer.android.com/training/basics/intents/result.html
            public void onClick(View v){
                Intent  intent = new Intent(HabitHistoryDetailActivity.this,ChangeLocationActivity.class);
                startActivityForResult(intent,1);
            }
        });

    }

    public void confirmHE(View view){
        Intent intent = new Intent(this, HabitHistoryActivity.class);
        try {
            HabitHistoryController control2 = HabitHistoryController.getInstance();
            control2.get(position2).setTitle(nameEvent.getText().toString());
            control2.get(position2).setComment(comment.getText().toString());
            control2.get(position2).setHabit(control2.get(position2).getHabitFromEvent());
            control2.storeAndUpdate(control2.get(position2));
//            control2.storeAll();
            startActivity(intent);
        } catch (HabitEventCommentTooLongException e) {
            Toast.makeText(HabitHistoryDetailActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (HabitEventNameException e) {
            Toast.makeText(HabitHistoryDetailActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void deleteHE(View view){
        Intent intent = new Intent(HabitHistoryDetailActivity.this, HabitHistoryActivity.class);
        HabitHistoryController control3 = HabitHistoryController.getInstance();
//        control3.remove(position2);
//        control3.store();
        control3.remove(position2);
        control3.store();
        startActivity(intent);
    }

    public void changeHabit(View view){
        Intent intent = new Intent(this, SelectHabitActivity.class);
        startActivityForResult(intent, 2);
    }

    //Has the path that is to be calculated in detail
    String CurrentPhotoPath;
    //Has the path already present
    String path;
    public void viewImage2(View view){
        Intent intent = new Intent(HabitHistoryDetailActivity.this, ImageActivity.class);
        HabitHistoryController control4 = HabitHistoryController.getInstance();
        path = control4.get(position2).getCurrentPhotoPath();
        if(path == null){
            path = CurrentPhotoPath;
        }
        intent.putExtra("CurrentPhotoPath",path);
        startActivity(intent);
    }

    public void changePicture2(View view) throws IOException {
        try {
            HabitHistoryController control4 = HabitHistoryController.getInstance();
            dispatchTakePictureIntent(control4.get(position2).getCurrentPhotoPath());
        }catch (NullPointerException e){
            dispatchTakePictureIntent(createImageFile());
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    private String createImageFile() throws IOException {
        // Create an image file name
        String timeStamp;
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        CurrentPhotoPath = image.getAbsolutePath();
        return CurrentPhotoPath;
    }


    private void dispatchTakePictureIntent(String CurrentPhotoPath) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = new File(CurrentPhotoPath);
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==2){
            if(resultCode == Activity.RESULT_OK) {
                //intent3 = data.getIntent();
                Bundle b = data.getExtras();
                i = b.getInt("position");
                TextView nameHabit = (TextView)findViewById(R.id.habitName2);
                HabitListController control = HabitListController.getInstance();
                nameHabit.setText(control.getHabit(i).getTitle().toString());
                HabitHistoryController control2 = HabitHistoryController.getInstance();
                control2.get(position2).setHabit(control.getHabit(i));
            }
        }

        if(requestCode == CHANGE_LOCATION_CODE && resultCode == Activity.RESULT_OK)
        {
            Bundle b = data.getExtras();
            Double latitude = b.getDouble("change_latitude");
            Double longtitude = b. getDouble("change_longtitude");
            String address = b.getString("change_address");

            T_address.setText(address);
            LatLng latLng = new LatLng(latitude,longtitude);


        }
    }
}