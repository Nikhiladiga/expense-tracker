package com.nikhil.expensetracker.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.adapters.ReportItemsAdapter;
import com.nikhil.expensetracker.databinding.ActivityReportBinding;
import com.nikhil.expensetracker.model.ReportData;
import com.nikhil.expensetracker.model.Transaction;
import com.nikhil.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportActivity extends AppCompatActivity {

    private ActivityReportBinding mBinding;

    //Data for charts
    private final List<ReportData> topExpenseCategoriesByMonth = new ArrayList<>();
    private final LinkedHashMap<String, Double> expenseTimeLineByMonth = new LinkedHashMap<>();

    private ReportItemsAdapter reportItemsAdapter;
    private String currentMonth;

    private BarChart barChart;
    private LineChart lineChart;

    ArrayList<BarEntry> barChartEntries = new ArrayList<>();
    ArrayList<Entry> lineChartEntries = new ArrayList<>();

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

        //Handle barchart rendering
        handleBarChartRender();

        //Handle linechart rendering
        handleLineChartRender();

    }

    private void handleLineChartRender() {


        System.out.println("EXPENSE MAP:" + expenseTimeLineByMonth);

        ArrayList<String> xAxisLabels = new ArrayList<>();

        mBinding.lineChart.getAxisLeft().setEnabled(false);
        mBinding.lineChart.getAxisRight().setEnabled(false);

        YAxis axisRight = mBinding.lineChart.getAxisRight();
        axisRight.setTextColor(Color.WHITE);
        axisRight.setTypeface(ResourcesCompat.getFont(this, R.font.interbold));

        mBinding.lineChart.getLegend().setEnabled(false);

        //Add dataset
        lineChartEntries.clear();
        int i = 0;
        for (Map.Entry<String, Double> entry : expenseTimeLineByMonth.entrySet()) {
            xAxisLabels.add(entry.getKey());
            lineChartEntries.add(new Entry(i, entry.getValue().floatValue()));
            i++;
        }

        XAxis xAxis = mBinding.lineChart.getXAxis();
        xAxis.setTextColor(getResources().getColor(R.color.tertiary));
        xAxis.setTypeface(ResourcesCompat.getFont(this, R.font.interbold));
        XAxis.XAxisPosition position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setPosition(position);
        xAxis.setLabelCount(xAxisLabels.size(), true);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(90f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));

        LineDataSet lineDataSet = new LineDataSet(lineChartEntries, null);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setValueTypeface(ResourcesCompat.getFont(this, R.font.interbold));
        lineDataSet.setValueTextColor(Color.RED);
        lineDataSet.setValueTextSize(9f);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        LineData lineData = new LineData(dataSets);
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "₹" + value;
            }
        });
        mBinding.barChart.animateY(1500);
        mBinding.barChart.animateX(1500);
        mBinding.lineChart.setData(lineData);
        mBinding.lineChart.getDescription().setEnabled(false);
        mBinding.lineChart.invalidate();

    }

    private void handleBarChartRender() {
        barChart = mBinding.barChart;
        barChart.getDescription().setEnabled(false);
        barChart.setExtraOffsets(5, 10, 5, 5);
        barChart.setDragDecelerationFrictionCoef(0.95f);
        barChart.setHighlightPerTapEnabled(true);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.interbold);

        Legend l = barChart.getLegend();
        l.setEnabled(false);

        ArrayList<String> xAxisLabels = new ArrayList<>();

        barChartEntries.clear();
        for (int i = 0; i < topExpenseCategoriesByMonth.size(); i++) {
            xAxisLabels.add(topExpenseCategoriesByMonth.get(i).getCategory());
            barChartEntries.add(new BarEntry(i, topExpenseCategoriesByMonth.get(i).getAmount().floatValue()));
        }

        BarDataSet barDataSet = new BarDataSet(barChartEntries, null);
        barDataSet.setDrawIcons(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(typeface);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(getResources().getColor(R.color.tertiary));
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setEnabled(false);

        YAxis rightAxis = barChart.getAxisRight();
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
        barChart.setData(data);
        mBinding.barChart.animateY(1500);
        mBinding.barChart.animateX(1500);
        barChart.invalidate();
    }

    private void handleNoTransactionsLayer() {
        if (topExpenseCategoriesByMonth.size() < 1) {
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
            handleBarChartRender();
            handleLineChartRender();
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
        reportItemsAdapter = new ReportItemsAdapter(this, topExpenseCategoriesByMonth);
        mBinding.topAmountSpentByCategoriesList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.topAmountSpentByCategoriesList.setAdapter(reportItemsAdapter);
    }

    private void getDataByMonth() {

        //Clear previous data
        topExpenseCategoriesByMonth.clear();
        expenseTimeLineByMonth.clear();

        // Get top n categories by amount spent
        topExpenseCategoriesByMonth.addAll(MainActivity.getInstance().database.getTransactionAmountSumByCategory(currentMonth));

        //Get transaction timeline data
        List<Transaction> transactions = MainActivity.getInstance().database.getTransactionsByMonth(currentMonth).getTransactions();

        //Filter values to include only expenses
        transactions = transactions.stream().filter(transaction -> transaction.getType().equals("DEBIT")).collect(Collectors.toList());

        for (int i = transactions.size() - 1; i >= 0; i--) {
            String transactionDate = DateUtils.convertTimestampToDate(transactions.get(i).getCreatedAt());
            Transaction transaction = transactions.get(i);
            if (expenseTimeLineByMonth.containsKey(transactionDate)) {
                expenseTimeLineByMonth.put(transactionDate, expenseTimeLineByMonth.get(transactionDate) + transaction.getAmount());
            } else {
                expenseTimeLineByMonth.put(transactionDate, transaction.getAmount());
            }
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }
}