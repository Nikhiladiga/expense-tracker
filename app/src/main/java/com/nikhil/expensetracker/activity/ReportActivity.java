package com.nikhil.expensetracker.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

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
import com.nikhil.expensetracker.databinding.ActivityReportBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private ActivityReportBinding activityReportBinding;
    private BarChart chart;
    private final List<Map.Entry<String, Double>> transactions = new ArrayList<>();
    private final List<String> keys = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityReportBinding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(activityReportBinding.getRoot());

        chart = activityReportBinding.barChart;

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") String month = new SimpleDateFormat("MMMM").format(calendar.getTime());
        transactions.addAll(MainActivity.getInstance().database.getTransactionAmountSumByCategory(month));

        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setHighlightPerTapEnabled(true);
        chart.animateY(1400, Easing.EaseInOutQuad);
        chart.getLegend().setEnabled(false);
        YAxis left = chart.getAxisLeft();
        left.setTextColor(Color.WHITE);

        XAxis down = chart.getXAxis();
        down.setCenterAxisLabels(true);
        down.setValueFormatter(new StockXAxisValueFormatter(keys));

        //Set data for piechart
        setData();

    }

    public static class StockXAxisValueFormatter extends IndexAxisValueFormatter {
        private final List<String> keys;

        public StockXAxisValueFormatter(List<String> keys) {
            this.keys = keys;
        }

        @Override
        public String getFormattedValue(float value) {
            System.out.println("KEY:" + Math.abs(Math.round(value)));
            return keys.get(Math.abs(Math.round(value)));
        }
    }

    private void setData() {
        ArrayList<BarEntry> entries = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Double> entry : transactions) {
            keys.add(entry.getKey());
            entries.add(new BarEntry(i, Float.parseFloat(String.valueOf(entry.getValue())), entry.getKey()));
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");

        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(c);
        }
        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        BarData data = new BarData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "₹" + value;
            }
        });
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }
}