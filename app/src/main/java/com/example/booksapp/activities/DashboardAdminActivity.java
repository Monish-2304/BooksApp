package com.example.booksapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.booksapp.adapters.AdapterCategory;
import com.example.booksapp.databinding.ActivityDashboardAdminBinding;
import com.example.booksapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {
private ActivityDashboardAdminBinding binding;
private FirebaseAuth firebaseAuth;
private ArrayList<ModelCategory> categoryArrayList;
private AdapterCategory adapterCategory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding=ActivityDashboardAdminBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot());

       firebaseAuth=FirebaseAuth.getInstance();
       checkUser();
       loadCategories();

       binding.searchEt.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

           }

           @Override
           public void onTextChanged(CharSequence s, int i, int i1, int i2) {
               try {
                   adapterCategory.getFilter().filter(s);
               }catch (Exception e){

               }

           }

           @Override
           public void afterTextChanged(Editable editable) {

           }
       });

       binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               firebaseAuth.signOut();
               checkUser();
           }
       });

       binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class));
           }
       });
binding.addPdfab.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class));
    }
});
    }

    private void loadCategories() {
        categoryArrayList=new ArrayList<>();
        DatabaseReference ref= FirebaseDatabase.getInstance("https://book-app-de5c5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Categories");
    ref.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            categoryArrayList.clear();
            for (DataSnapshot ds: snapshot.getChildren()){
                ModelCategory model=ds.getValue(ModelCategory.class);
                categoryArrayList.add(model);
            }
            adapterCategory = new AdapterCategory(DashboardAdminActivity.this,categoryArrayList);
            binding.categoriesRv.setAdapter(adapterCategory);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
    }

    private void checkUser() {
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(this, MainActivity.class));
        }else{
            String email=firebaseUser.getEmail();
            binding.subtitleTv.setText(email);
        }
    }
}