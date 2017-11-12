package com.wsfmn.habittracker.habitcontrollers;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;


import com.wsfmn.habit.Date;
import com.wsfmn.habit.DateNotValidException;
import com.wsfmn.habit.Habit;
import com.wsfmn.habit.HabitCommentTooLongException;
import com.wsfmn.habit.HabitEvent;
import com.wsfmn.habit.HabitHistory;
import com.wsfmn.habit.HabitList;
import com.wsfmn.habit.HabitTitleTooLongException;
import com.wsfmn.habit.Profile;
import com.wsfmn.habitcontroller.OfflineController;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fredric on 2017-10-21.
 * Edited by nmayne on 2017-10-25.
 *
 */


public class OfflineControllerTest extends ActivityInstrumentationTestCase2 {

    public OfflineControllerTest() {
        super(OfflineController.class);
    }


    public void testHabitList() {
        HabitList habitList = new HabitList();

        try {
            Habit myHabit = new Habit("My Habit", new Date());
            habitList.addHabit(myHabit);

            OfflineController.StoreHabitList storeHabitListOffline = new OfflineController.StoreHabitList();
            storeHabitListOffline.execute(habitList);

            OfflineController.GetHabitList getHabitListOffline = new OfflineController.GetHabitList();
            getHabitListOffline.execute();

            HabitList habitListNew = getHabitListOffline.get();

            assertEquals(habitList.getHabit(0).getTitle(), habitListNew.getHabit(0).getTitle());


        } catch (HabitTitleTooLongException e) {
            Log.i("TestStoreGetHabits", e.toString());

        } catch (DateNotValidException e) {
            Log.i("TestStoreGetHabits", e.toString());

        } catch (InterruptedException e) {
            Log.i("TestStoreGetHabits", e.toString());

        } catch (ExecutionException e) {
            Log.i("TestStoreGetHabits", e.toString());

        }
    }


    public void testHabitHistory() {
        HabitHistory habitHistory = new HabitHistory();

        try {
            Habit myHabit = new Habit("My Habit", new Date());
            HabitEvent myDoneHabitEvent = new HabitEvent(myHabit, "Title", "Did my habit!", null);
            HabitEvent myNotDoneHabitEvent = new HabitEvent(myHabit, "Title", "Did my habit!", null);

            habitHistory.add(myDoneHabitEvent);
            habitHistory.add(myNotDoneHabitEvent);

            assertTrue(habitHistory.contains(myDoneHabitEvent));
            assertTrue(habitHistory.contains(myNotDoneHabitEvent));

            OfflineController.StoreHabitHistory storeHabitHistoryOffline = new OfflineController.StoreHabitHistory();
            storeHabitHistoryOffline.execute(habitHistory);

            OfflineController.GetHabitHistory getHabitHistoryOffline = new OfflineController.GetHabitHistory();
            getHabitHistoryOffline.execute();

            HabitHistory habitHistoryNew = getHabitHistoryOffline.get();

//            assertTrue(habitHistoryNew.contains(myDoneHabitEvent));
//            assertTrue(habitHistoryNew.contains(myNotDoneHabitEvent));



        } catch (HabitTitleTooLongException e) {
            Log.i("TestStoreGetHabits", e.toString());

        } catch (DateNotValidException e){
            Log.i("TestStoreGetHabits", e.toString());

        } catch (InterruptedException e) {
            Log.i("TestStoreGetHabits", e.toString());

        } catch (ExecutionException e) {
            Log.i("TestStoreGetHabits", e.toString());
        }
        catch (HabitCommentTooLongException e) {
            Log.i("TestStoreGetHabits", e.toString());

        }
    }


    /**
     *
     */
    public void testUserProfile() {
        OfflineController.StoreUserProfile storeUserProfile =
                new OfflineController.StoreUserProfile();

        OfflineController.GetUserProfile getUserProfile =
                new OfflineController.GetUserProfile();

        Profile profile = new Profile();
        profile.setName("USERNAME");

        storeUserProfile.execute(profile);

        Profile retrievedProfile = new Profile();

        assertEquals(retrievedProfile.getName(), "");

        try {
            getUserProfile.execute();
            retrievedProfile = getUserProfile.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Log.d("PROFILE1:", ": " + retrievedProfile.getName());


        assertEquals(retrievedProfile.getName(), "");


    }

    /**
     * Delay for this many milliseconds
     * @param ms milliseconds to delay
     */
    private void Delay(int ms){
        // Delay 1 second for transaction to finish
        long currentTime = Calendar.getInstance().getTimeInMillis();
        while((Calendar.getInstance().getTimeInMillis() - currentTime) < ms ){}
    }
}
