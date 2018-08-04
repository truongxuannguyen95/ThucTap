package com.example.nguyen.fileencryption;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    private static ProgressDialog progressDialog;

    public static boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String pass) {
        return pass != null && pass.length() >= 6;
    }

    public static boolean isValidKey(String key){
        String KEY_PATTERN = "[A-Za-z0-9]{16}";
        Pattern pattern = Pattern.compile(KEY_PATTERN);
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
    }

    public static String rand(){
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String key = "";
        Random rand = new Random();
        for(int i=0; i<16; i++) {
            int index = rand.nextInt(36);
            key += saltChars.charAt(index);
        }
        return key;
    }

    public static boolean isOnline(Context context){
        NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    public static void showAlertDialog(String title, String message, Context myContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNegativeButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void showProgressDialog(String message, Context myContext) {
        progressDialog = new ProgressDialog(myContext);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
