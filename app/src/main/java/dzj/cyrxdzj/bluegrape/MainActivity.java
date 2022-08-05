package dzj.cyrxdzj.bluegrape;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private Intent WallpaperServiceIntent,AppListenerIntent;
    private AlertDialog dialog1,dialog2;
    public static boolean is_pause=false;
    private CommonUtil util=new CommonUtil();
    public static boolean isAccessibilitySettingsOn(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return accessibilityManager.isEnabled();
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
        CrashReport.UserStrategy user_strategy=new CrashReport.UserStrategy(this);
        user_strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback(){
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType,
                                                           String errorMessage, String errorStack) {
                try {
                    File log_folder=new File(getCacheDir().getAbsolutePath()+"/log");
                    String[] file_list=log_folder.list();
                    Arrays.sort(file_list);
                    if(file_list.length!=0)
                    {
                        Log.v("MainActivityTest","Uploaded log.");
                        return util.read_file(getCacheDir().getAbsolutePath()+"/log/"+file_list[file_list.length-1]).getBytes(StandardCharsets.UTF_8);
                    }
                    else
                    {
                        return null;
                    }
                } catch (Exception e) {
                    return null;
                }
            }
        });
        CrashReport.initCrashReport(getApplicationContext(), "67561c096b", true,user_strategy);
        LogUtils.Config log_config=LogUtils.getConfig();
        log_config.setLogHeadSwitch(false);
        log_config.setBorderSwitch(false);
        log_config.setDir(getCacheDir().getAbsolutePath()+"/log");
        log_config.setFilePrefix("BlueGrape-Log-"+BuildConfig.VERSION_NAME);
        log_config.addFileExtraHead("Build Time",BuildConfig.BUILD_TIME);
        log_config.addFileExtraHead("Build Type",BuildConfig.BUILD_TYPE);
        log_config.setSaveDays(30);
        log_config.setLog2FileSwitch(true);
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
        File folder=new File(util.get_storage_path());
        if(!folder.exists())
        {
            folder.mkdirs();
        }
        File file=new File(util.get_storage_path()+"current_wallpaper.json");
        LogUtils.dTag("MainActivity","Files will storage at here: "+util.get_storage_path()+"current_wallpaper.json");
        if(!file.exists())
        {
            try {
                file.createNewFile();
                util.write_file(util.get_storage_path()+"current_wallpaper.json","[]");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        try {
            create_settings_file();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            util.refresh_settings(this);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(1);
        }
        WallpaperServiceIntent=new Intent(MainActivity.this, AppListener.class);
        AppListenerIntent=new Intent(MainActivity.this, WallpaperService.class);
        startService(WallpaperServiceIntent);
        startService(AppListenerIntent);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume()
    {
        super.onResume();
        LogUtils.dTag("MainActivity","Activity resume");
        check_permission();
        update_pause_status();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void check_permission()
    {
        if(!isAccessibilitySettingsOn(this))
        //if(!PermissionUtils.isGranted("android.permission.BIND_ACCESSIBILITY_SERVICE"))
        {
            LogUtils.dTag("MainActivity","Accessibility settings off");
            show_ask_permission_dialog();
            return;
        }
        //if(!checkFloatPermission(this))
        if(!PermissionUtils.isGrantedDrawOverlays())
        {
            LogUtils.dTag("MainActivity","Float settings off");
            show_ask_permission2_dialog();
        }
        else
        {
            LogUtils.dTag("MainActivity","Float settings on");
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
    public void open_settings(View view)
    {
        Intent intent=new Intent();
        intent.setClass(MainActivity.this,dzj.cyrxdzj.bluegrape.Settings.class);
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
    public void feedback(View view)
    {
        Uri uri = Uri.parse("https://www.wjx.cn/vj/w7Os0kb.aspx");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    public void using_document(View view)
    {
        Uri uri = Uri.parse("https://gitee.com/cyrxdzj/BlueGrape/blob/master/README.md");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    public void open_web(View view)
    {
        Uri uri = Uri.parse("https://cyrxdzj.github.io/BlueGrapeWeb");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    private void create_settings_file() throws IOException, JSONException {
        File file=new File(util.get_storage_path()+"settings.json");
        if(!file.exists())
        {
            file.createNewFile();
            JSONObject settings_object=new JSONObject();
            JSONArray settings_template_object=new JSONArray(util.read_file(R.raw.settings,this));
            for(int i=0;i<settings_template_object.length();i++)
            {
                JSONArray settings_part=settings_template_object.getJSONObject(i).getJSONArray("settings");
                for(int j=0;j<settings_part.length();j++)
                {
                    settings_object.put(settings_part.getJSONObject(j).getString("id"),settings_part.getJSONObject(j).get("default"));
                }
            }
            LogUtils.dTag("MainActivity",settings_object.toString());
            util.write_file(util.get_storage_path()+"settings.json",settings_object.toString());
        }
    }
}