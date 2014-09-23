package com.denisigo.okreddit;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


public class LoginActivity extends Activity {

    private TextView mTvError;
    private ProgressBar mPbProgress;
    private EditText mEtUsername;
    private EditText mEtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mTvError = (TextView)findViewById(R.id.error);
        mPbProgress = (ProgressBar)findViewById(R.id.progress);
        mEtUsername = (EditText)findViewById(R.id.username);
        mEtPassword = (EditText)findViewById(R.id.password);
    }

    public void onLoginClick(View view){
        String username = mEtUsername.getText().toString().trim();
        String password = mEtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty())
            return;

        (new LoginTask()).execute(username, password);
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            mPbProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String username = strings[0];
            String password = strings[1];

            Reddit reddit = new Reddit(getApplicationContext());
            return reddit.loginUser(username, password);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mPbProgress.setVisibility(View.GONE);
            if (result){
                setResult(RESULT_OK);
                finish();
            } else {
                mTvError.setVisibility(View.VISIBLE);
            }
        }
    }
}



