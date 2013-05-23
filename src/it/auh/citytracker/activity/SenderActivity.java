package it.auh.citytracker.activity;

import it.auh.citytracker.R;
import it.auh.citytracker.R.id;
import it.auh.citytracker.R.layout;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class SenderActivity extends Activity {

	private Uri mImageCaptureUri;
	private ImageView mImageView;
	private Bitmap bitmap;
	private String path = "";

	private static final int PICK_FROM_CAMERA = 1;
	private static final int PICK_FROM_FILE = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sender);

		mImageView = (ImageView) findViewById(R.id.imageView);

		final String[] items = new String[] { "From Camera", "From SD Card" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Select Image");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					/*
					 * Intent intent = new
					 * Intent(MediaStore.ACTION_IMAGE_CAPTURE); File file = new
					 * File(Environment.getExternalStorageDirectory(),
					 * "tmp_avatar_" +
					 * String.valueOf(System.currentTimeMillis()) + ".jpg");
					 * mImageCaptureUri = Uri.fromFile(file);
					 */
					try {
						Intent intent = new Intent(
								android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						startActivityForResult(intent, PICK_FROM_CAMERA);
						// intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
						// mImageCaptureUri);
						// intent.putExtra("return-data", true);

						// startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (Exception e) {
						e.printStackTrace();
					}

					dialog.cancel();
				} else {
					Intent intent = new Intent();

					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);

					startActivityForResult(Intent.createChooser(intent,
							"Complete action using"), PICK_FROM_FILE);
				}
			}
		});

		final AlertDialog dialog = builder.create();

		((Button) findViewById(R.id.buttonTakephoto))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.show();
					}
				});

		if (savedInstanceState != null) {
			path = savedInstanceState.getString("image_path");
			bitmap = BitmapFactory.decodeFile(path);
			mImageView.setImageBitmap(bitmap);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		// Bitmap bitmap = null;
		// path = "";

		if (requestCode == PICK_FROM_FILE) {
			mImageCaptureUri = data.getData();
			path = getRealPathFromURI(mImageCaptureUri); // from Gallery

			if (path == null)
				path = mImageCaptureUri.getPath(); // from File Manager

			if (path != null)
				bitmap = BitmapFactory.decodeFile(path);
		} else {// da camera
			Uri uri = data.getData();
			path = getRealPathFromURI(uri);
			bitmap = (Bitmap) data.getExtras().get("data");

		}

		mImageView.setImageBitmap(bitmap);

		new UploadImage().execute(path);
	}

	private class UploadImage extends AsyncTask<String, Integer, Long> {
		protected Long doInBackground(String... params) {
			return null;

		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(Long result) {
		}
	}

	/*
	 * private void uploadImage() throws IOException {
	 * 
	 * // Creates Byte Array from picture ByteArrayOutputStream baos = new
	 * ByteArrayOutputStream(); // bitmap.compress(Bitmap.CompressFormat.JPEG,
	 * 50, baos); // Not sure whether this should be jpeg or png, try both and
	 * see which works best URL url = new URL("http://api.imgur.com/2/upload");
	 * 
	 * //encodes picture with Base64 and inserts api key String data =
	 * URLEncoder.encode("image", "UTF-8") + "=" +
	 * URLEncoder.encode(Base64.encode(baos.toByteArray(),
	 * Base64.DEFAULT).toString(), "UTF-8"); data += "&" +
	 * URLEncoder.encode("key", "UTF-8") + "=" +
	 * URLEncoder.encode("7e6cf46ff0dc3080128d2f1f6ede839d8b7009cd", "UTF-8");
	 * 
	 * // opens connection and sends data URLConnection conn =
	 * url.openConnection(); conn.setDoOutput(true); OutputStreamWriter wr = new
	 * OutputStreamWriter(conn.getOutputStream()); wr.write(data); wr.flush();
	 * 
	 * BufferedReader in = new BufferedReader( new InputStreamReader(
	 * conn.getInputStream()));
	 * 
	 * String inputLine;
	 * 
	 * while ((inputLine = in.readLine()) != null) in.close(); }
	 */
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);

		if (cursor == null)
			return null;

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("image_path", path);
	}

}