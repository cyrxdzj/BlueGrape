package dzj.cyrxdzj.bluegrape;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
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
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName().toString();
        int eventType = event.getEventType();
        Log.d("accessibility", "packageName = " + packageName + " eventType = " + eventType);
        if(!packageName.equals(last_package_name))
        {
            last_package_name=packageName;
            refresh();
        }
    }

    @Override
    public void onInterrupt() {

    }
    public void refresh()
    {
        Log.d("acc",this.last_package_name);
        try {
            String config_str=read_file("/storage/emulated/0/BlueGrape/current_wallpaper.json");
            JSONArray config=new JSONArray(config_str);
            String wallpaper_id=null;
            for(int i=0;i<config.length()&&wallpaper_id==null;i++)
            {
                JSONObject now_config=config.getJSONObject(i);
                JSONArray apps=now_config.getJSONArray("apps");
                for(int j=0;j<apps.length();j++)
                {
                    if(apps.getString(j).equals(this.last_package_name))
                    {
                        Log.d("acc-wallpaper_id-test",now_config.getString("wallpaper_id"));
                        wallpaper_id=now_config.getString("wallpaper_id");
                        break;
                    }
                }
            }
            if(wallpaper_id!=null)
            {
                Log.d("acc-wallpaper_id",wallpaper_id);
                String wallpaper_config_str=read_file("/storage/emulated/0/BlueGrape/"+wallpaper_id+"/config.json");
                JSONObject wallpaper_config=new JSONObject(wallpaper_config_str);
                WallpaperService.image_view.setAlpha((float) (wallpaper_config.getInt("alpha")/100.0));
                WallpaperService.image_view.setImageResource(R.drawable.default_image);
                WallpaperService.image_view.setImageURI(Uri.parse("file:///storage/emulated/0/BlueGrape/"+wallpaper_id+"/image.png"));
                AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) WallpaperService.image_view.getLayoutParams();
                Bitmap bitmap= BitmapFactory.decodeFile("/storage/emulated/0/BlueGrape/"+wallpaper_id+"/image.png");
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
                Log.d("Size",String.valueOf(image_view_width)+" "+String.valueOf(image_view_height));
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
                Log.d("Position",String.valueOf(x)+" "+String.valueOf(y));
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