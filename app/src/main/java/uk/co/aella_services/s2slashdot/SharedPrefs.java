package uk.co.aella_services.s2slashdot;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by dylan_000 on 29/06/2015.
 */
public class SharedPrefs
{
    public static final String MyPREFERENCES = "MyPrefs" ;
    private int mTheme = 1;
    private int mThreshhold = 2;
    private int mSwipe = 2;
    private int mSwipeDistance = 70;
    private int mStoryCount = 10;
    private int mStoryRefresh = 0;
    private int mFontSize=18;
    private String mFetchFrefs = "NEW";
    private int mShowGraphics = 1;
    SharedPreferences sharedpreferences;

    public SharedPrefs(Context mContext)
    {
        sharedpreferences = mContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        mTheme = Integer.parseInt(sharedpreferences.getString("THEME", "1"));
        mThreshhold = Integer.parseInt(sharedpreferences.getString("THRESHOLD","2"));
        mFetchFrefs = sharedpreferences.getString("FETCHFREFS", "NEW");
        mSwipe = Integer.parseInt(sharedpreferences.getString("SWIPE", "1"));
        mStoryCount = Integer.parseInt(sharedpreferences.getString("STORYCOUNT","20"));
        mStoryRefresh = Integer.parseInt(sharedpreferences.getString("STORYREFRESH","0"));
        mFontSize = Integer.parseInt(sharedpreferences.getString("FONTSIZE","14"));
        mShowGraphics = Integer.parseInt(sharedpreferences.getString("SHOWGRAPHIC","1"));
        mSwipeDistance = Integer.parseInt(sharedpreferences.getString("SWIPEDISTANCE","70"));

    }
    public int GetTheme() {return mTheme;}
    public int GetFontSize() {return mFontSize;}
    public int GetThreshold()
    {
        return mThreshhold;
    }
    public int GetSwipe()
    {
        return mSwipe;
    }
    public int GetSwipeDistance()
    {
        return mSwipeDistance;
    }
    public int GetStoryCount()
    {
        return mStoryCount;
    }
    public String GetGetchFrefs()
    {
        return mFetchFrefs;
    }
    public int GetRefreshPrefs()
    {
        return mStoryRefresh;
    }
    public int GetShowGraphics()
    {
        return mShowGraphics;
    }
}
