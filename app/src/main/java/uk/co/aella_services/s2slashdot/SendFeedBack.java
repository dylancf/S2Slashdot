package uk.co.aella_services.s2slashdot;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class SendFeedBack extends ActionBarActivity {
    Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetTheme();
        setContentView(R.layout.activity_send_feed_back);

        Button button = (Button) findViewById(R.id.ButtonSendFeedback);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;


                EditText mName = (EditText)findViewById(R.id.EditTextName);

                if(mName.getText().toString().equals(""))
                {
                    toast = Toast.makeText(context, "Please input your name", duration);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 150);
                    toast.show();
                    return;
                }

                EditText mEmail = (EditText)findViewById(R.id.EditTextEmail);

                if(mEmail.getText().toString().equals(""))
                {
                    toast = Toast.makeText(context, "Please input your email", duration);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 150);
                    toast.show();
                    return;
                }

                EditText mComment = (EditText)findViewById(R.id.EditTextFeedbackBody);

                if(mComment.getText().toString().equals(""))
                {
                    toast = Toast.makeText(context, "Please input a comment", duration);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 150);
                    toast.show();
                    return;
                }

                new NetworkAsyncTask().execute();
            }
        });
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_feed_back, menu);
        return true;
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

        return super.onOptionsItemSelected(item);
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

    }
    class NetworkAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            URL url;
            String response = "";
            EditText mName = (EditText)findViewById(R.id.EditTextName);
            EditText mEmail = (EditText)findViewById(R.id.EditTextEmail);
            EditText mComment = (EditText)findViewById(R.id.EditTextFeedbackBody);



            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("yourname", mName.getText().toString());
            hashMap.put("youremail", mEmail.getText().toString());
            hashMap.put("yourcomment", mComment.getText().toString());

            try
            {
                url = new URL("http://slashdot.aella-services.co.uk/send_feedback.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(hashMap));

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
