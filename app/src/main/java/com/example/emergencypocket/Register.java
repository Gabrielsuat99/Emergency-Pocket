package com.example.emergencypocket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    private EditText USERNAME, NAME, PASSWORD;
    private Button btnReg;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        USERNAME = findViewById(R.id.username);
        NAME = findViewById(R.id.name);
        PASSWORD = findViewById(R.id.password);

        btnReg = findViewById(R.id.registerbtn);

        db = FirebaseDatabase.getInstance().getReferenceFromUrl("https://emergency-pocket-98c73-default-rtdb.firebaseio.com/");

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Username = USERNAME.getText().toString();
                String Name = NAME.getText().toString();
                String Password = PASSWORD.getText().toString();

                if(Username.isEmpty() || Name.isEmpty() || Password.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Fill the blank", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    db = FirebaseDatabase.getInstance().getReference("users");



                    db.child(Username).child("UserName").setValue(Username);
                    db.child(Username).child("Name").setValue(Name);
                    db.child(Username).child("Password").setValue(Password);

                    Toast.makeText(getApplicationContext(), "Register Completed", Toast.LENGTH_SHORT).show();
                    Intent register = new Intent(getApplicationContext(), Login.class);
                    startActivity(register);
                }

            }
        });


    }
}