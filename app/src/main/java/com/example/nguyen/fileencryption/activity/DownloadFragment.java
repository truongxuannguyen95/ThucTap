package com.example.nguyen.fileencryption.activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.nguyen.fileencryption.R;
import com.example.nguyen.fileencryption.Utilities;
import com.example.nguyen.fileencryption.adapter.ListViewFiles;
import com.example.nguyen.fileencryption.model.Files;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DownloadFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DownloadFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static DownloadFragment newInstance(String param1, String param2) {
        DownloadFragment fragment = new DownloadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private TextView tvEmpty;
    private ListView lvFiles;
    private DatabaseReference mData;
    private ArrayList<Files> listFiles;
    private ListViewFiles listViewFiles;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        mData = FirebaseDatabase.getInstance().getReference();
        tvEmpty = view.findViewById(R.id.tvEmpty);
        lvFiles = view.findViewById(R.id.lvFiles);
        listFiles = new ArrayList<>();
        listViewFiles = new ListViewFiles(getContext(), listFiles);
        lvFiles.setAdapter(listViewFiles);
        getListFiles();
        return view;
    }

    private void getListFiles(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        final String owner = currentUser.getUid();
        mData.child("files_name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChild(owner)) {
                    tvEmpty.setText("Bạn chưa upload dữ liệu nào!");
                    tvEmpty.setTextColor(Color.DKGRAY);
                    lvFiles.setEmptyView(tvEmpty);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                try {
                    Utilities.showAlertDialog("Thông báo", "Đã xảy ra lỗi trong quá trình kiểm tra dữ liệu\nVui lòng thử lại sau", getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mData.child("files_name").child(owner).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listFiles.clear();
                if(dataSnapshot.exists()){
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        String key = data.child("key").getValue().toString();
                        String name = data.child("name").getValue().toString();
                        String keyData = data.child("keyData").getValue().toString();
                        listFiles.add(new Files(name, key, keyData));
                        listViewFiles.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.GONE);
                        lvFiles.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                try {
                    Utilities.showAlertDialog("Thông báo", "Đã xảy ra lỗi trong quá trình kiểm tra dữ liệu\nVui lòng thử lại sau", getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
