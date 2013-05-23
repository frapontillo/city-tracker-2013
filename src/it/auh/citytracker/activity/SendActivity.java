package it.auh.citytracker.activity;

import it.auh.citytracker.Consts;
import it.auh.citytracker.R;
import it.auh.citytracker.data.requestmanager.CityTrackerRequestFactory;
import it.auh.citytracker.data.requestmanager.CityTrackerRequestManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;

public class SendActivity extends SherlockFragmentActivity implements
		RequestListener {

	private static final String TAG = null;
	
	private Button mSend;
	private EditText mDesc;
	private CheckBox mHigh;
	
	private RequestManager mRequestManager;
	private String description;
	private double latitude;
	private double longitude;
	private boolean highPriority;
	
	private Request r;
	private ProgressDialog d;
	
	private static final String PARCELABLE_REQUEST = "PARCELABLE_REQUEST";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_report);

		mSend = (Button) findViewById(R.id.button_send_report);
		mDesc = (EditText) findViewById(R.id.edit_problem);
		mHigh = (CheckBox) findViewById(R.id.checkbox_high);
		
		mSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendReport();
			}
		});

		mRequestManager = CityTrackerRequestManager.from(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		Bundle b = getIntent().getExtras();
		latitude = b.getDouble(Consts.BUNDLE_LATITUDE);
		longitude = b.getDouble(Consts.BUNDLE_LONGITUDE);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(PARCELABLE_REQUEST, r);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		r = (Request) savedInstanceState.get(PARCELABLE_REQUEST);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (r != null) {
			showProgress();
		} else hideProgress();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (r != null) {
			hideProgress();
		}
	}
	
	private void showProgress() {
		if (d == null) d = new ProgressDialog(this);
		d.setCanceledOnTouchOutside(false);
		d.setIndeterminate(true);
		d.setTitle(getString(R.string.loading));
		d.setMessage(getString(R.string.loading_wait));
		d.show();
	}
	
	private void hideProgress() {
		if (d != null) {
			if (d.isShowing()) d.dismiss();
			d = null;
		}
	}
	
	private void sendReport() {
		r = CityTrackerRequestFactory.getPostRequest();
		description = mDesc.getText().toString();
		highPriority = mHigh.isChecked();
		r.put(Consts.BUNDLE_DESCRIPTION, description);
		r.put(Consts.BUNDLE_LATITUDE, latitude);
		r.put(Consts.BUNDLE_LONGITUDE, longitude);
		r.put(Consts.BUNDLE_HIGH_PRIORITY, highPriority);
		r.setMemoryCacheEnabled(true);
		mRequestManager.execute(r, this);
		
		showProgress();
	}

	@Override
	public void onRequestFinished(Request request, Bundle resultData) {
		// TODO Auto-generated method stub
		Log.i(TAG, "OK");
		hideProgress();
	}

	@Override
	public void onRequestConnectionError(Request request, int statusCode) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Connection error");
		hideProgress();
	}

	@Override
	public void onRequestDataError(Request request) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Data error");
		hideProgress();
	}

	@Override
	public void onRequestCustomError(Request request, Bundle resultData) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Custom error");
		hideProgress();
	}
}
