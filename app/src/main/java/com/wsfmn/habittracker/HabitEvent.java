package com.wsfmn.habittracker;

import android.media.Image;
import android.widget.ImageView;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Date;

/**
 * Created by skhanna1 on 10/16/17.
 */

public class HabitEvent{
    private String comment;
    private Habit habit;
    private Date date;
    private Boolean done;

    Integer location;
    //Will change to appropriate Data Type when implement it(ImageView).
    String pic;
  
    public HabitEvent(Habit habit, Date date, Boolean done, String comment) throws HabitCommentTooLongException{
        this.habit = habit;
        this.date = date;
        this.done = done;

        if(comment.length() > 20){
            throw new HabitCommentTooLongException();
        }
        this.comment = comment;
    }

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) throws HabitCommentTooLongException {
        if(comment.length() > 20){
            throw new HabitCommentTooLongException();
        }
        this.comment = comment;
    }


    public Date getDate(){return date;}

    public void setDate(Date date){this.date = date;}

    public void setDone(Boolean done) {
        this.done = done;
    }

    public Boolean getDone() {
        return done;
    }

    public Integer getLocation(){return this.location;}

    public String getPic(){return this.pic;}

    public void location(){this.location = 5;}

    public void AddPic(){this.pic = "Image";}

    public void updateHabitHistory(){}



}
