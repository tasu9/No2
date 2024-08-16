package com.example.no2;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

public final class MyVPAdapter extends FragmentStateAdapter {
    public MyVPAdapter(@NotNull FragmentActivity fa) {
        super(fa);
        Intrinsics.checkNotNullParameter(fa, "fa");

    }

    public int getItemCount() {
        return 2;
    }

    @NotNull
    public Fragment createFragment(int position) {
        Fragment var10000;
        switch (position) {
            case 1:
                var10000 = new CalendarFragment();
                break;
            case 0:
            default:
                var10000 = new MenuFragment();
        }

        return var10000;
    }
}