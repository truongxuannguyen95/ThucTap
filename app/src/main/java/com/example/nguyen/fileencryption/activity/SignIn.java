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
    private FirebaseAuth mAuth;
    private TextView tvReportFingerprint;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private Cipher cipher;
    public static Dialog dialog;

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

        final SharedPreferences pref = getSharedPreferences("sharedSettings", 0);
        Boolean check = pref.getBoolean("fingerprint", false);
        if(check){
            showDialogFingerprint();
        }

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

    public void showDialogFingerprint(){
        dialog = new Dialog(SignIn.this);
        dialog.setContentView(R.layout.fingerprint);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        tvReportFingerprint = dialog.findViewById(R.id.tvReportFingerprint);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if(!fingerprintManager.isHardwareDetected()) {
                tvReportFingerprint.setText("Không phát hiện trình quét vân tay trên thiết bị của bạn");
            } else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                tvReportFingerprint.setText("Trình quét vân tay chưa được cấp quyền cho ứng dụng");
            } else if(!keyguardManager.isKeyguardSecure()) {
                tvReportFingerprint.setText("Màn hình khóa bảo mật chưa được thiết lập\nVào 'Cài đặt -> Bảo mật -> Vân tay' để thiết lập vân tay");
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                tvReportFingerprint.setText("Vào 'Cài đặt -> Bảo mật -> Vân tay' và đăng ký ít nhất một vân tay để sử dụng tính năng này");
            } else {
                tvReportFingerprint.setText("Đặt ngón tay của bạn trên trình quét vân tay để truy cập ứng dụng");
                generateKey();
                if (cipherInit()) {
                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    FingerprintHandler fingerprintHandler = new FingerprintHandler(this);
                    fingerprintHandler.startAuth(fingerprintManager, cryptoObject);
                }
            }
        }
        dialog.show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(AES.cryptKey,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {
            e.printStackTrace();
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }
        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(AES.cryptKey, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }

    private void SignIn(String email, String password){
        Utilities.showProgressDialog("Đang đăng nhập", SignIn.this);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Utilities.dismissProgressDialog();
                        if(task.isSuccessful()) {
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
