package com.serenehealth.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.serenehealth.R;
import com.serenehealth.adapter.VisitHistoryAdapter;
import com.serenehealth.bean.Department;
import com.serenehealth.bean.Doctor;
import com.serenehealth.bean.VisitHistory;
import com.serenehealth.databinding.FragmentHealthArchiveBinding;
import com.serenehealth.databinding.ItemPrescriptionCardBinding;
import com.serenehealth.databinding.ItemReportCardBinding;
import com.serenehealth.db.DBHelper;
import com.serenehealth.util.SPUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthArchiveFragment extends Fragment {

    private FragmentHealthArchiveBinding binding;
    private DBHelper dbHelper;
    private TabLayoutMediator tabLayoutMediator;

    private final List<String[]> reportData = new ArrayList<>();
    private final List<String[]> prescriptionData = new ArrayList<>();
    private final List<VisitHistory> visitData = new ArrayList<>();
    private Map<Long, String> departmentMap = new HashMap<>();
    private Map<Long, String> doctorMap = new HashMap<>();

    private ReportAdapter reportAdapter;
    private PrescriptionAdapter prescriptionAdapter;
    private VisitHistoryAdapter visitAdapter;

    private static final int TAB_REPORTS = 0;
    private static final int TAB_PRESCRIPTIONS = 1;
    private static final int TAB_MEDICAL_RECORDS = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHealthArchiveBinding.inflate(inflater, container, false);
        initData();
        setupViewPager();
        return binding.getRoot();
    }

    private void initData() {
        dbHelper = DBHelper.getInstance(requireContext());

        initReportData();
        initPrescriptionData();
        initNameMaps();
        initVisitData();

        reportAdapter = new ReportAdapter();
        prescriptionAdapter = new PrescriptionAdapter();
        visitAdapter = new VisitHistoryAdapter(visitData, departmentMap, doctorMap);
    }

    private void initReportData() {
        reportData.clear();
        reportData.add(new String[]{"血常规", "检验", "白细胞、红细胞均在正常范围", "2026-06-20"});
        reportData.add(new String[]{"心电图", "检查", "窦性心律，未见明显异常", "2026-06-20"});
        reportData.add(new String[]{"B超(腹部)", "检查", "肝胆脾胰未见占位性病变", "2026-06-15"});
    }

    private void initPrescriptionData() {
        prescriptionData.clear();
        prescriptionData.add(new String[]{"维C银翘片", "每日3次，每次2片", "饭后服用", "5天"});
        prescriptionData.add(new String[]{"奥美拉唑肠溶胶囊", "每日1次，每次1粒", "晨起空腹", "14天"});
        prescriptionData.add(new String[]{"阿莫西林胶囊", "每日2次，每次1粒", "饭后服用", "5天"});
    }

    private void initNameMaps() {
        List<Department> departments = dbHelper.getDepartmentDao().queryAllDepartments();
        if (departments != null) {
            for (Department dept : departments) {
                departmentMap.put(dept.getId(), dept.getDeptName());
            }
        }
        List<Doctor> doctors = dbHelper.getDoctorDao().searchDoctors("");
        if (doctors != null) {
            for (Doctor doc : doctors) {
                doctorMap.put(doc.getId(), doc.getDoctorName());
            }
        }
    }

    private void initVisitData() {
        visitData.clear();
        long userId = SPUtil.getCurrentUserId();
        if (userId <= 0) {
            userId = 1;
        }
        List<VisitHistory> records = dbHelper.getVisitHistoryDao().queryByUserId(userId);
        if (records != null) {
            visitData.addAll(records);
        }
    }

    private void setupViewPager() {
        binding.viewPager.setAdapter(new TabPageAdapter());
        binding.viewPager.setOffscreenPageLimit(2);

        tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case TAB_REPORTS:
                            tab.setText(R.string.tab_reports);
                            break;
                        case TAB_PRESCRIPTIONS:
                            tab.setText(R.string.tab_prescriptions);
                            break;
                        case TAB_MEDICAL_RECORDS:
                            tab.setText(R.string.tab_medical_records);
                            break;
                    }
                });
        tabLayoutMediator.attach();
    }

    @Override
    public void onDestroyView() {
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }
        super.onDestroyView();
        binding = null;
    }

    // ==================== ViewPager2 Page Adapter ====================

    private class TabPageAdapter extends RecyclerView.Adapter<TabPageAdapter.PageViewHolder> {

        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            LinearLayout container = new LinearLayout(context);
            container.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            container.setOrientation(LinearLayout.VERTICAL);

            RecyclerView rv = new RecyclerView(context);
            rv.setLayoutManager(new LinearLayoutManager(context));
            rv.setClipToPadding(false);
            int padding = context.getResources().getDimensionPixelSize(R.dimen.margin_mobile);
            rv.setPadding(padding, padding, padding, padding);
            rv.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

            TextView emptyView = new TextView(context);
            emptyView.setText(R.string.no_records);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setTextAppearance(R.style.TextAppearance_SereneHealth_Body);
            emptyView.setTextColor(ContextCompat.getColorStateList(context, R.color.on_surface_variant));
            emptyView.setVisibility(View.GONE);
            emptyView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

            container.addView(rv);
            container.addView(emptyView);

            return new PageViewHolder(container, rv, emptyView);
        }

        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
            switch (position) {
                case TAB_REPORTS:
                    holder.recyclerView.setAdapter(reportAdapter);
                    if (reportData.isEmpty()) {
                        holder.recyclerView.setVisibility(View.GONE);
                        holder.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        holder.recyclerView.setVisibility(View.VISIBLE);
                        holder.emptyView.setVisibility(View.GONE);
                    }
                    break;
                case TAB_PRESCRIPTIONS:
                    holder.recyclerView.setAdapter(prescriptionAdapter);
                    if (prescriptionData.isEmpty()) {
                        holder.recyclerView.setVisibility(View.GONE);
                        holder.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        holder.recyclerView.setVisibility(View.VISIBLE);
                        holder.emptyView.setVisibility(View.GONE);
                    }
                    break;
                case TAB_MEDICAL_RECORDS:
                    holder.recyclerView.setAdapter(visitAdapter);
                    if (visitData.isEmpty()) {
                        holder.recyclerView.setVisibility(View.GONE);
                        holder.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        holder.recyclerView.setVisibility(View.VISIBLE);
                        holder.emptyView.setVisibility(View.GONE);
                    }
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        class PageViewHolder extends RecyclerView.ViewHolder {
            final RecyclerView recyclerView;
            final TextView emptyView;

            PageViewHolder(@NonNull View itemView, RecyclerView recyclerView, TextView emptyView) {
                super(itemView);
                this.recyclerView = recyclerView;
                this.emptyView = emptyView;
            }
        }
    }

    // ==================== Inner Adapters ====================

    private class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemReportCardBinding b = ItemReportCardBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String[] item = reportData.get(position);
            holder.binding.tvReportName.setText(item[0]);
            holder.binding.tvReportType.setText(item[1]);
            holder.binding.tvReportResult.setText(item[2]);
            holder.binding.tvReportDate.setText(item[3]);
        }

        @Override
        public int getItemCount() {
            return reportData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemReportCardBinding binding;

            ViewHolder(ItemReportCardBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    private class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemPrescriptionCardBinding b = ItemPrescriptionCardBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String[] item = prescriptionData.get(position);
            holder.binding.tvDrugName.setText(item[0]);
            holder.binding.tvDosage.setText(item[1]);
            holder.binding.tvTakeMethod.setText(item[2]);
            holder.binding.tvCourse.setText("疗程：" + item[3]);
        }

        @Override
        public int getItemCount() {
            return prescriptionData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemPrescriptionCardBinding binding;

            ViewHolder(ItemPrescriptionCardBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
