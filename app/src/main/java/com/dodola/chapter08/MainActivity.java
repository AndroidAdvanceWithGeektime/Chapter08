package com.dodola.chapter08;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.taobao.android.runtime.AndroidRuntime;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;

public class MainActivity extends Activity {
    static {
        System.loadLibrary("native-lib");
    }

    private TextView mLoadTimeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.hook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidRuntime runtime = AndroidRuntime.getInstance();
                runtime.init(MainActivity.this.getApplicationContext(), true);
                runtime.setVerificationEnabled(false);
            }
        });
        mLoadTimeView = findViewById(R.id.loading_time);
        findViewById(R.id.read_dex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Long>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected void onPostExecute(Long aLong) {
                        super.onPostExecute(aLong);
                        mLoadTimeView.setText("加载时长：" + aLong);
                    }

                    @Override
                    protected Long doInBackground(Void... voids) {

                        InputStream inputStream = getResources().openRawResource(R.raw.classes_dex);
                        try {
                            File file = new File(getFilesDir(), "calendar.dex");
                            if (file.exists()) {
                                file.delete();
                            }
                            copyFile(inputStream, new FileOutputStream(file));
                            DexClassLoader pathClassLoader = new DexClassLoader(file.getAbsolutePath(), getCacheDir().getAbsolutePath(), null, getClassLoader());
                            DexBackedDexFile backedDexFile = DexBackedDexFile.fromInputStream(Opcodes.forApi(21), getResources().openRawResource(R.raw.classes_dex));
                            Set<? extends DexBackedClassDef> classes = backedDexFile.getClasses();
                            Iterator<? extends DexBackedClassDef> iterator = classes.iterator();
                            long start = System.currentTimeMillis();

                            while (iterator.hasNext()) {
                                DexBackedClassDef next = iterator.next();
                                String type = next.getType();
                                type = type.replace('/', '.').substring(1, type.length() - 1);
                                try {
                                    pathClassLoader.loadClass(type);
                                } catch (Throwable ex) {
                                    ex.printStackTrace();
                                }
                            }

                            long end = System.currentTimeMillis();
                            return end - start;

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return -1L;
                    }
                }.execute();

            }
        });
    }

    public static void copyFile(File in, File out) throws Exception {
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        copyFile(fis, fos);
        fis.close();
        fos.close();
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }

    public static boolean unzip(File file, File outDir) throws IOException {
        if (!file.exists()) {
            return false;
        }
        if (!file.isFile()) {
            return false;
        }
        if (!outDir.exists()) {
            boolean mkResult = outDir.mkdirs();
            if (!mkResult) {
                return false;
            }
        }

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            int len = 0;
            byte[] bytes = new byte[1024];
            ZipEntry entry = null;
            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();

                File out = new File(outDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!out.exists()) {
                        out.mkdirs();
                    }
                } else {
                    InputStream in = zipFile.getInputStream(entry);
                    if (!out.exists()) {
                        out.getParentFile().mkdirs();
                    }
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out));

                    while ((len = in.read(bytes)) > 0) {
                        bos.write(bytes, 0, len);
                    }
                    bos.flush();
                    bos.close();
                    in.close();
                }
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
