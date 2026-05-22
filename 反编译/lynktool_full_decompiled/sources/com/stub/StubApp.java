package com.stub;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.TypedValue;
import com.tianyu.util.DtcLoader;
import com.tianyu.util.a;
import dalvik.system.DexFile;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
/* loaded from: classes.dex */
public final class StubApp extends Application {
    private static Context c;
    private static Application a = null;
    private static String b = "libjiagu";
    private static boolean loadFromLib = false;
    private static boolean needX86Bridge = false;
    private static boolean returnIntern = true;
    private static String d = null;
    private static String e = null;
    private static String f = null;
    private static String g = null;
    private static String h = null;
    private static Map<Integer, String> i = new ConcurrentHashMap();
    private static Map<String, Set<String>> perm = new ConcurrentHashMap();

    public static native void fcmark();

    public static native void interface11(int i2);

    public static native Enumeration<String> interface12(DexFile dexFile);

    public static native long interface13(int i2, long j, long j2, long j3, int i3, int i4, long j4);

    public static native String interface14(int i2);

    public static native AssetFileDescriptor interface17(AssetManager assetManager, String str);

    public static native InputStream interface18(Class cls, String str);

    public static native InputStream interface19(ClassLoader classLoader, String str);

    public static native InputStream interface199(AssetManager assetManager, String str);

    public static native void interface20();

    public static native void interface21(Application application);

    public static native void interface22(int i2, String[] strArr, int[] iArr);

    public static native void interface24(Activity activity, String[] strArr, int i2);

    public static native ZipEntry interface30(ZipFile zipFile, String str);

    public static native void interface5(Application application);

    public static native InputStream interface51(Resources resources, int i2);

    public static native InputStream interface52(Resources resources, int i2, TypedValue typedValue);

    public static native AssetFileDescriptor interface53(Resources resources, int i2);

    public static native MediaPlayer interface54(Context context, int i2);

    public static native MediaPlayer interface55(Context context, int i2, AudioAttributes audioAttributes, int i3);

    public static native int interface56(SoundPool soundPool, Context context, int i2, int i3);

    public static native String interface6(String str);

    public static native boolean interface7(Application application, Context context);

    public static native boolean interface8(Application application, Context context);

    public static native void interface99(Application application);

    public static native Location mark(LocationManager locationManager, String str);

    public static native void mark();

    public static native void mark(Location location);

    public static native synchronized Object n010333(Object obj, Object obj2);

    public static native void n0110();

    public static native int n0111();

    public static native Object n0113();

    public static native void n01130(Object obj);

    public static native boolean n01131(Object obj);

    public static native void n0113130(Object obj, int i2, Object obj2);

    public static native Object n0113133(Object obj, int i2, Object obj2);

    public static native Object n01133(Object obj);

    public static native void n011330(Object obj, Object obj2);

    public static native boolean n011331(Object obj, Object obj2);

    public static native Object n011333(Object obj, Object obj2);

    public static native Object n0113333(Object obj, Object obj2, Object obj3);

    public static native void pmark(Context context);

    public static native void rmark();

    public native synchronized void n11030(Object obj);

    public native synchronized boolean n110331(Object obj, Object obj2);

    public native void n1110();

    public native boolean n1111();

    public native void n11110(int i2);

    public native boolean n11111(boolean z);

    public native void n111130(int i2, Object obj);

    public native boolean n11113311(int i2, Object obj, Object obj2, int i3);

    public native Object n1113();

    public native void n11130(Object obj);

    public native boolean n11131(Object obj);

    public native void n111310(Object obj, int i2);

    public native void n1113130(Object obj, boolean z, Object obj2);

    public native Object n11133(Object obj);

    public native void n111330(Object obj, Object obj2);

    public native void n1113310(Object obj, Object obj2, boolean z);

    public native void n11133110(Object obj, Object obj2, boolean z, int i2);

    public native void n11133310(Object obj, Object obj2, Object obj3, int i2);

    public native Object n1113333(Object obj, Object obj2, Object obj3);

    public static String getDir() {
        return g;
    }

    public static String getSoPath1() {
        return e;
    }

    public static String getSoPath2() {
        return f;
    }

    public static Context getAppContext() {
        return c;
    }

    public static Context getOrigApplicationContext(Context context) {
        return context;
    }

