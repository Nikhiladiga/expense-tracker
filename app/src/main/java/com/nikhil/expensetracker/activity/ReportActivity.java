package com.nikhil.expensetracker.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.adapters.ReportItemsAdapter;
import com.nikhil.expensetracker.databinding.ActivityReportBinding;
import com.nikhil.expensetracker.model.ReportData;
import com.nikhil.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private ActivityReportBinding mBinding;
    private final List<ReportData> reportDataList = new ArrayList<>();
    private ReportItemsAdapter reportItemsAdapter;
    private String currentMonth;

    private BarChart horizontalBarChart;
    ArrayList<BarEntry> entries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        //Get current month
        currentMonth = DateUtils.getCurrentMonth();
        mBinding.currentMonth.setText(currentMonth.toUpperCase(Locale.ROOT));

        //Handle adapter for top spent amount
        handleReportAdapter();

        //Handle months popup
        handleMonthsPopup();

        //Handle no transactions layer
        handleNoTransactionsLayer();

        //Handle piechart rendering
        handleChartRender();

    }

    private void handleChartRender() {
        horizontalBarChart = mBinding.horizontalBarChart;
        horizontalBarChart.getDescription().setEnabled(false);
        horizontalBarChart.setExtraOffsets(5, 10, 5, 5);
        horizontalBarChart.setDragDecelerationFrictionCoef(0.95f);
        horizontalBarChart.setHighlightPerTapEnabled(true);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.interbold);

        Legend l = horizontalBarChart.getLegend();
        l.setEnabled(false);

        ArrayList<String> xAxisLabels = new ArrayList<>();

        entries.clear();
        for (int i = 0; i < reportDataList.size(); i++) {
            xAxisLabels.add(reportDataList.get(i).getCategory());
            entries.add(new BarEntry(i, reportDataList.get(i).getAmount().floatValue()));
        }

        BarDataSet barDataSet = new BarDataSet(entries, null);
        barDataSet.setDrawIcons(false);

        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(typeface);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(getResources().getColor(R.color.tertiary));
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));

        YAxis leftAxis = horizontalBarChart.getAxisLeft();
        leftAxis.setEnabled(false);

        YAxis rightAxis = horizontalBarChart.getAxisRight();
        rightAxis.setEnabled(false);

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        barDataSet.setColors(colors);

        BarData data = new BarData(barDataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "₹" + value;
            }
        });
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(typeface);
        data.setValueTextSize(9f);
        horizontalBarChart.setData(data);
        horizontalBarChart.invalidate();
    }

    private void handleNoTransactionsLayer() {
        if (reportDataList.size() < 1) {
            mBinding.spentMostHeader.setVisibility(View.GONE);
            mBinding.noReportsAvailable.setVisibility(View.VISIBLE);
            mBinding.topAmountSpentByCategoriesList.setVisibility(View.GONE);
        } else {
            mBinding.spentMostHeader.setVisibility(View.VISIBLE);
            mBinding.noReportsAvailable.setVisibility(View.GONE);
            mBinding.topAmountSpentByCategoriesList.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleMonthsPopup() {
        //Set months in dropdown
        PopupMenu popupMenu = new PopupMenu(this, mBinding.currentMonthIcon);
        popupMenu.inflate(R.menu.months);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            currentMonth = menuItem.getTitle().toString();
            mBinding.currentMonth.setText(currentMonth);
            getDataByMonth();
            handleNoTransactionsLayer();
            reportItemsAdapter.notifyDataSetChanged();
            handleChartRender();
            return true;
        });

        //Show month dropdown on icon click
        mBinding.currentMonthIcon.setOnClickListener(view -> {
            popupMenu.show();
        });
    }

    private void handleReportAdapter() {
        getDataByMonth();

        //Create adapter and set values
        reportItemsAdapter = new ReportItemsAdapter(this, reportDataList);
        mBinding.topAmountSpentByCategoriesList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.topAmountSpentByCategoriesList.setAdapter(reportItemsAdapter);
    }

    private void getDataByMonth() {
        //Get top n categories by amount spent
        reportDataList.clear();
        reportDataList.addAll(MainActivity.getInstance().database.getTransactionAmountSumByCategory(currentMonth));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }
}