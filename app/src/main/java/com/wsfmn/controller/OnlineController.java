package com.wsfmn.controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.JestDroidClient;



import com.wsfmn.model.Habit;
import com.wsfmn.model.HabitEvent;
import com.wsfmn.model.HabitHistory;
import com.wsfmn.model.HabitList;
import com.wsfmn.model.ProfileName;
import com.wsfmn.model.Request;
import com.wsfmn.model.RequestList;
import com.wsfmn.view.AddNewHabitEventActivity;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.searchbox.client.JestResult;
import io.searchbox.core.Delete;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;



/**
 * A controller for managing Habit and HabitEvent model data stored on an ElasticSearch server.
 */
public class OnlineController {
    private static final String SERVER_URL = "http://cmput301.softwareprocess.es:8080";
    private static final String INDEX_BASE = "team18_";
    private static final String ID_TAG = "_id";
    private static final int ID_TAG_OFFSET = 6;
    private static final int ID_LENGTH = 20;
    private static JestDroidClient client;

    /**
     * When StoreHabits.execute(Habit... habits) is called on a StoreHabits object,
     * this method will proceed if the device is connected to the internet and will store the
     * given habits on an ElasticSearch DB.
     *
     * If the device is not connected to the internet this method fails silently.
     *
     * When each Habit is stored, an ElasticSearch ID is returned and the local Habit ID attribute
     * is updated with this value. This ID is used to update and delete the remote copy of each
     * Habit passed to StoreHabits.execute(Habit... habits).
     *
     * Created by romansky on 10/20/16. Customized by nmayne 10/22/17.
     */
    public static class StoreHabits extends AsyncTask<Habit, Void, Void> {
        @Override
        protected Void doInBackground(Habit... habits) {
            if (isConnected()) {
                verifySettings();
                for (Habit habit : habits) {
                    Index index;
                    index = new Index.Builder(habit)
                            .index(INDEX_BASE + App.USERNAME)
                            .type("habit")
                            .id(habit.getId())
                            .build();
                    try {
                        DocumentResult result = client.execute(index);
                        if (result.isSucceeded())
                            habit.setId(result.getId().toString());
                        else
                        Log.i("Error", "Elasticsearch was not able to add the habit");
                    } catch (Exception e) {
                        Log.i("Error", "Habit Tracker failed to build and send the habits");
                    }
                }
            }
            return null;
        }
    }

