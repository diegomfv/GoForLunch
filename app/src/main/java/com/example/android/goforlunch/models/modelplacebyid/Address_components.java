package com.example.android.goforlunch.models.modelplacebyid;

/**
 * Created by Diego Fajardo on 19/05/2018.
 */
public class Address_components
{
    private String long_name;

    private String[] types;

    private String short_name;

    public String getLong_name ()
    {
        return long_name;
    }

    public void setLong_name (String long_name)
    {
        this.long_name = long_name;
    }

    public String[] getTypes ()
    {
        return types;
    }

    public void setTypes (String[] types)
    {
        this.types = types;
    }

    public String getShort_name ()
    {
        return short_name;
    }

    public void setShort_name (String short_name)
    {
        this.short_name = short_name;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [long_name = "+long_name+", types = "+types+", short_name = "+short_name+"]";
    }
}