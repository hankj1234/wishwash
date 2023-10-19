package com.example.wishwash;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class BoardActivity extends AppCompatActivity {
    String filename;
    DatePicker dp;
    Button btnWrite;
    EditText edtDiary;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        dp = findViewById(R.id.datePicker);
        btnWrite = findViewById(R.id.btnWrite);
        edtDiary = findViewById(R.id.edtDiary);
        setTitle("간단 메모장");

        ImageButton bt_b1 = findViewById(R.id.bt_b1);
        bt_b1.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(intent);
        });

        ImageButton bt_b2 = findViewById(R.id.bt_b2);
        bt_b2.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
            startActivity(intent);
        });

        Calendar cal = Calendar.getInstance();
        int cYear = cal.get(Calendar.YEAR);
        int cMonth = cal.get(Calendar.MONTH);
        int cday = cal.get(Calendar.DAY_OF_MONTH);

        // 초기 파일명을 현재 날짜로 설정 (이 부분이 추가되었습니다)
        filename = cYear + "_" + (cMonth + 1) + "_" + cday + ".txt";

        String str1 = readDiary(filename);
        edtDiary.setText(str1);
        btnWrite.setEnabled(true);

        dp.init(cYear, cMonth, cday, (datePicker, i, i1, i2) -> {
            filename = i + "_" + (i1 + 1) + "_" + i2 + ".txt";
            String str = readDiary(filename);
            edtDiary.setText(str);
            btnWrite.setEnabled(true);
        });

        btnWrite.setOnClickListener(view -> {
            try {
                FileOutputStream outfs = openFileOutput(filename, Context.MODE_PRIVATE);
                String str = edtDiary.getText().toString();
                outfs.write(str.getBytes());
                outfs.close();
                Toast.makeText(getApplicationContext(), filename + "이 저장됨", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    String readDiary(String filename) {
        String diaryStr = null;
        FileInputStream infs;
        try {
            infs = openFileInput(filename);
            byte[] txt = new byte[500];
            int bytesRead = infs.read(txt);
            if (bytesRead != -1) {
                diaryStr = (new String(txt, 0, bytesRead)).trim();
            }
            infs.close();
            btnWrite.setText("저장하기");
        } catch (FileNotFoundException e) {
            edtDiary.setHint("메모없음");
            btnWrite.setText("저장하기");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diaryStr;
    }
}