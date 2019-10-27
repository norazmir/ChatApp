package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.chatapp.Fragment.ChatFragment;
import com.example.chatapp.Fragment.FriendFragment;
import com.example.chatapp.Fragment.RequestFragment;

class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fragmentManager){
        super(fragmentManager);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 2:
                FriendFragment friendFragment = new FriendFragment();
                return friendFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){

        switch (position){
            case 0:
                return "REQUEST";
            case 1:
                return "CHAT";
            case 2:
                return "FRIENDS";
            default:
                return null;
        }
    }
}
