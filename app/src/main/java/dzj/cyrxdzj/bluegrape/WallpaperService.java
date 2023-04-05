package dzj.cyrxdzj.bluegrape;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.warnyul.android.widget.FastVideoView;

import java.io.ByteArrayInputStream;

;

public class WallpaperService extends Service {
    public static boolean isStarted = false;

    public static WindowManager windowManager;
    public static WindowManager.LayoutParams layoutParams;

    public static AbsoluteLayout layout;
    public static ImageView image_view;
    public static FastVideoView video_view;
    public static WebView html_view;
    public static TextView info_text_view;
    private CommonUtil util = new CommonUtil();
    public static boolean ready = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private WebViewClient web_view_client = new WebViewClient() {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView html_view, String url) {
            LogUtils.dTag("WallpaperService", "Request: " + url);
            if (url.startsWith("file://") && !url.startsWith("file://" + util.get_storage_path() + AppListener.wallpaper_id + "/src")) {
                return new WebResourceResponse("text/html", "utf-8", new ByteArrayInputStream("Wallpaper access to file protocol paths other than whitelist is prohibited.".getBytes()));
            } else if (!url.startsWith("file://") && NetworkUtils.isMobileData() && !dzj.cyrxdzj.bluegrape.Settings.bool_settings.get("html_wallpaper.allow_mobile_network")) {
                return new WebResourceResponse("text/html", "utf-8", new ByteArrayInputStream("Set to prohibit wallpaper from accessing the network under the mobile data network.".getBytes()));
            } else if (!url.startsWith("file://") && NetworkUtils.isWifiConnected() && !dzj.cyrxdzj.bluegrape.Settings.bool_settings.get("html_wallpaper.allow_wifi_network")) {
                return new WebResourceResponse("text/html", "utf-8", new ByteArrayInputStream("Set to prohibit wallpaper from accessing the network under the Wifi network.".getBytes()));
            } else {
                return null;
            }
        }
    };

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
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.x = 0;
        layoutParams.y = 0;
        LogUtils.dTag("WallpaperService", "Run");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        if (PermissionUtils.isGrantedDrawOverlays()) {
            ready = true;
            LogUtils.dTag("WallpaperService", "Ready");
            image_view = new ImageView(this);
            video_view = new FastVideoView(this);
            html_view = new WebView(this);
            html_view.setWebViewClient(web_view_client);
            html_view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            html_view.getSettings().setJavaScriptEnabled(true);
            html_view.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
            html_view.getSettings().setAllowFileAccessFromFileURLs(false);
            info_text_view = new TextView(this);
            info_text_view.setTextColor(Color.argb(255, 0, 0, 255));
            if (layout != null) {
                try {
                    windowManager.removeView(layout);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            layout = new AbsoluteLayout(this);
            layout.addView(image_view);
            layout.addView(video_view);
            layout.addView(html_view);
            layout.addView(info_text_view);
            windowManager.addView(layout, layoutParams);
        }
    }
}