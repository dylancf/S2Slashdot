package uk.co.aella_services.s2slashdot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import android.view.View;
import android.text.Spanned;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


public class ReadStory extends ActionBarActivity {

    private ListView mainListView ;
    private String mStrHeadline;
    private String mStrURL;
    float historicX = Float.NaN, historicY = Float.NaN;
    static final int TRIGGER_DELTA = 50; // Number of pixels to travel till trigger
    static final int READ_BLOCK_SIZE = 100000;
    String mJSONData = "";
    String mJSONDataExternal = "";
    String mJSONIndex = "";
    Integer mStoryCount = 0;
    private int mDefaultThreshold = 0;
    private int mFontSize;
    boolean CanSwipe = false;
    private GestureDetector gestureDetector;
    private int mShowGraphics = 1;
    private Boolean mIsXKCD = false;
    private int mSwipeDistance = 70;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        SetTheme();




        //((ListView)findViewById(R.id.listcomments)).setOnTouchListener(myGestureListener);

        gestureDetector = new GestureDetector(new SwipeGestureDetector(mSwipeDistance));

        Intent intent = getIntent();

        final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        mIsXKCD = Boolean.parseBoolean(intent.getStringExtra(MainActivity.EXTRA_ISXKCD));
        if(globalVariable.mStoryIndex.equals(""))
        {
            mJSONIndex = intent.getStringExtra(MainActivity.EXTRA_INDEX);
        }
        else
        {
            mJSONIndex = globalVariable.mStoryIndex;
        }

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

