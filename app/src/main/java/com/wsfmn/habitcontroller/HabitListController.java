package com.wsfmn.habitcontroller;

import android.util.Log;

import com.wsfmn.habit.Date;
import com.wsfmn.habit.Habit;
import com.wsfmn.habit.HabitList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by musaed on 2017-11-04.
 */

public class HabitListController {

    /**
     * This controller uses Singleton design pattern.
     * One instance of the model HabitList is created
     * and can be accessed anywhere in the program by
     * creating a HabitListController object
     *
     *
     *      HabitListController c = new HabitListController();
     *
     *
     * When this is first called, a new model is initialized
     * and we call init to populate the model with the data
     * (you don't need to do this, this is done by the constructor)
     *
     * Everything in the model is updated by calling methods
     * from the controller
     *
     *      c.addHabit(Habit);
     *      c.store();
     *
     * we call c.store to store the new habit in the file
     */

    private static HabitList habitList = null;

    public static HabitList getInstance() {
        if (habitList == null) {
            habitList = new HabitList();
            init();
        }

        return habitList;
    }

    public void addHabit(Habit habit) {
        habitList.addHabit(habit);
    }

    public void deleteHabit(Habit habit){
        habitList.deleteHabit(habit);
    }

    public void deleteHabitAt(int index){
        habitList.deleteHabitAt(index);
    }

    public int getSize() {
        return habitList.getSize();
    }

    public void addAllHabits(List<Habit> habitsToAdd) {
        habitList.addAllHabits(habitsToAdd);
    }

    public Habit getHabit(int index){
        return habitList.getHabit(index);
    }

    public void setHabit(int index, Habit habit){
        habitList.setHabit(index, habit);
    }


    public boolean hasHabit(Habit habit){
        return habitList.hasHabit(habit);
    }

    public ArrayList<Habit> getHabitList(){
        return  habitList.getHabitList();
    }

    public ArrayList<Habit> getHabitsWithDate(Date date){
        return getHabitsWithDate(date);
    }

    /**
     * used to load data into habitlist once it is created
     */
    public static void init() {
        try {
            OfflineController.GetHabitList getHabitListOffline =
                    new OfflineController.GetHabitList();
            getHabitListOffline.execute();
            habitList = getHabitListOffline.get();
        } catch (InterruptedException e) {
            Log.i("Error", e.getMessage());

        } catch (ExecutionException e) {
            Log.i("Error", e.getMessage());
        }
    }

    public void store(){
        OfflineController.StoreHabitList storeHabitListOffline = new OfflineController.StoreHabitList();
        storeHabitListOffline.execute(habitList);
    }
}