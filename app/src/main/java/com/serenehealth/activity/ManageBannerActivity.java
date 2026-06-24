package com.serenehealth.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serenehealth.R;

import com.google.android.material.card.MaterialCardView;
import com.serenehealth.bean.Banner;
import com.serenehealth.databinding.ActivityManageBannerBinding;
import com.serenehealth.db.BannerDao;
import com.serenehealth.db.DBHelper;

import java.util.List;

public class ManageBannerActivity extends AppCompatActivity {

    private ActivityManageBannerBinding binding;
    private DBHelper dbHelper;
    private BannerDao bannerDao;
    private List<Banner> bannerList;
    private BannerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageBannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DBHelper.getInstance(this);
        bannerDao = dbHelper.getBannerDao();

        initRecyclerView();
        loadBanners();
        setListeners();
    }

    private void initRecyclerView() {
        binding.rvBannerList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadBanners() {
        bannerList = bannerDao.queryAllBanners();
        if (bannerList.isEmpty()) {
            binding.rvBannerList.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.rvBannerList.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
            adapter = new BannerAdapter(bannerList);
            binding.rvBannerList.setAdapter(adapter);
        }
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        LinearLayout form = createFormLayout(null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("添加轮播图")
                .setView(form)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (saveBannerFromForm(form, null)) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void showEditDialog(Banner banner) {
        LinearLayout form = createFormLayout(banner);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("编辑轮播图")
                .setView(form)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (saveBannerFromForm(form, banner)) {
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private LinearLayout createFormLayout(Banner banner) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(
                getResources().getDimensionPixelSize(com.google.android.material.R.dimen.abc_dialog_padding_material),
                0,
                getResources().getDimensionPixelSize(com.google.android.material.R.dimen.abc_dialog_padding_material),
                0
        );

        // Title
        TextView titleLabel = new TextView(this);
        titleLabel.setText("标题");
        titleLabel.setTextSize(14);
        titleLabel.setTextColor(
        ContextCompat.getColor(this, R.color.on_surface));
        titleLabel.setPadding(0, dpToPx(12), 0, dpToPx(4));
        form.addView(titleLabel);

        EditText etTitle = new EditText(this);
        etTitle.setHint("请输入标题");
        etTitle.setId(View.generateViewId());
        if (banner != null && banner.getTitle() != null) {
            etTitle.setText(banner.getTitle());
        }
        form.addView(etTitle);

        // ImageUrl
        TextView imageUrlLabel = new TextView(this);
        imageUrlLabel.setText("图片地址");
        imageUrlLabel.setTextSize(14);
        imageUrlLabel.setTextColor(
        ContextCompat.getColor(this, R.color.on_surface));
        imageUrlLabel.setPadding(0, dpToPx(12), 0, dpToPx(4));
        form.addView(imageUrlLabel);

        EditText etImageUrl = new EditText(this);
        etImageUrl.setHint("请输入图片URL");
        etImageUrl.setId(View.generateViewId());
        if (banner != null && banner.getImageUrl() != null) {
            etImageUrl.setText(banner.getImageUrl());
        }
        form.addView(etImageUrl);

        // JumpType
        TextView jumpTypeLabel = new TextView(this);
        jumpTypeLabel.setText("跳转类型");
        jumpTypeLabel.setTextSize(14);
        jumpTypeLabel.setTextColor(
        ContextCompat.getColor(this, R.color.on_surface));
        jumpTypeLabel.setPadding(0, dpToPx(12), 0, dpToPx(4));
        form.addView(jumpTypeLabel);

        Spinner spJumpType = new Spinner(this);
        spJumpType.setId(View.generateViewId());
        ArrayAdapter<String> jumpTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"NONE", "URL", "PAGE"});
        jumpTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spJumpType.setAdapter(jumpTypeAdapter);
        if (banner != null && banner.getJumpType() != null) {
            String[] values = {"NONE", "URL", "PAGE"};
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(banner.getJumpType())) {
                    spJumpType.setSelection(i);
                    break;
                }
            }
        }
        form.addView(spJumpType);

        // JumpValue (initially hidden if NONE is selected)
        final LinearLayout jumpValueContainer = new LinearLayout(this);
        jumpValueContainer.setOrientation(LinearLayout.VERTICAL);
        jumpValueContainer.setId(View.generateViewId());

        TextView jumpValueLabel = new TextView(this);
        jumpValueLabel.setText("跳转值");
        jumpValueLabel.setTextSize(14);
        jumpValueLabel.setTextColor(
        ContextCompat.getColor(this, R.color.on_surface));
        jumpValueLabel.setPadding(0, dpToPx(12), 0, dpToPx(4));
        jumpValueContainer.addView(jumpValueLabel);

        EditText etJumpValue = new EditText(this);
        etJumpValue.setHint("请输入跳转值");
        etJumpValue.setId(View.generateViewId());
        if (banner != null && banner.getJumpValue() != null) {
            etJumpValue.setText(banner.getJumpValue());
        }
        jumpValueContainer.addView(etJumpValue);

        // Show/hide on selection
        spJumpType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                jumpValueContainer.setVisibility("NONE".equals(selected) ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                jumpValueContainer.setVisibility(View.GONE);
            }
        });

        String initialJumpType = (banner != null && banner.getJumpType() != null) ? banner.getJumpType() : "NONE";
        jumpValueContainer.setVisibility("NONE".equals(initialJumpType) ? View.GONE : View.VISIBLE);

        form.addView(jumpValueContainer);

        // SortNo
        TextView sortNoLabel = new TextView(this);
        sortNoLabel.setText("排序号");
        sortNoLabel.setTextSize(14);
        sortNoLabel.setTextColor(
        ContextCompat.getColor(this, R.color.on_surface));
        sortNoLabel.setPadding(0, dpToPx(12), 0, dpToPx(4));
        form.addView(sortNoLabel);

        EditText etSortNo = new EditText(this);
        etSortNo.setHint("请输入排序号");
        etSortNo.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etSortNo.setId(View.generateViewId());
        if (banner != null) {
            etSortNo.setText(String.valueOf(banner.getSortNo()));
        }
        form.addView(etSortNo);

        // Status checkbox
        CheckBox cbStatus = new CheckBox(this);
        cbStatus.setText("启用");
        cbStatus.setId(View.generateViewId());
        cbStatus.setChecked(banner == null || banner.getStatus() == 1);
        cbStatus.setPadding(0, dpToPx(8), 0, 0);
        form.addView(cbStatus);

        // Store references for later retrieval
        form.setTag(new FormTag(etTitle, etImageUrl, spJumpType, etJumpValue, etSortNo, cbStatus));

        return form;
    }

    private boolean saveBannerFromForm(LinearLayout form, Banner existing) {
        FormTag tag = (FormTag) form.getTag();

        String title = tag.etTitle.getText().toString().trim();
        String imageUrl = tag.etImageUrl.getText().toString().trim();
        String jumpType = tag.spJumpType.getSelectedItem().toString();
        String jumpValue = tag.etJumpValue.getText().toString().trim();
        String sortNoStr = tag.etSortNo.getText().toString().trim();
        int status = tag.cbStatus.isChecked() ? 1 : 0;

        if (imageUrl.isEmpty()) {
            Toast.makeText(this, "请输入图片地址", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (sortNoStr.isEmpty()) {
            Toast.makeText(this, "请输入排序号", Toast.LENGTH_SHORT).show();
            return false;
        }

        int sortNo;
        try {
            sortNo = Integer.parseInt(sortNoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "排序号请输入有效数字", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (existing != null) {
            // Update existing
            existing.setTitle(title.isEmpty() ? null : title);
            existing.setImageUrl(imageUrl);
            existing.setJumpType(jumpType);
            existing.setJumpValue("NONE".equals(jumpType) ? null : jumpValue);
            existing.setSortNo(sortNo);
            existing.setStatus(status);
            int rows = bannerDao.update(existing);
            if (rows > 0) {
                Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "修改失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Insert new
            Banner banner = new Banner();
            banner.setTitle(title.isEmpty() ? null : title);
            banner.setImageUrl(imageUrl);
            banner.setJumpType(jumpType);
            banner.setJumpValue("NONE".equals(jumpType) ? null : jumpValue);
            banner.setSortNo(sortNo);
            banner.setStatus(status);
            long newId = bannerDao.insert(banner);
            if (newId > 0) {
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
            }
        }

        refreshList();
        return true;
    }

    private void showDeleteDialog(Banner banner) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除轮播图 \"" + (banner.getTitle() != null ? banner.getTitle() : "") + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    int rows = bannerDao.delete(banner.getId());
                    if (rows > 0) {
                        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                        refreshList();
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void refreshList() {
        bannerList = bannerDao.queryAllBanners();
        if (bannerList.isEmpty()) {
            binding.rvBannerList.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } else {
            binding.rvBannerList.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
            if (adapter != null) {
                adapter.updateData(bannerList);
            } else {
                adapter = new BannerAdapter(bannerList);
                binding.rvBannerList.setAdapter(adapter);
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // ==================== Form Tag ====================

    private static class FormTag {
        EditText etTitle;
        EditText etImageUrl;
        Spinner spJumpType;
        EditText etJumpValue;
        EditText etSortNo;
        CheckBox cbStatus;

        FormTag(EditText etTitle, EditText etImageUrl, Spinner spJumpType,
                EditText etJumpValue, EditText etSortNo, CheckBox cbStatus) {
            this.etTitle = etTitle;
            this.etImageUrl = etImageUrl;
            this.spJumpType = spJumpType;
            this.etJumpValue = etJumpValue;
            this.etSortNo = etSortNo;
            this.cbStatus = cbStatus;
        }
    }

    // ==================== Adapter ====================

    private class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {

        private List<Banner> list;

        BannerAdapter(List<Banner> list) {
            this.list = list;
        }

        void updateData(List<Banner> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Build item view programmatically
            LinearLayout itemLayout = new LinearLayout(ManageBannerActivity.this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));

            // Left content: title, imageUrl, sortNo
            LinearLayout leftContent = new LinearLayout(ManageBannerActivity.this);
            leftContent.setOrientation(LinearLayout.VERTICAL);
            leftContent.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvTitle = new TextView(ManageBannerActivity.this);
            tvTitle.setId(View.generateViewId());
            tvTitle.setTextSize(16);
            tvTitle.setTextColor(
                    ContextCompat.getColor(ManageBannerActivity.this, R.color.on_surface));

            TextView tvImageUrl = new TextView(ManageBannerActivity.this);
            tvImageUrl.setId(View.generateViewId());
            tvImageUrl.setTextSize(12);
            tvImageUrl.setTextColor(
                    ContextCompat.getColor(ManageBannerActivity.this, R.color.on_surface_variant));
            tvImageUrl.setMaxLines(1);
            tvImageUrl.setEllipsize(android.text.TextUtils.TruncateAt.END);

            TextView tvSortNo = new TextView(ManageBannerActivity.this);
            tvSortNo.setId(View.generateViewId());
            tvSortNo.setTextSize(12);
            tvSortNo.setTextColor(
                    ContextCompat.getColor(ManageBannerActivity.this, R.color.on_surface_variant));
            tvSortNo.setPadding(0, dpToPx(4), 0, 0);

            leftContent.addView(tvTitle);
            leftContent.addView(tvImageUrl);
            leftContent.addView(tvSortNo);

            // Right: status badge
            TextView tvStatus = new TextView(ManageBannerActivity.this);
            tvStatus.setId(View.generateViewId());
            tvStatus.setTextSize(14);
            tvStatus.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
            tvStatus.setGravity(Gravity.CENTER);
            tvStatus.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            itemLayout.addView(leftContent);
            itemLayout.addView(tvStatus);

            // Wrap in MaterialCardView
            MaterialCardView cardView = new MaterialCardView(ManageBannerActivity.this);
            cardView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));
            cardView.setCardElevation(0);
            cardView.setStrokeWidth(1);
            cardView.setStrokeColor(
                    ContextCompat.getColor(ManageBannerActivity.this, R.color.outline_variant));
            cardView.setRadius(dpToPx(8));
            cardView.setUseCompatPadding(true);
            ((ViewGroup.MarginLayoutParams) cardView.getLayoutParams()).leftMargin = dpToPx(16);
            ((ViewGroup.MarginLayoutParams) cardView.getLayoutParams()).rightMargin = dpToPx(16);
            ((ViewGroup.MarginLayoutParams) cardView.getLayoutParams()).topMargin = dpToPx(4);
            ((ViewGroup.MarginLayoutParams) cardView.getLayoutParams()).bottomMargin = dpToPx(4);
            cardView.setClickable(true);
            cardView.setFocusable(true);
            android.util.TypedValue foregroundAttr = new android.util.TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.selectableItemBackground, foregroundAttr, true)) {
                cardView.setForeground(getDrawable(foregroundAttr.resourceId));
            }

            cardView.addView(itemLayout);

            return new ViewHolder(cardView, tvTitle, tvImageUrl, tvSortNo, tvStatus);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Banner banner = list.get(position);

            holder.tvTitle.setText(banner.getTitle() != null ? banner.getTitle() : "(无标题)");
            holder.tvImageUrl.setText(banner.getImageUrl());
            holder.tvSortNo.setText("排序: " + banner.getSortNo());

            if (banner.getStatus() == 1) {
                holder.tvStatus.setText("启用");
                holder.tvStatus.setTextColor(
                        ContextCompat.getColor(ManageBannerActivity.this, R.color.status_visited));
            } else {
                holder.tvStatus.setText("隐藏");
                holder.tvStatus.setTextColor(
                        ContextCompat.getColor(ManageBannerActivity.this, R.color.on_surface_variant));
            }

            // Click to edit
            holder.itemView.setOnClickListener(v -> showEditDialog(banner));

            // Long press to delete
            holder.itemView.setOnLongClickListener(v -> {
                showDeleteDialog(banner);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextView tvImageUrl;
            TextView tvSortNo;
            TextView tvStatus;

            ViewHolder(View itemView, TextView tvTitle, TextView tvImageUrl,
                       TextView tvSortNo, TextView tvStatus) {
                super(itemView);
                this.tvTitle = tvTitle;
                this.tvImageUrl = tvImageUrl;
                this.tvSortNo = tvSortNo;
                this.tvStatus = tvStatus;
            }
        }
    }
}
