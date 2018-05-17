package com.idoideas.sometimesettle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class WelcomeActivity extends Activity {

    Button signup, skipsignup;
    EditText email, username;
    static Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.welcome_activity);
        context = this;
        super.onCreate(savedInstanceState);

        signup = findViewById(R.id.signup);
        skipsignup = findViewById(R.id.skipsignup);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.prefs.edit().putString("username", username.getText().toString()).apply();
                MainActivity.prefs.edit().putBoolean("not-signed", false).apply();

                NetworkUtils.addNewUserToDatabase(getApplicationContext(), username.getText().toString(), email.getText().toString());
            }
        });

        skipsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.prefs.edit().putBoolean("not-signed", false).apply();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

    public static void openExistsPopup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Username is already taken.")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
