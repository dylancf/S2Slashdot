package uk.co.aella_services.s2slashdot;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import 	java.net.URL;
import  java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import android.os.AsyncTask;
import android.widget.SimpleAdapter;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


public class MainActivity extends ActionBarActivity
{
    Toast toast;
    private ListView mainListView ;
    private ArrayAdapter<String> listAdapter ;
    private String mGlobalJson;
    private  int mThreshold = 0;
    static final int READ_BLOCK_SIZE = 100000;
    public final static String EXTRA_MESSAGE = "uk.co.aella_services.s2slashdot.MESSAGE";
    public final static String EXTRA_INDEX = "uk.co.aella_services.s2slashdot.INDEX";
    public final static String EXTRA_ISXKCD  = "uk.co.aella_services.s2slashdot.XKCD";
    private static final int RESULT_SETTINGS = 1;
    private int mLastPostition = 0;
    String mWelcome = "";
    String mLoading = "";
    String mFetchPrefs = "";
    private String mDeviceID = "";
    private Integer mCurrentStoryIndex = 0;
    private Integer mStoryCount = 10;
    final Handler handler = new Handler();
    private int mRefreshMins = 0;
    private int mFontSize = 0;
    private boolean mStopRefresh = false;
    private MenuItem  mMenu;
    public static final String MyPREFERENCES = "MyPrefs" ;
    private int mShowGraphics = 1;
    private boolean mXKCDMode = false;

    @Override
    public void onPause()
    {
        super.onPause();
    }
    @Override
    public void onResume()
    {

        if(mRefreshMins > 0 && mStopRefresh == false)
        {
            BackgroundLoadSlashDotXML();
        }
        mStopRefresh = false;
        super.onResume();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SetTheme();



        mWelcome = getResources().getString(R.string.welcome_instruct);
        mLoading = getResources().getString(R.string.loading_str);






        // Configure the refreshing colors


        try
        {
            FileInputStream fileIn=openFileInput("datacache");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();

            mGlobalJson = s;
            if(s == "")
            {
                ShowWelcome(mWelcome);
            }
            else
            {
                LoadSlashDotJSON(s);
            }


        }
        catch (Exception e)
        {
            ShowWelcome(mWelcome);
        }


    }
    private void SetTheme()
    {
        SharedPrefs mPrefs = new SharedPrefs(this);
        mThreshold = mPrefs.GetThreshold();
        mFetchPrefs = mPrefs.GetGetchFrefs();
        mStoryCount = mPrefs.GetStoryCount();
        mRefreshMins = mPrefs.GetRefreshPrefs();
        mFontSize = mPrefs.GetFontSize();
        mShowGraphics = mPrefs.GetShowGraphics();




        if(mPrefs.GetTheme() == 1)
        {

            this.setTheme(R.style.AppTheme);
        }
        else
        {
            this.setTheme(R.style.AppTheme_Dark);
        }
        setContentView(R.layout.activity_main);










    }



    private void BackgroundLoadSlashDotXML()
    {
        //ShowError(mLoading);
        if(mStopRefresh == false)
        {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;

            toast = Toast.makeText(context, mLoading, duration);
            toast.show();
            new MyTask().execute();
        }


    }
    public void ShowWelcome(String mErr)
    {
        mainListView = (ListView) findViewById( R.id.listarticle );
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
        listAdapter.add(mErr);
        mainListView.setAdapter(listAdapter);

    }
    public void ShowError(String mErr)
    {
        if(mStopRefresh == false) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            toast = Toast.makeText(context, mErr, duration);
            toast.show();
        }
        //new MyTask().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu =  menu.findItem(R.id.action_daynightmode);

        SharedPrefs mPrefs = new SharedPrefs(this);

