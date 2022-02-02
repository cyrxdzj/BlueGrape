package dzj.cyrxdzj.bluegrape;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Intent WallpaperServiceIntent,AppListenerIntent;
    public void write_file(String path,String content) throws IOException
    {
        FileWriter writer=new FileWriter(new File(path));
        writer.write(content);
        writer.close();
    }
    public static boolean isAccessibilitySettingsOn(Context context, String className) {
        if (context == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningServiceInfo> runningServices =
                    activityManager.getRunningServices(100);// 获取正在运行的服务列表
            if (runningServices.size() < 0) {
                return false;
            }
            for (int i = 0; i < runningServices.size(); i++) {
                ComponentName service = runningServices.get(i).service;
                if (service.getClassName().equals(className)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
    public void show_ask_permission_dialog()
    {
        Context context=this;
        AlertDialog dialog = new AlertDialog.Builder(this)
                //.setTitle(title)
                .setMessage(getString(R.string.accessibility_permission_requests))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //新建BlueGrape文件夹
        File folder=new File("/storage/emulated/0/BlueGrape");
        if(!folder.exists())
        {
            folder.mkdir();
        }
        File file=new File("/storage/emulated/0/BlueGrape/current_wallpaper.json");
        if(!file.exists())
        {
            try {
                file.createNewFile();
                write_file("/storage/emulated/0/BlueGrape/current_wallpaper.json","[]");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        if(!isAccessibilitySettingsOn(this,AppListener.class.getName()))
        {
            show_ask_permission_dialog();
        }
        WallpaperServiceIntent=new Intent(MainActivity.this, AppListener.class);
        AppListenerIntent=new Intent(MainActivity.this, WallpaperService.class);
        startService(WallpaperServiceIntent);
        startService(AppListenerIntent);
    }
    public void open_my_wallpaper(View view)
    {
        Intent intent=new Intent();
        intent.setClass(MainActivity.this,MyWallpaper.class);
        MainActivity.this.startActivity(intent);
    }
    public void open_current_wallpaper(View view)
    {
        Intent intent=new Intent();
        intent.setClass(MainActivity.this,CurrentWallpaper.class);
        MainActivity.this.startActivity(intent);
    }
    public void open_about_this_software(View view)
    {
        Intent intent=new Intent();
        intent.setClass(MainActivity.this,AboutThisSoftware.class);
        MainActivity.this.startActivity(intent);
    }
    public void stop_running(View view)
    {
        stopService(WallpaperServiceIntent);
        stopService(AppListenerIntent);
        System.exit(0);
    }
}