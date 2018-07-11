package com.example.nguyen.fileencryption.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.nguyen.fileencryption.activity.DecryptFragment;
import com.example.nguyen.fileencryption.activity.DownloadFragment;
import com.example.nguyen.fileencryption.activity.EncryptFragment;
import com.example.nguyen.fileencryption.activity.UploadFragment;

public class HomePager extends FragmentStatePagerAdapter {
    private int numOfTabs;
    public HomePager(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                EncryptFragment encryptFragment = new EncryptFragment();
                return encryptFragment;
            case 1:
                DecryptFragment decryptFragment = new DecryptFragment();
                return decryptFragment;
            case 2:
                UploadFragment uploadFragment = new UploadFragment();
                return uploadFragment;
            case 3:
                DownloadFragment downloadFragment = new DownloadFragment();
                return downloadFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
