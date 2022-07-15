package com.example.androidproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.androidproject.databinding.ActivitySignUpBinding;
import com.example.androidproject.utils.AppConstant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getCanonicalName();
    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        setTitle("Sign up");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.binding.btnCreateAccount.setOnClickListener(this);
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        if (view != null){
            switch (view.getId()){
                case R.id.btn_create_account:{
                    Log.d(TAG, "onClick: Create Account button Clicked");
                    this.validateData();
                    break;
                }
            }
        }
    }

    private void validateData(){
        boolean validData = true;
        String email = "";
        String password = "";

        if (this.binding.email.getText().toString().isEmpty()){
            this.binding.email.setError("Email Cannot be Empty");
            validData = false;
        }else{
            email = this.binding.email.getText().toString();
        }

        if (this.binding.password.getText().toString().isEmpty()){
            this.binding.password.setError("Password Cannot be Empty");
            validData = false;
        }else {

            if (this.binding.confirmPassword.getText().toString().isEmpty()) {
                this.binding.confirmPassword.setError("Confirm Password Cannot be Empty");
                validData = false;
            } else {

                if (!this.binding.password.getText().toString().equals(this.binding.confirmPassword.getText().toString())) {
                    this.binding.confirmPassword.setError("Both passwords must be same");
                    validData = false;
                }else{
                    password = this.binding.password.getText().toString();
                }

            }
        }

        if (validData){
            this.createAccount(email, password);
        }else{
            Toast.makeText(this, "Please provide correct inputs", Toast.LENGTH_SHORT).show();
        }

    }

    private void createAccount(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //save the user/login info on Shared Prefs
                            saveToPrefs(email, AppConstant.USERTYPE_PRODUCER);

                            //navigate to Main screen
                            goToMain();
                        } else{
                            Log.e(TAG, "onComplete: Failed to create user with email and password" + task.getException() + task.getException().getLocalizedMessage());
                            Toast.makeText(SignUpActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveToPrefs(String email, String userType) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);
        prefs.edit().putString("USER_EMAIL", email).apply();
        prefs.edit().putString("USER_TYPE", userType).apply();
    }

    private void goToMain(){
        Intent mainIntent = new Intent(this, RequestActivity.class);
        startActivity(mainIntent);
        finishAffinity();
    }
}