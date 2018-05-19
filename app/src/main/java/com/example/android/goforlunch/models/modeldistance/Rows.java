package com.example.android.goforlunch.models.modeldistance;

/**
 * Created by Diego Fajardo on 19/05/2018.
 */
public class Rows
{
    private Elements[] elements;

    public Elements[] getElements ()
    {
        return elements;
    }

    public void setElements (Elements[] elements)
    {
        this.elements = elements;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [elements = "+elements+"]";
    }
}
