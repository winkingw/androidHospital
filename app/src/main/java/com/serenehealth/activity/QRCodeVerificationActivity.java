package com.serenehealth.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.serenehealth.bean.User;
import com.serenehealth.databinding.ActivityQrcodeVerificationBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

public class QRCodeVerificationActivity extends AppCompatActivity {

    private ActivityQrcodeVerificationBinding binding;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrcodeVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initData();
        setListeners();
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(this);
        User user = dbHelper.getUserDao().queryUserById(SPUtil.getCurrentUserId());
        if (user == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String qrContent = "SERENE_HEALTH|USER:" + user.getId()
                + "|NAME:" + user.getRealName()
                + "|PHONE:" + user.getPhone();
        binding.ivQrcode.setImageBitmap(createVirtualQr(qrContent, 240));
        binding.tvName.setText(user.getRealName());
        binding.tvPhone.setText("手机号：" + mask(user.getPhone(), 3, 4));
        binding.tvIdCard.setText("身份证：" + mask(user.getIdCardNo(), 3, 4));
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private Bitmap createVirtualQr(String content, int size) {
        int cells = 29;
        int cellSize = size / cells;
        int imageSize = cellSize * cells;
        Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        canvas.drawColor(Color.WHITE);

        int hash = content.hashCode();
        for (int y = 0; y < cells; y++) {
            for (int x = 0; x < cells; x++) {
                boolean finder = isFinderCell(x, y, cells);
                boolean data = ((x * 31 + y * 17 + hash + (x * y)) & 3) == 0;
                if (finder || data) {
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(x * cellSize, y * cellSize,
                            (x + 1) * cellSize, (y + 1) * cellSize, paint);
                }
            }
        }
        return bitmap;
    }

    private boolean isFinderCell(int x, int y, int cells) {
        return isInFinder(x, y, 0, 0)
                || isInFinder(x, y, cells - 7, 0)
                || isInFinder(x, y, 0, cells - 7);
    }

    private boolean isInFinder(int x, int y, int startX, int startY) {
        boolean inBlock = x >= startX && x < startX + 7 && y >= startY && y < startY + 7;
        if (!inBlock) {
            return false;
        }
        int localX = x - startX;
        int localY = y - startY;
        return localX == 0 || localX == 6 || localY == 0 || localY == 6
                || (localX >= 2 && localX <= 4 && localY >= 2 && localY <= 4);
    }

    private String mask(String text, int prefix, int suffix) {
        if (text == null || text.length() <= prefix + suffix) {
            return text != null ? text : "未填写";
        }
        return text.substring(0, prefix) + "****" + text.substring(text.length() - suffix);
    }
}
