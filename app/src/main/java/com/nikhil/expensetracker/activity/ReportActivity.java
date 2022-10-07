package com.nikhil.expensetracker.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.nikhil.expensetracker.MainActivity;
import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.adapters.ReportItemsAdapter;
import com.nikhil.expensetracker.databinding.ActivityReportBinding;
import com.nikhil.expensetracker.model.ReportData;
import com.nikhil.expensetracker.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ReportActivity extends AppCompatActivity {

    private ActivityReportBinding mBinding;
    private final List<ReportData> reportDataList = new ArrayList<>();
    private ReportItemsAdapter reportItemsAdapter;
    private String currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        //Get current month
        Calendar calendar = Calendar.getInstance();
        currentMonth = new SimpleDateFormat("MMMM").format(calendar.getTime());
        mBinding.currentMonth.setText(currentMonth.toUpperCase(Locale.ROOT));

        //Handle adapter for top spent amount
        handleReportAdapter();

        //Handle months popup
        handleMonthsPopup();

        //Handle no transactions layer
        handleNoTransactionsLayer();

        //Handle expense comparison with previous month
        handleExpenseComparisonWithPrevMonth();

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

    @SuppressLint("SetTextI18n")
    private void handleExpenseComparisonWithPrevMonth() {
        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
        String currentMonth = thisMonth.format(monthFormatter);
        String prevMonth = lastMonth.format(monthFormatter);

        Double currentMonthExpense = MainActivity.getInstance().database.getTotalExpenseByMonth(currentMonth);
        Double prevMonthExpense = MainActivity.getInstance().database.getTotalExpenseByMonth(prevMonth);

        System.out.println("CURRENT MONTH AND EXPENSE :" + currentMonth + " " + currentMonthExpense);
        System.out.println("PREV MONTH AND EXPENSE :" + prevMonth + " " + prevMonthExpense);

        mBinding.prevMonthExpense.setText("₹" + prevMonthExpense);
        mBinding.currentMonthExpense.setText("₹" + currentMonthExpense);

        if (Objects.equals(currentMonthExpense, prevMonthExpense)) {
            mBinding.expenseDifference.setText("You have spent the same amount as last month.");
            mBinding.expenseDifference.setTextColor(Color.WHITE);
        } else if (currentMonthExpense < prevMonthExpense) {
            mBinding.expenseDifference.setText("₹" + Math.floor(Math.abs(prevMonthExpense - currentMonthExpense)) + " less than last month");
            mBinding.expenseDifference.setTextColor(Color.GREEN);
        } else {
            mBinding.expenseDifference.setText("₹" + Math.floor(Math.abs(currentMonthExpense - prevMonthExpense)) + " more than last month");
            mBinding.expenseDifference.setTextColor(Color.RED);
        }


    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }
}