package dzj.cyrxdzj.bluegrape;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

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
        if(packageName!=last_package_name)
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
                    Log.d("acc-wallpaper_id-test",apps.getString(j));
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
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}