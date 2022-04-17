package dzj.cyrxdzj.bluegrape;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.warnyul.android.widget.FastVideoView;

public class WallpaperService extends Service {
    public static boolean isStarted = false;

    public static WindowManager windowManager;
    public static WindowManager.LayoutParams layoutParams;

    public static AbsoluteLayout layout;
    public static ImageView image_view;
    public static FastVideoView video_view;
    public static TextView info_text_view;
    public static boolean ready=false;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.x = 0;
        layoutParams.y = 0;
        Log.d("WallpaperService","Run");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            ready=true;
            Log.d("WallpaperService","Ready");
            image_view = new ImageView(this);
            video_view = new FastVideoView(this);
            info_text_view=new TextView(this);
            info_text_view.setTextColor(Color.argb(255,0,0,255));
            //info_text_view.setText("Hello");
            layout=new AbsoluteLayout(this);
            //image_view.setImageResource(R.drawable.default_image);
            layout.addView(image_view);
            layout.addView(video_view);
            layout.addView(info_text_view);
            windowManager.addView(layout, layoutParams);
        }
    }
}