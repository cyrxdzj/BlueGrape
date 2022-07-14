package dzj.cyrxdzj.bluegrape;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.blankj.utilcode.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class CurrentWallpaper extends AppCompatActivity {

    private List<String> wallpaper_list=new ArrayList<String>(),wallpaper_name_list=new ArrayList<String>();
    private ListView apply_list;
    private JSONArray current_wallpaper;
    private CommonUtil util=new CommonUtil();
    private String get_wallpaper_name(String wallpaper_id) throws IOException, JSONException {
        String config_str = util.read_file(util.get_storage_path()+wallpaper_id+"/config.json");
        JSONObject config=new JSONObject(config_str);
        return URLDecoder.decode(config.getString("name"),"UTF-8");
    }
    public void show_info_dialog(String title,String content)
    {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    public void show_delete_question_dialog(String delete_wallpaper)
    {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.confirm_cancel_apply))
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete(delete_wallpaper);
                        refresh();
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    public void clear_delete()
    {
        try {
            wallpaper_list.clear();
            wallpaper_name_list.clear();
            current_wallpaper=new JSONArray(util.read_file(util.get_storage_path()+"current_wallpaper.json"));
            String result="";
            boolean is_add=false;
            result+="[";
            for(int i=0;i<current_wallpaper.length();i++)
            {
                File fobj=new File(util.get_storage_path()+current_wallpaper.getJSONObject(i).getString("wallpaper_id"));
                LogUtils.dTag("CurrentWallpaper","This wallpaper will be checked:"+util.get_storage_path()+current_wallpaper.getJSONObject(i).getString("wallpaper_id"));
                if(fobj.exists())
                {
                    if(is_add)
                    {
                        result+=",";
                    }
                    is_add=true;
                    result+="{\"apps\":[";
                    for(int j=0;j<current_wallpaper.getJSONObject(i).getJSONArray("apps").length();j++)
                    {
                        if(j!=0)
                        {
                            result+=",";
                        }
                        result+="\""+current_wallpaper.getJSONObject(i).getJSONArray("apps").getString(j)+"\"";
                    }
                    result+="],\"wallpaper_id\":\""+current_wallpaper.getJSONObject(i).getString("wallpaper_id")+"\"}";
                    wallpaper_list.add(current_wallpaper.getJSONObject(i).getString("wallpaper_id"));
                    wallpaper_name_list.add(get_wallpaper_name(current_wallpaper.getJSONObject(i).getString("wallpaper_id")));
                }
            }
            result+="]";
            LogUtils.dTag("CurrentWallpaper","Config content:\n"+result);
            util.write_file(util.get_storage_path()+"current_wallpaper.json",result);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    public void delete(String delete_wallpaper)
    {
        try {
            wallpaper_list.clear();
            wallpaper_name_list.clear();
            current_wallpaper=new JSONArray(util.read_file(util.get_storage_path()+"current_wallpaper.json"));
            String result="";
            boolean is_add=false;
            result+="[";
            for(int i=0;i<current_wallpaper.length();i++)
            {
                LogUtils.dTag("CurrentWallpaper","This wallpaper will be checked:"+util.get_storage_path()+current_wallpaper.getJSONObject(i).getString("wallpaper_id"));
                if(!current_wallpaper.getJSONObject(i).getString("wallpaper_id").equals(delete_wallpaper))
                {
                    if(is_add)
                    {
                        result+=",";
                    }
                    is_add=true;
                    result+="{\"apps\":[";
                    for(int j=0;j<current_wallpaper.getJSONObject(i).getJSONArray("apps").length();j++)
                    {
                        if(j!=0)
                        {
                            result+=",";
                        }
                        result+="\""+current_wallpaper.getJSONObject(i).getJSONArray("apps").getString(j)+"\"";
                    }
                    result+="],\"wallpaper_id\":\""+current_wallpaper.getJSONObject(i).getString("wallpaper_id")+"\"}";
                    wallpaper_list.add(current_wallpaper.getJSONObject(i).getString("wallpaper_id"));
                    wallpaper_name_list.add(get_wallpaper_name(current_wallpaper.getJSONObject(i).getString("wallpaper_id")));
                }
            }
            result+="]";
            LogUtils.dTag("CurrentWallpaper","Config content:\n"+result);
            util.write_file(util.get_storage_path()+"current_wallpaper.json",result);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    private void refresh()
    {
        apply_list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,wallpaper_name_list.toArray(new String[wallpaper_name_list.size()])));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_wallpaper);
        Context context=this;
        clear_delete();
        apply_list=findViewById(R.id.apply_list);
        apply_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String show_info="当前使用此壁纸的APP：\n";
                    for(int i=0;i<current_wallpaper.getJSONObject(position).getJSONArray("apps").length();i++)
                    {
                        show_info+=context.getPackageManager().getApplicationInfo(current_wallpaper.getJSONObject(position).getJSONArray("apps").getString(i),0).loadLabel(context.getPackageManager()).toString()+"\n";
                        LogUtils.dTag("CurrentWallpaper","This APP is using this wallpaper: "+current_wallpaper.getJSONObject(position).getJSONArray("apps").getString(i));
                    }
                    show_info_dialog("",show_info);
                }
                catch (JSONException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        apply_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                show_delete_question_dialog(wallpaper_list.get(position));
                return true;
            }
        });
        refresh();
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