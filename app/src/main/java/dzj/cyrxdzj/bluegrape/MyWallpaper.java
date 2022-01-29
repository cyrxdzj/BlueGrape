package dzj.cyrxdzj.bluegrape;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyWallpaper extends AppCompatActivity {

    private String[] wallpaper_list={};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallpaper);
        //初始化壁纸列表
        File folder=new File("/storage/emulated/0/BlueGrape");
        String[] get_wallpaper_list=folder.list();
        List<String> wallpaper_list_array=new ArrayList<String>();
        for(int i=0;i<get_wallpaper_list.length;i++)
        {
            if(!get_wallpaper_list[i].equals("current_wallpaper.json"))
            {
                wallpaper_list_array.add(get_wallpaper_list[i]);
            }
        }
        this.wallpaper_list=wallpaper_list_array.toArray(new String[wallpaper_list_array.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,this.wallpaper_list);
        ListView my_wallpaper_list = (ListView) findViewById(R.id.my_wallpaper_list);
        my_wallpaper_list.setAdapter(adapter);
        my_wallpaper_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MyWallpaper",wallpaper_list[position]);
            }
        });
    }
}