        if(mPrefs.GetTheme() == 1)
        {
            mMenu.setIcon(R.drawable.sun);
        }
        else
        {
            mMenu.setIcon(R.drawable.moon);
        }


        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case RESULT_SETTINGS:
                SetTheme();
                LoadSlashDotJSON(mGlobalJson);

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
    /*
        if (id == R.id.action_donate)
        {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=UVWKYP5B5STYG"));
            startActivity(browserIntent);
            return true;
        }
        if (id == R.id.action_send_feedback)
        {
            Intent intent = new Intent(this, SendFeedBack.class);
            startActivity(intent);
            return true;
        }*/
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            try
            {
                Intent i = new Intent(this, SettingActivity.class);
                i.putExtra(EXTRA_MESSAGE, String.valueOf( mThreshold));
                startActivityForResult(i, RESULT_SETTINGS);
            }
            catch(Exception e)
            {
                ShowError(e.getMessage());
            }
            return true;
        }
        if (id == R.id.action_loadsl)
        {
            mXKCDMode = false;
            BackgroundLoadSlashDotXML();
            return true;
        }
        if (id == R.id.action_daynightmode)
        {
            SwapDayNightMode();
        }




        return super.onOptionsItemSelected(item);
    }
    private void SwapXKCD()
    {
        if(mXKCDMode == true)
        {
            mXKCDMode=false;
            LoadSlashDotJSON(mGlobalJson);

        }
        else
        {
            mXKCDMode = true;
            LoadSlashDotJSONXKCD(mGlobalJson);

        }
    }
    private void SwapDayNightMode()
    {

        SharedPrefs mPrefs = new SharedPrefs(this);

        SharedPreferences sharedpreferences;
        sharedpreferences = MainActivity.this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();


        if(mPrefs.GetTheme() == 1)
        {
            editor.putString("THEME", "2");
            editor.commit();
        }
        else
        {
            editor.putString("THEME", "1");
            editor.commit();
        }

        mPrefs = new SharedPrefs(this);
        if(mPrefs.GetTheme() == 1)
        {
            mMenu.setIcon(R.drawable.sun);
        }
        else
        {
            mMenu.setIcon(R.drawable.moon);
        }
        SetTheme();
        LoadSlashDotJSON(mGlobalJson);
    }
    private boolean TestWebData(String mJSON)
    {
        try
        {
            JSONObject mainObject = new JSONObject(mJSON);
            JSONArray mStories = mainObject.getJSONArray("stories");
            return true;
        }
        catch(Exception e)
        {
           return false;
        }
    }
    private void SaveSlashdotFile(String mJSON)
    {
        if(mJSON != "FAIL1")
        {
            String filename = "datacache";
            String string = mJSON;
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public class MySimpleAdapter extends SimpleAdapter
    {


        public MySimpleAdapter(Context context,
                               List<? extends Map<String, ?>> data,
                               int resource,
                               String[] from, int[] to)
        {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View returnView = super.getView(position, convertView, parent);

            TextView shopName = (TextView)returnView.findViewById(R.id.textViewDate);
            TextView col1 = (TextView)returnView.findViewById(R.id.textViewDate);
            shopName.setTextSize(mFontSize);

            TextView shopName2 = (TextView)returnView.findViewById(R.id.textViewDescription);
            shopName2.setTextSize(mFontSize);

            ImageView ImageViewItem = (ImageView)returnView.findViewById(R.id.catimage);

            HashMap<String, Object> obj = (HashMap<String, Object>) this.getItem(position);

            String mImage = (String) obj.get("image");



            TextView mViewDate = (TextView)returnView.findViewById(R.id.artickledate);
            mViewDate.setTextSize(mFontSize-2);

            if(mShowGraphics == 1)
            {
                Picasso.with(getApplicationContext()).load(mImage).error(R.drawable.no_image).into(ImageViewItem);
            }
            else
            {
                int mWidth = ImageViewItem.getLayoutParams().width;
                int mNewWidth = shopName.getLayoutParams().width;
                mNewWidth +=mWidth;

                ImageViewItem.getLayoutParams().width = 0;
                shopName.getLayoutParams().width = mNewWidth;
                mViewDate.getLayoutParams().width = mNewWidth;
            }

            return returnView;
        }

    }
    private void LoadSlashDotJSON(String mJSON)
    {
        mGlobalJson = mJSON;
        mainListView = (ListView) findViewById( R.id.listarticle );
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
        String mText="";

        mainListView = (ListView) findViewById(R.id.listarticle);
        ArrayList<HashMap<String, String>> feedList= new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();






        try
        {
            JSONObject mainObject = new JSONObject(mJSON);
            JSONArray mStories = mainObject.getJSONArray("stories");
            for(int i=0;i<mStories.length(); i++){
                JSONObject jsonas = mStories.getJSONObject(i);
                String headline = jsonas.getString("headline");
                String mCount = jsonas.getString("comment_count");
                String mTopic = jsonas.getString("topic_image");
                String mSubmitter = jsonas.getString("submitter");
                String mDate = jsonas.getString("day_published");

                //String dateStr = "Jul 16, 2013 12:08:59 AM";
                //SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = df.parse(mDate);
                df.setTimeZone(TimeZone.getDefault());
                df.applyPattern("dd MMMM yyyy HH:mm");
                String formattedDate = df.format(date);

                mSubmitter = mSubmitter+" on "+formattedDate;

                map = new HashMap<String, String>();
                map.put("date", headline);
                map.put("description", mCount);
                map.put("daypublished", mSubmitter);


                    map.put("image", "http://slashdot.aella-services.co.uk/images/" + mTopic+".png");


                feedList.add(map);

            }
            MySimpleAdapter simpleAdapter = new MySimpleAdapter(this, feedList, R.layout.view_item, new String[]{"date", "description","daypublished","catimage"}, new int[]{R.id.textViewDate, R.id.textViewDescription,R.id.artickledate});


            mainListView.setAdapter(simpleAdapter);



            OnItemClickListener listener = new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    LoadStoryItem(position,false);


                }
            };
            mainListView.setOnItemClickListener(listener);

        }
        catch(Exception e)
        {
            ShowError(e.getMessage());
        }

        //mainListView.setAdapter(listAdapter );

    }
    private void LoadSlashDotJSONXKCD(String mJSON)
    {
        mGlobalJson = mJSON;
        mainListView = (ListView) findViewById( R.id.listarticle );
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);
        String mText="";

        mainListView = (ListView) findViewById(R.id.listarticle);
        ArrayList<HashMap<String, String>> feedList= new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();






        try
        {
            JSONObject mainObject = new JSONObject(mJSON);
            JSONArray mStories = mainObject.getJSONArray("stories");
            for(int i=0;i<mStories.length(); i++){
                JSONObject jsonas = mStories.getJSONObject(i);
                String headline = jsonas.getString("headline");
                String mCount = jsonas.getString("comment_count");
                String mTopic = jsonas.getString("topic_image");
                String mSubmitter = jsonas.getString("submitter");
                String mDate = jsonas.getString("day_published");
                String mIsXKCD = jsonas.getString("is_xkcd");
                //String dateStr = "Jul 16, 2013 12:08:59 AM";
                //SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
                if(mIsXKCD.equals( "1"))
                {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = df.parse(mDate);
                    df.setTimeZone(TimeZone.getDefault());
                    df.applyPattern("dd MMMM yyyy HH:mm");
                    String formattedDate = df.format(date);

                    mSubmitter = mSubmitter+" on "+formattedDate;

                    map = new HashMap<String, String>();
                    map.put("date", headline);
                    map.put("description", mCount);
                    map.put("daypublished", mSubmitter);
                    map.put("positionfile", Integer.toString(i));


                    map.put("image", "http://slashdot.aella-services.co.uk/images/" + mTopic+".png");


                    feedList.add(map);
                }



            }
            MySimpleAdapter simpleAdapter = new MySimpleAdapter(this, feedList, R.layout.view_item, new String[]{"date", "description","daypublished","catimage","positionfile"}, new int[]{R.id.textViewDate, R.id.textViewDescription,R.id.artickledate});


            mainListView.setAdapter(simpleAdapter);



            OnItemClickListener listener = new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id)
                {
                    Object item = parent.getItemAtPosition(position);
                    Map<String, Object> map = (Map<String, Object>)parent.getItemAtPosition(position);
                    String _positionfile = (String) map.get("positionfile");
                    LoadStoryItem(Integer.parseInt(_positionfile),true);
                }
            };
            mainListView.setOnItemClickListener(listener);

        }
        catch(Exception e)
        {
            ShowError(e.getMessage());
        }

        //mainListView.setAdapter(listAdapter );

    }

    private void LoadStoryItem(Integer mPosition,Boolean mIsXKCD)
    {
        try
        {
            final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
            globalVariable.mStoryIndex = "";
            mLastPostition = mPosition;
            Intent i = new Intent(MainActivity.this, ReadStory.class);
            i.putExtra(EXTRA_INDEX, mPosition.toString());
            i.putExtra(EXTRA_ISXKCD, mIsXKCD.toString());

            startActivity(i);
            mStopRefresh=true;
        }
        catch (Exception e)
        {
            ShowError(e.getMessage());
        }
    }
    private class MyTask extends AsyncTask<Void, Void, Void>{

        String textResult;




        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                URL url;
                if(mFetchPrefs == "NEW")
                {
                   url = new URL("http://slashdot.aella-services.co.uk/get_fetch_prefs.php");
                   BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));


                    String str;
                    textResult="";
                    while ((str = in.readLine()) != null)
                    {
                        if(textResult == "")
                        {
                            textResult = str;
                        }
                        else
                        {
                            textResult = textResult+"\n"+str;
                        }

                    }
                    in.close();
                    SharedPreferences sharedpreferences;
                    sharedpreferences = MainActivity.this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("FETCHFREFS", textResult);
                    editor.commit();
                    mFetchPrefs = textResult;

                }

                String versionName = BuildConfig.VERSION_NAME;
                String StrThreshold = String.valueOf(mThreshold);
                String mLocale = Locale.getDefault().getISO3Language();
                url = new URL("http://slashdot.aella-services.co.uk/story_data/get_json.php?sh="+StrThreshold+"&ma="+mFetchPrefs+"&vs="+versionName+"&sc="+mStoryCount.toString()+"&lc="+mLocale);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str;
                textResult="";
                while ((str = in.readLine()) != null)
                {
                    if(textResult == "")
                    {
                        textResult = str;
                    }
                    else
                    {
                        textResult = textResult+"\n"+str;
                    }

                }
                in.close();

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                textResult = e.toString();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                textResult = "FAIL1";
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void result)
        {
            try
            {

                if(textResult != "FAIL1")
                {
                    if(TestWebData(textResult) == false)
                    {
                        ShowError("Not getting good results");
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Not getting good results");
                        alertDialog.setMessage("I tried to collect data from the server, but what I recieved doesn't seem to be what I need.\n\nI was expecting some kind of JSON string and got back something else.\nThis could be for several reasons.\n1) You are not signed in to your hotel wifi.\n2) The CIA is redirecting your traffic.\n3) Something that my creators have not thought about.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                    else
                    {
                        SaveSlashdotFile(textResult);
                        LoadSlashDotJSON(textResult);
                    }

                }
                else
                {
                    ShowError("Cannot Connect To Server");
                }
            }
            catch (Exception e)
            {
                ShowError(e.getMessage());
                return;
            }


            super.onPostExecute(result);
        }



    }
}

