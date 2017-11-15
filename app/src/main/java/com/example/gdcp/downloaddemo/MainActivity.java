package com.example.gdcp.downloaddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gdcp.downloaddemo.adapter.FilelistAdapter;
import com.example.gdcp.downloaddemo.entity.FileInfo;
import com.example.gdcp.downloaddemo.service.DownloadService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private List<FileInfo>fileInfoList;
    private FilelistAdapter filelistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        initData();
    }

    private void initData() {
        fileInfoList=new ArrayList<>();
        FileInfo fileInfo1=new FileInfo(0,"http://sw.bos.baidu.com/sw-search-sp/software/10eafbd67f16b/QQ_8.9.6.22404_setup.exe",
                "QQsetup.exe",0,0);
        FileInfo fileInfo2=new FileInfo(1,"http://sw.bos.baidu.com/sw-search-sp/software/41fb96fbfa410/QQMusic_15.6.0.0_Setup.exe",
                "QQMusicSetup.exe",0,0);
        FileInfo fileInfo3=new FileInfo(2,"http://sw.bos.baidu.com/sw-search-sp/software/e335feb5c2f01/WeChatSetup.exe",
                "WeChatSetup.exe",0,0);
        fileInfoList.add(fileInfo1);
        fileInfoList.add(fileInfo2);
        fileInfoList.add(fileInfo3);
        filelistAdapter=new FilelistAdapter(this,fileInfoList);
        listView.setAdapter(filelistAdapter);
    }

    private void initEvent() {
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDATE);
        intentFilter.addAction(DownloadService.ACTION_FINISHED);
        registerReceiver(broadcastReceiver,intentFilter);
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }



    private void initView() {
        listView= (ListView) findViewById(R.id.listview);
    }

    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadService.ACTION_UPDATE)){
                long finished=intent.getLongExtra("finished",0);
                int id=intent.getIntExtra("id",0);
                filelistAdapter.updateProgress(id,finished);
                //Log.i("dd", "onReceive: "+finished);
                //progressBar.setProgress(finished);
            }else if (intent.getAction().equals(DownloadService.ACTION_FINISHED)){
                FileInfo fileInfo= (FileInfo) intent.getSerializableExtra("fileInfo");
                filelistAdapter.updateProgress(fileInfo.getId(),100);
                Log.i("dd", "onReceive: "+fileInfoList.get(fileInfo.getId()).getFileName()+"下载完毕");
                Toast.makeText(MainActivity.this,fileInfoList.get(fileInfo.getId()).getFileName()+"下载完毕",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
}
