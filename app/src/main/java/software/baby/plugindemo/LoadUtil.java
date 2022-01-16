package software.baby.plugindemo;

import android.content.Context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

public class LoadUtil {

    private static final String apkPath = "/sdcard/plugin-debug.apk";

    public static void loadClass(Context context) {
        // 宿主的dexElements   ---》 dexElementsField  --》
        // DexPathList对象  --》 pathList的Field --》 **BaseDexClassLoader** 对象
        // --》 能不能宿主和插件的类加载器

        try {
            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            Class<?> classLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = classLoaderClass.getDeclaredField("pathList");
            pathListField.setAccessible(true);

            // 1. 获取宿主的类加载器
            ClassLoader pathClassLoader = context.getClassLoader();
            Object hostPathList = pathListField.get(pathClassLoader);
            // 目的：dexElements的对象
            // new Test().print();
            // 静态的： Test.print();
            Object[] hostDexElements = (Object[]) dexElementsField.get(hostPathList);

            // 2.插件,类加载器
            // 版本  -- 7.0之后
            ClassLoader pluginClassLoader = new DexClassLoader(apkPath,
                    context.getCacheDir().getAbsolutePath(), null, pathClassLoader);
            Object pluginPathList = pathListField.get(pluginClassLoader);
            // 目的：dexElements的对象
            // new Test().print();
            // 静态的： Test.print();
            Object[] pluginDexElements = (Object[]) dexElementsField.get(pluginPathList);

            // 合并
            // new Elements[];
            Object[] newElements = (Object[]) Array.newInstance(hostDexElements.getClass().getComponentType(),
                    hostDexElements.length + pluginDexElements.length);

            System.arraycopy(hostDexElements, 0, newElements, 0, hostDexElements.length);
            System.arraycopy(pluginDexElements, 0, newElements, hostDexElements.length, pluginDexElements.length);

            // 赋值到宿主的dexElements
            // hostDexElements = newElements;
            dexElementsField.set(hostPathList, newElements);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 目的：dexElements的对象
    }


}
