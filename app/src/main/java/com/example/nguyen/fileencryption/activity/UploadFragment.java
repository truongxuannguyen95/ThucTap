package com.example.nguyen.fileencryption.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.nguyen.fileencryption.R;
import com.example.nguyen.fileencryption.Utilities;
import com.example.nguyen.fileencryption.model.AES;
import com.example.nguyen.fileencryption.model.Files;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public UploadFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static UploadFragment newInstance(String param1, String param2) {
        UploadFragment fragment = new UploadFragment();
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

    private Button btnChooseFile, btnUpload;
    private TextView tvFileName;
    private Uri uri;
    private AES aes;
    private DatabaseReference mData;
    private String fileNameUpload = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        mData = FirebaseDatabase.getInstance().getReference();
        btnChooseFile = view.findViewById(R.id.btnChooseFile);
        btnUpload = view.findViewById(R.id.btnUpload);
        tvFileName = view.findViewById(R.id.tvFileName);
        mData = FirebaseDatabase.getInstance().getReference();
        aes = new AES();

        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent = Intent.createChooser(intent, "Chọn file để upload");
                startActivityForResult(intent, 0);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileNameUpload.length() == 0) {
                    Utilities.showAlertDialog("Thông báo", "Vui lòng chọn file để upload lên server", getContext());
                } else {
                    if(Utilities.isOnline(getContext()))
                        new MyAsyncTask().execute();
                    else
                        Utilities.showAlertDialog("Thông báo", "Thiết bị của bạn chưa được kết nối internet\nVui lòng kết nối internet để sử dụng chức năng này", getContext());
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode != getActivity().RESULT_OK) return;
        if(requestCode == 0)
        {
            uri = data.getData();
            String filePath = uri.getPath();
            String fileName = filePath;
            if(filePath.contains("/")) {
                fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            }
            fileNameUpload = fileName;
            if(fileName.length() > 24) {
                fileName = fileName.substring(0,20) + "...";
            }
            /*String type = FileInfo.substring(FileInfo.lastIndexOf(".")+1);
            String name = FileInfo.substring(0,FileInfo.length()-type.length()-1);*/
            tvFileName.setText(fileName);
            tvFileName.setTextColor(Color.BLACK);
        }
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

    private class MyAsyncTask extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;

        protected void onPreExecute() {

            progressDialog = new ProgressDialog( getContext() );
            progressDialog.setIndeterminate( true );
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage( "Đang mã hóa file và upload lên server..." );
            progressDialog.show();
        }

        @Override
        protected String doInBackground( String... params ) {
            byte[] bytes = new byte[16384];
            int nRead;
            try {
                InputStream is = getActivity().getContentResolver().openInputStream(uri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                while ((nRead = is.read(bytes, 0, bytes.length)) != -1) {
                    buffer.write(bytes, 0, nRead);
                }
                buffer.flush();
                String fileData = aes.static_byteArrayToString(buffer.toByteArray());
                aes.setKey(HomePage.myKey);
                fileData = aes.Encrypt(fileData);
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                final String owner = currentUser.getUid();
                final DatabaseReference keyRef = mData.child("files_name").child(owner).push();
                String key = keyRef.getKey();
                final Files files = new Files(fileNameUpload, key);
                mData.child("files_data").child(key).setValue(fileData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                keyRef.setValue(files);
                                progressDialog.dismiss();
                                Utilities.showAlertDialog("Thông báo", "Upload file thành công", getContext());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Utilities.showAlertDialog("Upload file thất bại", "Đã xảy ra lỗi trong quá trình upload", getContext());
                            }
                        });
            } catch (FileNotFoundException e) {
                if ( progressDialog != null && progressDialog.isShowing() ) {
                    progressDialog.dismiss();
                    Utilities.showAlertDialog("Upload file thất bại", "File này không còn tồn tại", getContext());
                }
                e.printStackTrace();
            } catch (IOException e) {
                if ( progressDialog != null && progressDialog.isShowing() ) {
                    progressDialog.dismiss();
                    Utilities.showAlertDialog("Upload file thất bại", "Đã xảy ra lỗi trong quá trình đọc file", getContext());
                }
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute( String result ) {
            tvFileName.setText("");
            fileNameUpload = "";
            /*if ( progressDialog != null && progressDialog.isShowing() ) {
                progressDialog.dismiss();
                Utilities.showAlertDialog("Thông báo", "Upload file thành công", getContext());
            }*/
        }
    }
}
