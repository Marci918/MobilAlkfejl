package com.example.zeneblog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    EditText userNameET;
    EditText emailET;
    EditText passwordET;
    EditText passwordConfirmET;
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        //Bundle bundle = getIntent().getExtras();
        //int secret_key = bundle.getInt("SECRET_KEY")
        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 99){ finish(); }

        userNameET = findViewById(R.id.editTextUserName);
        emailET = findViewById(R.id.editTextEmail);
        passwordET = findViewById(R.id.editTextPassword);
        passwordConfirmET = findViewById(R.id.editTextPasswordConfirm);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String password = preferences.getString("password", "");

        mAuth = FirebaseAuth.getInstance();
    }

    public void register(View view) {
        String userName = userNameET.getText().toString();
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordConfirm = passwordConfirmET.getText().toString();

        if(!password.equals(passwordConfirm)){
            Log.e(LOG_TAG, "Nem egyezik meg a ket jelszo!");
            return;
        }

        Log.i(LOG_TAG, "Regisztrált: " + userName + ",email: " + email + ", jelszó: " + password + ", jelszó ismét: " + passwordConfirm);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG, "User created successfully");
                    startBlogging();
                } else {
                    Log.d(LOG_TAG, "User wasn't created");
                    Toast.makeText(RegisterActivity.this, "User wasn't created: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void cancel(View view) {
        finish();
    }

    private void startBlogging(){
        Intent intent = new Intent(this, BlogReadingActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }
}