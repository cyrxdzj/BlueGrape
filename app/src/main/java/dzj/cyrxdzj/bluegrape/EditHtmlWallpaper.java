package dzj.cyrxdzj.bluegrape;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;

public class EditHtmlWallpaper extends AppCompatActivity {

    private String wallpaper_id;
    private EditText wallpaper_name_editor;
    private EditText input_url_view;
    private SeekBar alpha_seekbar;
    private TextView wallpaper_raw_name;
    private ImageView wallpaper_preview_view;
    private ProgressDialog loading_dialog;
    private CommonUtil util=new CommonUtil();
    private Context context;
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
    public void show_delete_question_dialog()
    {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.confirm_delete))
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete();
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_html_wallpaper);
        util.ask_permission(this);
        Intent intent = getIntent();
        wallpaper_id = intent.getStringExtra("wallpaper_id");
        LogUtils.dTag("EditHtmlWallpaper", "This wallpaper will be edited: " + wallpaper_id);
        try {
            //初始化配置
            String config_str = util.read_file(util.get_storage_path() + wallpaper_id + "/config.json");
            JSONObject config = new JSONObject(config_str);
            wallpaper_name_editor = (EditText) findViewById(R.id.wallpaper_name);
            wallpaper_name_editor.setText(URLDecoder.decode(config.getString("name"), "UTF-8"));
            alpha_seekbar=(SeekBar)findViewById(R.id.alpha);
            alpha_seekbar.setProgress(config.getInt("alpha"));
            String raw_config_str = util.read_file(util.get_storage_path() + wallpaper_id + "/src/config.json");
            JSONObject raw_config = new JSONObject(raw_config_str);
            wallpaper_raw_name=(TextView)findViewById(R.id.raw_name);
            wallpaper_raw_name.setText(raw_config.getString("raw_name"));
            wallpaper_preview_view=(ImageView)findViewById(R.id.preview_image);
            wallpaper_preview_view.setImageURI(Uri.fromFile(new File(util.get_storage_path()+wallpaper_id+"/src/preview.png")));
        } catch (Exception e) {
            util.show_info_dialog("", getString(R.string.load_failed),this);
            e.printStackTrace();
        }
    }
    public void save(View view)
    {
        try {
            save_without_dialog();
            util.show_info_dialog("",getString(R.string.save_successfully),this);
        }
        catch (Exception ex) {
            util.show_info_dialog("",getString(R.string.save_failed),this);
        }
    }
    public void save_without_dialog() throws IOException {
        String save_str = "{\n"+
                "\t\"name\":\""+ URLEncoder.encode(wallpaper_name_editor.getText().toString(),"UTF-8")+"\",\n"+
                "\t\"alpha\":"+String.valueOf(alpha_seekbar.getProgress())+"\n}";
        LogUtils.dTag("EditHtmlWallpaper","Config content:\n"+save_str);
        util.write_file(util.get_storage_path()+wallpaper_id+"/config.json",save_str);
    }
    public void apply(View view)
    {
        try {
            save_without_dialog();
        }
        catch (Exception ex) {
            util.show_info_dialog("",getString(R.string.save_failed),this);
        }
        Intent intent=new Intent();
        intent.setClass(this,ApplyWallpaper.class);
        intent.putExtra("wallpaper_id",wallpaper_id);
        intent.putExtra("wallpaper_name",wallpaper_name_editor.getText().toString());
        this.startActivity(intent);
    }
    public void button_delete(View view)
    {
        show_delete_question_dialog();
    }
    public void delete()
    {
        LogUtils.dTag("EditHtmlWallpaper","This wallpaper will be deleted");
        File file_obj=new File(util.get_storage_path()+wallpaper_id);
        util.delete_dir(file_obj);
        finish();
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
    public void update(View view)
    {
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
            MyWallpaper.download_wallpaper_id.put(download_id,wallpaper_id);
            MyWallpaper.download_path.put(download_id,path.getPath());
            LogUtils.vTag("EditHtmlWallpaperTest",download_id);
            LogUtils.dTag("EditHtmlWallpaper","Wallpaper will be downloaded at "+path.getPath());
            loading_dialog = new ProgressDialog(this);
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
                        MyWallpaper.download_cancel.put(download_id,true);
                        LogUtils.iTag("EditHtmlWallpaper","Download canceled.");
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
                    LogUtils.dTag("EditHtmlWallpaper","Download started.");
                    while(true)
                    {
                        try {
                            if(MyWallpaper.download_cancel.get(download_id)!=null)
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
                                //LogUtils.vTag("EditHtmlWallpaperTest",c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));
                                int status=c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                                LogUtils.vTag("EditHtmlWallpaperTest",String.valueOf(status));
                                if((status>=1000&&status<=1009)||status==1)
                                {
                                    LogUtils.eTag("EditHtmlWallpaper","Download ERROR. "+String.valueOf(status));
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
                                    finish();
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
            try {
                save_without_dialog();
            }
            catch (Exception ex) {
                util.show_info_dialog("",getString(R.string.save_failed),this);
            }
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}