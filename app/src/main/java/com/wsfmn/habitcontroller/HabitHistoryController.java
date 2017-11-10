package com.wsfmn.habitcontroller;

import android.support.annotation.Nullable;
import android.util.Log;

import com.wsfmn.habit.HabitEvent;
import com.wsfmn.habit.HabitHistory;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by nicholasmayne on 2017-11-08.
 */

public class HabitHistoryController {
    private static final HabitHistoryController INSTANCE = new HabitHistoryController();
    private static HabitHistory habitHistory = new HabitHistory();

    /**
     * Instantiate the habitHistory attribute
     * This pulls the data from the locally saved version via OfflineController
     */
    private HabitHistoryController() {
        try {
            OfflineController.GetHabitHistory getHabitHistoryOffline =
                    new OfflineController.GetHabitHistory();
            getHabitHistoryOffline.execute();
            habitHistory = getHabitHistoryOffline.get();
        } catch (InterruptedException e) {
            Log.i("Error", e.getMessage());

        } catch (ExecutionException e) {
            Log.i("Error", e.getMessage());
        }

    }

    /**
     * Access the instance of the HabitHistoryController singleton
     * @return INSTANCE: the instance of singleton HabitHistoryController
     */
    public static HabitHistoryController getInstance() {
        return INSTANCE;
    }

    /**
     * Checks it the HabitHistory is empty
     * @return Boolean true if HabitHistory is empty
     */
    public static Boolean isEmpty() {
        return habitHistory.isEmpty();
    }

    /**
     * Get the size of the habit history
     * @return int the number of entries in HabitHistory
     */
    public static int size() {
        return habitHistory.size();
    }

    /**
     * Appends a HabitEvent to the end of HabitHistory
     * @param he HabitEvent: HabitEvent to add to HabitHistory
     */
    public static void add(HabitEvent he) {
        habitHistory.add(he);
    }

    /**
     * Stores a HabitEvent online, locally (appended to HabitHistory), and offline
     * @param he HabitEvent: HabitEvent to add to HabitHistory
     */
    public static void addAndStore(HabitEvent he) {
        OnlineController.StoreHabitEvents storeHabitEvents =
                new OnlineController.StoreHabitEvents();
        OfflineController.StoreHabitHistory storeHabitHistory =
                new OfflineController.StoreHabitHistory();

        try {
            storeHabitEvents.execute(he).get(); // .get() waits for this task to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        habitHistory.add(he);
        storeHabitHistory.execute(habitHistory);
    }

    /**
     * Return the HabitEvent at a particular index
     * @param idx int: the index of the HabitEvent to get
     * @return HabitEvent at the specified index
     * @throws IndexOutOfBoundsException
     */
    public static HabitEvent get(int idx) throws IndexOutOfBoundsException {
        return habitHistory.get(idx);
    }

    /**
     * Removes and returns a HabitEvent at the specified index in HabitHistory,
     * and decrements the HabitHistory indices that follow it.
     * @param idx int: the index of the HabitEvent to remove
     * @return HabitEvent removed from the specified index
     * @throws IndexOutOfBoundsException
     */
    public static HabitEvent remove(int idx) throws IndexOutOfBoundsException{
        return habitHistory.remove(idx);
    }

    /**
     * Removes and returns a HabitEvent from HabitHistory,
     * and decrements the HabitHistory indices that follow it.
     * @param habitEvent int: the index of the HabitEvent to remove
     * @return HabitEvent removed from HabitHistory or null if in HabitHistory
     * @throws IndexOutOfBoundsException
     */
    @Nullable
    public static HabitEvent remove(HabitEvent habitEvent) throws IndexOutOfBoundsException{
        int idx = habitHistory.indexOf(habitEvent);
        if (idx != -1) {
            return habitHistory.remove(idx);
        } else {
            return null;
        }
    }

    /**
     * Check to see if a HabitEvent is in HabitHistory
     * @param habitEvent HabitEvent: check HabitHistory for this HabitEvent
     * @return Boolean true if the HabitEvent is in HabitHistory
     */
    public static Boolean contains(HabitEvent habitEvent) {
        return habitHistory.contains(habitEvent);
    }

    /**
     * Get the first index of the specified HabitEvent, if it is in HabitHistory
     * @param habitEvent HabitEvent: return the first index of this HabitEvent
     * @return int first index of the specified HabitEvent
     */
    public static int indexOf(HabitEvent habitEvent){
        return habitHistory.indexOf(habitEvent);
    }

    /**
     * Add a List of HabitEvents to the HabitHistory
     * @param habitEvents List<HabitEvent>: a List of HabitEvents to add to HabitHistory
     */
    public static void addAllHabitEvents(List<HabitEvent> habitEvents) {
        habitHistory.addAllHabitEvents(habitEvents);
    }

    /**
     * Stores HabitHistory online, and offline
     */
    public static void storeAll() {
        OnlineController.StoreHabitEvents storeHabitEvents =
                new OnlineController.StoreHabitEvents();

        OfflineController.StoreHabitHistory storeHabitHistory =
                new OfflineController.StoreHabitHistory();

        int size = habitHistory.size();
        HabitEvent[] habitEvents = new HabitEvent[size];

        for (int i = 0; i < size; i++){
            habitEvents[i] = habitHistory.get(i);
        }

        try {
            storeHabitEvents.execute(habitEvents).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        storeHabitHistory.execute(habitHistory);

    }
}