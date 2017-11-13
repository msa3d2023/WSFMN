package com.wsfmn.habittracker.habittracker;


import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.robotium.solo.Solo;
import com.wsfmn.habittracker.MainActivity;
import com.wsfmn.habittracker.ProfileActivity;
import com.wsfmn.habittracker.R;
import com.wsfmn.habittracker.UserName_Activity;

import junit.framework.TestCase;

import static java.lang.Thread.sleep;


/**
 * Created by Fredric on 2017-11-12.
 */



public class ProfileActivityTest extends ActivityInstrumentationTestCase2<ProfileActivity> {
    private Solo solo;

    public ProfileActivityTest() {
        super(com.wsfmn.habittracker.ProfileActivity.class);
    }

    public void setUp() throws Exception{
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testStart() throws Exception {
        Activity activity = getActivity();

    }

    // Must wipe memory first, There needs to be no username in local files.
    // Test if we can get to UserName_Activity from ProfileActivity if there is no profilename.
    public void testProfileName(){
        solo.assertCurrentActivity("Wrong Activity", ProfileActivity.class);
        solo.enterText((EditText) solo.getView(R.id.yourUserName), "test");
        solo.clickOnButton("Confirm");
        solo.clickOnButton("OK");

    }

    //Part 5
    public void testShareButton(){

    }

    //Part 5
    public void testFollowButton(){

    }


    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

}
