package de.monks.test.textreceiver;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {
	
	private static final String TAG = "TextReceiver";
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		verifyStoragePermissions(this);
		
		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		
		Log.d(TAG, "got type: " + type);
		
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (sharedText != null) {
				Log.d(TAG, "got text: " + sharedText);
			}
			Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
			if (imageUri != null) {
				// Update UI to reflect image being shared
				Log.d(TAG, "got URI: " + imageUri);
				if ("application/zip".equals(type)) {
					Log.d(TAG, "it's a ZIP!");
					this.unzip(imageUri);
				}
			}
			
		}
	}
	
	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		
		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					activity,
					PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE
			);
		}
	}
	
	public void unzip(Uri uri) {
		
		ZipInputStream zis;
		try {
			Log.d(TAG, "got uri: " + uri.toString());
			Log.d(TAG, "got path: " + uri.getPath());
			File f = new File(new URI(uri.toString()));
			FileInputStream fis = new FileInputStream(f);
			zis = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;
			
			while ((ze = zis.getNextEntry()) != null) {
				Log.d(TAG, ze.getName());
				if (!ze.isDirectory()) {
					Log.d(TAG, "it's a FILE!");
					// buffered read
					ByteArrayOutputStream fout = new ByteArrayOutputStream();
					while ((count = zis.read(buffer)) != -1) {
						Log.d(TAG, "got "+count+" bytes!");
						fout.write(buffer, 0, count);
					}
					String res = new String(fout.toByteArray(), "UTF-8");
					Log.d(TAG, "String length: "+res.length());
					Log.d(TAG, res);
					CSVUtils.parse(res);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
