package com.example.nguyen.fileencryption;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nguyen.fileencryption.activity.HomePage;
import com.example.nguyen.fileencryption.activity.SignIn;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context myContext;

    public FingerprintHandler(Context myContext) {
        this.myContext = myContext;
    }

    public void startAuth(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
        CancellationSignal cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        this.update("Đã xảy ra lỗi xác thực", false);
    }

    @Override
    public void onAuthenticationFailed() {
        this.update("Xác thực thất bại! Xin hãy thử lại", false);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        this.update(helpString + "", false);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("Xác thực thành công", true);
    }

    private void update(String s, boolean b) {
        TextView tvReportFingerprint = HomePage.dialog.findViewById(R.id.tvReportFingerprint);
        ImageView imgFingerprint = HomePage.dialog.findViewById(R.id.imgFingerprint);
        Button btnOK = HomePage.dialog.findViewById(R.id.btnOK);
        tvReportFingerprint.setText(s);
        if(b == false) {
            tvReportFingerprint.setTextColor(ContextCompat.getColor(myContext, R.color.colorAccent));
            btnOK.setVisibility(View.GONE);
        } else {
            tvReportFingerprint.setTextColor(ContextCompat.getColor(myContext, R.color.colorPrimaryDark));
            imgFingerprint.setImageResource(R.mipmap.icon_done);
            btnOK.setVisibility(View.VISIBLE);
            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomePage.dialog.dismiss();
                }
            });
        }
    }
}
