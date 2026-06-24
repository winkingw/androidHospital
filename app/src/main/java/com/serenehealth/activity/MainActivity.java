package com.serenehealth.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.serenehealth.R;
import com.serenehealth.databinding.ActivityMainBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.fragment.HealthArchiveFragment;
import com.serenehealth.fragment.HomeFragment;
import com.serenehealth.fragment.ProfileFragment;
import com.serenehealth.util.MockDataUtil;
import com.serenehealth.util.SPUtil;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnHomeActionListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Redirect to login if not authenticated
        if (!SPUtil.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (SPUtil.isFirstLaunch()) {
            DBHelper dbHelper = DBHelper.getInstance(this);
            dbHelper.getWritableDatabase();
            MockDataUtil.initAll(dbHelper);
            SPUtil.setFirstLaunchDone();
        }

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

    @Override
    public void switchToHealthArchive() {
        binding.bottomNav.setSelectedItemId(R.id.nav_health);
    }
}
