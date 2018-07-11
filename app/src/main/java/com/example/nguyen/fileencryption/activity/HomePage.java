package com.example.nguyen.fileencryption.activity;

import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.nguyen.fileencryption.R;
import com.example.nguyen.fileencryption.Utilities;
import com.example.nguyen.fileencryption.adapter.HomePager;
import com.example.nguyen.fileencryption.model.AES;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomePage extends AppCompatActivity {

    private Dialog dialogFingerprint, dialogInputKey;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private DatabaseReference mData;
    private String userID;
    private AES aes;
    public static String myKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        aes = new AES();
        aes.setKey(AES.cryptKey);

        mData = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        userID = currentUser.getUid();
        mData.child("user").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    myKey = dataSnapshot.getValue().toString();
                    if(myKey.equals("")) {
                        showDialogInputKey();
                    } else {
                        myKey = aes.Decrypt(myKey);
                        showTab();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utilities.showAlertDialog("Thông báo", "Đã xảy ra lỗi trong quá trình kiểm tra dữ liệu\nVui lòng thử lại sau", HomePage.this);
            }
        });

        showActionBar();
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
                    showDialogFingerprint();
                }
            }
        }
        if(item.getItemId() == R.id.item_change) {
            startActivity(new Intent(HomePage.this, ChangePassword.class));
        }
        if(item.getItemId() == R.id.item_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(HomePage.this, SignIn.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDialogFingerprint(){
        dialogFingerprint = new Dialog(HomePage.this);
        dialogFingerprint.setContentView(R.layout.fingerprint_manager);
        dialogFingerprint.setCancelable(false);
        dialogFingerprint.setCanceledOnTouchOutside(false);
        final Switch swFingerprint = dialogFingerprint.findViewById(R.id.swFingerprint);
        Button btnManagerFingerprint = dialogFingerprint.findViewById(R.id.btnManagerFingerprint);
        final SharedPreferences pref = getSharedPreferences("sharedSettings", 0);
        Boolean check = pref.getBoolean("fingerprint", false);
        swFingerprint.setChecked(check);
        btnManagerFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = pref.edit();
                if(swFingerprint.isChecked()){
                    editor.putBoolean("fingerprint", true);
                    Toast.makeText(getApplicationContext(), "Truy cập ứng dụng với dấu vân tay đã được kích hoạt", Toast.LENGTH_SHORT).show();
                } else {
                    editor.putBoolean("fingerprint", false);
                    Toast.makeText(getApplicationContext(), "Truy cập ứng dụng với dấu vân tay đã được hủy bỏ", Toast.LENGTH_SHORT).show();
                }
                editor.commit();
                dialogFingerprint.dismiss();
            }
        });
        dialogFingerprint.show();
    }

    public void showDialogInputKey(){
        dialogInputKey = new Dialog(HomePage.this);
        dialogInputKey.setContentView(R.layout.dialog_input_key);
        dialogInputKey.setCancelable(false);
        dialogInputKey.setCanceledOnTouchOutside(false);
        final EditText edtKey = dialogInputKey.findViewById(R.id.edtKey);
        Button btnInputKey = dialogInputKey.findViewById(R.id.btnInputKey);
        btnInputKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isValidKey(edtKey.getText().toString())) {
                    edtKey.setError("Key không được chứa ký tự đặc biệt và phải có đúng 16 ký tự");
                } else {
                    myKey = edtKey.getText().toString();
                    mData.child("user").child(userID).setValue(aes.Encrypt(myKey))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialogInputKey.dismiss();
                                    showTab();
                                    Utilities.showAlertDialog("Tạo key thành công", "Bạn đã có thể sử dụng các tính năng của ứng dụng", HomePage.this);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialogInputKey.dismiss();
                                    showTab();
                                    Utilities.showAlertDialog("Tạo key thất bại", "Đã xảy ra lỗi trong quá trình tạo key", HomePage.this);
                                }
                            });

                }
            }
        });
        dialogInputKey.show();
    }

    public boolean isValidKey(String key){
        String KEY_PATTERN = "[A-Za-z0-9]{16}";
        Pattern pattern = Pattern.compile(KEY_PATTERN);
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
    }
}
