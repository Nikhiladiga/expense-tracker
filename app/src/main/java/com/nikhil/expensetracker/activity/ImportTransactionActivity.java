package com.nikhil.expensetracker.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.nikhil.expensetracker.R;
import com.nikhil.expensetracker.databinding.ActivityImportBinding;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class ImportTransactionActivity extends AppCompatActivity {

    private ActivityImportBinding mBinding;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityImportBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.uploadSheet.setOnClickListener(view -> {

            //Check if read permissions is present
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                handleFileChooser();
            }

        });

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Cursor cursor = this.getContentResolver().query(data.getData(), null, null, null);

                            if (cursor == null) {
                                handleFileRead(data.getData().getPath());
                            } else {
                                cursor.moveToFirst();
                                if (cursor.getString(2).contains("xls") || cursor.getString(2).contains("xlsx")) {
                                    handleFileRead(cursor.getString(2));
                                } else {
                                    Toast.makeText(this, "Please upload a valid spreadsheet", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
        );

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void handleFileRead(String fileName) {
        try {
            FileInputStream fileInputStream = this.openFileInput(fileName);
            Workbook workbook = new HSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() > 0) {
                    // Iterate through all the cells in a row (Excluding header row)
                    Iterator<Cell> cellIterator = row.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();

                        // Check cell type and format accordingly
                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_NUMERIC:
                                // Print cell value
                                System.out.println(cell.getNumericCellValue());
                                break;

                            case Cell.CELL_TYPE_STRING:
                                System.out.println(cell.getStringCellValue());
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            handleFileChooser();
        } else {
            Toast.makeText(this, "Please give permission to read files", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            filePickerLauncher.launch(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
    }
}
