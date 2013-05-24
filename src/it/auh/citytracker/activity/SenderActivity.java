package com.example.citytracker2;

import java.io.File;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

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

		mImageView = (ImageView) findViewById(R.id.ImageView);

		final String[] items = new String[] { "From Camera", "From SD Card" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Select Image");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					try {
						Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (Exception e) {
						e.printStackTrace();
					}

					dialog.cancel();
				} else {
					Intent intent = new Intent();

					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);

					startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
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

	private class UploadImage extends AsyncTask<String, Integer, Boolean> {
		String upload_to;

		@Override
		protected Boolean doInBackground(String... params) {
			final String upload_to = "https://api.imgur.com/3/upload.json";
			final String yourClientId = "de100b2384f0419";
			// final String API_key =
			// "a0fc6cefe1b1c9c2b73f0f878ce234ce5374b6c9";

			// final String TAG = "Awais";

			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(upload_to);
			httpPost.setHeader("Authorization", "Client-ID " + yourClientId);

			try {
				final MultipartEntity entity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);

				entity.addPart("image", new FileBody(new File(params[0])));
				// entity.addPart("key", new StringBody(API_key));

				httpPost.setEntity(entity);

				final HttpResponse response = httpClient.execute(httpPost,
						localContext);

				final String response_string = EntityUtils.toString(response
						.getEntity());

				final JSONObject json = new JSONObject(response_string);

				JSONObject json2 = json.getJSONObject("data");

				// //qui c'è l'URL dell'immagine
				String imageURL = "http://i.imgur.com/" + json2.getString("id")
						+ ".jpg";

				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

	}

	protected void onProgressUpdate(Integer... progress) {
	}

	protected void onPostExecute(Long result) {

	}

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