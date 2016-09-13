package uk.co.aella_services.s2slashdot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.widget.TextView;

public class SettingActivity extends ActionBarActivity {

    public static final String MyPREFERENCES = "MyPrefs" ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SetTheme();




    }
    private void SetTheme()
    {

        SharedPrefs mPrefs = new SharedPrefs(this);
        if(mPrefs.GetTheme() == 1)
        {
            this.setTheme(R.style.AppTheme);
        }
        else
        {
            this.setTheme(R.style.AppTheme_Dark);
        }
        setContentView(R.layout.activity_setting);
        TextView t = (TextView) findViewById(R.id.theme_setting);
        try {
            if (mPrefs.GetTheme() == 1) {
                t.setText(getResources().getString(R.string.SettingsMenuTheme)+" Black On White");
            } else {
                t.setText(getResources().getString(R.string.SettingsMenuTheme)+" White On Black");
            }
        }
        catch (Exception e)
        {
            t.setText(getResources().getString(R.string.SettingsMenuTheme)+" White On Black");
        }

        t = (TextView) findViewById(R.id.threshold_setting);
        t.setText(getResources().getString(R.string.SettingsMenuThreshHold)+" " + String.valueOf(mPrefs.GetThreshold()));

        t = (TextView) findViewById(R.id.swipe_setting);
        if(mPrefs.GetSwipe() == 1)
        {
            t.setText(getResources().getString(R.string.SettingsMenuSwipe)+" Enabled");
        }
        else
        {
            t.setText(getResources().getString(R.string.SettingsMenuSwipe)+" Disabled");
        }
        t = (TextView) findViewById(R.id.story_count_setting);
        t.setText(getResources().getString(R.string.SettingsMenuStoryCount)+" " + String.valueOf(mPrefs.GetStoryCount()));

        String mRefresh = String.valueOf(mPrefs.GetRefreshPrefs());
        if(mRefresh.equals("0")  )
        {
            mRefresh = "No";
        }
        else
        {
            mRefresh  = "Yes";
        }
        t = (TextView) findViewById(R.id.auto_refresh_setting);
        t.setText(getResources().getString(R.string.SettingsRefresh)+" " + mRefresh);


        t = (TextView) findViewById(R.id.font_size_setting);
        t.setText(getResources().getString(R.string.SettingsMenuFontSize)+" " + String.valueOf(mPrefs.GetFontSize()));

        String mShowGraphs = String.valueOf(mPrefs.GetShowGraphics());
        if(mShowGraphs.equals("0")  )
        {
            mShowGraphs = " No";
        }
        else
        {
            mShowGraphs = " Yes";
        }
        t = (TextView) findViewById(R.id.story_show_graphic);
        t.setText("Display Graphics:" + mShowGraphs);

        String mSwipeDistance = String.valueOf(mPrefs.GetSwipeDistance());
        t = (TextView) findViewById(R.id.swipe_distance);
        t.setText("Swipe Distance: " + mSwipeDistance);



        ThemeClick();
        ThreshHoldClick();
        SwipeClick();
        StoryCountClick();
        FontSizeClick();
        BackGroundRefreshClick();
        ShowGraphicClick();
        SwipeDistanceClick();
    }
    public void ThreshHoldClick() {
        TextView t = (TextView) findViewById(R.id.threshold_setting);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog levelDialog;

                final CharSequence[] items = {"-1","0","1", "2", "3", "4", "5"};

                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Select Comment Threshold");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        SharedPreferences sharedpreferences;
                        sharedpreferences = SettingActivity.this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("THRESHOLD", String.valueOf(item-1));
                        editor.commit();
                        dialog.dismiss();
                        SetTheme();
                    }
                });
                levelDialog = builder.create();
                levelDialog.show();
            }
        });
    }
    public void SwipeDistanceClick() {
        TextView t = (TextView) findViewById(R.id.swipe_distance);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog levelDialog;

                final CharSequence[] items = {"70","140","210", "280", "350", "420", "490"};

                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Select Swipe Distance");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        SharedPreferences sharedpreferences;
                        sharedpreferences = SettingActivity.this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("SWIPEDISTANCE", String.valueOf((item+1)*70));
                        editor.commit();
                        dialog.dismiss();
                        SetTheme();
                    }
                });
                levelDialog = builder.create();
                levelDialog.show();
            }
        });
    }
    public void BackGroundRefreshClick() {
        TextView t = (TextView) findViewById(R.id.auto_refresh_setting);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog levelDialog;

                final CharSequence[] items = {"No","Yes"};

                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Choose to refresh on load or not");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        SharedPreferences sharedpreferences;
                        sharedpreferences = SettingActivity.this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        Integer mSetting = item;
                        //mSetting++;
                        mSetting = mSetting;
                        editor.putString("STORYREFRESH", String.valueOf(mSetting));
                        editor.commit();
                        dialog.dismiss();
                        SetTheme();
                    }
                });
                levelDialog = builder.create();
                levelDialog.show();
            }
        });
    }
    public void StoryCountClick() {
        TextView t = (TextView) findViewById(R.id.story_count_setting);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog levelDialog;

                final CharSequence[] items = {"10","20", "30", "40", "50","60","70", "80", "90", "100"};

                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Select Story Count");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        SharedPreferences sharedpreferences;
                        sharedpreferences = SettingActivity.this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        Integer mSetting = item;
                        mSetting++;
                        mSetting = mSetting*10;
                        editor.putString("STORYCOUNT", String.valueOf(mSetting));
                        editor.commit();
                        dialog.dismiss();
                        SetTheme();
                    }
                });
                levelDialog = builder.create();
                levelDialog.show();
            }
        });
    }
    public void FontSizeClick()
    {
        TextView t = (TextView) findViewById(R.id.font_size_setting);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CharSequence items[] = new CharSequence[] {"10", "11","12","13","14","15","16","17","18","19","20","21","22","23","24","25"};

                AlertDialog levelDialog;


                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Select A Font Size");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item)
                    {
                        SharedPreferences sharedpreferences;
                        sharedpreferences = SettingActivity.this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("FONTSIZE", String.valueOf(item + 10));
                        editor.commit();
                        dialog.dismiss();
                        SetTheme();
                    }
                });
                levelDialog = builder.create();
                levelDialog.show();



            }
        });
    }
    public void ThemeClick()
    {
        TextView t = (TextView) findViewById(R.id.theme_setting);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CharSequence items[] = new CharSequence[] {"Black On White", "White On Black"};

                AlertDialog levelDialog;


                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Select Theme");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item)
                    {
                        SharedPreferences sharedpreferences;
                        sharedpreferences = SettingActivity.this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("THEME", String.valueOf(item + 1));
                        editor.commit();
                        dialog.dismiss();
                        SetTheme();
                    }
                });
                levelDialog = builder.create();
                levelDialog.show();



            }
        });
    }
    public void SwipeClick()
    {
        TextView t = (TextView) findViewById(R.id.swipe_setting);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CharSequence items[] = new CharSequence[] {"Enabled", "Disabled"};

                AlertDialog levelDialog;


                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Enable/Disable Swipe");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        SharedPreferences sharedpreferences;
                        sharedpreferences = SettingActivity.this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("SWIPE", String.valueOf(item + 1));
                        editor.commit();
                        dialog.dismiss();
                        SetTheme();
                    }
                });
                levelDialog = builder.create();
                levelDialog.show();



            }
        });
    }
    public void ShowGraphicClick()
    {
        TextView t = (TextView) findViewById(R.id.story_show_graphic);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CharSequence items[] = new CharSequence[] {"No", "Yes"};

                AlertDialog levelDialog;


                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Show Graphics");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        SharedPreferences sharedpreferences;
                        sharedpreferences = SettingActivity.this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("SHOWGRAPHIC", String.valueOf(item ));
                        editor.commit();
                        dialog.dismiss();
                        SetTheme();
                    }
                });
                levelDialog = builder.create();
                levelDialog.show();



            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == android.R.id.home) {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            onBackPressed();
            return true;
        }



        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed()
    {

        String mThreshHold = "3";


        String filename = "app_settings";
        String string = "{" + "\"threshold\":"+mThreshHold+"}";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finish();

    }
    @Override
    public void finish() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);

        super.finish();
    }
}
