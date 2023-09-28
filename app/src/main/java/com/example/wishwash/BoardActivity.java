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

import com.example.wishwash.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.zip.Inflater;

public class BoardActivity extends AppCompatActivity {
    String filename;
    DatePicker dp;
    Button btnWrite;
    EditText edtDiary;
    ImageButton bt_b1;
    ImageButton bt_b2;
    ImageButton bt_b3;
    ImageButton bt_b4;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        dp=(DatePicker)findViewById(R.id.datePicker);
        btnWrite=(Button)findViewById(R.id.btnWrite);
        edtDiary=(EditText)findViewById(R.id.edtDiary);
        setTitle("간단 메모장");
        Calendar cal=Calendar.getInstance();
        int cYear=cal.get(Calendar.YEAR);
        int cMonth=cal.get(Calendar.MONTH);
        int cday=cal.get(Calendar.DAY_OF_MONTH);
        String filename1= Integer.toString(cYear)+"_"+Integer.toString(cMonth+1)+"_"+Integer.toString(cday)+".txt";
        String str1=readDiary(filename1);
        edtDiary.setText(str1);
        btnWrite.setEnabled(true);

        dp.init(cYear,cMonth,cday, new DatePicker.OnDateChangedListener() {


            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                filename=Integer.toString(i)+"_"+ Integer.toString(i1+1)+"_"+Integer.toString(i2)+".txt";
                String str=readDiary(filename);
                edtDiary.setText(str);
                btnWrite.setEnabled(true);
            }
        });
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FileOutputStream outfs=openFileOutput(filename, Context.MODE_PRIVATE);
                    String str=edtDiary.getText().toString();
                    outfs.write(str.getBytes());
                    outfs.close();
                    Toast.makeText(getApplicationContext(),filename+"이 저장됨",Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ImageButton bt_b2 = (ImageButton) findViewById(R.id.bt_b2);
        bt_b2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
            }
        });

        ImageButton bt_b3 = (ImageButton) findViewById(R.id.bt_b3);
        bt_b3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
                startActivity(intent);
            }
        });

    }

    String readDiary(String filename) {
        String diaryStr=null;
        FileInputStream infs;
        try {
            infs=openFileInput(filename);
            byte txt[]=new byte[500];
            infs.read(txt);
            infs.close();
            diaryStr=(new String(txt)).trim();
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
