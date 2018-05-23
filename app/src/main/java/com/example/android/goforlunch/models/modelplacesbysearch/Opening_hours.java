package com.example.android.goforlunch.models.modelplacesbysearch;

/**
 * Created by Diego Fajardo on 23/05/2018.
 */
public class Opening_hours {

    private String open_now;

    private String[] weekday_text;

    public String getOpen_now ()
    {
        return open_now;
    }

    public void setOpen_now (String open_now)
    {
        this.open_now = open_now;
    }

    public String[] getWeekday_text ()
    {
        return weekday_text;
    }

    public void setWeekday_text (String[] weekday_text)
    {
        this.weekday_text = weekday_text;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [open_now = "+open_now+", weekday_text = "+weekday_text+"]";
    }
}
