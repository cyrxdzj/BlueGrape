package dzj.cyrxdzj.bluegrape;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ApplyWallpaper extends AppCompatActivity {

    public JSONArray current_wallpaper;
    public List<App> package_array=new ArrayList<App>();
    public String wallpaper_id,wallpaper_name;
    private CommonUtil util=new CommonUtil();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_wallpaper);
        Intent intent=getIntent();
        wallpaper_id=intent.getStringExtra("wallpaper_id");
        wallpaper_name=intent.getStringExtra("wallpaper_name");
        this.setTitle("将壁纸 "+wallpaper_name+" 应用到：");
        try {
            current_wallpaper=new JSONArray(util.read_file(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/current_wallpaper.json"));
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
                if((p.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)!=0||isInputMethodApp(this,p.packageName))
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
                    LogUtils.dTag("ApplyWallpaper","Clicked APP id: "+package_array.get(position).package_id);
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
        try {
            List<Pair<String,String[]>> temp_list=new ArrayList<Pair<String,String[]>>();
            String result="";
            if(choose_apps.size()!=0)
            {
                temp_list.add(new Pair<String, String[]>(wallpaper_id,choose_apps.toArray(new String[choose_apps.size()])));
            }
            for(int i=0;i<current_wallpaper.length();i++)
            {
                if(!current_wallpaper.getJSONObject(i).getString("wallpaper_id").equals(wallpaper_id))
                {
                    ArrayList<String>temp=new ArrayList<String>();
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
            LogUtils.dTag("ApplyWallpaper","Config content:\n"+result);
            util.write_file(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/current_wallpaper.json",result);
            Toast.makeText(this,R.string.apply_successful,Toast.LENGTH_SHORT).show();
            finish();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}