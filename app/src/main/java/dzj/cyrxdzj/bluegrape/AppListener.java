package dzj.cyrxdzj.bluegrape;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class AppListener extends AccessibilityService {

    private String last_package_name="";
    public String read_file(String path) throws IOException {
        FileReader reader=new FileReader(new File(path));
        char[] temp=new char[500];
        reader.read(temp);
        reader.close();
        return new String(temp);
    }
    public static boolean isInputMethodApp(Context context, String strPkgName) {
        PackageManager pkm = context.getPackageManager();
        boolean bIsIME = false;
        PackageInfo pkgInfo;
        try {
            pkgInfo = pkm.getPackageInfo(strPkgName, PackageManager.GET_SERVICES);
            ServiceInfo[] servicesInfos = pkgInfo.services;
            if(null != servicesInfos){
                for (int i = 0; i < servicesInfos.length; i++) {
                    ServiceInfo sInfo = servicesInfos[i];
                    if(null != sInfo.permission && sInfo.permission.equals("android.permission.BIND_INPUT_METHOD")){
                        bIsIME = true;
                        break;
                    };
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return bIsIME;
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName().toString();
        int eventType = event.getEventType();
        Log.d("AppListener", "Event infomation: "+"packageName = " + packageName+" eventType = "+eventType + " eventTypeByString = " + AccessibilityEvent.eventTypeToString(eventType)+" eventClass = "+event.getClassName());
        Log.d("AppListener", "Now Activity class name: "+((ActivityManager)getSystemService(this.ACTIVITY_SERVICE)).getRunningTasks(1).get(0).topActivity.getClassName());
        if(isInputMethodApp(this,packageName))
        {
            return;
        }
        if(event.getClassName().equals("com.android.server.am.AppErrorDialog"))
        {
            return;
        }
        if(!packageName.equals(last_package_name))
        {
            last_package_name=packageName;
            refresh();
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("AppListener","Close.");
        Context context=this;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.accessibility_permission_requests_on_close))
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
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
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
        return false;
    }
    public void refresh()
    {
        Log.d("AppListener","Now package name: "+this.last_package_name);
        if(!WallpaperService.ready)
        {
            return;
        }
        Log.d("AppListener","Wallpaper Service ready");
        try {
            String config_str=read_file(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/current_wallpaper.json");
            JSONArray config=new JSONArray(config_str);
            String wallpaper_id=null;
            for(int i=0;i<config.length();i++)
            {
                JSONObject now_config=config.getJSONObject(i);
                JSONArray apps=now_config.getJSONArray("apps");
                for(int j=0;j<apps.length();j++)
                {
                    if(apps.getString(j).equals(this.last_package_name))
                    {
                        Log.d("AppListener","Config wallpaper ID: "+now_config.getString("wallpaper_id"));
                        wallpaper_id=now_config.getString("wallpaper_id");
                        break;
                    }
                }
            }
            if(wallpaper_id!=null&&!MainActivity.is_pause)
            {
                Log.d("AppListener","Use wallpaper ID: "+wallpaper_id);
                String wallpaper_config_str=read_file(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/"+wallpaper_id+"/config.json");
                JSONObject wallpaper_config=new JSONObject(wallpaper_config_str);
                WallpaperService.image_view.setAlpha((float) (wallpaper_config.getInt("alpha")/100.0));
                WallpaperService.image_view.setImageResource(R.drawable.default_image);
                WallpaperService.image_view.setImageURI(Uri.parse("file://"+Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/"+wallpaper_id+"/image.png"));
                AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) WallpaperService.image_view.getLayoutParams();
                Bitmap bitmap= BitmapFactory.decodeFile(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/"+wallpaper_id+"/image.png");
                int image_width= bitmap.getWidth(),image_height=bitmap.getHeight();
                int image_view_width,image_view_height;
                if(wallpaper_config.getString("fill_method").equals("left-right"))
                {
                    image_view_width=this.getResources().getDisplayMetrics().widthPixels;
                    image_view_height= (int) (image_height*(image_view_width*1.0/image_width));
                }
                else
                {
                    image_view_height=this.getResources().getDisplayMetrics().heightPixels;
                    image_view_width=(int)(image_width*(image_view_height*1.0/image_height));
                }
                layoutParams.width=image_view_width;
                layoutParams.height=image_view_height;
                Log.d("AppListener","Image size: "+String.valueOf(image_view_width)+" "+String.valueOf(image_view_height));
                int x,y;
                if(wallpaper_config.getString("position").equals("left-top"))
                {
                    x=y=0;
                }
                else
                {
                    if(wallpaper_config.getString("fill_method").equals("left-right"))
                    {
                        x=0;
                        y=this.getResources().getDisplayMetrics().heightPixels-image_view_height;
                    }
                    else
                    {
                        x=this.getResources().getDisplayMetrics().widthPixels-image_view_width;
                        y=0;
                    }
                }
                layoutParams.x=x;
                layoutParams.y=y;
                Log.d("AppListener","Image position: "+String.valueOf(x)+" "+String.valueOf(y));
                WallpaperService.image_view.setLayoutParams(layoutParams);
                WallpaperService.image_view.setScaleType(ImageView.ScaleType.FIT_XY);
                //WallpaperService.layout.setLayoutParams(WallpaperService.layoutParams);
            }
            else
            {
                WallpaperService.image_view.setAlpha(0f);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}