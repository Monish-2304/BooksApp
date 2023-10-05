package com.example.booksapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.booksapp.BookUserFragment;
import com.example.booksapp.databinding.ActivityDashboardUserBinding;
import com.example.booksapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {
    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private ActivityDashboardUserBinding binding;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });
    }

private void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);
         categoryArrayList=new ArrayList<>();

    DatabaseReference ref= FirebaseDatabase.getInstance("https://book-app-de5c5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Categories");
    ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            categoryArrayList.clear();
            ModelCategory modelAll=new ModelCategory("01","All","",1);
            ModelCategory modelMostViewed=new ModelCategory("02","Most Viewed","",1);
            ModelCategory modelMostDownloaded=new ModelCategory("03","Most Downloaded","",1);
            categoryArrayList.add(modelAll);
            categoryArrayList.add(modelMostViewed);
            categoryArrayList.add((modelMostDownloaded));

            viewPagerAdapter.addFragment(BookUserFragment.newInstance(""+modelAll.getId(),
                    ""+modelAll.getCategory(),
                    ""+modelAll.getUid())
                    ,modelAll.getCategory());
            viewPagerAdapter.addFragment(BookUserFragment.newInstance(""+modelMostViewed.getId(),
                            ""+modelMostViewed.getCategory(),
                            ""+modelMostViewed.getUid())
                    ,modelMostViewed.getCategory());
            viewPagerAdapter.addFragment(BookUserFragment.newInstance(""+modelMostDownloaded.getId(),
                            ""+modelMostDownloaded.getCategory(),
                            ""+modelMostDownloaded.getUid())
                    ,modelMostDownloaded.getCategory());



            for(DataSnapshot ds:snapshot.getChildren()){
                ModelCategory model=ds.getValue(ModelCategory.class);
                categoryArrayList.add(model);
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(""+model.getId(),""+model.getCategory(),""+model.getUid()), model.getCategory());
                viewPagerAdapter.notifyDataSetChanged();
            }
            viewPagerAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });

    viewPager.setAdapter(viewPagerAdapter);
}

public class ViewPagerAdapter extends FragmentPagerAdapter{
private ArrayList<BookUserFragment> fragmentList=new ArrayList<>();
private ArrayList<String> fragmentTitleList=new ArrayList<>();
private Context context;
    public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
        super(fm, behavior);
        this.context=context;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    private void addFragment(BookUserFragment fragment,String title){
        fragmentList.add(fragment);
        fragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitleList.get(position);

    }
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