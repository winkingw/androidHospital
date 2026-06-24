package com.serenehealth.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.serenehealth.R;
import com.serenehealth.databinding.ActivityMainBinding;
import com.serenehealth.fragment.HealthArchiveFragment;
import com.serenehealth.fragment.HomeFragment;
import com.serenehealth.fragment.ProfileFragment;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnHomeActionListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_health) {
                fragment = new HealthArchiveFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    @Override
    public void switchToProfile() {
        binding.bottomNav.setSelectedItemId(R.id.nav_profile);
    }
}
