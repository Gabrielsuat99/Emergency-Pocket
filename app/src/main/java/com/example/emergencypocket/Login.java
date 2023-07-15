package com.example.emergencypocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private EditText Username,Password;
    private TextView btnReg;

    private Button btnLogin;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnReg = findViewById(R.id.registerNow);
        btnLogin = findViewById(R.id.loginbtn);
        Username = findViewById(R.id.username);
        Password = findViewById(R.id.password);


        CheckBox showPasswordCheckbox = findViewById(R.id.showPassword);
        EditText passwordEditText = findViewById(R.id.password);

        showPasswordCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });



        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent register = new Intent(getApplicationContext(), Register.class);
                startActivity(register);
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String USERNAME = Username.getText().toString();
                String PASSWORD = Password.getText().toString();

                db = FirebaseDatabase.getInstance().getReference("users");
                
                if(USERNAME.isEmpty() || PASSWORD.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Wrong username or Password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    db.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.child(USERNAME).exists())
                            {
                                if(snapshot.child(USERNAME).child("Password").getValue(String.class).equals(PASSWORD))
                                {
                                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();


                                    Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    mainIntent.putExtra("name", USERNAME);
                                    startActivity(mainIntent);

                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "User not exist", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });


    }
}