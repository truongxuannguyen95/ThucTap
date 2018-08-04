package com.example.nguyen.fileencryption.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.nguyen.fileencryption.R;
import com.example.nguyen.fileencryption.Utilities;
import com.example.nguyen.fileencryption.model.AES;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EncryptFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public EncryptFragment() {
    }

    public static EncryptFragment newInstance(String param1, String param2) {
        EncryptFragment fragment = new EncryptFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private Button btnChooseFile, btnRandom, btnEncrypt;
    private CheckBox ckbRandom;
    private TextView tvFileName;
    private EditText edtKeyEncrypt;
    private Uri uri;
    private AES aes;
    private String fileNameEncrypt = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encrypt, container, false);
        tvFileName = view.findViewById(R.id.tvFileName);
        edtKeyEncrypt = view.findViewById(R.id.edtKeyEncrypt);
        btnChooseFile = view.findViewById(R.id.btnChooseFile);
        btnRandom = view.findViewById(R.id.btnRandom);
        btnEncrypt = view.findViewById(R.id.btnEncrypt);
        ckbRandom = view.findViewById(R.id.ckbRandom);

        aes = new AES();

        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent = Intent.createChooser(intent, "Chọn file để mã hóa");
                startActivityForResult(intent, 0);
            }
        });

        ckbRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    edtKeyEncrypt.setText(Utilities.rand());
                    edtKeyEncrypt.setError(null);
                    btnRandom.setVisibility(View.VISIBLE);
                } else {
                    btnRandom.setVisibility(View.GONE);
                    edtKeyEncrypt.setText("");
                }
            }
        });

        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtKeyEncrypt.setText(Utilities.rand());
                edtKeyEncrypt.setError(null);
            }
        });

        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileNameEncrypt.length() == 0) {
                    Utilities.showAlertDialog("Thông báo", "Vui lòng chọn file để mã hóa", getContext());
                } else if(!Utilities.isValidKey(edtKeyEncrypt.getText().toString())) {
                    edtKeyEncrypt.setError("Key không được chứa ký tự đặc biệt và phải có đúng 16 ký tự");
                } else if(Utilities.isOnline(getContext())){
                    new MyAsyncTask().execute();
                } else {
                    Utilities.showAlertDialog("Thông báo", "Thiết bị của bạn chưa được kết nối internet\nVui lòng kiểm tra kết nối internet", getContext());
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
            fileNameEncrypt = fileName;
            if(fileName.length() > 24) {
                fileName = fileName.substring(0,20) + "...";
            }
            tvFileName.setText(fileName);
            tvFileName.setTextColor(Color.BLACK);
        }
    }

    private class MyAsyncTask extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;

        protected void onPreExecute() {
            progressDialog = new ProgressDialog( getContext() );
            progressDialog.setIndeterminate( true );
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage( "Đang mã hóa file..." );
            progressDialog.show();
        }

        @Override
        protected String doInBackground( String... params ) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String owner = currentUser.getUid();
            DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
            byte[] bytes = new byte[32768];
            int nRead;
            try {
                InputStream is = getActivity().getContentResolver().openInputStream(uri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                while ((nRead = is.read(bytes, 0, bytes.length)) != -1) {
                    buffer.write(bytes, 0, nRead);
                }
                buffer.flush();
                String key = edtKeyEncrypt.getText().toString();
                aes.setKey(AES.cryptKey);
                String stKey = aes.Encrypt(key);
                aes.setKey(stKey);
                String ndKey = aes.Encrypt(stKey);
                HomePage.listKeys.add(stKey);
                aes.setKey(key);
                String fileData = aes.static_byteArrayToString(buffer.toByteArray());
                fileData = aes.Encrypt(fileData);
                String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                String rootPath = storagePath + "/Download/Cryptol";
                File root = new File(rootPath);
                root.mkdirs();
                File file = new File(rootPath + "/" + fileNameEncrypt);
                file.createNewFile();
                mData.child("users").child(owner).push().setValue(stKey);
                byte[] bytess = aes.static_stringToByteArray(ndKey + fileData);
                BufferedOutputStream bos = null;
                bos = new BufferedOutputStream(new FileOutputStream(file, false));
                bos.write(bytess);
                bos.flush();
                bos.close();
            } catch (FileNotFoundException e) {
                if ( progressDialog != null && progressDialog.isShowing() ) {
                    progressDialog.dismiss();
                    Utilities.showAlertDialog("Mã hóa thất bại", "File này không còn tồn tại", getContext());
                }
                e.printStackTrace();
            } catch (IOException e) {
                if ( progressDialog != null && progressDialog.isShowing() ) {
                    progressDialog.dismiss();
                    Utilities.showAlertDialog("Mã hóa thất bại", "Đã xảy ra lỗi trong quá trình đọc file", getContext());
                }
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute( String result ) {

            tvFileName.setText("");
            edtKeyEncrypt.setText("");
            fileNameEncrypt = "";
            ckbRandom.setChecked(false);
            if ( progressDialog != null && progressDialog.isShowing() ) {
                progressDialog.dismiss();
                Utilities.showAlertDialog("Mã hóa thành công", "File mã hóa được lưu trong thư mục /Download/Cryptol", getContext());
            }
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
}
