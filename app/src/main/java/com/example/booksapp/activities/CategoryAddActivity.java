package com.example.booksapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.booksapp.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {
    private ActivityCategoryAddBinding binding;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth=FirebaseAuth.getInstance();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }
private String category="";
    private void validateData() {
        category=binding.categoryEt.getText().toString().trim();
        if(TextUtils.isEmpty(category)){
            Toast.makeText(this,"Please enter Category",Toast.LENGTH_SHORT).show();
        }else {
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
        long timeStamp=System.currentTimeMillis();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("id",""+timeStamp);
        hashMap.put("category",""+category);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        DatabaseReference ref= FirebaseDatabase.getInstance("https://book-app-de5c5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Categories");
        ref.child(""+timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(CategoryAddActivity.this,"Category added successfully",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CategoryAddActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}