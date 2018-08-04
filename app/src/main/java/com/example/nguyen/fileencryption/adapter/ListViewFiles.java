package com.example.nguyen.fileencryption.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.nguyen.fileencryption.R;
import com.example.nguyen.fileencryption.Utilities;
import com.example.nguyen.fileencryption.activity.HomePage;
import com.example.nguyen.fileencryption.model.AES;
import com.example.nguyen.fileencryption.model.Files;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ListViewFiles extends BaseAdapter {

    private Context myContext;
    private ArrayList<Files> listFiles;
    private String tempPath;

    public ListViewFiles(Context myContext, ArrayList<Files> listFiles) {
        this.myContext = myContext;
        this.listFiles = listFiles;
    }

    @Override
    public int getCount() {
        return listFiles.size();
    }

    @Override
    public Object getItem(int i) {
        return listFiles.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowview = view;
        if(rowview == null)
        {
            LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowview = inflater.inflate(R.layout.row_files, null);
        }
        TextView tvFileName = rowview.findViewById(R.id.tvFileName);
        String checkName = listFiles.get(i).getName();
        if(checkName.length() > 24) {
            checkName = checkName.substring(0,20) + "...";
        }
        tvFileName.setText(checkName);
        Button btnDownload = rowview.findViewById(R.id.btnDownload);
        Button btnDelete = rowview.findViewById(R.id.btnDelete);
        final int position = i;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String owner = currentUser.getUid();
        final DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utilities.isOnline(myContext)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
                    builder.setTitle("Bạn có chắc muốn tải file");
                    builder.setMessage(listFiles.get(position).getName());
                    builder.setCancelable(false);
                    builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            final AES aes = new AES();
                            aes.setKey(AES.cryptKey);
                            aes.setKey(aes.Decrypt(listFiles.get(position).getKey()));
                            String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                            String rootPath = storagePath + "/Download";
                            String fileName = "/" + listFiles.get(position).getName();
                            File root = new File(rootPath);
                            root.mkdirs();
                            File file = new File(rootPath + fileName);
                            tempPath = rootPath + fileName;
                            try {
                                int permissionCheck = ContextCompat.checkSelfPermission(myContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    Utilities.showProgressDialog("Đang tải file và giải mã", myContext);
                                    file.createNewFile();
                                    mData.child("files_data").child(listFiles.get(position).getKeyData()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String data = dataSnapshot.getValue().toString();
                                                data = aes.Decrypt(data);
                                                byte[] bytes;
                                                bytes = aes.static_stringToByteArray(data);
                                                BufferedOutputStream bos = null;
                                                File file = new File(tempPath);
                                                try {
                                                    bos = new BufferedOutputStream(new FileOutputStream(file, false));
                                                    bos.write(bytes);
                                                    bos.flush();
                                                    bos.close();
                                                    Utilities.dismissProgressDialog();
                                                    Utilities.showAlertDialog("Thông báo", "Tải file thành công", myContext);
                                                } catch (FileNotFoundException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Utilities.dismissProgressDialog();
                                            Utilities.showAlertDialog("Tải file thất bại", "Đã xảy ra lỗi trong quá trình tải file", myContext);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Utilities.dismissProgressDialog();
                                Utilities.showAlertDialog("Tải file thất bại", "Đã xảy ra lỗi trong quá trình tải file", myContext);
                            }
                        }
                    });
                    builder.setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                        }
                    });
                    alertDialog.show();
                } else {
                    Utilities.showAlertDialog("Tải file thất bại", "Thiết bị của bạn chưa được kết nối internet\nVui lòng thử lại sau", myContext);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
                builder.setTitle("Bạn có chắc muốn xóa file");
                builder.setMessage(listFiles.get(position).getName());
                builder.setCancelable(false);
                builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(Utilities.isOnline(myContext)) {
                            mData.child("files_name").child(owner).child(listFiles.get(position).getKeyData()).removeValue();
                            mData.child("files_data").child(listFiles.get(position).getKeyData()).removeValue();
                            listFiles.remove(position);
                            dialogInterface.dismiss();
                            notifyDataSetChanged();
                        } else {
                            Utilities.showAlertDialog("Xóa thất bại", "Thiết bị của bạn chưa được kết nối internet", myContext);
                        }
                    }
                });
                builder.setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                    }
                });
                alertDialog.show();
            }
        });
        return rowview;
    }
}
