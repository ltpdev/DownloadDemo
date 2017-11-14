package com.example.gdcp.downloaddemo.db;

import com.example.gdcp.downloaddemo.entity.ThreadInfo;

import java.util.List;

/**数据访问接口
 * Created by asus- on 2017/11/13.
 */

public interface ThreadDao {
    public void insertThread(ThreadInfo threadInfo);
    public void deleteThread(String url,int thread_id);
    public void updateThread(String url,int thread_id,int finished);
    public List<ThreadInfo>getThreads(String url);
    public boolean isExists(String url,int thread_id);
}
