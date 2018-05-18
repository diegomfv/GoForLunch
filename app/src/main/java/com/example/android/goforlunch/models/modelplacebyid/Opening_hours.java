package com.example.android.goforlunch.models.modelplacebyid;

/**
 * Created by Diego Fajardo on 19/05/2018.
 */
public class Opening_hours
{
    private Periods[] periods;

    private String open_now;

    private String[] weekday_text;

    public Periods[] getPeriods ()
    {
        return periods;
    }

    public void setPeriods (Periods[] periods)
    {
        this.periods = periods;
    }

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
        return "ClassPojo [periods = "+periods+", open_now = "+open_now+", weekday_text = "+weekday_text+"]";
    }
}
