package dzj.cyrxdzj.bluegrape;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AboutThisSoftware extends AppCompatActivity {

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
                .setMessage("当前版本"+now_version+"，最新版本"+latest_version+"，是否更新？")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("AboutThisSoftware",now_version+" "+latest_version+" "+url);
                        Toast.makeText(context,"将会打开浏览器下载安装包。下载完成后，您可能要在通知栏中或其它地方手动操作。",Toast.LENGTH_LONG).show();
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_this_software);
        TextView app_version=(TextView)findViewById(R.id.app_version);
        app_version.setText("Version "+get_version());
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
    public void check_update(View view)
    {
        Context context=this;
        Thread request_thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    URL update_url=new URL("https://cyrxdzj.github.io/BlueGrapeWeb/apks.json");
                    HttpURLConnection connection=(HttpURLConnection)update_url.openConnection();
                    connection.setRequestMethod("GET");
                    assert connection.getResponseCode()==200;
                    Log.d("AboutThisSoftware", String.valueOf(connection.getResponseCode()));
                    InputStream stream=connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder temp= new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        temp.append(line).append("\n");
                    }
                    String response=temp.toString();
                    Log.d("AboutThisSoftware", response);
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
                    Looper.loop();
                }
            }
        });
        request_thread.start();
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