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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class AboutThisSoftware extends AppCompatActivity {

    private CommonUtil util=new CommonUtil();
    public String get_version()
    {
        try {
            return this.getPackageManager().getPackageInfo(this.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
    public void show_update_question_dialog(String now_version,String latest_version,String url)
    {
        Context context=this;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.current_version)+now_version+"\n"+getString(R.string.latest_version)+latest_version+"\n"+getString(R.string.do_update_question))
                .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtils.dTag("AboutThisSoftware","Info: "+now_version+" "+latest_version+" "+url);
                        //Toast.makeText(context,"将会打开浏览器下载安装包。下载完成后，您可能要在通知栏中或其它地方手动操作。",Toast.LENGTH_LONG).show();
                        /*Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);*/
                        download_apk(url);
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    private void download_apk(String url)
    {
        if(NetworkUtils.isMobileData())
        {
            AlertDialog dialog=new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_download_under_4g)
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            download_and_install(url);
                            dialog.dismiss();
                        }
                    }).create();
            dialog.show();
        }
        else
        {
            download_and_install(url);
        }
    }
    private void download_and_install(String url)
    {
        try {
            Context context=this;
            String[] temp=url.split("\\?")[0].split("/");
            String file_name=temp[temp.length-1];
            DownloadManager manager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request_wallpaper = new DownloadManager.Request(Uri.parse(url));
            Uri path=Uri.fromFile(new File(this.getExternalFilesDir("") + "/download/"+file_name));
            request_wallpaper.setDestinationUri(path);
            request_wallpaper.setMimeType("application/apk");
            request_wallpaper.setTitle(getString(R.string.apk_download_task));
            request_wallpaper.setDescription(getString(R.string.downloading_apk));
            request_wallpaper.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            Long download_id=manager.enqueue(request_wallpaper);
            MyWallpaper.download_path.put(download_id,path.getPath());
            LogUtils.dTag("AboutThisSoftware","APK will be downloaded at "+path.getPath());
            ProgressDialog loading_dialog = new ProgressDialog(this);
            loading_dialog.setMessage(getString(R.string.downloading_apk));
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
                        LogUtils.iTag("AboutThisSoftware","Download canceled.");
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
                    LogUtils.dTag("AboutThisSoftware","Download started.");
                    while(true)
                    {
                        try {
                            if(MyWallpaper.download_cancel.get(download_id)!=null)
                            {
                                manager.remove(download_id);
                                loading_dialog.dismiss();
                                util.show_info_dialog("",getString(R.string.download_apk_canceled),context);
                                break;
                            }
                            DownloadManager.Query query = new DownloadManager.Query().setFilterById(download_id);
                            Cursor c =  manager.query(query);
                            if(c.moveToFirst())
                            {
                                //LogUtils.vTag("AboutThisSoftwareTest",c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));
                                int status=c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
                                //LogUtils.vTag("AboutThisSoftwareTest",String.valueOf(status));
                                if((status>=1000&&status<=1009)||status==1)
                                {
                                    LogUtils.eTag("AboutThisSoftware","Download ERROR. "+String.valueOf(status));
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
                                    LogUtils.dTag("AboutThisSoftware","APK download finished.");
                                    AppUtils.installApp(path);
                                    break;
                                }
                            }
                        }
                        catch (Exception ex)
                        {
                            LogUtils.dTag("AboutThisSoftware","APK download failed.");
                            ex.printStackTrace();
                            Toast.makeText(context, getString(R.string.download_apk_failed)+ex.toString(),Toast.LENGTH_LONG).show();
                            loading_dialog.dismiss();
                            break;
                        }
                    }
                    loading_dialog.dismiss();
                }
            }.start();
        } catch (Exception e){
            util.show_info_dialog("",getString(R.string.download_apk_failed),this);
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_this_software);
        TextView app_version=(TextView)findViewById(R.id.app_version);
        app_version.setText("Version "+get_version());
        TextView build_time=(TextView)findViewById(R.id.build_time);
        build_time.setText("Build Time "+BuildConfig.BUILD_TIME);
    }
    public void reward_and_contribution(View view)
    {
        Intent intent=new Intent();
        intent.setClass(this,RewardAndContribution.class);
        this.startActivity(intent);
    }
    public void feedback(View view)
    {
        Uri uri = Uri.parse("https://www.wjx.cn/vj/w7Os0kb.aspx");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    public void using_document(View view)
    {
        Uri uri = Uri.parse("https://gitee.com/cyrxdzj/BlueGrape/blob/master/README.md");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    public void open_web(View view)
    {
        Uri uri = Uri.parse("https://cyrxdzj.github.io/BlueGrapeWeb");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    public void check_update(View view)
    {
        Context context=this;
        ProgressDialog loading_dialog = new ProgressDialog(context);
        loading_dialog.setMessage(getString(R.string.checking_update));
        loading_dialog.show();
        new Thread()
        {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    URL update_url=new URL("https://cyrxdzj.github.io/BlueGrapeWeb/apks.json");
                    HttpURLConnection connection=(HttpURLConnection)update_url.openConnection();
                    connection.setRequestMethod("GET");
                    assert connection.getResponseCode()==200;
                    LogUtils.dTag("AboutThisSoftware","Response status: "+String.valueOf(connection.getResponseCode()));
                    InputStream stream=connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder temp= new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        temp.append(line).append("\n");
                    }
                    String response=temp.toString();
                    LogUtils.dTag("AboutThisSoftware","Response content:\n"+response);
                    JSONObject apks=new JSONObject(response);
                    if(!apks.getString("latest_version").equals(get_version()))
                    {
                        show_update_question_dialog(get_version(),apks.getString("latest_version"),apks.getJSONObject("apks").getString(apks.getString("latest_version")));
                    }
                    else
                    {
                        Toast.makeText(context,"无更新",Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | AssertionError | JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context,"检查更新时遇到错误",Toast.LENGTH_SHORT).show();
                } finally {
                    loading_dialog.dismiss();
                }
                Looper.loop();
            }
        }.start();
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