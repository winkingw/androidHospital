package com.serenehealth.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.databinding.ActivitySuggestionFeedbackBinding;

public class SuggestionFeedbackActivity extends AppCompatActivity {

    private ActivitySuggestionFeedbackBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuggestionFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSubmit.setOnClickListener(v -> submitSuggestion());
    }

    private void submitSuggestion() {
        String content = binding.etSuggestion.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入反馈内容", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "感谢反馈，我们已收到", Toast.LENGTH_SHORT).show();
        finish();
    }
}
