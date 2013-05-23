package it.auh.citytracker.data.operation;

import java.io.IOException;

import it.auh.citytracker.CloudBackendMessaging;
import it.auh.citytracker.CloudEntity;
import it.auh.citytracker.Consts;
import it.auh.citytracker.cloud.Tables;
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
		
		Log.d(TAG, "PostReportOperation started");
		
		// TODO: aggiungere foto e ottenere link
		
		CloudEntity ce = new CloudEntity(Tables.Issue.NAME);
		ce.put(Tables.Issue.DESCRIPTION, desc);
		ce.put(Tables.Issue.LATITUDE, latitude);
		ce.put(Tables.Issue.LONGITUDE, longitude);
		ce.put(Tables.Issue.HIGH_PRIORITY, highPriority);
		
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

}
