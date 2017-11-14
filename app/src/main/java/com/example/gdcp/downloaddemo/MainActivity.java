package com.example.gdcp.downloaddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gdcp.downloaddemo.entity.FileInfo;
import com.example.gdcp.downloaddemo.service.DownloadService;

public class MainActivity extends AppCompatActivity {
    private Button btnStart;
    private Button btnStop;
    private TextView tvFileName;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        initData();
    }

    private void initData() {

        progressBar.setMax(100);
    }

    private void initEvent() {
        final FileInfo fileInfo=new FileInfo(0,"http://www.imooc.com/mobile/mukewang.apk",
                "imooc.apk",0,0);
        tvFileName.setText(fileInfo.getFileName());
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo",fileInfo);
                startService(intent);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo",fileInfo);
                startService(intent);
            }
        });

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(broadcastReceiver,intentFilter);
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void initView() {
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
        tvFileName = (TextView) findViewById(R.id.tv_name_file);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.ACTION_UPDATE)){
                int finished=intent.getIntExtra("finished",0);
                progressBar.setProgress(finished);
            }
        }
    };
}
