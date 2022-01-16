package software.baby.plugindemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printClassLoader();

                try {
                    Class<?> clazz = Class.forName("software.baby.plugin.Test");
                    Method print = clazz.getMethod("print");
                    print.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                startActivity(new Intent(MainActivity.this, ProxyActivity.class));
//                startActivity(new Intent(MainActivity.this,SecondActivity.class));
                // 怎么让本来要启动plugin.MainActivity 变成启动ProxyActivity

                Intent intent = new Intent();
                intent.setComponent(new ComponentName("software.baby.plugin", "software.baby.plugin.MainActivity"));
                startActivity(intent);
            }
        });
    }

    private void printClassLoader() {
        ClassLoader classLoader = getClassLoader();
        while (classLoader != null) {
            Log.e(TAG, "printClassLoader: " + classLoader);
            classLoader = classLoader.getParent();
        }
        Log.e(TAG, "Activity：printClassLoader: " + Activity.class.getClassLoader());// pa1  boot2
        Log.e(TAG, "Activity：printClassLoader: " + AppCompatActivity.class.getClassLoader());// pa3 boot4
    }
}