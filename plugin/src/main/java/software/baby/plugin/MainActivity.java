package software.baby.plugin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 暂时注释，会用到资源
//        setContentView(R.layout.activity_main);
        Log.e(TAG, "onCreate: 我是插件的Activity");

        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null);
        setContentView(view);
    }
}