    /**
     * When DeleteHabits.execute(Habit... habits) is called on a DeleteHabits object,
     * this method will will delete the given habits on an ElasticSearch DB.
     *
     * If the device is not connected to the internet this method adds the Habit ID's
     * to Delete.sav so they can be deleted once a connection is established.
     *
     * Created by nmayne 11/07/17.
     */
    public static class DeleteHabits extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... habit_ids) {
            verifySettings();
            for (String h_id : habit_ids) {
                Delete delete = new Delete.Builder(h_id)
                        .index(INDEX_BASE + App.USERNAME)
                        .type("habit")
                        .build();
                try {
                    client.execute(delete);
                } catch (IOException e) {
                    Log.i("Error", "Delete Habit failed");
                }
            }
            return null;
        }
    }

    /**
     * When GetHabits.execute(String... search_params) is called on a GetHabits object, this method
     * will proceed if the device is connected to the internet and currently will return a
     * HabitList object containing at most 10 Habit objects that match the search parameter.
     *
     * If the device is not connected to the internet this method fails silently and returns null
     *
     * Created by romansky on 10/20/16. Customized by nmayne 10/22/17.
     */
    public static class GetHabits extends AsyncTask<String, Void, HabitList> {
        @Override
        protected HabitList doInBackground(String... search_parameters) {
            HabitList habitList = null;

            if (isConnected()) {
                verifySettings();
                habitList = new HabitList();

                String query = "{\"query\":{\"match_all\":{}}}";

                Search search = new Search.Builder(query)
                        .addIndex(INDEX_BASE + App.USERNAME)
                        .addType("habit")
                        .build();
                try {
                    SearchResult result = client.execute(search);
                    if (result.isSucceeded()) {
                        int idx = 0;
                        String JsonString = result.getJsonString();
                        for (SearchResult.Hit hit : result.getHits(Habit.class)) {
                            Habit habit = (Habit) hit.source;
                            idx = JsonString.indexOf(ID_TAG, idx) + ID_TAG_OFFSET;
                            habit.setId(JsonString.substring(idx, idx + ID_LENGTH));
                            habitList.addHabit(habit);
                        }
                    } else {
                        Log.i("Error", "The search query failed");
                    }
                } catch (Exception e) {

                    Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
                }
            }
            return habitList;
        }
    }

    /**
     * When StoreHabitEvents.execute(HabitEvent... habitEvents) is called on a StoreHabitEvents
     * object, this method will proceed if the device is connected to the internet and will store
     * the given habitEvents on an ElasticSearch DB.
     *
     * If the device is not connected to the internet this method fails silently.
     *
     * When each HabitEvent is stored, an ElasticSearch ID is returned and the local HabitEvent ID
     * attribute is updated with this value. This ID is used to update and delete the remote copy of each Habit
     * passed to StoreHabits.execute(Habit... habits).
     * Created by romansky on 10/20/16. Customized by nmayne 11/08/17.
     */
    public static class StoreHabitEvents extends AsyncTask<HabitEvent, Void, Void> {
        @Override
        protected Void doInBackground(HabitEvent... habitEvents) {
            if (isConnected()) {
                verifySettings();
                for (HabitEvent he : habitEvents) {
                    Index index;
                        index = new Index.Builder(he)
                                .index(INDEX_BASE + App.USERNAME)
                                .type("habitevent")
                                .id(he.getId())
                                .build();
                    try {
                        DocumentResult result = client.execute(index);
                        if (result.isSucceeded())
                            he.setId(result.getId().toString());
                        else
                            Log.i("Error", "Elasticsearch was not able to add the habit events");
                    } catch (Exception e) {
                        Log.i("Error", "Habit Tracker failed to build and send the habit events");
                    }
                }
            }
            return null;
        }
    }

    /**
     * When DeleteHabitEvents.execute is called on a DeleteHabitEvents object,
     * this method will will delete the given habit events on ElasticSearch DB.
     *
     * If the device is not connected to the internet this method adds the HabitEVent
     * ID's to Delete.sav file so they can be deleted once a connection is established.
     *
     * Created by nmayne 11/07/17.
     */
    public static class DeleteHabitEvents extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... habitEvent_ids) {
            verifySettings();
            for (String he_ids : habitEvent_ids) {
                Delete delete = new Delete.Builder(he_ids)
                        .index(INDEX_BASE + App.USERNAME)
                        .type("habitevent")
                        .build();
                try {
                    client.execute(delete);
                } catch (IOException e) {
                    Log.i("Error", "Delete Habit Event failed");
                }
            }
            return null;
        }
    }

    /**
     * Created by romansky on 10/20/16. Customized by nmayne 11/08/17.
     */
    public static class GetHabitEvents extends AsyncTask<String, Void, HabitHistory> {
        @Override
        protected HabitHistory doInBackground(String... search_parameters) {
            HabitHistory habitHistory = null;

            if (isConnected()) {
                verifySettings();
                habitHistory = new HabitHistory();
                String query = "{\"query\":{\"match_all\":{}}}";

                Search search = new Search.Builder(query)
                        .addIndex(INDEX_BASE + App.USERNAME)
                        .addType("habitevent")
                        .build();
                try {
                    SearchResult result = client.execute(search);
                    if (result.isSucceeded()) {
                        int idx = 0;
                        String JsonString = result.getJsonString();
                        for (SearchResult.Hit hit : result.getHits(HabitEvent.class)) {
                            HabitEvent he = (HabitEvent) hit.source;
                            idx = JsonString.indexOf(ID_TAG, idx) + ID_TAG_OFFSET;
                            he.setId(JsonString.substring(idx, idx + ID_LENGTH));
                            habitHistory.add(he);
                            Log.d("GotHabit:", he.getComment());
                        }
                    }

                    if (result.isSucceeded()) {
                        List<HabitEvent> foundHabitEvents = result.getSourceAsObjectList(HabitEvent.class);
                        habitHistory.addAllHabitEvents(foundHabitEvents);
                    } else {
                        Log.i("Error", "The search query failed");
                    }
                } catch (Exception e) {
                    Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
                }
            }
            return habitHistory;
        }
    }


    public static class AddFriend extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... search_parameters) {
            verifySettings();

            ProfileName newFriend = new ProfileName(App.USERNAME);

            Index index = new Index.Builder(newFriend)
                    .index(INDEX_BASE + search_parameters[0])
                    .type("friend")
                    .build();
            try {
                DocumentResult execute = client.execute(index);

                if (execute.isSucceeded()) {
                    newFriend.setId(execute.getId());
                } else {
                    Log.i("Error", "Could not send request");
                }
            } catch (Exception e) {


                Log.i("Error", "The application failed to build and send the requests");
            }

            return null;
        }
    }

    public void addFriend(String id){
        //Check controller for name
        OnlineController.AddFriend check =
                new OnlineController.AddFriend();
        check.execute(id);

    }

    public static class GetFriendNames extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... search_parameters) {
            verifySettings();


            ArrayList<String> names = new ArrayList<String>();
            // TODO Build the query
            String query = "{ \"_source\" :  [\"name\"]," +
                    "\"query\" : { \"match_all\" : { } } }";


            Search search = new Search.Builder(query)
                    .addIndex(INDEX_BASE + App.USERNAME)
                    .addType("friend")
                    .build();

            try {

                // TODO get the results of the query
                SearchResult result = client.execute(search);

                if (result.isSucceeded()) {
                    int idx = 0;
                    String JsonString = result.getJsonString();
                    for (SearchResult.Hit hit : result.getHits(ProfileName.class)) {
                        ProfileName fHabit = (ProfileName) hit.source;
                        names.add(fHabit.getName());
                    }
                }

                else {
                    Log.i("Error", "The search query failed to find any requests that matched");
                }
            }
            catch (Exception e) {


                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
            }
            System.out.println("finished");
            return names;
        }
    }


    public static class GetFriendScore extends AsyncTask<String, Void, ArrayList<ProfileName>> {
        @Override
        protected ArrayList<ProfileName> doInBackground(String... search_parameters) {
            verifySettings();
            ArrayList<ProfileName> ProfileNameScore = new ArrayList<ProfileName>();

            for(String name : search_parameters) {
                String query =  "{ \"query\": { \"match_phrase\": { \"name\": \""
                        + name + "\" } } }\n";
                Search search = new Search.Builder(query)
                        .addIndex(INDEX_BASE)
                        .addType("profilename")
                        .build();

                try {

                    // TODO get the results of the query
                    SearchResult result = client.execute(search);

                    if (result.isSucceeded()) {
                        int idx = 0;
                        String JsonString = result.getJsonString();
                        for (SearchResult.Hit hit : result.getHits(ProfileName.class)) {
                            ProfileName score = (ProfileName) hit.source;
                            System.out.println(score.getName());
                            ProfileNameScore.add(score);
                        }
                    } else {
                        Log.i("Error", "The search query failed to find any requests that matched");
                    }
                } catch (Exception e) {


                    Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
                }
            }
            return ProfileNameScore;
        }
    }



    public static class GetHabitNames extends AsyncTask<String, Void, ArrayList<Habit>> {
        @Override
        protected ArrayList<Habit> doInBackground(String... search_parameters) {
            verifySettings();

            ArrayList<Habit> habits = new ArrayList<Habit>();

            // TODO Build the query
            String query = "{\"query\" : { \"match_all\" : { } } }";

            for(String name : search_parameters) {
                Search search = new Search.Builder(query)
                        .addIndex(INDEX_BASE + name)
                        .addType("habit")
                        .build();

                try {

                    // TODO get the results of the query
                    SearchResult result = client.execute(search);

                    if (result.isSucceeded()) {
                        int idx = 0;
                        String JsonString = result.getJsonString();
                        for (SearchResult.Hit hit : result.getHits(Habit.class)) {
                            Habit fHabit = (Habit) hit.source;
                            fHabit.setOwner(name);
                            habits.add(fHabit);
                        }
                    } else {
                        Log.i("Error", "The search query failed to find any requests that matched");
                    }
                } catch (Exception e) {


                    Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
                }
            }
            return habits;
        }
    }

    public static class GetRecentEvent extends AsyncTask<String, Void, HabitEvent> {
        @Override
        protected HabitEvent doInBackground(String... search_parameters) {
            verifySettings();

            HabitEvent recent = null;

            // TODO Build the query
            String query = "{\"query\" : { \"match_phrase\" : {\"title\" : \"" +search_parameters[0] +"\"} }, " +
                    "\"size\" : 1, \"sort\" : [{\"actualdate\" : { \"order\" : \"desc\"}}] }";

                Search search = new Search.Builder(query)
                        .addIndex(INDEX_BASE + search_parameters[1])
                        .addType("habitevent")
                        .build();

                try {

                    // TODO get the results of the query
                    SearchResult result = client.execute(search);

                    if (result.isSucceeded()) {
                        int idx = 0;
                        String JsonString = result.getJsonString();
                        for (SearchResult.Hit hit : result.getHits(HabitEvent.class)) {
                            recent = (HabitEvent) hit.source;
                            recent.setOwner(search_parameters[1]);
                            return recent;
                        }
                    } else {
                        Log.i("Error", "The search query failed to find any requests that matched");
                    }
                } catch (Exception e) {


                    Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
                }
            return recent;
        }
    }


    public static class CheckFriends extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... search_parameters) {
            verifySettings();
            Boolean flag = false;
            // TODO Build the query
            String query = "{" + " \"query\": { \"match_phrase\": {\"name\":\"" + search_parameters[0] + "\"} }\n" + "}";
            Search search = new Search.Builder(query)
                    .addIndex(INDEX_BASE + App.USERNAME)
                    .addType("friend")
                    .build();
            try {
                // TODO get the results of the query
                SearchResult result = client.execute(search);
                if (result.isSucceeded()){
                    String JsonString = result.getJsonString();
                    for (SearchResult.Hit hit : result.getHits(ProfileName.class)) {

                        Log.d("Name Exisits:", "Name already in friendlist");
                        return false;
                    }
                }
                return true;
            }
            catch (Exception e) {


                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
            }
            return flag;
        }
    }

    public boolean checkFriends(String name){
        //Check controller for name
        boolean flag = false;
        OnlineController.CheckFriends check =
                new OnlineController.CheckFriends();
        check.execute(name);

        try{
            flag = check.get();

        } catch (Exception e) {
            Log.i("Error", "Couldn't get flag from async object");
        }
        return flag;

    }

    /**
     * Send Requests to ElasticSearch  proceed if the device is connected to the internet and will store the
     * given Request on an ElasticSearch DB. Request will be given an id from ElasticSearch.
     */
    public static class SendRequest extends AsyncTask<Request, Void, Void> {

        @Override
        protected Void doInBackground(Request... requests) {
            verifySettings();

            for (Request request : requests) {
                Index index = new Index.Builder(request)
                        .index(INDEX_BASE)
                        .type("request")
                        .build();
                try {
                    DocumentResult execute = client.execute(index);

                    if(execute.isSucceeded()) {
                        request.setId(execute.getId());
                    }
                    else
                    {
                        Log.i("Error", "Could not send request");
                    }
                }
                catch (Exception e) {


                    Log.i("Error", "The application failed to build and send the requests");
                }
            }
            return null;
        }
    }

    public static class DeleteRequest extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... search_parameters) {
            verifySettings();
                Delete delete = new Delete.Builder(search_parameters[0])
                        .index(INDEX_BASE)
                        .type("request")
                        .build();
                try {
                    client.execute(delete);
                } catch (IOException e) {
                    Log.i("Error", "Delete Habit Event failed");
                }
            return null;
        }
    }

    public void deleteRequest(String id){
        //Check controller for name
        if (isConnected()) {
            OnlineController.DeleteRequest check =
                    new OnlineController.DeleteRequest();
            check.execute(id);
        } else {
            // Store these Requests for online deletion upon next connection
            OfflineController.addToOfflineDelete("REQ", id);
        }

    }

    /**
     * Retrieve requests from ElasticSearch, Get back at most 10 Requests from Elastic Search with
     * given query search based on search_parameters.
     */
    public static class GetRequest extends AsyncTask<String, Void, RequestList> {
        @Override
        protected RequestList doInBackground(String... search_parameters) {
            verifySettings();

            RequestList requests = new RequestList();

            // TODO Build the query
            String query = "{\n" + " \"query\": { \"match_phrase\": {\"searchName\":\"" + search_parameters[0] + "\"} }\n" + "}";


            Search search = new Search.Builder(query)
                    .addIndex(INDEX_BASE)
                    .addType("request")
                    .build();

            try {

                // TODO get the results of the query
                SearchResult result = client.execute(search);

                if (result.isSucceeded()) {
                    int idx = 0;
                    String JsonString = result.getJsonString();
                    for (SearchResult.Hit hit : result.getHits(Request.class)) {
                        Request request = (Request) hit.source;
                        idx = JsonString.indexOf(ID_TAG, idx) + ID_TAG_OFFSET;
                        request.setId(JsonString.substring(idx, idx + ID_LENGTH));
                        requests.addRequst(request);
                        Log.d("GotRequest:", request.getId());
                    }
                }

                else {
                    Log.i("Error", "The search query failed to find any requests that matched");
                }
            }
            catch (Exception e) {


                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
            }
            return requests;
        }
    }


    /**
     * Check for a certain Request matching name, searchName, and  requestType. Checks ElasticSearch if
     * request exists and returns true or false if there is a hit.
     */
    public static class RequestExist extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... search_parameters) {
            verifySettings();
            Boolean flag = false;
            // TODO Build the query
            String query2 = "{\n" + " \"query\": { \"bool\": {\"must\":" +
                    "[{\"match\":  {\"name\":\""+ search_parameters[0] + "\"}}, " +
                    "{\"match\":  {\"requestType\":\""+ search_parameters[1] + "\"}}, " +
                    "{\"match\":  {\"searchName\":\""+ search_parameters[2] + "\"}} ] } } }";

            Search search = new Search.Builder(query2)
                    .addIndex(INDEX_BASE )
                    .addType("request")
                    .build();
            try {
                // TODO get the results of the query
                SearchResult result = client.execute(search);
                if (result.isSucceeded()){
                    String JsonString = result.getJsonString();
                    int idx = 0;
                    for (SearchResult.Hit hit : result.getHits(Request.class)) {
                        Log.d("Request Exists:", "Requesting already");
                        return false;
                    }
                }
                return true;
            }
            catch (Exception e) {


                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
            }
            return flag;
        }
    }

    public boolean checkRequest(String name, String requestType, String searchName){
        //Check controller for name
        boolean flag = false;
        OnlineController.RequestExist check =
                new OnlineController.RequestExist();
        check.execute(name, requestType, searchName);

        try{
            flag = check.get();

        } catch (Exception e) {
            Log.i("Error", "Couldn't get flag from async object");
        }
        return flag;

    }





    /**
     * Checking ElasticSearch if a name exists in DB. Will return true if the name is unique and false
     * if there is a hit.
     *
     */
    public static class CheckUnique extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... search_parameters) {
            verifySettings();
            Boolean flag = false;
            // TODO Build the query
            String query = "{" + " \"query\": { \"match_phrase\": {\"name\":\"" + search_parameters[0] + "\"} }\n" + "}";
            Search search = new Search.Builder(query)
                    .addIndex(INDEX_BASE)
                    .addType("profilename")
                    .build();
            try {
                // TODO get the results of the query
                SearchResult result = client.execute(search);
                if (result.isSucceeded()){
                    String JsonString = result.getJsonString();
                    for (SearchResult.Hit hit : result.getHits(ProfileName.class)) {

                        Log.d("Name Exisits:", "Name already in database");
                        return false;
                    }
                }
                return true;
            }
            catch (Exception e) {


                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
            }
            return flag;
        }
    }

    /**
     * OnlineController method to check name for activities to use
     * @param name
     * @return
     */
    public boolean checkName(String name){
        //Check controller for name
        boolean flag = false;
        OnlineController.CheckUnique check =
                new OnlineController.CheckUnique();
        check.execute(name);

        try{
            flag = check.get();

        } catch (Exception e) {
            Log.i("Error", "Couldn't get flag from async object");
        }
        return flag;

    }

    /**
     * Storing the user's ProfileName in ElasticSearch DB
     */
    public static class StoreNameInDataBase extends AsyncTask<ProfileName, Void, Void> {

        @Override
        protected Void doInBackground(ProfileName... names) {
            if (isConnected()) {
                verifySettings();

                for (ProfileName profileName : names) {
                    Index index = new Index.Builder(profileName)
                            .index(INDEX_BASE)
                            .type("profilename")
                            .build();
                    try {
                        DocumentResult result = client.execute(index);
                        if (result.isSucceeded()) {
                            profileName.setId(result.getId());
                        } else {
                            Log.i("Error", "Could not send profileName to elasticsearch");
                        }
                    } catch (Exception e) {
                        Log.i("Error", "The application failed to build and send the profileName");
                    }
                }
            }
            return null;
        }
    }

    /**
     * OnlineController Method for Activties to use to store profilename to ElasticSearch.
     * @param name
     */
    public void storeName(ProfileName name){
        OnlineController.StoreNameInDataBase store =
                new OnlineController.StoreNameInDataBase();
        store.execute(name);
    }

    /**
     * Deletes a profilename in ElasticSearch, Main use for intent testing. Cleaning up ElasticSearch DB
     */
    public static class DeleteProfileName extends AsyncTask<String, Void, ArrayList<ProfileName>> {
        @Override
        protected ArrayList<ProfileName> doInBackground(String... search_parameters) {
            if(isConnected()) {
                verifySettings();

                ArrayList<ProfileName> requests = new ArrayList<ProfileName>();

                // TODO Build the query
                String query = "{\n" + " \"query\": { \"match_phrase\": {\"name\":\"" + search_parameters[0] + "\"} }\n" + "}";

                DeleteByQuery delete = new DeleteByQuery.Builder(query)
                        .addIndex(INDEX_BASE)
                        .addType("profilename")
                        .build();
                try {
                    // TODO get the results of the query
                    JestResult result = client.execute(delete);
                } catch (Exception e) {
                    Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
                }
                return requests;
            }
            return null;
        }
    }

    /**
     * Created by romansky on 10/20/16. Customized by nmayne 10/22/17.
     */
    public static void verifySettings() {
        if (client == null) {
            DroidClientConfig.Builder builder = new DroidClientConfig.Builder(SERVER_URL);
            DroidClientConfig config = builder.build();

            JestClientFactory factory = new JestClientFactory();
            factory.setDroidClientConfig(config);
            client = (JestDroidClient) factory.getObject();
        }
    }

    /**
     * Delete all the objects online that were deleted while offline and then save the cleared list
     */
    public static void syncDeleted() {
        String[] deleted = new String[0];
        try {
            OfflineController.GetDeleted getDeleted = new OfflineController.GetDeleted();
            getDeleted.execute();
            deleted = getDeleted.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (deleted != null && deleted.length > 0) {
            for (int i = 0; i < deleted.length; i++) {
                if (deleted[i].startsWith("PRO")) {
                    DeleteProfileName delete_pro = new DeleteProfileName();
                    delete_pro.execute(deleted[i].substring(3));

                } else if (deleted[i].startsWith("HAB")) {
                    DeleteHabits delete_hab = new DeleteHabits();
                    delete_hab.execute(deleted[i].substring(3));

                } else if (deleted[i].startsWith("HEV")) {
                    DeleteHabitEvents delete_hev = new DeleteHabitEvents();
                    delete_hev.execute(deleted[i].substring(3));

                } else if (deleted[i].startsWith("REQ")) {
                    DeleteRequest delete_req = new DeleteRequest();
                    delete_req.execute(deleted[i].substring(3));
                }
            }
        }
        OfflineController.ResetDeleted resetDeleted = new OfflineController.ResetDeleted();
        resetDeleted.execute();
    }

    /**
     * Delete all locally known Habits and Habit Events at the current username index.
     */
    public void deleteAllHabitsAndEvents() {
        ArrayList<Habit> habitList = HabitListController.getInstance().getHabitList();
        ArrayList<HabitEvent> habitHisory =
                HabitHistoryController.getInstance().getHabitEventList();

        for (Habit h: habitList) {
            DeleteHabits deleteHabits = new DeleteHabits();
            try {
                deleteHabits.execute(h.getId()).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        for (HabitEvent he: habitHisory) {
            DeleteHabitEvents deleteHabitEvents = new DeleteHabitEvents();
            try {
                deleteHabitEvents.execute(he.getId()).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if the device is connected to the internet via wifi or mobile.
     * https://stackoverflow.com/questions/5474089/how-to-check-currently-internet-connection-is-available-or-not-in-android
     * @return Boolean: true is connection is alive, otherwise false
     */
    @NonNull
    public static Boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.CONTEXT.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        } else {
            return false;
        }
    }
}