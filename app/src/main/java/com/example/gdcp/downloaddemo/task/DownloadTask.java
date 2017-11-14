package com.example.gdcp.downloaddemo.task;

import android.content.Context;
import android.content.Intent;

import com.example.gdcp.downloaddemo.db.ThreadDao;
import com.example.gdcp.downloaddemo.db.ThreadDaoImpl;
import com.example.gdcp.downloaddemo.entity.FileInfo;
import com.example.gdcp.downloaddemo.entity.ThreadInfo;
import com.example.gdcp.downloaddemo.service.DownloadService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by asus- on 2017/11/13.
 */

public class DownloadTask {
    private Context context;
    private FileInfo fileInfo;
    private ThreadDao threadDao;
    private int finished;
    public boolean paused = false;

    public DownloadTask(Context context, FileInfo fileInfo) {
        this.context = context;
        this.fileInfo = fileInfo;
        threadDao = new ThreadDaoImpl(context);
    }


    public void download(){
        List<ThreadInfo>threadInfoList=threadDao.getThreads(fileInfo.getUrl());
        ThreadInfo threadInfo=null;
        if (threadInfoList.size()==0){
            //初始化线程信息
            threadInfo=new ThreadInfo(0,fileInfo.getUrl(),0,fileInfo.getLength(),0);
        }else {
            threadInfo=threadInfoList.get(0);
        }
        //创建子线程进行下载
        new DownloadThread(threadInfo).start();
    }


    class DownloadThread extends Thread {
        private ThreadInfo threadInfo;

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            super.run();
            if (!threadDao.isExists(threadInfo.getUrl(), threadInfo.getId())) {
                threadDao.insertThread(threadInfo);
            }
            HttpURLConnection conn = null;
            InputStream input = null;
            RandomAccessFile randomAccessFile = null;
            try {
                URL url = new URL(fileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                int start = threadInfo.getStart() + threadInfo.getFinished();
                conn.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());
                File file = new File(DownloadService.DOWNLOAD_PATH, fileInfo.getFileName());
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(start);
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                finished = finished + threadInfo.getFinished();
                //开始下载
                if (conn.getResponseCode() == 206) {
                    //读取数据
                    input = conn.getInputStream();
                    byte[] buffer = new byte[4 * 1024];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = input.read(buffer)) != -1) {
                        //写入文件
                        randomAccessFile.write(buffer, 0, len);
                        finished = finished + len;
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            //把下载进度发送广播给活动
                            intent.putExtra("finished", finished * 100 / fileInfo.getLength());
                            context.sendBroadcast(intent);
                        }
                        //在暂停时，保存下载进度
                        if (paused) {
                            threadDao.updateThread(threadInfo.getUrl(), threadInfo.getId(), finished);
                            return;
                        }

                    }
                    //删除线程信息
                    threadDao.deleteThread(threadInfo.getUrl(), threadInfo.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    randomAccessFile.close();
                    conn.disconnect();
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

