package it.auh.citytracker.cloud;

import it.auh.citytracker.CloudBackendMessaging;
import it.auh.citytracker.CloudCallbackHandler;
import it.auh.citytracker.CloudEntity;
import it.auh.citytracker.Consts;

import java.util.List;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

public class SherlockFragmentCloudBackendActivity extends
		SherlockFragmentActivity {
	private static final int REQUEST_ACCOUNT_PICKER = 2;

	private static final String PREF_KEY_ACCOUNT_NAME = "PREF_KEY_ACCOUNT_NAME";

	private GoogleAccountCredential credential;

	private CloudBackendMessaging cloudBackend;

	// is this app subscribed to the Cloud Message?
	private static boolean isSubscribedToBroadcastMessage = false;

	/**
	 * Returns {@link CloudBackendMessaging} instance for this activity.
	 * 
	 * @return {@link CloudBackendMessaging}
	 */
	protected CloudBackendMessaging getCloudBackend() {
		return cloudBackend;
	}

	/**
	 * Subclasses may override this to return false if the app need to disable
	 * authentication.
	 */
	public boolean isAuthEnabled() {
		return Consts.IS_AUTH_ENABLED;
	}

	/**
	 * Subclasses may override this to execute initialization of the activity.
	 * If it uses any CloudBackend features, it should be executed inside
	 * {@link #onPostCreate()} that will be called after CloudBackend
	 * initializations such as user authentication.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init backend
		cloudBackend = new CloudBackendMessaging(this);

		// create credential
		credential = GoogleAccountCredential.usingAudience(this,
				Consts.AUTH_AUDIENCE);
		cloudBackend.setCredential(credential);

		// if auth enabled, get account name from the shared pref
		if (isAuthEnabled()) {
			String accountName = cloudBackend.getSharedPreferences().getString(
					PREF_KEY_ACCOUNT_NAME, null);
			if (accountName == null) {
				// let user pick an account
				super.startActivityForResult(
						credential.newChooseAccountIntent(),
						REQUEST_ACCOUNT_PICKER);
				return; // continue init in onActivityResult
			} else {
				credential.setSelectedAccountName(accountName);
			}
		}

		// post create initialization
		_onPostCreate();
	}

	private void _onPostCreate() {

		// init subscription to broadcast message
		if (!isSubscribedToBroadcastMessage) {
			isSubscribedToBroadcastMessage = true;
			CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
				@Override
				public void onComplete(List<CloudEntity> results) {
					onBroadcastMessageReceived(results);
				}
			};
			cloudBackend.subscribeToCloudMessage(
					CloudBackendMessaging.TOPIC_ID_BROADCAST, handler);
		}

		// call onPostCreate
		this.onPostCreate();
	}

	/**
	 * Will be called after initialization process including the account
	 * picking. Subclasses may override to execute any initialization of the
	 * activity.
	 */
	protected void onPostCreate() {
	}

	/**
	 * Handles broadcast messages from the backend. Subclasses may override this
	 * to handle the message accordingly.
	 * 
	 * @param message
	 *            {@link CloudEntity} that contains the message values.
	 */
	public void onBroadcastMessageReceived(List<CloudEntity> message) {
	}

	/**
	 * Handles callback from Intents like authorization request or account
	 * picking.
	 */
	@Override
	protected final void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (data != null && data.getExtras() != null) {

				// set the picked account name to the credential
				String accountName = data
						.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				credential.setSelectedAccountName(accountName);

				// save account name to shared pref
				SharedPreferences.Editor e = cloudBackend
						.getSharedPreferences().edit();
				e.putString(PREF_KEY_ACCOUNT_NAME, accountName);
				e.commit();
			}

			// post create initialization
			_onPostCreate();
			break;
		}
	}

	/**
	 * Returns account name selected by user, or null if any account is
	 * selected.
	 */
	protected String getAccountName() {
		return credential == null ? null : credential.getSelectedAccountName();
	}
}
