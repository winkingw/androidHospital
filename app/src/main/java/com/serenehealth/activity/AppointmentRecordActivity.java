package com.serenehealth.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.serenehealth.databinding.ActivityAppointmentRecordBinding;

public class AppointmentRecordActivity extends AppCompatActivity {
    private ActivityAppointmentRecordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
