package software.baby.plugin;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;

public class BaseActivity extends AppCompatActivity {

    protected Context mContext;

//    @Override
//    public Resources getResources() {
//
////        if (getApplication() != null && getApplication().getResources() != null) {
////            return getApplication().getResources();
////        }
////        return super.getResources();
//
//        Resources resources = LoadUtil.getResources(getApplication());
//        Log.e("leo", "getResources:11 " + getApplication());
//        return resources == null ? super.getResources() : resources;
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources resources = ResourceUtil.getResources(getApplication());

        mContext = new ContextThemeWrapper(getBaseContext(), 0);

        // 替换了插件的
        Class<? extends Context> clazz = mContext.getClass();
        try {
            Field mResourcesField = clazz.getDeclaredField("mResources");
            mResourcesField.setAccessible(true);
            mResourcesField.set(mContext, resources);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
