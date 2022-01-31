package dzj.cyrxdzj.bluegrape;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyWallpaper extends AppCompatActivity {

    private String[] wallpaper_list={},wallpaper_name_list={};
    private ArrayAdapter<String> adapter;
    private ListView my_wallpaper_list;
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
    private String get_wallpaper_name(String wallpaper_id) throws IOException, JSONException {
        String config_str = read_file("/storage/emulated/0/BlueGrape/"+wallpaper_id+"/config.json");
        JSONObject config=new JSONObject(config_str);
        return URLDecoder.decode(config.getString("name"),"UTF-8");
    }
    public void refresh_list()
    {
        File folder=new File("/storage/emulated/0/BlueGrape");
        String[] get_wallpaper_list=folder.list();
        List<String> wallpaper_list_array=new ArrayList<String>(),wallpaper_name_array=new ArrayList<String>();
        for(int i=0;i<get_wallpaper_list.length;i++)
        {
            try {
                if (!get_wallpaper_list[i].equals("current_wallpaper.json")) {
                    wallpaper_name_array.add(get_wallpaper_name(get_wallpaper_list[i]));
                    wallpaper_list_array.add(get_wallpaper_list[i]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.wallpaper_list=wallpaper_list_array.toArray(new String[wallpaper_list_array.size()]);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,wallpaper_name_array.toArray(new String[wallpaper_name_array.size()]));
        ListView my_wallpaper_list = (ListView) findViewById(R.id.my_wallpaper_list);
        my_wallpaper_list.setAdapter(adapter);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallpaper);
        //初始化壁纸列表
        File folder=new File("/storage/emulated/0/BlueGrape");
        String[] get_wallpaper_list=folder.list();
        List<String> wallpaper_list_array=new ArrayList<String>(),wallpaper_name_array=new ArrayList<String>();
        for(int i=0;i<get_wallpaper_list.length;i++)
        {
            try {
                if (!get_wallpaper_list[i].equals("current_wallpaper.json")) {
                    wallpaper_name_array.add(get_wallpaper_name(get_wallpaper_list[i]));
                    wallpaper_list_array.add(get_wallpaper_list[i]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        my_wallpaper_list = (ListView) findViewById(R.id.my_wallpaper_list);
        my_wallpaper_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MyWallpaper",wallpaper_list[position]);
                Intent intent=new Intent();
                intent.setClass(MyWallpaper.this,EditWallpaper.class);
                intent.putExtra("wallpaper_id",wallpaper_list[position]);
                MyWallpaper.this.startActivity(intent);
            }
        });
    }
    @Override
    public void onResume()
    {
        super.onResume();
        Log.d("MyWallpaper","resume");
        refresh_list();
    }
    public void new_wallpaper(View view)
    {
        try {
            Date d=new Date();
            String wallpaper_id="wallpaper-"+d.getTime();
            Log.d("MyWallpaper",wallpaper_id);
            File fobj=new File("/storage/emulated/0/BlueGrape/"+wallpaper_id);
            fobj.mkdirs();
            fobj=new File("/storage/emulated/0/BlueGrape/"+wallpaper_id+"/config.json");
            fobj.createNewFile();
            write_file("/storage/emulated/0/BlueGrape/"+wallpaper_id+"/config.json","{\n"+
                    "\t\"name\":\""+ URLEncoder.encode("新建壁纸","UTF-8")+"\",\n"+
                    "\t\"alpha\":25,\n"+
                    "\t\"fill_method\":\"left-right\",\n"+
                    "\t\"position\":\"left-top\"\n}");
            Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.default_image);
            FileOutputStream writer=new FileOutputStream(new File("/storage/emulated/0/BlueGrape/"+wallpaper_id+"/image.png"));
            bitmap.compress(Bitmap.CompressFormat.PNG,100,writer);
            writer.flush();
            writer.close();
            Log.d("MyWallpaper",wallpaper_id);
            Intent intent=new Intent();
            intent.setClass(MyWallpaper.this,EditWallpaper.class);
            intent.putExtra("wallpaper_id",wallpaper_id);
            MyWallpaper.this.startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}