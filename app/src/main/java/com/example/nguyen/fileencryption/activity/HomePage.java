package com.example.nguyen.fileencryption.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nguyen.fileencryption.FingerprintHandler;
import com.example.nguyen.fileencryption.R;
import com.example.nguyen.fileencryption.Utilities;
import com.example.nguyen.fileencryption.adapter.HomePager;
import com.example.nguyen.fileencryption.model.AES;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


/*GenerateKey(): sẽ tạo một khóa mã hóa sau đó được lưu trữ an toàn trên thiết bị.
CipherInit(): sẽ khởi tạo mật mã và sẽ được sử dụng để tạo ra các FingerprintManager mã hóa.
Đối tượng CryptoObject và kiểm tra khác nhau trước khi bắt đầu quá trình xác thực
được thực hiện bên trong phương thức onCreate().*/

public class HomePage extends AppCompatActivity {

    private Dialog dialogFingerprint;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private TextView tvReportFingerprint;
    private KeyStore keyStore;
    private Cipher cipher;
    public static Dialog dialog;
    public static ArrayList<String> listKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        if(!Utilities.isOnline(HomePage.this))
            showAlertDialog();
        else
            getKeys();

        SharedPreferences pref = getSharedPreferences("sharedSettings", 0);
        Boolean check = pref.getBoolean("fingerprint", false);
        if(check){
            showDialogFingerprint();
        }

        showActionBar();
    }

    public void getKeys(){
        listKeys = new ArrayList<>();
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        String userID = currentUser.getUid();
        mData.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        String key = data.getValue().toString();
                        listKeys.add(key);
                    }
                    showTab();
                } else {
                    showTab();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utilities.showAlertDialog("Thông báo", "Đã xảy ra lỗi trong quá trình kiểm tra dữ liệu. Vui lòng thử lại sau", HomePage.this);
            }
        });
    }

    private void showTab(){
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(" Mã hóa "));
        tabLayout.addTab(tabLayout.newTab().setText(" Giải mã "));
        tabLayout.addTab(tabLayout.newTab().setText(" Upload "));
        tabLayout.addTab(tabLayout.newTab().setText(" Download "));
        final ViewPager homePager = findViewById(R.id.homePager);
        HomePager homePagerFragment = new HomePager(
                getSupportFragmentManager(),
                tabLayout.getTabCount()
        );
        homePager.setAdapter(homePagerFragment);
        homePager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                homePager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.item_fingerprint) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
                keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                if(!fingerprintManager.isHardwareDetected()) {
                    Utilities.showAlertDialog("Thông báo", "Không phát hiện trình quét vân tay trên thiết bị của bạn", HomePage.this);
                } else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    Utilities.showAlertDialog("Thông báo", "Trình quét vân tay chưa được cấp quyền cho ứng dụng", HomePage.this);
                } else if(!keyguardManager.isKeyguardSecure()) {
                    Utilities.showAlertDialog("Màn hình khóa bảo mật chưa được thiết lập", "Vào 'Cài đặt -> Bảo mật -> Vân tay' để thiết lập vân tay", HomePage.this);
                } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                    Utilities.showAlertDialog("Thông báo", "Vào 'Cài đặt -> Bảo mật -> Vân tay' và đăng ký ít nhất một vân tay để sử dụng tính năng này", HomePage.this);
                } else {
                    showDialogManagerFingerprint();
                }
            }
        }
        if(item.getItemId() == R.id.item_change) {
            startActivity(new Intent(HomePage.this, ChangePassword.class));
        }
        if(item.getItemId() == R.id.item_logout) {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences pref = getSharedPreferences("sharedSettings", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("password", "");
            editor.putInt("length", 0);
            editor.commit();
            finish();
            startActivity(new Intent(HomePage.this, SignIn.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( HomePage.this);
        builder.setTitle("Lỗi kết nối");
        builder.setMessage("Thiết bị của bạn chưa được kết nối internet. Vui lòng kiểm tra lại");
        builder.setCancelable(false);
        builder.setNegativeButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(!Utilities.isOnline(HomePage.this))
                    showAlertDialog();
                else
                    getKeys();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showDialogManagerFingerprint(){
        dialogFingerprint = new Dialog(HomePage.this);
        dialogFingerprint.setContentView(R.layout.fingerprint_manager);
        dialogFingerprint.setCancelable(false);
        dialogFingerprint.setCanceledOnTouchOutside(false);
        final Switch swFingerprint = dialogFingerprint.findViewById(R.id.swFingerprint);
        Button btnManagerFingerprint = dialogFingerprint.findViewById(R.id.btnManagerFingerprint);
        Button btnCancel = dialogFingerprint.findViewById(R.id.btnCancel);
        final TextView tvReport = dialogFingerprint.findViewById(R.id.tvReport);
        final EditText edtCheckPwd = dialogFingerprint.findViewById(R.id.edtCheckPwd);
        final SharedPreferences pref = getSharedPreferences("sharedSettings", 0);
        final Boolean check = pref.getBoolean("fingerprint", false);
        tvReport.setVisibility(View.GONE);
        edtCheckPwd.setVisibility(View.GONE);
        swFingerprint.setChecked(check);
        swFingerprint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tvReport.setVisibility(View.GONE);
                if(isChecked) {
                    edtCheckPwd.setVisibility(View.GONE);
                } else {
                    if(check) {
                        edtCheckPwd.setVisibility(View.VISIBLE);
                        edtCheckPwd.setText("");
                    }
                }
            }
        });
        btnManagerFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = pref.edit();
                if(swFingerprint.isChecked()){
                    if(!check) {
                        editor.putBoolean("fingerprint", true);
                        Toast.makeText(getApplicationContext(), "Truy cập ứng dụng với dấu vân tay đã được kích hoạt", Toast.LENGTH_SHORT).show();
                    }
                    dialogFingerprint.dismiss();
                } else {
                    if(check) {
                        if(edtCheckPwd.getText().toString().equals(SignIn.pwd)) {
                            editor.putBoolean("fingerprint", false);
                            dialogFingerprint.dismiss();
                            Toast.makeText(getApplicationContext(), "Truy cập ứng dụng với dấu vân tay đã được hủy bỏ", Toast.LENGTH_SHORT).show();
                        } else{
                            tvReport.setVisibility(View.VISIBLE);
                            if(edtCheckPwd.getText().toString().equals(""))
                                tvReport.setText("Vui lòng nhập mật khẩu để hủy bỏ");
                            else
                                tvReport.setText("Mật khẩu không đúng");
                        }
                    }
                }
                editor.commit();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFingerprint.dismiss();
            }
        });
        dialogFingerprint.show();
    }

    public void showDialogFingerprint(){
        dialog = new Dialog(HomePage.this);
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
}
