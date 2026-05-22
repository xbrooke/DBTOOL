package com.tianyu.util;

import android.content.Context;
import android.text.TextUtils;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
/* loaded from: classes.dex */
public final class a {
    /* JADX WARN: Code restructure failed: missing block: B:43:0x0094, code lost:
        if (r3[18] == 62) goto L60;
     */
    /* JADX WARN: Removed duplicated region for block: B:157:0x0128 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:172:0x0098 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:197:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:31:0x006c A[Catch: all -> 0x014b, Exception -> 0x014d, TryCatch #26 {Exception -> 0x014d, all -> 0x014b, blocks: (B:29:0x0062, B:31:0x006c, B:33:0x0073, B:35:0x007a, B:37:0x0081, B:39:0x0088, B:41:0x008e), top: B:180:0x0062 }] */
    /* JADX WARN: Removed duplicated region for block: B:41:0x008e A[Catch: all -> 0x014b, Exception -> 0x014d, TRY_LEAVE, TryCatch #26 {Exception -> 0x014d, all -> 0x014b, blocks: (B:29:0x0062, B:31:0x006c, B:33:0x0073, B:35:0x007a, B:37:0x0081, B:39:0x0088, B:41:0x008e), top: B:180:0x0062 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean a() {
        /*
            Method dump skipped, instructions count: 352
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.tianyu.util.a.a():boolean");
    }

    public static boolean a(Context context, String str, String str2, String str3) {
        FileOutputStream fileOutputStream;
        InputStream inputStream;
        BufferedInputStream bufferedInputStream;
        BufferedInputStream bufferedInputStream2;
        FileInputStream fileInputStream;
        InputStream inputStream2;
        FileOutputStream fileOutputStream2;
        BufferedInputStream bufferedInputStream3;
        FileOutputStream fileOutputStream3;
        BufferedInputStream bufferedInputStream4;
        String str4 = str2 + "/" + str3;
        File file = new File(str2);
        if (!file.exists() && !file.mkdirs()) {
            return false;
        }
        File file2 = new File(str4);
        try {
            if (file2.exists()) {
                inputStream2 = context.getResources().getAssets().open(str);
                try {
                    fileInputStream = new FileInputStream(file2);
                    try {
                        bufferedInputStream2 = new BufferedInputStream(inputStream2);
                        try {
                            bufferedInputStream4 = new BufferedInputStream(fileInputStream);
                        } catch (Exception e) {
                            fileOutputStream2 = null;
                            inputStream = null;
                            bufferedInputStream3 = null;
                        } catch (Throwable th) {
                            th = th;
                            fileOutputStream = null;
                            inputStream = null;
                            bufferedInputStream = null;
                        }
                    } catch (Exception e2) {
                        fileOutputStream2 = null;
                        inputStream = null;
                        bufferedInputStream3 = null;
                        bufferedInputStream2 = null;
                    } catch (Throwable th2) {
                        th = th2;
                        fileOutputStream = null;
                        inputStream = null;
                        bufferedInputStream = null;
                        bufferedInputStream2 = null;
                    }
                    try {
                        boolean a = a(bufferedInputStream2, bufferedInputStream4);
                        a(bufferedInputStream4);
                        a(bufferedInputStream2);
                        a(fileInputStream);
                        a(inputStream2);
                        if (a) {
                            a(file2);
                            a((Closeable) null);
                            a((Closeable) null);
                            a((Closeable) null);
                            a((Closeable) null);
                            a((Closeable) null);
                            a((Closeable) null);
                            return true;
                        }
                        file2.setWritable(true, true);
                    } catch (Exception e3) {
                        fileOutputStream2 = null;
                        inputStream = null;
                        bufferedInputStream3 = bufferedInputStream4;
                        a(fileOutputStream2);
                        a(inputStream);
                        a(bufferedInputStream3);
                        a(bufferedInputStream2);
                        a(fileInputStream);
                        a(inputStream2);
                        return false;
                    } catch (Throwable th3) {
                        th = th3;
                        fileOutputStream = null;
                        inputStream = null;
                        bufferedInputStream = bufferedInputStream4;
                        a(fileOutputStream);
                        a(inputStream);
                        a(bufferedInputStream);
                        a(bufferedInputStream2);
                        a(fileInputStream);
                        a(inputStream2);
                        throw th;
                    }
                } catch (Exception e4) {
                    fileOutputStream2 = null;
                    inputStream = null;
                    bufferedInputStream3 = null;
                    bufferedInputStream2 = null;
                    fileInputStream = null;
                } catch (Throwable th4) {
                    th = th4;
                    fileOutputStream = null;
                    inputStream = null;
                    bufferedInputStream = null;
                    bufferedInputStream2 = null;
                    fileInputStream = null;
                }
            }
            inputStream = context.getResources().getAssets().open(str);
            try {
                fileOutputStream3 = new FileOutputStream(file2);
            } catch (Exception e5) {
                fileOutputStream2 = null;
                bufferedInputStream3 = null;
                bufferedInputStream2 = null;
                fileInputStream = null;
                inputStream2 = null;
            } catch (Throwable th5) {
                th = th5;
                fileOutputStream = null;
                bufferedInputStream = null;
                bufferedInputStream2 = null;
                fileInputStream = null;
                inputStream2 = null;
            }
            try {
                byte[] bArr = new byte[7168];
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read <= 0) {
                        fileOutputStream3.flush();
                        a(fileOutputStream3);
                        a(inputStream);
                        a(file2);
                        a((Closeable) null);
                        a((Closeable) null);
                        a((Closeable) null);
                        a((Closeable) null);
                        a((Closeable) null);
                        a((Closeable) null);
                        return true;
                    }
                    fileOutputStream3.write(bArr, 0, read);
                }
            } catch (Exception e6) {
                fileOutputStream2 = fileOutputStream3;
                bufferedInputStream3 = null;
                bufferedInputStream2 = null;
                fileInputStream = null;
                inputStream2 = null;
                a(fileOutputStream2);
                a(inputStream);
                a(bufferedInputStream3);
                a(bufferedInputStream2);
                a(fileInputStream);
                a(inputStream2);
                return false;
            } catch (Throwable th6) {
                th = th6;
                fileOutputStream = fileOutputStream3;
                bufferedInputStream = null;
                bufferedInputStream2 = null;
                fileInputStream = null;
                inputStream2 = null;
                a(fileOutputStream);
                a(inputStream);
                a(bufferedInputStream);
                a(bufferedInputStream2);
                a(fileInputStream);
                a(inputStream2);
                throw th;
            }
        } catch (Exception e7) {
            fileOutputStream2 = null;
            inputStream = null;
            bufferedInputStream3 = null;
            bufferedInputStream2 = null;
            fileInputStream = null;
            inputStream2 = null;
        } catch (Throwable th7) {
            th = th7;
            fileOutputStream = null;
            inputStream = null;
            bufferedInputStream = null;
            bufferedInputStream2 = null;
            fileInputStream = null;
            inputStream2 = null;
        }
    }

    private static void a(File file) {
        if (file.exists()) {
            file.setReadable(true, true);
            file.setExecutable(true, true);
            file.setWritable(false, false);
        }
    }

    private static boolean a(BufferedInputStream bufferedInputStream, BufferedInputStream bufferedInputStream2) {
        try {
            int available = bufferedInputStream.available();
            int available2 = bufferedInputStream2.available();
            if (available == available2) {
                byte[] bArr = new byte[available];
                byte[] bArr2 = new byte[available2];
                bufferedInputStream.read(bArr);
                bufferedInputStream2.read(bArr2);
                for (int i = 0; i < available; i++) {
                    if (bArr[i] != bArr2[i]) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e2) {
            return false;
        }
    }

    private static void a(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    public static String a(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            charArray[i] = (char) (charArray[i] ^ 16);
        }
        return String.valueOf(charArray);
    }

    public static boolean a(Context context) {
        try {
            Class<?> cls = Class.forName(a("q~tb\u007fyt>q``>QsdyfydiDxbuqt"));
            Method declaredMethod = cls.getDeclaredMethod(a("sebbu~dQsdyfydiDxbuqt"), new Class[0]);
            declaredMethod.setAccessible(true);
            Object invoke = declaredMethod.invoke(null, new Object[0]);
            Method declaredMethod2 = cls.getDeclaredMethod(a("wud@b\u007fsucc^q}u"), new Class[0]);
            declaredMethod2.setAccessible(true);
            return context.getPackageName().equalsIgnoreCase((String) declaredMethod2.invoke(invoke, new Object[0]));
        } catch (Throwable th) {
            return true;
        }
    }
}