    @Override // android.content.ContextWrapper
    protected final void attachBaseContext(Context context) {
        boolean a2;
        super.attachBaseContext(context);
        if (Build.VERSION.SDK_INT == 28) {
            try {
                Class.forName(a.a("q~tb\u007fyt>s\u007f~du~d>`}>@qs{qwu@qbcub4@qs{qwu")).getDeclaredConstructor(String.class).setAccessible(true);
            } catch (Throwable th) {
            }
            try {
                Class<?> cls = Class.forName(a.a("q~tb\u007fyt>q``>QsdyfydiDxbuqt"));
                Method declaredMethod = cls.getDeclaredMethod(a.a("sebbu~dQsdyfydiDxbuqt"), new Class[0]);
                declaredMethod.setAccessible(true);
                Object invoke = declaredMethod.invoke(null, new Object[0]);
                Field declaredField = cls.getDeclaredField(a.a("}Xyttu~Q`yGqb~y~wCx\u007fg~"));
                declaredField.setAccessible(true);
                declaredField.setBoolean(invoke, true);
            } catch (Throwable th2) {
            }
        }
        c = context;
        if (a == null) {
            a = this;
        }
        Boolean valueOf = Boolean.valueOf(a.a());
        Boolean bool = false;
        Boolean bool2 = false;
        bool = (Build.CPU_ABI.contains("64") || Build.CPU_ABI2.contains("64")) ? true : true;
        bool2 = (Build.CPU_ABI.contains("mips") || Build.CPU_ABI2.contains("mips")) ? true : true;
        if (valueOf.booleanValue() && needX86Bridge) {
            System.loadLibrary("X86Bridge");
        }
        if (loadFromLib) {
            if (valueOf.booleanValue() && !needX86Bridge) {
                System.loadLibrary("jiagu_x86");
            } else {
                System.loadLibrary("jiagu");
            }
        } else {
            String absolutePath = context.getFilesDir().getParentFile().getAbsolutePath();
            try {
                absolutePath = context.getFilesDir().getParentFile().getCanonicalPath();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            String str = absolutePath + "/.jiagu";
            h = a(str, bool.booleanValue(), bool2.booleanValue());
            d = a(str, false, false);
            e = str + File.separator + d;
            f = str + File.separator + h;
            g = str;
            if (bool2.booleanValue()) {
                a.a(context, b + "_mips.so", str, d);
            } else if (valueOf.booleanValue() && !needX86Bridge) {
                a.a(context, b + "_x86.so", str, d);
            } else {
                a.a(context, b + ".so", str, d);
            }
            if (bool.booleanValue() && !bool2.booleanValue()) {
                if (valueOf.booleanValue() && !needX86Bridge) {
                    a2 = a.a(context, b + "_x64.so", str, h);
                } else {
                    a2 = a.a(context, b + "_a64.so", str, h);
                }
                if (a2) {
                    System.load(str + "/" + h);
                } else {
                    System.load(str + "/" + d);
                }
            } else {
                System.load(str + "/" + d);
            }
        }
        DtcLoader.init();
        interface5(this);
        interface99(this);
    }

    private static String a(String str, boolean z, boolean z2) {
        String str2 = b;
        if (Build.VERSION.SDK_INT < 23) {
            str2 = str2 + str.hashCode();
        }
        if (z && !z2) {
            return str2 + "_64.so";
        }
        return str2 + ".so";
    }

    @Override // android.app.Application
    public final void onCreate() {
        super.onCreate();
        interface21(this);
        Context context = c;
        if (this != null && context != null && a.a(context)) {
            try {
                Method declaredMethod = Class.forName(a.a("s\u007f}>zw>rx>Bu`\u007fbdcDy}u")).getDeclaredMethod(a.a("BuwycdubQsdyfydiSq||Rqs{c"), Application.class);
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(null, this);
            } catch (Exception e2) {
            }
        }
    }

    public static String getString2(int i2) {
        String str = i.get(Integer.valueOf(i2));
        if (str == null) {
            str = interface14(i2);
            i.put(Integer.valueOf(i2), str);
        }
        if (str != null && returnIntern) {
            return str.intern();
        }
        return str;
    }

    public static String getString2(String str) {
        try {
            return getString2(Integer.parseInt(str));
        } catch (NumberFormatException e2) {
            return null;
        }
    }

    public static boolean isX86Arch() {
        return a.a();
    }

    public static void put(Object obj, String[] strArr) {
        try {
            String canonicalName = obj.getClass().getCanonicalName();
            Set<String> set = perm.get(canonicalName);
            if (set != null) {
                set.addAll(Arrays.asList(strArr));
            } else {
                Set<String> newSetFromMap = Collections.newSetFromMap(new ConcurrentHashMap());
                Collections.addAll(newSetFromMap, strArr);
                Set<String> put = perm.put(canonicalName, newSetFromMap);
                if (put != null) {
                    put.addAll(Arrays.asList(strArr));
                }
            }
        } catch (Exception e2) {
        }
    }
}
