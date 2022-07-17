package dzj.cyrxdzj.bluegrape;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
;
import android.os.Looper;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ZipUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public class MyWallpaper extends AppCompatActivity {

    private String[] wallpaper_list={},wallpaper_name_list={};
    private ArrayAdapter<String> adapter;
    private ListView my_wallpaper_list;
    private CommonUtil util=new CommonUtil();
    private EditText input_url_view;
    private Map<Long,String> download_wallpaper_id=new HashMap<Long,String>();
    private Map<Long,String> download_path=new HashMap<Long,String>();
    private Map<Long, Boolean> download_cancel=new HashMap<Long, Boolean>();
    private BroadcastReceiver download_done_receiver;
    private ProgressDialog loading_dialog;
    private Context context=this;
    private String get_wallpaper_name(String wallpaper_id) throws IOException, JSONException {
        String config_str = util.read_file(util.get_storage_path()+wallpaper_id+"/config.json");
        JSONObject config=new JSONObject(config_str);
        return URLDecoder.decode(config.getString("name"),"UTF-8");
    }
    public void refresh_list()
    {
        File folder=new File(util.get_storage_path());
        String[] get_wallpaper_list=folder.list();
        List<String> wallpaper_list_array=new ArrayList<String>(),wallpaper_name_array=new ArrayList<String>();
        for(int i=0;i<get_wallpaper_list.length;i++)
        {
            try {
                if ((new File(util.get_storage_path()+get_wallpaper_list[i])).isDirectory()) {
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
        my_wallpaper_list = (ListView) findViewById(R.id.my_wallpaper_list);
        //初始化壁纸列表
        refresh_list();
        my_wallpaper_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtils.dTag("MyWallpaper","This wallpaper was clicked: "+wallpaper_list[position]);
                Intent intent=new Intent();
                if(util.is_image(wallpaper_list[position]))
                {
                    intent.setClass(MyWallpaper.this,EditWallpaper.class);
                }
                else if(util.is_video(wallpaper_list[position]))
                {
                    intent.setClass(MyWallpaper.this,EditVideoWallpaper.class);
                }
                else
                {
                    intent.setClass(MyWallpaper.this,EditHtmlWallpaper.class);
                }
                intent.putExtra("wallpaper_id",wallpaper_list[position]);
                MyWallpaper.this.startActivity(intent);
            }
        });
        IntentFilter download_done_intent_filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        download_done_receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long download_id=intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if(download_wallpaper_id.get(download_id)!=null)
                {
                    try {
                        if(download_cancel.get(download_id)!=null)
                        {
                            LogUtils.dTag("MyWallpaper","Download canceled.");
                            return;
                        }
                        LogUtils.dTag("MyWallpaper","Download done.");
                        String wallpaper_id=download_wallpaper_id.get(download_id);
                        String zip_path=download_path.get(download_id);
                        File fobj=new File(util.get_storage_path()+wallpaper_id);
                        fobj.mkdirs();
                        fobj=new File(util.get_storage_path()+wallpaper_id+"/config.json");
                        fobj.createNewFile();
                        fobj=new File(util.get_storage_path()+wallpaper_id+"/html_wallpaper");
                        fobj.createNewFile();
                        util.write_file(util.get_storage_path()+wallpaper_id+"/config.json","{\n"+
                                "\t\"name\":\""+ URLEncoder.encode("新建HTML壁纸","UTF-8")+"\",\n"+
                                "\t\"alpha\":25\n"+
                                "}");
                        File zip_file_object=new File(zip_path);
                        try {
                            ZipUtils.unzipFile(zip_path,util.get_storage_path()+wallpaper_id+"/src");
                        }
                        catch (IllegalArgumentException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(context,getString(R.string.unzip_failed),Toast.LENGTH_LONG).show();
                        }
                        zip_file_object.delete();
                        Intent done_intent=new Intent();
                        done_intent.setClass(MyWallpaper.this,EditHtmlWallpaper.class);
                        done_intent.putExtra("wallpaper_id",wallpaper_id);
                        MyWallpaper.this.startActivity(done_intent);
                    }
                    catch (Exception e)
                    {
                        util.show_info_dialog("",getString(R.string.download_failed),context);
                        e.printStackTrace();
                    }
                }
            }
        };
        registerReceiver(download_done_receiver,download_done_intent_filter);
    }
    @Override
    public void onResume()
    {
        super.onResume();
        LogUtils.dTag("MyWallpaper","Activity resume");
        refresh_list();
    }
    public void new_wallpaper(View view)
    {
        try {
            Date d=new Date();
            String wallpaper_id="wallpaper-"+d.getTime();
            LogUtils.dTag("MyWallpaper","The ID of the new wallpaper is: "+wallpaper_id);
            File fobj=new File(util.get_storage_path()+wallpaper_id);
            fobj.mkdirs();
            fobj=new File(util.get_storage_path()+wallpaper_id+"/config.json");
            fobj.createNewFile();
            util.write_file(util.get_storage_path()+wallpaper_id+"/config.json","{\n"+
                    "\t\"name\":\""+ URLEncoder.encode("新建壁纸","UTF-8")+"\",\n"+
                    "\t\"alpha\":25,\n"+
                    "\t\"fill_method\":\"left-right\",\n"+
                    "\t\"position\":\"left-top\"\n}");
            Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.default_image);
            FileOutputStream writer=new FileOutputStream(new File(util.get_storage_path()+wallpaper_id+"/image.png"));
            bitmap.compress(Bitmap.CompressFormat.PNG,100,writer);
            writer.flush();
            writer.close();
            Intent intent=new Intent();
            intent.setClass(MyWallpaper.this,EditWallpaper.class);
            intent.putExtra("wallpaper_id",wallpaper_id);
            MyWallpaper.this.startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void new_video_wallpaper(View view)
    {
        try {
            Date d=new Date();
            String wallpaper_id="wallpaper-"+d.getTime();
            LogUtils.dTag("MyWallpaper","The ID of the new wallpaper is: "+wallpaper_id);
            File fobj=new File(util.get_storage_path()+wallpaper_id);
            fobj.mkdirs();
            fobj=new File(util.get_storage_path()+wallpaper_id+"/config.json");
            fobj.createNewFile();
            fobj=new File(util.get_storage_path()+wallpaper_id+"/video_wallpaper");
            fobj.createNewFile();
            util.write_file(util.get_storage_path()+wallpaper_id+"/config.json","{\n"+
                    "\t\"name\":\""+ URLEncoder.encode("新建视频壁纸","UTF-8")+"\",\n"+
                    "\t\"wallpaper_path\":\"none\",\n"+
                    "\t\"alpha\":25,\n"+
                    "\t\"fill_method\":\"left-right\",\n"+
                    "\t\"position\":\"left-top\"\n}");
            Intent intent=new Intent();
            intent.setClass(MyWallpaper.this,EditVideoWallpaper.class);
            intent.putExtra("wallpaper_id",wallpaper_id);
            MyWallpaper.this.startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void show_download_question_dialog(String url,String wallpaper_id)
    {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.confirm_download_under_4g))
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        download_wallpaper(url,wallpaper_id);
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    public void new_html_wallpaper(View view)
    {
        Date d=new Date();
        String wallpaper_id="wallpaper-"+d.getTime();
        LogUtils.dTag("MyWallpaper","The ID of the new wallpaper is: "+wallpaper_id);
        input_url_view=new EditText(this);
        input_url_view.setMaxLines(1);
        input_url_view.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog dialog = new AlertDialog.Builder(this)
                //.setTitle(getString(R.string.input_url))
                .setMessage(R.string.input_url)
                .setView(input_url_view)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(NetworkUtils.isMobileData())
                        {
                            show_download_question_dialog(input_url_view.getText().toString(),wallpaper_id);
                        }
                        else
                        {
                            download_wallpaper(input_url_view.getText().toString(),wallpaper_id);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    private void download_wallpaper(String url,String wallpaper_id)
    {
        try{
            Date date = new Date();
            DownloadManager manager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request_wallpaper = new DownloadManager.Request(Uri.parse(url));
            Uri path=Uri.fromFile(new File(this.getExternalFilesDir("") + String.format("/download/%s.zip", util.format_time(date))));
            request_wallpaper.setDestinationUri(path);
            request_wallpaper.setMimeType("application/zip");
            request_wallpaper.setTitle(getString(R.string.wallpaper_download_task));
            request_wallpaper.setDescription(getString(R.string.downloading_wallpaper));
            request_wallpaper.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            Long download_id=manager.enqueue(request_wallpaper);
            download_wallpaper_id.put(download_id,wallpaper_id);
            download_path.put(download_id,path.getPath());
            LogUtils.vTag("MyWallpaperTest",download_id);
            LogUtils.dTag("MyWallpaper","Wallpaper will be downloaded at "+path.getPath());
            loading_dialog = new ProgressDialog(context);
            loading_dialog.setMessage(getString(R.string.downloading_wallpaper));
            loading_dialog.setCancelable(false);
            loading_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            loading_dialog.setMax(100);
            loading_dialog.setProgress(0);
            loading_dialog.setProgressNumberFormat("Please wait.");
            loading_dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        download_cancel.put(download_id,true);
                        LogUtils.iTag("MyWallpaper","Download canceled.");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            loading_dialog.show();
            new Thread()
            {
                @SuppressLint("DefaultLocale")
                @Override
                public void run()
                {
                    Looper.prepare();
                    LogUtils.dTag("MyWallpaper","Download started.");
                    while(true)
                    {
                        try {
                            if(download_cancel.get(download_id)!=null)
                            {
                                manager.remove(download_id);
                                loading_dialog.dismiss();
                                util.show_info_dialog("",getString(R.string.download_canceled),context);
                                break;
                            }
                            DownloadManager.Query query = new DownloadManager.Query().setFilterById(download_id);
                            Cursor c =  manager.query(query);
                            if(c.moveToFirst())
                            {
                                //LogUtils.vTag("MyWallpaperTest",c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));
                                int status=c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                                LogUtils.vTag("MyWallpaperTest",String.valueOf(status));
                                if((status>=1000&&status<=1009)||status==1)
                                {
                                    LogUtils.eTag("MyWallpaper","Download ERROR. "+String.valueOf(status));
                                    throw new Exception("Download ERROR. "+String.valueOf(status));
                                }
                                int downloadBytesIdx = c.getColumnIndexOrThrow(
                                        DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                                int totalBytesIdx = c.getColumnIndexOrThrow(
                                        DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                                long downloadBytes = c.getLong(downloadBytesIdx);
                                long totalBytes = c.getLong(totalBytesIdx);
                                loading_dialog.setProgress((int)(downloadBytes*100/totalBytes));
                                loading_dialog.setProgressNumberFormat(String.format("%.2fKB/%.2fKB",downloadBytes/1024.0,totalBytes/1024.0));
                                if(downloadBytes==totalBytes)
                                {
                                    break;
                                }
                            }
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                            Toast.makeText(context, getString(R.string.download_failed)+ex.toString(),Toast.LENGTH_LONG);
                            loading_dialog.dismiss();
                            break;
                        }
                    }
                    loading_dialog.dismiss();
                }
            }.start();
        } catch (Exception e){
            util.show_info_dialog("",getString(R.string.download_failed),this);
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
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(download_done_receiver);
    }
}