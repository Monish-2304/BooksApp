package com.example.booksapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.booksapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String email="",password="";
    private void validateData() {
        email=binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"invalid email pattern",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"enter password",Toast.LENGTH_SHORT).show();
        }else{
            loginUser();
        }
    }

    private void loginUser() {
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                checkUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance("https://book-app-de5c5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
        ref.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String userType=""+snapshot.child("userType").getValue();
                if(userType.equals("user")){
                    startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                    finish();
                }else if(userType.equals("admin")){
                    startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}