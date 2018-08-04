package com.example.nguyen.fileencryption.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.nguyen.fileencryption.FingerprintHandler;
import com.example.nguyen.fileencryption.R;
import com.example.nguyen.fileencryption.Utilities;
import com.example.nguyen.fileencryption.model.AES;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SignIn extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnSignIn, btnSignUp, btnForgetPw;
    private CheckBox ckbRemember;
    private FirebaseAuth mAuth;
    private AES aes;
    public static String pwd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        mAuth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnForgetPw = findViewById(R.id.btnForgetPw);
        ckbRemember = findViewById(R.id.ckbRemember);

        aes = new AES();
        aes.setKey(AES.cryptKey);

        final SharedPreferences pref = getSharedPreferences("sharedSettings", 0);
        String email = pref.getString("email", "");
        String password = pref.getString("password", "");
        Boolean remember = pref.getBoolean("remember", false);
        int length = pref.getInt("length", 0);

        ckbRemember.setChecked(remember);

        if(ckbRemember.isChecked()) {
            if(email.length() > 1 && password.length() > 1) {
                edtEmail.setText(email);
                edtPassword.setText(aes.Decrypt(password).substring(0,length));
                SignIn(email, aes.Decrypt(password));
            } else if(email.length() > 1) {
                edtEmail.setText(email);
            }
        }

        ckbRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = pref.edit();
                if(isChecked) {
                    editor.putBoolean("remember", true);
                } else {
                    editor.putBoolean("remember", false);
                }
                editor.commit();
            }
        });

        getPermissions();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                if(email.isEmpty())
                    edtEmail.setError("Vui lòng nhập email");
                else if(!Utilities.isValidEmail(email))
                    edtEmail.setError("Email không hợp lệ");
                else if(password.isEmpty())
                    edtPassword.setError("Vui lòng nhập mật khẩu");
                else {
                    SignIn(email, password);
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignIn.this, SignUp.class));
            }
        });

        btnForgetPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utilities.isOnline(getApplicationContext())) {
                    String email = edtEmail.getText().toString();
                    if (Utilities.isValidEmail(email)) {
                        Utilities.showProgressDialog("Đang gửi mật khẩu mới", SignIn.this);
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Utilities.dismissProgressDialog();
                                        if (task.isSuccessful()) {
                                            Utilities.showAlertDialog("Thông báo", "Kiểm tra email để nhận mật khẩu mới", SignIn.this);
                                        } else {
                                            Utilities.showAlertDialog("Thông báo", "Có vẻ đã xảy ra lỗi gì đó", SignIn.this);
                                        }
                                    }
                                });
                    } else {
                        Utilities.showAlertDialog("Thông báo", "Bạn cần cung cấp email hợp lệ", SignIn.this);
                        edtEmail.setError("Bạn cần cung cấp email hợp lệ");
                    }
                } else {
                    Utilities.showAlertDialog("Thông báo", "Thiết bị của bạn chưa được kết nối internet", SignIn.this);
                }
            }
        });
    }

    private void SignIn(String email, final String password){
        Utilities.showProgressDialog("Đang đăng nhập", SignIn.this);
        mAuth.signInWithEmailAndPassword(email, aes.Encrypt(password))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Utilities.dismissProgressDialog();
                        if(task.isSuccessful()) {
                            pwd = edtPassword.getText().toString();
                            SharedPreferences pref = getSharedPreferences("sharedSettings", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("email", edtEmail.getText().toString());
                            if(ckbRemember.isChecked()) {
                                editor.putString("password", aes.Encrypt(pwd));
                                editor.putInt("length", pwd.length());
                            } else {
                                editor.putString("password", "");
                                editor.putInt("length", 0);
                            }
                            editor.commit();
                            finish();
                            startActivity(new Intent(SignIn.this, HomePage.class));
                        } else {
                            if(Utilities.isOnline(getApplicationContext()))
                                Utilities.showAlertDialog("Đăng nhập thất bại", "Email hoặc mật khẩu sai!", SignIn.this);
                            else
                                Utilities.showAlertDialog("Thông báo", "Thiết bị của bạn chưa được kết nối internet", SignIn.this);
                        }
                    }
                });
    }

    public void getPermissions(){
        ActivityCompat.requestPermissions(SignIn.this,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                1);
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
                return;
        }
    }
}
