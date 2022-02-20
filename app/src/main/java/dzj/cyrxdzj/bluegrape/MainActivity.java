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
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private Intent WallpaperServiceIntent,AppListenerIntent;
    private AlertDialog dialog1,dialog2;
    public static boolean is_pause=false;
    public void write_file(String path,String content) throws IOException
    {
        FileWriter writer=new FileWriter(new File(path));
        writer.write(content);
        writer.close();
    }
    public static boolean isAccessibilitySettingsOn(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return accessibilityManager.isEnabled();
    }
    public static boolean checkFloatPermission(Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1));
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }
    public void show_ask_permission_dialog()
    {
        Context context=this;
        dialog1 = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.accessibility_permission_requests))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create();
        dialog1.show();
    }
    public void show_ask_permission2_dialog()
    {
        Context context=this;
        dialog2 = new AlertDialog.Builder(this)
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
                })
                .setCancelable(false)
                .create();
        dialog2.show();
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
        File folder=new File(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files");
        if(!folder.exists())
        {
            folder.mkdirs();
        }
        File file=new File(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/current_wallpaper.json");
        Log.d("MainActivity","Files will storage at here: "+Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/current_wallpaper.json");
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
        WallpaperServiceIntent=new Intent(MainActivity.this, AppListener.class);
        AppListenerIntent=new Intent(MainActivity.this, WallpaperService.class);
        startService(WallpaperServiceIntent);
        startService(AppListenerIntent);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d("MainActivity","Activity resume");
        check_permission();
        update_pause_status();
    }
    private void check_permission()
    {
        if(!isAccessibilitySettingsOn(this))
        {
            Log.d("MainActivity","Accessibility settings off");
            show_ask_permission_dialog();
            return;
        }
        if(!checkFloatPermission(this))
        {
            Log.d("MainActivity","Float settings off");
            show_ask_permission2_dialog();
        }
        else
        {
            Log.d("MainActivity","Float settings on");
        }
    }
    private void update_pause_status()
    {
        Button pause_or_continue_button=(Button)findViewById(R.id.pause_or_continue);
        if(is_pause)
        {
            pause_or_continue_button.setText(R.string.continue_running);
            pause_or_continue_button.setBackgroundColor(Color.GREEN);
        }
        else
        {
            pause_or_continue_button.setText(R.string.pause_running);
            pause_or_continue_button.setBackgroundColor(Color.rgb(0x62,0,0xee));
        }
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
    public void pause_or_continue(View view)
    {
        this.is_pause=!this.is_pause;
        update_pause_status();
    }
    public void stop_running(View view)
    {
        stopService(WallpaperServiceIntent);
        stopService(AppListenerIntent);
        System.exit(0);
    }
}