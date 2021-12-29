package com.rcfin.messenger.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.rcfin.messenger.views.FragmentContacts;
import com.rcfin.messenger.views.FragmentCalls;
import com.rcfin.messenger.views.FragmentMessages;


public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new FragmentContacts();
            case 2:
                return new FragmentCalls();
            default:
                return new FragmentMessages();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
