package com.example.gdcp.downloaddemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gdcp.downloaddemo.MainActivity;
import com.example.gdcp.downloaddemo.R;
import com.example.gdcp.downloaddemo.entity.FileInfo;
import com.example.gdcp.downloaddemo.service.DownloadService;

import java.util.List;

/**
 * Created by asus- on 2017/11/14.
 */

public class FilelistAdapter extends BaseAdapter{
    private Context context;
    private List<FileInfo>fileInfoList;

    public FilelistAdapter(Context context, List<FileInfo> fileInfoList) {
        this.context = context;
        this.fileInfoList = fileInfoList;
    }

    @Override
    public int getCount() {
        return fileInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        final FileInfo fileInfo=fileInfoList.get(position);
        if (convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(context).inflate(R.layout.item_list,parent,false);
            viewHolder.btnStart= (Button) convertView.findViewById(R.id.btn_start);
            viewHolder.btnStop= (Button) convertView.findViewById(R.id.btn_stop);
            viewHolder.tvFileName= (TextView) convertView.findViewById(R.id.tv_name_file);
            viewHolder.progressBar= (ProgressBar) convertView.findViewById(R.id.progressBar);
            viewHolder.tvFileName.setText(fileInfo.getFileName());
            viewHolder.progressBar.setMax(100);
            viewHolder.btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_START);
                    intent.putExtra("fileInfo",fileInfo);
                    context.startService(intent);
                }
            });
            viewHolder.btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_STOP);
                    intent.putExtra("fileInfo",fileInfo);
                    context.startService(intent);
                }
            });
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        viewHolder.progressBar.setProgress((int) fileInfo.getFinished());
        return convertView;
    }

    //更新进度条

    public void updateProgress(int id,long progress){
        FileInfo fileInfo=fileInfoList.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }


    static class ViewHolder{
         Button btnStart;
         Button btnStop;
         TextView tvFileName;
         ProgressBar progressBar;
    }
}
