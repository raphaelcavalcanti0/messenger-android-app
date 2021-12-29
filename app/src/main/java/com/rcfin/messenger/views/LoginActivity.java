package com.rcfin.messenger.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.rcfin.messenger.utils.CheckConnection;
import com.rcfin.messenger.R;

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail;
    EditText editTextPassword;
    Button btn_login;
    TextView registerTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btn_login = findViewById(R.id.btn_login);
        registerTxt = findViewById(R.id.textRegister);

        btn_login.setOnClickListener(v -> {
            String verif_email = editTextEmail.getText().toString().trim();
            String verif_pass = editTextPassword.getText().toString().trim();

            if (verif_email.equals("")) {
                Toast.makeText(this,  "Digite um email!", Toast.LENGTH_SHORT).show();
            } else {
                if (verif_pass.equals("")) {
                    Toast.makeText(this,  "Digite uma senha!", Toast.LENGTH_SHORT).show();
                } else {
                    if (CheckConnection.isNetworkAvailable(getApplicationContext())) {
                        btn_login.setEnabled(false);
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(verif_email, verif_pass)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(LoginActivity.this, MessengerActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(),
                                            "Email ou senha incorretos.",
                                            Toast.LENGTH_SHORT).show();
                                    btn_login.setEnabled(true);
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), "Sem conexão com a internet.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        registerTxt.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}