            mJSONData = s;
            if(s == "")
            {

            }
            else
            {
                LoadJsonStory(-1);
            }




        }
        catch (Exception e){ }



    }
    private void SetTheme()
    {
        SharedPrefs mPrefs = new SharedPrefs(this);
        mFontSize = mPrefs.GetFontSize();
        mDefaultThreshold = mPrefs.GetThreshold();
        mShowGraphics = mPrefs.GetShowGraphics();
        mSwipeDistance = mPrefs.GetSwipeDistance();
        if(mPrefs.GetTheme() == 1)
        {
            this.setTheme(R.style.AppTheme);
        }
        else
        {
            this.setTheme(R.style.AppTheme_Dark);
        }
        setContentView(R.layout.activity_read_story);
        CanSwipe=false;
        if (mPrefs.GetSwipe() == 1)
        {
            CanSwipe = true;
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        if(CanSwipe == false)
        {
            return super.dispatchTouchEvent(ev);
        }
        if (gestureDetector.onTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read_story, menu);
        return true;
    }
    private void onLeftSwipe()
    {
        Integer mIndex = Integer.parseInt(mJSONIndex);
        mIndex++;
        if(mIndex > mStoryCount-1)
        {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, "End Of Stories", duration);
            toast.show();
            return;
        }
        mJSONIndex = String.valueOf(mIndex);
        LoadJsonStory(-1);
    }

    private void onRightSwipe() {
        Integer mIndex = Integer.parseInt(mJSONIndex);
        if (mIndex == 0)
        {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, "Start Of Stories", duration);
            toast.show();
            return;
        }
        mIndex--;
        mJSONIndex = String.valueOf(mIndex);
        LoadJsonStory(-1);
    }
    private void LoadJsonStoryFromSlashDot(int mTempThreshold)
    {
        MyTask mTask = new MyTask();
        mTask.mTempThreshold = mTempThreshold;
        mTask.execute();
    }
    public class MySimpleAdapter extends SimpleAdapter{

        public MySimpleAdapter(Context context,
                               List<? extends Map<String, ?>> data,
                               int resource,
                               String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View returnView = super.getView(position, convertView, parent);
            TextView shopName = (TextView)returnView.findViewById(R.id.txttitle);
            shopName.setTextSize(mFontSize);

            shopName = (TextView)returnView.findViewById(R.id.txtcontent);
            shopName.setTextSize(mFontSize);

            TextView mAuther = (TextView)returnView.findViewById(R.id.txtauther);
            mAuther.setTextSize(mFontSize-1);

            return returnView;
        }

    }
    private void LoadJsonStoryFromSlashDot2(int mTempThreshold)
    {
        JSONObject mainObject;
        boolean mCanAdd = false;
        try
        {

                JSONObject mainSuperObject = new JSONObject(mJSONData);
                JSONArray mStories = mainSuperObject.getJSONArray("stories");
                mStoryCount = mStories.length();
                Integer mJSONIndexInt = Integer.parseInt(mJSONIndex);
                mainObject = new JSONObject(mStories.getJSONObject(mJSONIndexInt).toString());

            mainListView = (ListView) findViewById(R.id.listcomments);
            ArrayList<HashMap<String, String>> feedList= new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map = new HashMap<String, String>();

            map = new HashMap<String, String>();
            map.put("title","");
            map.put("content", mainObject.getString("introtext"));
            map.put("auther","");
            feedList.add(map);


            JSONObject mainCommentSuperObject = new JSONObject(mJSONDataExternal);
            JSONArray mComments = mainCommentSuperObject.getJSONArray("comments");

            TextView mCommentCount = (TextView) findViewById(R.id.textViewDescription);
            mCommentCount.setText(String.valueOf(mComments.length()));
            mCommentCount.setTextSize(mFontSize);

            for(int i=0;i<mComments.length(); i++)
            {
                mCanAdd=true;
                JSONObject jsonas = mComments.getJSONObject(i);
                if(mTempThreshold > -1)
                {
                    mCanAdd=false;
                    if(Integer.parseInt(jsonas.getString("points")) >= mTempThreshold)
                    {
                        mCanAdd = true;
                    }
                }
                if(mCanAdd == true)
                {
                    String title = jsonas.getString("subject");
                    String score = "<small>(Score:" + jsonas.getString("points") + ", " + jsonas.getString("reason") + ")</small>";
                    String comment = jsonas.getString("comment");
                    String mAuther = jsonas.getString("profile_identifier");
                    String mComDate = jsonas.getString("date");

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = df.parse(mComDate);
                    df.setTimeZone(TimeZone.getDefault());
                    df.applyPattern("dd MMMM yyyy HH:mm");
                    String formattedDate = df.format(date);


                    //title=title+" <font size=2> (Score:"+jsonas.getString("points")+", "+jsonas.getString("reason")+")</font>";
                    map = new HashMap<String, String>();
                    map.put("title", title);
                    map.put("content", comment);
                    map.put("auther", mAuther + " " + score + " " + formattedDate);
                    feedList.add(map);
                }

            }
            MySimpleAdapter simpleAdapter = new MySimpleAdapter(ReadStory.this, feedList, R.layout.remark_item, new String[]{"title", "content","auther"}, new int[]{R.id.txttitle, R.id.txtcontent,R.id.txtauther});
            simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                                            public boolean setViewValue(View view, Object data, String textRepresentation) {
                                                if (data instanceof Spanned && view instanceof TextView) {
                                                    ((TextView) view).setText((Spanned) data);
                                                } else {
                                                    ((TextView) view).setText(Html.fromHtml(String.valueOf(data)));
                                                    ((TextView) view).setMovementMethod(LinkMovementMethod.getInstance());

                                                }
                                                return true;
                                            }
                                        }
            );

            mainListView.setAdapter(simpleAdapter);
        }
        catch (Exception e)
        {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, e.getMessage(), duration);
            toast.show();
        }
    }
    private void LoadJsonStory(int mTempThreshold)
    {
        boolean mCanAdd = true;
        try
        {
            JSONObject mainSuperObject = new JSONObject(mJSONData);
            JSONArray mStories = mainSuperObject.getJSONArray("stories");
            mStoryCount = mStories.length();



            final GlobalClass globalVariable = (GlobalClass) getApplicationContext();
            globalVariable.mStoryIndex = mJSONIndex;
            Integer mJSONIndexInt = Integer.parseInt(mJSONIndex);
            JSONObject mainObject = new JSONObject(mStories.getJSONObject(mJSONIndexInt).toString());

            //myTextview.setText(Html.fromHtml(mainObject.getString("introtext")));

            mStrHeadline = mainObject.getString("headline").toString();
            mStrURL="http://www.slashdot.org/story/"+mainObject.getString("story_id");
            String mCount = mainObject.getString("comment_count");
            String mTopic = mainObject.getString("topic_image");
            String mDate = mainObject.getString("day_published");
            String mSubmitter = mainObject.getString("submitter");

            String mStory = mainObject.getString("introtext");
            SimpleDateFormat dfa = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dfa.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date datea = dfa.parse(mDate);
            dfa.setTimeZone(TimeZone.getDefault());
            dfa.applyPattern("dd MMMM yyyy HH:mm");
            String formattedDatea = dfa.format(datea);

            mSubmitter = mSubmitter+" on "+formattedDatea;

            TextView mHeadline = (TextView) findViewById(R.id.textViewDate);
            mHeadline.setText(mStrHeadline);
            mHeadline.setTextSize(mFontSize);

            TextView mArtDate = (TextView) findViewById(R.id.artickledate);
            mArtDate.setText(mSubmitter);
            mArtDate.setTextSize(mFontSize - 2);

            TextView mCommentCount = (TextView) findViewById(R.id.textViewDescription);
            mCommentCount.setText(mCount);
            mCommentCount.setTextSize(mFontSize);

/*
            TextView mIntroText = (TextView) findViewById(R.id.artickle);
            mIntroText.setText(mStory);
            mIntroText.setTextSize(mFontSize);
*/
            ImageView ImageViewItem = (ImageView) findViewById(R.id.catimage);


            if(mShowGraphics == 1)
            {
                Picasso.with(getApplicationContext()).load("http://slashdot.aella-services.co.uk/images/" + mTopic+".png").error(R.drawable.no_image).into(ImageViewItem);
            }
            else
            {
                int mWidth = ImageViewItem.getLayoutParams().width;
                int mNewWidth = mHeadline.getLayoutParams().width;
                mNewWidth +=mWidth;

                ImageViewItem.getLayoutParams().width = 0;
                mHeadline.getLayoutParams().width = mNewWidth;
                mArtDate.getLayoutParams().width = mNewWidth;
            }


            mainListView = (ListView) findViewById(R.id.listcomments);
            ArrayList<HashMap<String, String>> feedList= new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map = new HashMap<String, String>();


            map = new HashMap<String, String>();

            map.put("title", "");
            map.put("content", mainObject.getString("introtext"));
            map.put("auther","");
            feedList.add(map);


            JSONArray mComments = mainObject.getJSONArray("comments");
            Integer mComCount = 0;
            for(int i=0;i<mComments.length(); i++)
            {
                mCanAdd=true;
                JSONObject jsonas = mComments.getJSONObject(i);
                String mIs_xkcd = jsonas.getString("is_xkcd");
                if(mTempThreshold > -1)
                {
                    mCanAdd=false;
                    if(Integer.parseInt(jsonas.getString("points")) >= mTempThreshold)
                    {
                        mCanAdd = true;
                    }
                }
                if(mIsXKCD == true)
                {
                    mCanAdd=false;
                    if(mIs_xkcd.equals("1"))
                    {
                        mCanAdd=true;
                    }
                }
                if(mCanAdd == true)
                {
                    String title = jsonas.getString("subject");
                    String score = "<small>(Score:" + jsonas.getString("points") + ", " + jsonas.getString("reason") + ")</small>";
                    String comment = jsonas.getString("comment");
                    String mAuther = jsonas.getString("profile_identifier");
                    String mComDate = jsonas.getString("comment_time");

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = df.parse(mComDate);
                    df.setTimeZone(TimeZone.getDefault());
                    df.applyPattern("dd MMMM yyyy HH:mm");
                    String formattedDate = df.format(date);


                    //title=title+" <font size=2> (Score:"+jsonas.getString("points")+", "+jsonas.getString("reason")+")</font>";
                    map = new HashMap<String, String>();
                    map.put("title", title);
                    map.put("content", comment);
                    map.put("auther", mAuther + " " + score+ " " + formattedDate);
                    feedList.add(map);
                    mComCount++;
                }

            }
            MySimpleAdapter simpleAdapter = new MySimpleAdapter(this, feedList, R.layout.remark_item, new String[]{"title", "content","auther"}, new int[]{R.id.txttitle, R.id.txtcontent,R.id.txtauther});
            simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                                            public boolean setViewValue(View view, Object data, String textRepresentation) {
                                                if (data instanceof Spanned && view instanceof TextView) {
                                                    ((TextView) view).setText((Spanned) data);
                                                } else {
                                                    ((TextView) view).setText(Html.fromHtml(String.valueOf(data)));
                                                    ((TextView) view).setMovementMethod(LinkMovementMethod.getInstance());
                                                }
                                                return true;
                                            }
                                        }
            );

            mainListView.setAdapter(simpleAdapter);
            mCommentCount.setText(mComCount.toString());

        }
        catch (Exception e)
        {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, e.getMessage(), duration);
            toast.show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share)
        {
            String mHtml = mStrHeadline+"\n\n"+mStrURL;
            mHtml= mStrURL;
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mHtml);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Send..."));
            //startActivity(sendIntent);
        }
        if (id == R.id.action_refilter)
        {
            ReloadComments();
        }


        return super.onOptionsItemSelected(item);
    }
    private void ReloadComments()
    {
        AlertDialog levelDialog;

        final CharSequence[] items = {"0 And Above","1 And Above", "2 And Above", "3 And Above", "4 And Above", "5 And Above"};

        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ReadStory.this);
        builder.setTitle("Select Comment Threshold");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item)
            {
                if(item >= mDefaultThreshold)
                {
                    LoadJsonStory(item);
                }
                else
                {
                    LoadJsonStoryFromSlashDot(item);
                }

                dialog.dismiss();
            }
        });
        levelDialog = builder.create();
        levelDialog.show();
    }
    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener
    {
        private int SWIPE_MIN_DISTANCE = 70;
        private static final int SWIPE_MAX_OFF_PATH = 200;
        private static final int SWIPE_THRESHOLD_VELOCITY = 100;




        public SwipeGestureDetector(int mDistance)
        {
            SWIPE_MIN_DISTANCE = mDistance;
        }

        // Swipe properties, you can change it to make the swipe
        // longer or shorter and speed


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            try {
                float diffAbs = Math.abs(e1.getY() - e2.getY());
                float diff = e1.getX() - e2.getX();

                if (diffAbs > SWIPE_MAX_OFF_PATH)
                    return false;

                // Left swipe
                if (diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    ReadStory.this.onLeftSwipe();

                    // Right swipe
                } else if (-diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    ReadStory.this.onRightSwipe();
                }
            } catch (Exception e) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, e.getMessage(), duration);
                toast.show();
            }
            return false;
        }
    }
    private class MyTask extends AsyncTask<Void , Void, Void> {


        public Integer mTempThreshold;
        JSONObject mainObject;


        @Override
        protected Void doInBackground(Void... params)
        {




            if(mJSONDataExternal == "")
            {
                try
                {
                    JSONObject mainSuperObject = new JSONObject(mJSONData);
                    JSONArray mStories = mainSuperObject.getJSONArray("stories");
                    mStoryCount = mStories.length();
                    Integer mJSONIndexInt = Integer.parseInt(mJSONIndex);
                    mainObject = new JSONObject(mStories.getJSONObject(mJSONIndexInt).toString());

                    String mUrl = "http://m.slashdot.org/api/v1/discussionthreaded/"+mainObject.getString("discussion_id")+".json?api_key=MdotSLEDY7Ss2nEy7Op9lkKJctCqbGjWUBAUsatNKsQ";
                    URL url = new URL(mUrl);
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String str;
                    while ((str = in.readLine()) != null)
                    {
                        if(mJSONDataExternal == "")
                        {
                            mJSONDataExternal = str;
                        }
                        else
                        {
                            mJSONDataExternal = mJSONDataExternal+"\n"+str;
                        }

                    }
                    in.close();
                }
                catch (Exception e)
                {
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, e.getMessage(), duration);
                    toast.show();
                }

            }

            return null;

        }


        @Override
        protected void onPostExecute(Void result)
        {
            LoadJsonStoryFromSlashDot2(mTempThreshold);
            super.onPostExecute(result);
        }



    }
}
