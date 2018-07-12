package com.example.nguyen.fileencryption.activity;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.nguyen.fileencryption.R;
import com.example.nguyen.fileencryption.Utilities;
import com.example.nguyen.fileencryption.model.AES;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class DecryptFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DecryptFragment() {
    }

    public static DecryptFragment newInstance(String param1, String param2) {
        DecryptFragment fragment = new DecryptFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private Button btnChooseFile, btnDecrypt;
    private TextView tvFileName;
    private Uri uri;
    private AES aes;
    private String fileNameDecrypt = "";

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
        View view = inflater.inflate(R.layout.fragment_decrypt, container, false);
        btnChooseFile = view.findViewById(R.id.btnChooseFile);
        btnDecrypt = view.findViewById(R.id.btnDecrypt);
        tvFileName = view.findViewById(R.id.tvFileName);
        aes = new AES();

        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent = Intent.createChooser(intent, "Chọn file để giải mã");
                startActivityForResult(intent, 0);
            }
        });

        btnDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileNameDecrypt.length() == 0) {
                    Utilities.showAlertDialog("Thông báo", "Vui lòng chọn file để giải mã", getContext());
                } else if(Utilities.isOnline(getContext())){
                    new MyAsyncTask().execute();
                } else {
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
            fileNameDecrypt = fileName;
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
            progressDialog.setMessage( "Đang giải mã file..." );
            progressDialog.show();
        }

        private boolean flag = false;

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
                fileData = aes.Decrypt(fileData);
                String check = fileData.substring(0,10);
                if(check.equals("N14DCAT002")) {
                    String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String rootPath = storagePath + "/Download/Cryptol";
                    File root = new File(rootPath);
                    root.mkdirs();
                    File file = new File(rootPath + "/" + fileNameDecrypt);
                    file.createNewFile();
                    byte[] bytess = aes.static_stringToByteArray(fileData.substring(10));
                    BufferedOutputStream bos = null;
                    bos = new BufferedOutputStream(new FileOutputStream(file, false));
                    bos.write(bytess);
                    bos.flush();
                    bos.close();
                    flag = true;
                } else {
                    flag = false;
                }

            } catch (FileNotFoundException e) {
                if ( progressDialog != null && progressDialog.isShowing() ) {
                    progressDialog.dismiss();
                    Utilities.showAlertDialog("Giải mã thất bại", "File này không còn tồn tại", getContext());
                }
                e.printStackTrace();
            } catch (IOException e) {
                if ( progressDialog != null && progressDialog.isShowing() ) {
                    progressDialog.dismiss();
                    Utilities.showAlertDialog("Giải mã thất bại", "Đã xảy ra lỗi trong quá trình đọc file", getContext());
                }
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute( String result ) {
            tvFileName.setText("");
            fileNameDecrypt = "";
            if ( progressDialog != null && progressDialog.isShowing() ) {
                progressDialog.dismiss();
                if(flag)
                    Utilities.showAlertDialog("Giải mã thành công", "File giải mã được lưu trong thư mục /Download/Cryptol", getContext());
                else
                    Utilities.showAlertDialog("Giải mã thất bại", "File này chưa được mã hóa hoặc dùng không đúng key", getContext());
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
