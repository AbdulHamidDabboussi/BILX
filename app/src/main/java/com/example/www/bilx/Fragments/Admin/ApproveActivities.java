package com.example.www.bilx.Fragments.Admin;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import com.example.www.bilx.Fragments.Club.ClubActivitiesAdapter;
import com.example.www.bilx.Fragments.Club.ClubActivitiesObject;
import com.example.www.bilx.Fragments.Club.CreateNewActivity;
import com.example.www.bilx.Fragments.User.Notifications_User;
import com.example.www.bilx.R;
import com.example.www.bilx.SignUp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  The settings fragment for the admin class.
 *  @author Hanzallah Burney
 */

public class ApproveActivities extends android.support.v4.app.Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static List<ApproveActivitiesObject> approveActivityList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ApproveActivitiesAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        final View view =  inflater.inflate(R.layout.approve_activities, container, false);
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());

        approveActivityList = new ArrayList<>();

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.clubActivities);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Approve Activities");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                approveActivityList = new ArrayList<>();
                for (final DataSnapshot ds: dataSnapshot.getChildren() ){
                    String s = ds.toString();
                    final String val = s.substring(s.indexOf('=')+1,s.indexOf(','));

                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Approve Activities").child(val);
                    databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (final DataSnapshot ds1: ds.getChildren()){
                                String str = ds1.toString();
                                final String val2 = str.substring(str.indexOf('=')+1,str.indexOf(',')).trim();

                                DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("Approve Activities")
                                        .child(val);
                                Query query = databaseReference2.orderByPriority();
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String ge,time, lang, loc,desc, date;
                                        ge = "";
                                        time = "";
                                        lang ="";
                                        loc ="";
                                        desc= "";
                                        date = "";
                                        for (DataSnapshot ds2: ds1.getChildren()){
                                            String str1 = ds2.toString();
                                            String name = str1.substring(str1.lastIndexOf('{')+1,str1.lastIndexOf('='));
                                            String val3 = str1.substring(str1.lastIndexOf('=')+1,str1.indexOf('}'));
                                            if (name.equals("Time")){
                                                time = val3;
                                            }
                                            else if (name.equals("Location")){
                                                loc = val3;
                                            }
                                            else if (name.equals("GE")){
                                                ge = val3;
                                            }
                                            else if (name.equals("Language")){
                                                lang = val3;
                                            }
                                            else if (name.equals("Date")){
                                                date = val3;
                                            }
                                            else if (name.equals("Description")){
                                                desc = val3;
                                            }
                                        }
                                        adapter = new ApproveActivitiesAdapter(approveActivityList);
                                        recyclerView = (RecyclerView) view.findViewById(R.id.approveAct_recycler_view);

                                        recyclerView.setLayoutManager(mLayoutManager);
                                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                                        recyclerView.setAdapter(adapter);
                                        addItem(new ApproveActivitiesObject("Activity Name: "+ val2,
                                                "Club Name: "+ val,"GE Points: "+ ge,
                                                "Time: "+ time,"Date: "+ date,"Location: "+ loc,
                                                "Language: "+ lang,"Activity Description: "+ desc));
                                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
                                        itemTouchHelper.attachToRecyclerView(recyclerView);
                                    }


                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refreshApprove);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                        new ApproveActivities()).commit();
            }
        });

        return view;
    }

    public void addItem(ApproveActivitiesObject newItem) {
        approveActivityList.add(0,newItem);
        adapter.notifyDataSetChanged();
    }

    private ItemTouchHelper.Callback createHelperCallback() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN
                , ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                    final AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(getActivity());
                    }
                    builder.setTitle("Approve or Reject Activity")
                            .setMessage("Do you want to approve the activity or reject it?");
                    builder.setNegativeButton("Approve", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ApproveActivitiesObject approveActivitiesObject = approveActivityList.get(viewHolder.getAdapterPosition());
                            String clubName = approveActivitiesObject.getClubName().substring(
                                    approveActivitiesObject.getClubName().indexOf(':')+1,approveActivitiesObject.getClubName().length()).trim();
                            String activityName = approveActivitiesObject.getActivityName().substring(
                                    approveActivitiesObject.getActivityName().indexOf(':')+1,approveActivitiesObject.getActivityName().length()).trim();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Club Activities")
                                    .child(clubName).child(activityName).child("Status");
                            Map status = new HashMap();
                            status.put("Status","True");
                            ref.setValue(status);

                            approveActivityList.remove(viewHolder.getAdapterPosition());
                            adapter.removeAdapter(viewHolder.getAdapterPosition());
                            adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Approve Activities").child(clubName).child(activityName);
                            reference.removeValue();
                            Snackbar.make(getActivity().findViewById(R.id.approveAct), "Approval sent to club", Snackbar.LENGTH_LONG).show();


                        }
                    });
                    builder.setPositiveButton("Reject", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                ApproveActivitiesObject approveActivitiesObject = approveActivityList.get(viewHolder.getAdapterPosition());
                                String clubName = approveActivitiesObject.getClubName().substring(
                                        approveActivitiesObject.getClubName().indexOf(':')+1,approveActivitiesObject.getClubName().length()).trim();
                                String activityName = approveActivitiesObject.getActivityName().substring(
                                        approveActivitiesObject.getActivityName().indexOf(':')+1,approveActivitiesObject.getActivityName().length()).trim();

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Club Activities")
                                        .child(clubName).child(activityName).child("Status");
                                Map status = new HashMap();
                                status.put("Status","False");
                                ref.setValue(status);

//                                approveActivityList.remove(viewHolder.getAdapterPosition());
//                                adapter.removeAdapter(viewHolder.getAdapterPosition());
//                                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Approve Activities").child(clubName).child(activityName);
                                reference.removeValue();
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                                        new ApproveActivities()).commit();
                                Snackbar.make(getActivity().findViewById(R.id.approveAct), "Rejection sent to club", Snackbar.LENGTH_LONG).show();
                            } catch (Exception e){

                            }
                        }
                    }).show();


            }

        };
        return simpleCallback;
    }
    @Override
    public void onRefresh(){
        // Empty
    }
}