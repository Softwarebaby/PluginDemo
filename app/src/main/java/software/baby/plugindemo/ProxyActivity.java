package software.baby.plugindemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class ProxyActivity extends AppCompatActivity {
    private static final String TAG = "ProxyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_proxy);

        Log.e(TAG, "onCreate: 我是代理的Activity" );
    }
}