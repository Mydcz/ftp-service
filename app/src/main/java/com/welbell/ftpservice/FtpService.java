package com.welbell.ftpservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.util.ArrayList;
import java.util.List;

public class FtpService extends Service {
    private FtpServer server;
    private static String rootPath;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //FTP 共享目录
        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        try {
            init();
            Toast.makeText(this, "启动ftp服务成功", Toast.LENGTH_SHORT).show();
        } catch (FtpException e) {
            e.printStackTrace();
            Toast.makeText(this, "启动ftp服务失败", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        Toast.makeText(this, "关闭ftp服务", Toast.LENGTH_SHORT).show();
    }


    public void init() throws FtpException {
        release();
        startFtp();
    }


    private void startFtp() throws FtpException {
        SharedPreferences sp = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean isAnonymous = sp.getBoolean("ftp_anonymous",true); //是否匿名
        String userName = sp.getString("ftp_username", "admin");
        String password = sp.getString("ftp_password","admin123");
        int port = sp.getInt("ftp_port",2121);

        FtpServerFactory serverFactory = new FtpServerFactory();

        //设置访问用户名和密码还有共享路径
        BaseUser baseUser = new BaseUser();
//        if(isAnonymous) {
//            baseUser.setName("anonymous");  //匿名
//        }else{
            baseUser.setName(userName);
            baseUser.setPassword(password);
//        }
        baseUser.setHomeDirectory(rootPath);

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        baseUser.setAuthorities(authorities);
        serverFactory.getUserManager().save(baseUser);


        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port);
        serverFactory.addListener("default", factory.createListener());

        server = serverFactory.createServer();
        server.start();
    }


    public void release() {
        stopFtp();
    }

    private void stopFtp() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

}
