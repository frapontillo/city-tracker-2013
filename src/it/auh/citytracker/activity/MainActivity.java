package it.auh.citytracker.activity;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import it.auh.citytracker.CloudCallbackHandler;
import it.auh.citytracker.CloudEntity;
import it.auh.citytracker.Consts;
import it.auh.citytracker.R;
import it.auh.citytracker.CloudQuery.Order;
import it.auh.citytracker.CloudQuery.Scope;
import it.auh.citytracker.cloud.SherlockFragmentCloudBackendActivity;
import it.auh.citytracker.cloud.Tables;
import it.auh.citytracker.utils.StringUtils;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends SherlockFragmentCloudBackendActivity
		implements ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener, OnInfoWindowClickListener {

	private GoogleMap mMap;
	private LocationClient mLocationClient;
	private List<CloudEntity> mIssues;
	private boolean alreadyCentered;
	private HashMap<Marker, CloudEntity> mHashMap;

	// These settings are the same as the settings for the map. They will in
	// fact give you updates at
	// the maximal rates currently possible.
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
	private static final String ALREADY_CENTERED = "ALREADY_CENTERED";

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
		if (item.getItemId() == R.id.action_new_report) {
			double lat = getLatitude();
			double lon = getLongitude();
			if (lat+lon >= 0) {
				// Opens SendActivity
				Intent intent = new Intent(this, SendActivity.class);
				intent.putExtra(Consts.BUNDLE_LATITUDE, lat);
				intent.putExtra(Consts.BUNDLE_LONGITUDE, lon);
				startActivity(intent);
			} else {
				Toast.makeText(this, getString(R.string.wait_for_location),
						Toast.LENGTH_SHORT).show();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate() {
		super.onPostCreate();
		getAllIssues();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(ALREADY_CENTERED, alreadyCentered);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		alreadyCentered = savedInstanceState.getBoolean(ALREADY_CENTERED);
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
	
	private double getLatitude() {
		Location loc = mLocationClient.getLastLocation();
		if (loc != null) {
			return loc.getLatitude();
		}
		return -1;
	}
	
	private double getLongitude() {
		Location loc = mLocationClient.getLastLocation();
		if (loc != null) {
			return loc.getLongitude();
		}
		return -1;
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
		getCloudBackend().listByKind(Tables.Issue.NAME, CloudEntity.PROP_CREATED_AT,
				Order.DESC, 50, Scope.FUTURE_AND_PAST, handler);
	}

	/**
	 * Converts Issues into maps point, updating the UI
	 */
	private void updateIssuesOnMap() {
		mMap.clear();
		mMap.setOnInfoWindowClickListener(this);
		mHashMap = new HashMap<Marker, CloudEntity>();
		
		for (CloudEntity issue : mIssues) {
			BigDecimal lat = (BigDecimal) issue.get(Tables.Issue.LATITUDE);
			BigDecimal lon = (BigDecimal) issue.get(Tables.Issue.LONGITUDE);
			String desc = (String) issue.get(Tables.Issue.DESCRIPTION);
			Date date = issue.getCreatedAt();
			boolean high = (Boolean) issue.get(Tables.Issue.HIGH_PRIORITY);
			float color = high ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_AZURE;
			
			Marker mMarker = mMap.addMarker(new MarkerOptions()
		        .position(new LatLng(lat.doubleValue(), lon.doubleValue()))
		        .title(SimpleDateFormat.getDateInstance().format(date))
		        .snippet(StringUtils.ellipsis(desc, 0, 50))
		        .icon(BitmapDescriptorFactory.defaultMarker(color)));
			
			mHashMap.put(mMarker, issue);
		}
	}
	
	@Override
	public void onInfoWindowClick(Marker marker) {
		CloudEntity issue = mHashMap.get(marker);
		Intent i = new Intent(this, DetailActivity.class);
		i.putExtra(Consts.BUNDLE_USER, issue.getCreatedBy());
		i.putExtra(Consts.BUNDLE_DATE, issue.getCreatedAt().getTime());
		i.putExtra(Consts.BUNDLE_DESCRIPTION, (String)issue.get(Tables.Issue.DESCRIPTION));
		i.putExtra(Consts.BUNDLE_HIGH_PRIORITY, (Boolean)issue.get(Tables.Issue.HIGH_PRIORITY));
		i.putExtra(Consts.BUNDLE_IMAGE_URL, (String)issue.get(Tables.Issue.IMAGE_URL));
		startActivity(i);
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
	@SuppressWarnings("unused")
	private void showMyLocation() {
		if (mLocationClient != null && mLocationClient.isConnected()) {
			String msg = "Location = " + mLocationClient.getLastLocation();
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (!alreadyCentered) {
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(location.getLatitude(), location.getLongitude()), 18));
			alreadyCentered = true;
		}
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
