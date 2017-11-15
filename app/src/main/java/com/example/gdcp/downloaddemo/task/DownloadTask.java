package com.example.gdcp.downloaddemo.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by asus- on 2017/11/13.
 */

public class DownloadTask {
    private Context context;
    private FileInfo fileInfo;
    private ThreadDao threadDao;
    private long finished=0;
    public boolean paused = false;
    //线程数量
    private int threadCount = 1;
    private List<DownloadThread> downloadThreadList;
    //线程池
    public static ExecutorService executorService= Executors.newCachedThreadPool();

    public DownloadTask(Context context, FileInfo fileInfo, int threadCount) {
        this.context = context;
        this.fileInfo = fileInfo;
        this.threadCount = threadCount;
        threadDao = new ThreadDaoImpl(context);
    }


    public void download() {
        //从数据库获得下载进度
        List<ThreadInfo> threadInfoList = threadDao.getThreads(fileInfo.getUrl());
       /* ThreadInfo threadInfo=null;
        if (threadInfoList.size()==0){
            //初始化线程信息
            threadInfo=new ThreadInfo(0,fileInfo.getUrl(),0,fileInfo.getLength(),0);
        }else {
            threadInfo=threadInfoList.get(0);
        }
        //创建子线程进行下载
        new DownloadThread(threadInfo).start();*/
        if (threadInfoList.size() == 0) {
            long length = fileInfo.getLength() / threadCount;
            for (int i = 0; i < threadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, fileInfo.getUrl(), i * length, (i + 1) * length - 1, 0);
                if (i == threadCount - 1) {
                    threadInfo.setEnd(fileInfo.getLength());
                }
                threadInfoList.add(threadInfo);
                threadDao.insertThread(threadInfo);
            }
        }
        downloadThreadList = new ArrayList<>();
        for (ThreadInfo threadInfo : threadInfoList) {
            DownloadThread downloadThread = new DownloadThread(threadInfo);
            DownloadTask.executorService.execute(downloadThread);
            downloadThreadList.add(downloadThread);
        }
    }


    class DownloadThread extends Thread {
        private ThreadInfo threadInfo;
        public boolean isFinished=false;

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            super.run();
            /*if (!threadDao.isExists(threadInfo.getUrl(), threadInfo.getId())) {
                threadDao.insertThread(threadInfo);
            }*/
            HttpURLConnection conn = null;
            InputStream input = null;
            RandomAccessFile randomAccessFile = null;
            //Log.i("dd", "Length: "+fileInfo.getLength());
            try {
                URL url = new URL(fileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                long start = threadInfo.getStart() + threadInfo.getFinished();
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
                        threadInfo.setFinished(threadInfo.getFinished()+len);
                        if (System.currentTimeMillis() - time > 1000) {
                            time = System.currentTimeMillis();
                            //把下载进度发送广播给活动
                            long radis=finished * 100 / fileInfo.getLength();
                            //radis=Math.abs(radis);
                            Log.i("dd", "finished: "+radis);
                            intent.putExtra("finished", radis);
                            intent.putExtra("id", fileInfo.getId());
                            context.sendBroadcast(intent);
                        }
                        //在暂停时，保存下载进度
                        if (paused) {
                            threadDao.updateThread(threadInfo.getUrl(), threadInfo.getId(), threadInfo.getFinished());
                            return;
                        }

                    }
                    //标识线程执行完毕
                    isFinished=true;
                   /* //删除线程信息
                    threadDao.deleteThread(threadInfo.getUrl(), threadInfo.getId());*/
                    //检查所有下载线程是否完成
                    checkAllThreadsFinished();
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


    /*
    *
    * 判断所有线程是否执行完毕*/
    private synchronized void checkAllThreadsFinished(){
        boolean allFinished=true;
        for (DownloadThread downloadThread:downloadThreadList){
            if (!downloadThread.isFinished){
                allFinished=false;
                break;
            }
        }
        if (allFinished){
            //删除线程信息
            threadDao.deleteThread(fileInfo.getUrl());
            Intent intent=new Intent(DownloadService.ACTION_FINISHED);
            intent.putExtra("fileInfo",fileInfo);
            context.sendBroadcast(intent);
        }

    }

}

