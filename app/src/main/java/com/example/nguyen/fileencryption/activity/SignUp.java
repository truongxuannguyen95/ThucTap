package com.example.nguyen.fileencryption.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nguyen.fileencryption.R;
import com.example.nguyen.fileencryption.Utilities;
import com.example.nguyen.fileencryption.model.AES;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtIdentify;
    private Button btnSignUp, btnCancel;
    private FirebaseAuth mAuth;
    AES aes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        mAuth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtIdentify = findViewById(R.id.edtIdentify);
        btnSignUp = findViewById(R.id.btnSignup);
        btnCancel = findViewById(R.id.btnCancel);

        aes = new AES();
        aes.setKey(AES.cryptKey);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                String identify = edtIdentify.getText().toString();
                if(email.isEmpty())
                    edtEmail.setError("Vui lòng nhập email");
                else if(!Utilities.isValidEmail(email))
                    edtEmail.setError("Email không hợp lệ");
                else if(password.isEmpty())
                    edtPassword.setError("Vui lòng nhập mật khẩu");
                else if(!Utilities.isValidPassword(password))
                    edtPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
                else if(identify.isEmpty())
                    edtIdentify.setError("Vui lòng nhập xác thực mật khẩu");
                else if(!identify.equals(password))
                    edtIdentify.setError("Mật khẩu xác thực không khớp");
                else
                    SignUp(email, password);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(SignUp.this, SignIn.class));
            }
        });

    }

    private void SignUp(String email, String password){
        Utilities.showProgressDialog("Đang đăng ký", SignUp.this);
        mAuth.createUserWithEmailAndPassword(email, aes.Encrypt(password))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Utilities.dismissProgressDialog();
                        if(task.isSuccessful())
                        {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
                            mData.child("users").child(currentUser.getUid()).setValue("");
                            showAlertDialog("Đăng ký thành công", "Bạn đã có thể tiến hành đăng nhập bằng tài khoản này", true);
                        }
                        else {
                            if(Utilities.isOnline(getApplicationContext()))
                                showAlertDialog("Đăng ký thất bại", "Email này đã được sử dụng\nVui lòng sử dụng 1 email khác", false);
                            else
                                showAlertDialog("Đăng ký thất bại", "Thiết bị của bạn chưa được kết nối internet", false);
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
                    startActivity(new Intent(SignUp.this, SignIn.class));
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
