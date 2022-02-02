package dzj.cyrxdzj.bluegrape;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApplyWallpaper extends AppCompatActivity {

    public String[] package_id,package_name;
    public JSONArray current_wallpaper;
    public List<App> package_array=new ArrayList<App>();
    public String wallpaper_id,wallpaper_name;
    public String read_file(String path) throws IOException {
        FileReader reader=new FileReader(new File(path));
        char[] temp=new char[500];
        reader.read(temp);
        reader.close();
        return new String(temp);
    }
    public void write_file(String path,String content) throws IOException
    {
        FileWriter writer=new FileWriter(new File(path));
        writer.write(content);
        writer.close();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_wallpaper);
        Intent intent=getIntent();
        wallpaper_id=intent.getStringExtra("wallpaper_id");
        wallpaper_name=intent.getStringExtra("wallpaper_name");
        this.setTitle("将壁纸 "+wallpaper_name+" 应用到：");
        try {
            current_wallpaper=new JSONArray(read_file("/storage/emulated/0/BlueGrape/current_wallpaper.json"));
            JSONArray now_apps=new JSONArray("[]");
            for(int i=0;i<current_wallpaper.length();i++)
            {
                if(current_wallpaper.getJSONObject(i).getString("wallpaper_id").equals(wallpaper_id))
                {
                    now_apps=current_wallpaper.getJSONObject(i).getJSONArray("apps");
                }
            }
            List<PackageInfo> package_list = this.getPackageManager().getInstalledPackages(0);
            //List<String> package_id_array=new ArrayList<String>(),package_name=new ArrayList<String>();
            for(PackageInfo p:package_list)
            {
                if((p.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)!=0)
                {
                    continue;
                }
                boolean flag=false;
                for(int i=0;i<now_apps.length();i++)
                {
                    if(now_apps.getString(i).equals(p.packageName))
                    {
                        flag=true;
                        break;
                    }
                }
                package_array.add(new App(p.applicationInfo.loadIcon(this.getPackageManager()),p.packageName,p.applicationInfo.loadLabel(this.getPackageManager()).toString(),flag));
                //Log.d("ApplyWallpaper",p.packageName+" "+p.applicationInfo.loadLabel(this.getPackageManager()).toString());
            }
            for(int i=0;i<package_array.size();i++)
            {
                Log.d("ApplyWallpaper",package_array.get(i).package_id+" "+package_array.get(i).app_name);
            }
            for(int i=0;i<package_array.size();i++)
            {
                if(package_array.get(i).package_id.equals("com.tencent.mm"))
                {
                    App t=package_array.get(0);
                    package_array.set(0,package_array.get(i));
                    package_array.set(i,t);
                }
                else if(package_array.get(i).package_id.equals("com.tencent.mobileqq"))
                {
                    App t=package_array.get(1);
                    package_array.set(1,package_array.get(i));
                    package_array.set(i,t);
                }
            }
            ListView apps=(ListView)findViewById(R.id.apps);
            LinkedList<App> apps_list=new LinkedList<App>();
            for(App app:package_array)
            {
                apps_list.add(app);
            }
            AppAdapter adapter=new AppAdapter(apps_list,this);
            apps.setAdapter(adapter);
            apps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("ApplyWallpaper",package_array.get(position).package_id);
                    if(package_array.get(position).is_apply)
                    {
                        package_array.get(position).is_apply=false;
                        TextView is_apply=(TextView)view.findViewById(R.id.is_apply);
                        is_apply.setText("未被应用");
                        is_apply.setTextColor(Color.BLUE);
                    }
                    else
                    {
                        package_array.get(position).is_apply=true;
                        TextView is_apply=(TextView)view.findViewById(R.id.is_apply);
                        is_apply.setText("已被应用");
                        is_apply.setTextColor(Color.GREEN);
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void confirm_apply(View view)
    {
        ArrayList<String> choose_apps=new ArrayList<String>();
        for(int i=0;i<package_array.size();i++)
        {
            if(package_array.get(i).is_apply)
            {
                choose_apps.add(package_array.get(i).package_id);
            }
        }
        if(choose_apps.size()==0)
        {
            return;
        }
        try {
            List<Pair<String,String[]>> temp_list=new ArrayList<Pair<String,String[]>>();
            ArrayList<String>temp=new ArrayList<String>();
            String result="";
            temp_list.add(new Pair<String, String[]>(wallpaper_id,choose_apps.toArray(new String[choose_apps.size()])));
            for(int i=0;i<current_wallpaper.length();i++)
            {
                if(!current_wallpaper.getJSONObject(i).getString("wallpaper_id").equals(wallpaper_id))
                {
                    for(int j=0;j<current_wallpaper.getJSONObject(i).getJSONArray("apps").length();j++)
                    {
                        boolean flag=true;
                        String app=current_wallpaper.getJSONObject(i).getJSONArray("apps").getString(j);
                        for(String now:choose_apps)
                        {
                            if(now.equals(app))
                            {
                                flag=false;
                                break;
                            }
                        }
                        if(flag)
                        {
                            temp.add(app);
                        }
                    }
                    if(temp.size()!=0)
                    {
                        temp_list.add(new Pair<String, String[]>(current_wallpaper.getJSONObject(i).getString("wallpaper_id"), temp.toArray(new String[temp.size()])));
                    }
                }
            }
            result+="[";
            for(int i=0;i<temp_list.size();i++)
            {
                if(i!=0)
                {
                    result+=",";
                }
                result+="{\"apps\":[";
                for(int j=0;j<temp_list.get(i).second.length;j++)
                {
                    if(j!=0)
                    {
                        result+=",";
                    }
                    result+="\""+temp_list.get(i).second[j]+"\"";
                }
                result+="]";
                result+=",\"wallpaper_id\":\""+temp_list.get(i).first+"\"}";
            }
            result+="]";
            Log.d("ApplyWallpaper",result);
            write_file("/storage/emulated/0/BlueGrape/current_wallpaper.json",result);
            Toast.makeText(this,R.string.apply_successful,Toast.LENGTH_SHORT).show();
            finish();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}