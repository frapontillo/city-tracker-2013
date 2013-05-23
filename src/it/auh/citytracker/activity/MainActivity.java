package it.auh.citytracker.activity;

import java.io.IOException;
import java.util.List;

import it.auh.citytracker.CloudCallbackHandler;
import it.auh.citytracker.CloudEntity;
import it.auh.citytracker.R;
import it.auh.citytracker.CloudQuery.Order;
import it.auh.citytracker.CloudQuery.Scope;
import it.auh.citytracker.cloud.SherlockFragmentCloudBackendActivity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends SherlockFragmentCloudBackendActivity
		implements ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener {

	private GoogleMap mMap;
	private LocationClient mLocationClient;
	private List<CloudEntity> mIssues;

	// These settings are the same as the settings for the map. They will in
	// fact give you updates at
	// the maximal rates currently possible.
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO: open SendActivity
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate() {
		super.onPostCreate();
		getAllIssues();
	}

	@Override
	public void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	/**
	 * Executes "SELECT * FROM Issues ORDER BY _createdAt DESC LIMIT 50"
	 * This query will be re-executed when matching entity is updated.
	 */
	private void getAllIssues() {
		// Create a response handler that will receive the query result or an error
		CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
			@Override
			public void onComplete(List<CloudEntity> results) {
				mIssues = results;
				updateIssuesOnMap();
			}

			@Override
			public void onError(IOException exception) {
				handleEndpointException(exception);
			}
		};

		// execute the query with the handler
		getCloudBackend().listByKind("Issues_1", CloudEntity.PROP_CREATED_AT,
				Order.DESC, 50, Scope.FUTURE_AND_PAST, handler);
	}

	/**
	 * Converts Issues into maps point, updating the UI
	 */
	private void updateIssuesOnMap() {
		for (CloudEntity issue : mIssues) {
			// TODO: add marker on map
		}
	}

	private void handleEndpointException(IOException e) {
		Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				mMap.setMyLocationEnabled(true);
			}
		}
	}

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(this, this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	/**
	 * Gets the current Location as required, without needing to register a
	 * LocationListener.
	 */
	private void showMyLocation() {
		if (mLocationClient != null && mLocationClient.isConnected()) {
			String msg = "Location = " + mLocationClient.getLastLocation();
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	/**
	 * Callback called when connected to GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
	}

	/**
	 * Callback called when disconnected from GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onDisconnected() {
		// Do nothing
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Do nothing
	}

}
