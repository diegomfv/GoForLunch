package com.example.android.goforlunch.models.modelplacebyid;

/**
 * Created by Diego Fajardo on 19/05/2018.
 */
public class Reviews
{
    private String time;

    private String text;

    private String profile_photo_url;

    private String relative_time_description;

    private String author_url;

    private String author_name;

    private String rating;

    private String language;

    public String getTime ()
    {
        return time;
    }

    public void setTime (String time)
    {
        this.time = time;
    }

    public String getText ()
    {
        return text;
    }

    public void setText (String text)
    {
        this.text = text;
    }

    public String getProfile_photo_url ()
    {
        return profile_photo_url;
    }

    public void setProfile_photo_url (String profile_photo_url)
    {
        this.profile_photo_url = profile_photo_url;
    }

    public String getRelative_time_description ()
    {
        return relative_time_description;
    }

    public void setRelative_time_description (String relative_time_description)
    {
        this.relative_time_description = relative_time_description;
    }

    public String getAuthor_url ()
    {
        return author_url;
    }

    public void setAuthor_url (String author_url)
    {
        this.author_url = author_url;
    }

    public String getAuthor_name ()
    {
        return author_name;
    }

    public void setAuthor_name (String author_name)
    {
        this.author_name = author_name;
    }

    public String getRating ()
    {
        return rating;
    }

    public void setRating (String rating)
    {
        this.rating = rating;
    }

    public String getLanguage ()
    {
        return language;
    }

    public void setLanguage (String language)
    {
        this.language = language;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [time = "+time+", text = "+text+", profile_photo_url = "+profile_photo_url+", relative_time_description = "+relative_time_description+", author_url = "+author_url+", author_name = "+author_name+", rating = "+rating+", language = "+language+"]";
    }
}
