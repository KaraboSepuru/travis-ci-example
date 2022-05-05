package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CourseContent extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    TextView coursename,coursedesc,courseinst,coursecode;
    Button subscribe;
    Boolean subscribed=false;
    String coursename1,courseinstructor,coursecode1,courseid;
    RecyclerView recyclerView;
    All_pdf_adapter mainAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_course_content);
        getWindow().setStatusBarColor(ContextCompat.getColor(CourseContent.this, R.color.teal_700));

        //bottom navigation bar
        createnavigationbar();
        //

        courseinst=findViewById(R.id.instructor_name);
        coursename=findViewById(R.id.course_name);
        coursedesc=findViewById(R.id.course_description);
        subscribe=findViewById(R.id.subscribe);

        coursename1=getIntent().getStringExtra("course_name");
        courseinstructor=getIntent().getStringExtra("course_teacher");
        coursecode1=getIntent().getStringExtra("course_code");
        courseid=getIntent().getStringExtra("course_id");

        courseinst.setText(courseinstructor);
        coursename.setText(coursecode1);

        recyclerView = (RecyclerView)findViewById(R.id.show_pdfs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        retrievepdf();


        FirebaseDatabase.getInstance().getReference("Course Description").child(coursename1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        coursedesc.setText(snapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CourseContent.this, "Couldnt load course description", Toast.LENGTH_SHORT).show();
                    }
                });


        if(getIntent().getStringExtra("dashboard_or_mycourses").equals("subscribed")){
            subscribe.setEnabled(false);
            subscribe.setText("Subscribed");
        }
        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!subscribed){

                    module module=new module(coursename1,coursecode1,courseinstructor);
                    FirebaseDatabase.getInstance().getReference("Enrol")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(coursecode1)
                            .setValue(module)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(CourseContent.this,"Successfully subscribed to the course",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    subscribe.setText("Subscribed");
                    subscribed=true;
                }else{
                    Toast.makeText(CourseContent.this,"Already subscribed to the course",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    @Override
    public void onBackPressed()
    {

    }

    private void createnavigationbar(){
        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelected(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.dashboard:
                        startActivity(new Intent(CourseContent.this,Student_Dashboard.class).putExtra("activity","account"));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.student_courses:
                        startActivity(new Intent(CourseContent.this,StudentCourses.class).putExtra("activity","account"));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.student_account:
                        startActivity(new Intent(CourseContent.this,StudentAccount.class).putExtra("activity","account"));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    private void retrievepdf() {
        FirebaseRecyclerOptions<uploadpdf> options =
                new FirebaseRecyclerOptions.Builder<uploadpdf>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Course Material").child(coursename1), uploadpdf.class)//.orderByChild("modName").equalTo("APHY8010")
                        .build();
        mainAdapter = new All_pdf_adapter(options,getApplicationContext());
        recyclerView.setAdapter(mainAdapter);
    }

    @Override
    protected void onStart(){
        super.onStart();
        mainAdapter.startListening();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            startActivity(new Intent(CourseContent.this,login.class));
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        mainAdapter.stopListening();
    }
}