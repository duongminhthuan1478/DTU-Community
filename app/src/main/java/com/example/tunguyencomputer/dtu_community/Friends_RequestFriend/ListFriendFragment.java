package com.example.tunguyencomputer.dtu_community.Friends_RequestFriend;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import com.example.tunguyencomputer.dtu_community.ChatActivity;
import com.example.tunguyencomputer.dtu_community.FriendActivity;
import com.example.tunguyencomputer.dtu_community.Model.Friend;
import com.example.tunguyencomputer.dtu_community.PersonProfileActivity;
import com.example.tunguyencomputer.dtu_community.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ListFriendFragment extends Fragment {

    /** Hai biến thể hiện trạng thái khi user onl/off */
    private static final String USER_ONLINE = "ONLINE";
    private static final String USER_OFFLINE = "OFFLINE";

    private DatabaseReference mFriendDatabaseRef, mUserDatabaseRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;

    private FirebaseRecyclerAdapter<Friend, ListFriendViewHolder>
            mFriendFirebaseRecyclerAdapter;

    private RecyclerView mFriendListRecyclerview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_listfriends, container, false);


        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();
        mFriendDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserID);
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendListRecyclerview = (RecyclerView) rootView.findViewById(R.id.friends_list_recyclerview);
        mFriendListRecyclerview.setHasFixedSize(true);
        mFriendListRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        displayAllFriend();


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUserState(USER_ONLINE);
        mFriendFirebaseRecyclerAdapter.startListening();

    }
    @Override
    public void onResume() {
        super.onResume();
        updateUserState(USER_ONLINE);
    }

    @Override
    public void onStop() {
        updateUserState(USER_OFFLINE);
        /** Phương thức chạy khi người dùng rời khỏi chương trình (vd nhấn Home) */
        super.onStop();
        mFriendFirebaseRecyclerAdapter.stopListening();
    }

    /** Check người dùng đang online hay offline*/
    private void updateUserState(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentUserState = new HashMap();
        currentUserState.put("time", saveCurrentTime);
        currentUserState.put("date", saveCurrentDate);
        currentUserState.put("type", state);

        mUserDatabaseRef.child(mCurrentUserID).child("userState").updateChildren(currentUserState);

    }

    private void displayAllFriend() {
        /** Vào child của người dùng hiện tại (đang online)
         * Trong child online có các node con là uid của những user khác
         * getKey từng cái và vào đó lấy thông tin hiển thị ra*/
        Query query = mFriendDatabaseRef;
        FirebaseRecyclerOptions<Friend> options =
                new FirebaseRecyclerOptions.Builder<Friend>().setQuery(query, Friend.class).build();

        mFriendFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friend, ListFriendViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final ListFriendViewHolder holder, int position,
                    @NonNull final Friend model) {
                /** Khi khởi động Adapter sẽ đi từng vị trí của mFriendDatabaseRef như mảng
                 * do đó chúng ta get key của từng vị trí đó để lấy dữ liệu other user*/
                final String userID = getRef(position).getKey();

                mUserDatabaseRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String fullname = dataSnapshot.child("fullname").getValue().toString();
                            String profileimage = dataSnapshot.child("profileimage").getValue().toString();
                            final String type;
                            if(dataSnapshot.hasChild("userState")){
                                type = dataSnapshot.child("userState").child("type").getValue().toString();
                                if(type.equals(USER_ONLINE)){
                                    holder.onlineStatusImage.setVisibility(View.VISIBLE);
                                }else {
                                    holder.onlineStatusImage.setVisibility(View.INVISIBLE);
                                }
                            }

                            //  Vì lấy ảnh và name theo từng vị trí từng người cụ thể nên không thể lấy từ model
                            // Lấy trực tiếp từ database
                            holder.setFullname(fullname);
                            holder.setProfileimage(profileimage);
                            holder.setFriend_date(model.getFriend_date());

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Tạo AlertDialog với 2 tùy chọn (charserquence) xem thông tin / gửi tin nhắn
                                    CharSequence options[] = new CharSequence[]{
                                            "Xem thông tin của " + fullname,
                                            "Gửi Tin Nhắn"
                                    };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setItems(options, new DialogInterface.OnClickListener
                                            () {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Profile
                                            if(which == 0){
                                                /** PersonProfileActivity biến mReceiverUserID sẽ lấy visit_user_id của một user
                                                 * và vào đúng node để hiển thị thông tin nên cần chuyển cho nó userID*/
                                                Intent intent = new Intent(getActivity(),
                                                        PersonProfileActivity.class);
                                                intent.putExtra("visit_user_id", userID);
                                                startActivity(intent);

                                            }
                                            // Chat with friend
                                            if(which == 1){
                                                Intent intent = new Intent(getActivity(),
                                                        ChatActivity.class);
                                                intent.putExtra("visit_user_id", userID);
                                                intent.putExtra("fullname", fullname);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ListFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                /** Dùng chung layout item , thay đổi status thành ngày kết bạn*/
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_user_display_item, parent, false);
                return new ListFriendViewHolder(view);
            }
        };
        mFriendListRecyclerview.setAdapter(mFriendFirebaseRecyclerAdapter);
    }



    public static class ListFriendViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageView onlineStatusImage;

        public ListFriendViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusImage = itemView.findViewById(R.id.all_user_online_icon);
        }
        public void setProfileimage(String profileimage) {
            CircleImageView imageView = mView.findViewById(R.id.all_user_profile_image);
            Picasso.get().load(profileimage).into(imageView);
        }
        public void setFullname(String fullname) {
            TextView myName = mView.findViewById(R.id.all_user_profile_fullname);
            myName.setText(fullname);
        }
        public void setFriend_date(String friend_date) {
            TextView date = mView.findViewById(R.id.all_user_profile_status);
            date.setText("Ngày kết bạn: "+ friend_date);
        }
    }

}
