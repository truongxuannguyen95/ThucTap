package com.example.nguyen.fileencryption.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.nguyen.fileencryption.R;
import com.example.nguyen.fileencryption.Utilities;
import com.example.nguyen.fileencryption.model.AES;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private EditText edtOldPw, edtNewPw, edtIdentify;
    private Button btnChange, btnCancel;
    private AES aes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepw);

        edtOldPw = findViewById(R.id.edtOldPw);
        edtNewPw = findViewById(R.id.edtNewPw);
        edtIdentify = findViewById(R.id.edtIdentify);
        btnChange = findViewById(R.id.btnChangePw);
        btnCancel = findViewById(R.id.btnCancel);

        aes = new AES();
        aes.setKey(AES.cryptKey);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChangePassword.this, HomePage.class));
                finish();
            }
        });
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPw = edtOldPw.getText().toString();
                final String newPw = edtNewPw.getText().toString();
                String identify = edtIdentify.getText().toString();
                if(oldPw.isEmpty())
                    edtOldPw.setError("Vui lòng nhập mật khẩu hiện tại");
                else if(newPw.isEmpty())
                    edtNewPw.setError("Vui lòng nhập mật khẩu mới");
                else if(!Utilities.isValidPassword(newPw))
                    edtNewPw.setError("Mật khẩu phải từ 6 ký tự trở lên");
                else if(identify.isEmpty())
                    edtIdentify.setError("Vui lòng nhập xác thực mật khẩu mới");
                else if(!identify.equals(newPw))
                    edtIdentify.setError("Mật khẩu xác thực không khớp");
                else {
                    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    currentUser.reauthenticate(EmailAuthProvider.getCredential(currentUser.getEmail(), oldPw)).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Utilities.showProgressDialog("Đang đổi mật khẩu", ChangePassword.this);
                                currentUser.updatePassword(newPw).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Utilities.dismissProgressDialog();
                                        if(task.isSuccessful()){
                                            SharedPreferences pref = getSharedPreferences("sharedSettings", 0);
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putString("password", aes.Encrypt(edtNewPw.getText().toString()));
                                            editor.commit();
                                            showAlertDialog("Thông báo", "Đổi mật khẩu thành công", true);
                                        }
                                        else {
                                            showAlertDialog("Thông báo", "Đổi mật khẩu thất bại\nCó vẻ đã xảy ra lỗi gì đó!", false);
                                        }
                                    }
                                });
                            } else {
                                if(Utilities.isOnline(getApplicationContext()))
                                    showAlertDialog("Đổi mật khẩu thất bại", "Mật khẩu cũ không đúng", false);
                                else
                                    Utilities.showAlertDialog("Đổi mật khẩu thất bại", "Thiết bị của bạn chưa được kết nối internet", getApplicationContext());
                            }
                        }
                    });
                }
            }
        });
    }

    public void showAlertDialog(String title, String message, final boolean flag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNegativeButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(flag) {
                    finish();
                    startActivity(new Intent(ChangePassword.this, HomePage.class));
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
