package com.example.fileextractorchaquopy;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;
import android.provider.DocumentsContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;

import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_DIRECTORY = 100;  //constant request code dunno if will use it
    private static String epubPath;
    private String destPath;
    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if (uri != null) {
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                String fileName = cursor.getString(index);
                cursor.close();

                File file = new File(getCacheDir(), fileName);
                filePath = file.getAbsolutePath();

                try {
                    InputStream inputStream = resolver.openInputStream(uri);
                    OutputStream outputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filePath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_DIRECTORY && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            String folderPath = getFilePathFromUri(uri);
            Log.d("file name", folderPath);
            epubPath = folderPath;
            Log.d("epub path", epubPath);
            Python py = Python.getInstance();
            PyObject epubParser = py.getModule("epubParser");
            if(epubPath != null && epubPath.toLowerCase().endsWith(".epub")){
                Log.d("file name", epubPath);
                PyObject getContent = epubParser.callAttr("parseEpub",epubPath,"/data/data/com.example.fileextractorchaquopy/files/");
                epubPath = null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!Python.isStarted()) {
            // Initialize Python with the Chaquopy SDK
            Python.start(new AndroidPlatform(this));
        }
        TextView fun = findViewById(R.id.textView2);
        Button inp1 = findViewById(R.id.button1);
        Button inp2 = findViewById(R.id.button2);
        Button copy = findViewById(R.id.copy);

        copy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast toast = Toast.makeText(getApplicationContext(), "copy underway", Toast.LENGTH_LONG);
                toast.show();
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_DIRECTORY);
            }
        });
    }

}