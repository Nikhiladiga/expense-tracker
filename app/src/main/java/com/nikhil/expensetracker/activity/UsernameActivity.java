package com.nikhil.expensetracker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.nikhil.expensetracker.databinding.ActivityUsernameBinding;

public class UsernameActivity extends AppCompatActivity {

    private ActivityUsernameBinding activityUsernameBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUsernameBinding = ActivityUsernameBinding.inflate(getLayoutInflater());
        setContentView(activityUsernameBinding.getRoot());

        //Save username
        activityUsernameBinding.saveUsername.setOnClickListener(view -> {

            //Check if name is empty
            if (activityUsernameBinding.userName.getText() != null && activityUsernameBinding.userName.getText().length() < 1) {
                activityUsernameBinding.userName.setError("Please enter your name");
                return;
            }

            //Check if name is below 20 chars
            if (activityUsernameBinding.userName.getText() != null && activityUsernameBinding.userName.getText().length() > 20) {
                activityUsernameBinding.usernameLayout.setError("Please make sure name is below 20 characters");
                return;
            }

            //Save username to shared prefs
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", String.valueOf(activityUsernameBinding.userName.getText()));
            editor.apply();

            //Return success intent
            Intent returnIntent = new Intent();
            returnIntent.putExtra("success", "true");
            setResult(Activity.RESULT_OK, returnIntent);
            finish();

        });

    }
}