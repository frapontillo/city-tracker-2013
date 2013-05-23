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
import android.widget.Toast;

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
	private ProgressDialog mDialog;
	
	private static final String DIALOG_SHOWN = "DIALOG_SHOWN";
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);

    	if (mDialog != null && mDialog.isShowing()) {
        	// Dismiss the dialog, in order to avoid a memory leak
    		hideTheDialog();
        	// Adds the status to the outState Bundle
        	outState.putBoolean(DIALOG_SHOWN, true);
        	outState.putParcelable(PARCELABLE_REQUEST, r);
    	} else
    		outState.putBoolean(DIALOG_SHOWN, false);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mRequestManager.removeRequestListener(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
        	r = savedInstanceState.getParcelable(PARCELABLE_REQUEST);
        	// Show the dialog, if there has to be one
        	if (savedInstanceState.getBoolean(DIALOG_SHOWN))
        		showTheDialog();
        }
    }
	
    @Override
    protected void onResume() {
        super.onResume();
        // Add the listener so we can listen to a response
        if (r != null && mRequestManager.isRequestInProgress(r))
        	mRequestManager.addRequestListener(this, r);
        // If there is a dialog, then display it
        if (mDialog != null) {
        	mDialog.show();
        }
    }
    
    /**
     * Show the dialog with the current parameters.
     */
    private void showTheDialog() {

        // Dialog, not dismissable by the user
        mDialog = ProgressDialog.show(
        		this,
        		getString(R.string.loading),
        		getString(R.string.loading_wait),
        		true, false, null);
		Log.d(TAG, "Showing the dialog " + mDialog.hashCode());
    }

    /**
     * Hides the current dialog, if any.
     */
    private void hideTheDialog() {
		Log.d(TAG, "Hiding the dialog " + mDialog.hashCode());
    	if (mDialog != null) {
    		mDialog.dismiss();
    		mDialog = null;
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
		
		showTheDialog();
	}
	
	private void handleError() {
		Toast.makeText(this, getString(R.string.error_connection), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onRequestFinished(Request request, Bundle resultData) {
		Log.i(TAG, "OK");
		hideTheDialog();
		finish();
	}

	@Override
	public void onRequestConnectionError(Request request, int statusCode) {
		Log.e(TAG, "Connection error");
		hideTheDialog();
		handleError();
	}

	@Override
	public void onRequestDataError(Request request) {
		Log.e(TAG, "Data error");
		hideTheDialog();
		handleError();
	}

	@Override
	public void onRequestCustomError(Request request, Bundle resultData) {
		Log.e(TAG, "Custom error");
		hideTheDialog();
		handleError();
	}
}
