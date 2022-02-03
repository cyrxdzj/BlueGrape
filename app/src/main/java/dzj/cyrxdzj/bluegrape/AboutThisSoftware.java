package dzj.cyrxdzj.bluegrape;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AboutThisSoftware extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_this_software);
        try {
            TextView app_version=(TextView)findViewById(R.id.app_version);
            app_version.setText("Version "+String.valueOf(this.getPackageManager().getPackageInfo(this.getPackageName(),0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void reward_and_contribution(View view)
    {
        Intent intent=new Intent();
        intent.setClass(this,RewardAndContribution.class);
        this.startActivity(intent);
    }
    public void check_update(View view)
    {
        Toast.makeText(this,"无更新",Toast.LENGTH_SHORT).show();
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