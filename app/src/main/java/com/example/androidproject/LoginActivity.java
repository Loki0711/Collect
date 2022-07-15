package com.example.androidproject;

import static com.example.androidproject.utils.AppConstant.USERTYPE_COLLECTOR;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.androidproject.databinding.ActivityLoginBinding;
import com.example.androidproject.models.Collectors;
import com.example.androidproject.utils.AppConstant;
import com.example.androidproject.viewmodels.CollectorViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getCanonicalName();
    private ActivityLoginBinding binding;

    private CollectorViewModel collectorViewModel;
    private Collectors matchedCollector;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private String user = "";

    private ArrayList<String> userType = new ArrayList<String>();

    AdapterView.OnItemSelectedListener userTypeListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG, "PERSON SELECTED AN ITEM " + userType.get(i));
            user = userType.get(i);

            if (user.equals(USERTYPE_COLLECTOR)) {
                binding.btnlogin.setVisibility(View.VISIBLE);
                //Collection is not allowed to signup, so hide signup button
                binding.btnsignup.setVisibility(View.GONE);
            } else {
                binding.btnlogin.setVisibility(View.VISIBLE);
                binding.btnsignup.setVisibility(View.VISIBLE);
            }
            // this function is activated when the person selects an item from the spinner
            // int i = is the position of the item the person clicked on
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // ignore this for now
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        collectorViewModel = new ViewModelProvider(LoginActivity.this).get(CollectorViewModel.class);
        prefs = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);
        this.mAuth = FirebaseAuth.getInstance();

        initUI();
        setAdapter();
        onClick();
    }

    private void setAdapter() {
        userType.add(AppConstant.USERTYPE_PRODUCER);
        userType.add(USERTYPE_COLLECTOR);

        ArrayAdapter<String> adapterUser = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, userType);
        this.binding.spUserType.setAdapter(adapterUser);
        this.binding.spUserType.setOnItemSelectedListener(userTypeListener);
    }

    private void initUI() {
        setTitle("Authentication");


        binding.txtemailaddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    binding.emailContainer.setError(null);
                }
            }
        });

        binding.txtpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    binding.passwordContainer.setError(null);
                }
            }
        });
    }

    public void onClick() {
        this.binding.btnlogin.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Sign In Button Clicked");
            if (!validateData()) {
                Toast.makeText(this, "Please provide correct inputs", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnsignup.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Sign Up Button Clicked");
            Intent signUpIntent = new Intent(this, SignUpActivity.class);
            startActivity(signUpIntent);
        });
    }

    private boolean validateData() {
        String email = "";
        String password = "";

        if (this.binding.txtemailaddress.getText().toString().isEmpty()) {
            this.binding.emailContainer.setError("Email Cannot be Empty");
            return false;
        } else {
            email = this.binding.txtemailaddress.getText().toString();
        }

        if (this.binding.txtpassword.getText().toString().isEmpty()) {
            this.binding.emailContainer.setError("Password Cannot be Empty");
            return false;
        } else {
            password = this.binding.txtpassword.getText().toString();
        }


        if (user.equals(USERTYPE_COLLECTOR)) {
            Log.d(TAG, "Signing as a collector");
            this.signInCollector(email, password);

        } else {
            Log.d(TAG, "Signing as a producer");
            this.signIn(email, password);
        }

        return true;
    }

    private void signIn(String email, String password) {
        try {
            this.mAuth = FirebaseAuth.getInstance();
            binding.progresslogin.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Sign In Successful");
                                saveToPrefs(email, AppConstant.USERTYPE_PRODUCER);
                                binding.progresslogin.hide();
                                goToRequest();
                            } else {
                                binding.progresslogin.hide();
                                Log.e(TAG, "onComplete: Sign In Failed", task.getException());
                                Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }


                    });
        } catch (Exception e) {
            binding.progresslogin.hide();
            Toast.makeText(LoginActivity.this, "Authentication Failed !!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }


    private void signInCollector(String email, String password) {

        this.collectorViewModel.searchCollector(email, password);

        this.collectorViewModel.getCollectorRepository().collectorsFromDB.observe(LoginActivity.this, new Observer<Collectors>() {
            @Override
            public void onChanged(Collectors collectors) {
                matchedCollector = collectors;
                saveToPrefs(email, USERTYPE_COLLECTOR);
                goToRequest();
            }

        });
    }

    private void saveToPrefs(String email, String userType) {
        prefs.edit().putString("USER_EMAIL", email).apply();
        prefs.edit().putString("USER_TYPE", userType).apply();
    }



    private void goToRequest() {
        Intent requestIntent = new Intent(this, RequestActivity.class);
        startActivity(requestIntent);
        finishAffinity();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!prefs.getString(AppConstant.USER_EMAIL, "").isEmpty()) {
            goToRequest();
        }

    }
}