package it.auh.citytracker.data.operation;

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

import java.io.IOException;

import it.auh.citytracker.CloudBackendMessaging;
import it.auh.citytracker.CloudEntity;
import it.auh.citytracker.Consts;
import it.auh.citytracker.cloud.Tables;
import it.auh.citytracker.utils.StringUtils;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class PostReportOperation implements Operation {

	private static final String TAG = PostReportOperation.class.getSimpleName();

	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException, CustomRequestException {
		
		String desc = request.getString(Consts.BUNDLE_DESCRIPTION);
		double latitude = request.getDouble(Consts.BUNDLE_LATITUDE);
		double longitude = request.getDouble(Consts.BUNDLE_LONGITUDE);
		boolean highPriority = request.getBoolean(Consts.BUNDLE_HIGH_PRIORITY);
		String imageUri = request.getString(Consts.BUNDLE_IMAGE_LOCAL);
		String imageInternet = null;
		
		Log.d(TAG, "PostReportOperation started");
		
		if (!StringUtils.isEmpty(imageUri)) {
			imageInternet = uploadImage(imageUri);
		}
		
		CloudEntity ce = new CloudEntity(Tables.Issue.NAME);
		ce.put(Tables.Issue.DESCRIPTION, desc);
		ce.put(Tables.Issue.LATITUDE, latitude);
		ce.put(Tables.Issue.LONGITUDE, longitude);
		ce.put(Tables.Issue.HIGH_PRIORITY, highPriority);
		if (!StringUtils.isEmpty(imageInternet)) {
			ce.put(Tables.Issue.IMAGE_URL, highPriority);
		}
		
		CloudBackendMessaging backend = new CloudBackendMessaging(context);
		
		GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(
				context, Consts.AUTH_AUDIENCE);
		backend.setCredential(credential);
		
		if (Consts.IS_AUTH_ENABLED) {
			String accountName = backend.getSharedPreferences().getString(
					Consts.PREF_KEY_ACCOUNT_NAME, null);
			credential.setSelectedAccountName(accountName);
		}
		
		try {
			Log.d(TAG, "Inserting " + ce.toString());
			backend.insert(ce);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		
		Log.d(TAG, "PostReportOperation finished");
		
		return null;
	}
	
	private String uploadImage(String uri) {
		final String upload_to = "https://api.imgur.com/3/upload.json";
		final String yourClientId = "de100b2384f0419";

		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpPost httpPost = new HttpPost(upload_to);
		httpPost.setHeader("Authorization", "Client-ID " + yourClientId);

		try {
			final MultipartEntity entity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);

			entity.addPart("image", new FileBody(new File(uri)));
			httpPost.setEntity(entity);

			final HttpResponse response = httpClient.execute(httpPost, localContext);
			final String response_string = EntityUtils.toString(response .getEntity());
			final JSONObject json = new JSONObject(response_string);
			JSONObject json2 = json.getJSONObject("data");

			// Get the image URL
			String imageURL = "http://i.imgur.com/" + json2.getString("id") + ".jpg";

			return imageURL;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
