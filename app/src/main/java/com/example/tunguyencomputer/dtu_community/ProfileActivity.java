package com.example.tunguyencomputer.dtu_community;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.tunguyencomputer.dtu_community.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mProfileUserDatabaseRef;
    private String mCurrentUserID;


    private TextView mUserName, mFullName, mUserStatus, mUserCountry, mUserGender, mUserRelation, mUserDOB;
    private CircleImageView mUserProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findID();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();
        mProfileUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserID);
        mProfileUserDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myFullName = dataSnapshot.child("fullname").getValue().toString();
                    String myStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelation = dataSnapshot.child("relationshipstatus").getValue().toString();
                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(mUserProfileImage);


                    mUserName.setText("@" + myUserName);
                    mFullName.setText(myFullName);
                    mUserStatus.setText(myStatus);
                    mUserGender.setText("Gender: " + myGender);
                    mUserCountry.setText("Country: " + myCountry);
                    mUserDOB.setText("DOB: " + myDOB);
                    mUserRelation.setText("Relationship " + myRelation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void findID() {
        mUserName = (TextView) findViewById(R.id.my_profile_username);
        mFullName = (TextView) findViewById(R.id.my_profile_fullname);
        mUserStatus = (TextView) findViewById(R.id.my_profile_status);
        mUserCountry = (TextView) findViewById(R.id.my_profile_country);
        mUserGender = (TextView) findViewById(R.id.my_profile_gender);
        mUserDOB = (TextView) findViewById(R.id.my_profile_dob);
        mUserRelation = (TextView) findViewById(R.id.my_profile_relationship_status);
        mUserProfileImage = (CircleImageView) findViewById(R.id.my_profile_circle_image);

    }

}
