package com.nikhil.expensetracker.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.model.ReportData;
import com.nikhil.expensetracker.utils.StringUtils;

import java.util.List;

public class ReportItemsAdapter extends RecyclerView.Adapter<ReportItemsAdapter.ReportItemViewHolder> {

    private Context context;
    private List<ReportData> reportDataList;

    public ReportItemsAdapter(Context context, List<ReportData> reportDataList) {
        this.context = context;
        this.reportDataList = reportDataList;
    }

    @NonNull
    @Override
    public ReportItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReportItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.report_list_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ReportItemViewHolder holder, int position) {
        ReportData reportData = reportDataList.get(position);
        holder.categoryTitle.setText(reportData.getCategory());
        holder.categoryAmount.setText("₹" + reportData.getAmount());
    }

    @Override
    public int getItemCount() {
        return reportDataList.size();
    }

    public static class ReportItemViewHolder extends RecyclerView.ViewHolder {
        private View parentView;
        private TextView categoryTitle;
        private TextView categoryAmount;

        public ReportItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.parentView = itemView;
            this.categoryTitle = itemView.findViewById(R.id.categoryTitle);
            this.categoryAmount = itemView.findViewById(R.id.categoryAmount);
        }
    }
}
