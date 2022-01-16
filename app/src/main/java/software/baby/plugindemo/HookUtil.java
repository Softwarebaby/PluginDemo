package software.baby.plugindemo;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class HookUtil {

    private static String TARGET_INTENT = "target_Intent";

    public static void hookAMS() {

        // 动态代理需要替换的是IActivityManager
        try {

            // 目的： 为了获取系统的IActivityManager对象 -- private IActivityManager mInstance;
            // Singleton对象
            // 10

            Field iActivityManagerSingletonField = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Class<?> clazz = Class.forName("android.app.ActivityManager");
                iActivityManagerSingletonField = clazz.getDeclaredField("IActivityManagerSingleton");
            } else {
                Class<?> clazz = Class.forName("android.app.ActivityManagerNative");
                iActivityManagerSingletonField = clazz.getDeclaredField("gDefault");
            }

            iActivityManagerSingletonField.setAccessible(true);
            Object singleton = iActivityManagerSingletonField.get(null);

            // mInstance 对象  ---》 IActivityManager对象
            Class<?> signletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = signletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            final Object mInstance = mInstanceField.get(singleton);

            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");
            Object mInstanceProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {


                            /**
                             * startActivity(whoThread, who.getBasePackageName(), intent,
                             *                         intent.resolveTypeIfNeeded(who.getContentResolver()),
                             *                         token, target != null ? target.mEmbeddedID : null,
                             *                         requestCode, 0, null, options);
                             */

                            if ("startActivity".equals(method.getName())) {
                                // 修改Intent
                                int index = 0;

                                for (int i = 0; i < objects.length; i++) {
                                    if (objects[i] instanceof Intent) {
                                        index = i;
                                        break;
                                    }
                                }
                                // 启动插件的 1 --- 代理 2
                                Intent intent = (Intent) objects[index];

                                // 该成启动代理Intent
                                Intent intentProxy = new Intent();
                                intentProxy.setClassName("software.baby.plugindemo",
                                        "software.baby.plugindemo.ProxyActivity");

                                intentProxy.putExtra(TARGET_INTENT, intent);

                                objects[index] = intentProxy;
                            }


                            // 第一个参数：系统的IActivityManager对象
                            return method.invoke(mInstance, objects);
                        }
                    });
            // 用代理对象替换系统的IActivityManager对象 ---> field
//            mInstanceProxy --> 替换系统的 IActivityManager对象  ---》 反射去实现
            // mInstance = mInstanceProxy
            mInstanceField.set(singleton, mInstanceProxy);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hookHandler() {

        //系统的Callback对象 --》 mh对象 --》 ActivityThread对象 --- 》
        // private static volatile ActivityThread sCurrentActivityThread;

        try {
            // sCurrentActivityThread
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            Object activityThread = sCurrentActivityThreadField.get(null);

            // mh对象
            Field mHField = clazz.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mH = (Handler) mHField.get(activityThread);

            Class<?> handlerClass = Class.forName("android.os.Handler");
            Field mCallbackField = handlerClass.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);

            Handler.Callback callback = new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message message) {
                    switch (message.what) {
                        case 100:
                            // 拿到了message
                            // ActivityClientRecord的对象 --- msg.obj
                            try {
                                Field intentField = message.obj.getClass().getDeclaredField("intent");
                                intentField.setAccessible(true);
                                //  启动代理 2
                                Intent intentProxy = (Intent) intentField.get(message.obj);
                                // 启动插件的1
                                Intent intent = intentProxy.getParcelableExtra(TARGET_INTENT);
                                if (intent != null) {
                                    intentField.set(message.obj, intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case 159:
                            // ClientTransaction transaction = (ClientTransaction) msg.obj

                            Class<?> transactionClass = message.obj.getClass();
                            // private List<ClientTransactionItem> mActivityCallbacks;
                            try {
                                Field mActivityCallbacksField = transactionClass.getDeclaredField("mActivityCallbacks");
                                mActivityCallbacksField.setAccessible(true);
                                List list = (List) mActivityCallbacksField.get(message.obj);

                                for (int i = 0; i < list.size(); i++) {
                                    // LaunchActivityItem的对象
                                    if (list.get(i).getClass().getName()
                                            .equals("android.app.servertransaction.LaunchActivityItem")) {
                                        Object launchActivityItem = list.get(i);
                                        // Intent
                                        Field mIntentProxyField = launchActivityItem.getClass().getDeclaredField("mIntent");
                                        mIntentProxyField.setAccessible(true);
                                        Intent intentProxy = (Intent) mIntentProxyField.get(launchActivityItem);
                                        // 启动插件的1
                                        Intent intent = intentProxy.getParcelableExtra(TARGET_INTENT);
                                        if (intent != null) {
                                            mIntentProxyField.set(launchActivityItem, intent);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            break;
                    }
                    return false;
                }
            };

            // 系统的callback = 自己创建的callback
            mCallbackField.set(mH, callback);

        } catch (Exception e) {
            e.printStackTrace();
        }


        // 用我们创建的Callback对象，替换系统的Callback对象
    }


}
