package dzj.cyrxdzj.bluegrape;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    //判断是否开启悬浮窗权限   context可以用你的Activity.或者tiis
    public static boolean checkFloatPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
                return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
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
                        if(!checkFloatPermission(context))
                        {
                            show_ask_permission2_dialog();
                        }
                    }
                }).create();
        dialog.show();
    }
    public void show_ask_permission2_dialog()
    {
        Context context=this;
        AlertDialog dialog = new AlertDialog.Builder(this)
                //.setTitle(title)
                .setMessage(getString(R.string.overlay_permission_requests))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int sdkInt = Build.VERSION.SDK_INT;
                        if (sdkInt >= Build.VERSION_CODES.O) {//8.0以上
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            context.startActivity(intent);
                        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            context.startActivity(intent);
                        }
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            String[] PERMISSIONS_STORAGE = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE" };
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,PERMISSIONS_STORAGE,1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        File folder=new File(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/current_wallpaper.json");
        if(!folder.exists())
        {
            folder.mkdirs();
        }
        File file=new File(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/current_wallpaper.json");
        Log.d("MainActivity",Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/current_wallpaper.json");
        if(!file.exists())
        {
            try {
                file.createNewFile();
                write_file(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/current_wallpaper.json","[]");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        if(!isAccessibilitySettingsOn(this,AppListener.class.getName()))
        {
            show_ask_permission_dialog();
        }
        else if(!checkFloatPermission(this))
        {
            show_ask_permission2_dialog();
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