/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements View.OnKeyListener, View.OnClickListener {

    EditText username;
    EditText password;
    Button mainBtn;
    TextView mainText;
    TextView alertText;
    ImageView titleImage;
    boolean loginMode;

    RelativeLayout relativeLayout;

    public void mainButtonClick(View view) {
        final String usernameStr = username.getText().toString();
        final String passwordStr = password.getText().toString();

        if(usernameStr.equals("") || passwordStr.equals("")){
            alertText.setTextColor(Color.RED);
            alertText.setText("A username and password are required.");
        }
        else {

        //When Login
        if (loginMode) {
            ParseUser.logInInBackground(usernameStr, passwordStr, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {
                        afterLogin();
                    } else {
                        alertText.setTextColor(Color.RED);
                        alertText.setText("The combination of username and password is not correct.");
                    }
                }
            });
        }
        //When Sign up
        else {
            ParseUser user = new ParseUser();

            user.setUsername(usernameStr);
            user.setPassword(passwordStr);

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        alertText.setTextColor(Color.BLACK);
                        alertText.setText("Sign up successfully! You can use the username and password to login now.");
                        changeText(mainText);
                    } else {
                        alertText.setTextColor(Color.RED);
                        alertText.setText("Sign up failed: " + e);
                    }
                }
            });

        }
    }
  }

  public void changeText(View view) {
      if(loginMode){
          mainBtn.setText("Sign Up");
          mainText.setText("Or Login");
        loginMode = false;
      }
      else {
          mainBtn.setText("Login");
          mainText.setText("Or Sign Up");
          loginMode = true;
      }
  }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = (EditText) findViewById(R.id.input_username);
        password = (EditText) findViewById(R.id.input_password);
        mainBtn = (Button) findViewById(R.id.btn_login);
        mainText = (TextView) findViewById(R.id.text_signup);
        alertText = (TextView) findViewById(R.id.login_alert);
        relativeLayout = (RelativeLayout) findViewById(R.id.backgroundRelativeLayout);
        titleImage = (ImageView) findViewById(R.id.img_title);

        loginMode = true;
        alertText.setText("");

        password.setOnKeyListener(this);
        relativeLayout.setOnClickListener(this);
        titleImage.setOnClickListener(this);

        //ParseUser.logOut();

        if(ParseUser.getCurrentUser() != null) {
            afterLogin();
        }

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    private void afterLogin(){
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }


    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {

        if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            mainButtonClick(mainBtn);
        }
        return false;
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.backgroundRelativeLayout || view.getId() == R.id.img_title) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
    }
}