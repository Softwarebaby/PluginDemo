package software.baby.plugin;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.Method;

public class ResourceUtil {

    private static final String apkPath = "/sdcard/plugin-debug.apk";

    private static Resources mResources;

    public static Resources getResources(Context context) {
        if (mResources == null) {
            mResources = loadResources(context);
        }
        return mResources;
    }

    // 可不可以放到插件代码中 ---
    private static Resources loadResources(Context context) {
        try {
            // 1. 创建一个 AssetManager
            AssetManager assetManager = AssetManager.class.newInstance();
            // 2. 添加插件的资源
            Method addAssetPathMethod = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(assetManager, apkPath);

            // 3. 创建Resources， 传入创建的AssetManager
            Resources resources = context.getResources();
            return new Resources(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
