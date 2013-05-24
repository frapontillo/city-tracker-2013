package it.auh.citytracker.activity;

import it.auh.citytracker.Consts;
import it.auh.citytracker.R;
import it.auh.citytracker.data.requestmanager.CityTrackerRequestFactory;
import it.auh.citytracker.data.requestmanager.CityTrackerRequestManager;
import it.auh.citytracker.utils.StringUtils;
import it.auh.citytracker.utils.UriUtils;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;

public class SendActivity extends SherlockActivity implements
		RequestListener {

	private static final String TAG = null;
	
	private Button mSend;
	private EditText mDesc;
	private ToggleButton mHigh;
	private Button mAttach;
	private ImageView mImage;
	
	private RequestManager mRequestManager;
	private String description;
	private double latitude;
	private double longitude;
	private boolean highPriority;
	
	private Request r;
	private ProgressDialog mDialog;
	private AlertDialog mChooseDialog;
	private String path;
	private Bitmap bitmap;
	
	private static final String DIALOG_SHOWN = "DIALOG_SHOWN";
	private static final String PARCELABLE_REQUEST = "PARCELABLE_REQUEST";
	private static final String CHOOSE_DIALOG_SHOWN = "CHOOSE_DIALOG_SHOWN";
	private static final String IMAGE_PATH = "IMAGE_PATH";
	private static final int PICK_FROM_CAMERA = 1;
	private static final int PICK_FROM_FILE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_report);

		mSend = (Button) findViewById(R.id.button_send_report);
		mDesc = (EditText) findViewById(R.id.edit_problem);
		mHigh = (ToggleButton) findViewById(R.id.toggle_important);
		mAttach = (Button) findViewById(R.id.button_attach_image);
		mImage = (ImageView) findViewById(R.id.image_attachment);
		
		buildChooseImageDialog();
		
		mSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendReport();
			}
		});
		
		mAttach.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showChooseImage();
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
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	
        if (savedInstanceState != null) {
        	r = savedInstanceState.getParcelable(PARCELABLE_REQUEST);
        	// Show the dialog, if there has to be one
        	if (savedInstanceState.getBoolean(DIALOG_SHOWN))
        		showTheDialog();
        	// Show the list dialog, if there has to be one
        	if (savedInstanceState.getBoolean(CHOOSE_DIALOG_SHOWN))
        		showChooseImage();
			path = savedInstanceState.getString(IMAGE_PATH);
        }
        
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
    	
    	if (mChooseDialog != null && mChooseDialog.isShowing()) {
    		mChooseDialog.dismiss();
    		outState.putBoolean(CHOOSE_DIALOG_SHOWN, true);
    	} else {
    		outState.putBoolean(CHOOSE_DIALOG_SHOWN, false);
    	}
    	
    	outState.putString(IMAGE_PATH, path);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mRequestManager.removeRequestListener(this);
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
        // If the path is not null, we have an image to set
        if (!StringUtils.isEmpty(path)) {
			bitmap = BitmapFactory.decodeFile(path);
			mImage.setImageBitmap(bitmap);
			mImage.setVisibility(View.VISIBLE);
			mAttach.setVisibility(View.GONE);
        } else {
			mImage.setVisibility(View.GONE);
			mAttach.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Builds the choose image alert dialog.
     */
    private void buildChooseImageDialog() {
    	final String[] items = new String[] {
				getString(R.string.from_camera),
				getString(R.string.from_sd)
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Title
		builder.setTitle(getString(R.string.select_image));
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this, android.R.layout.select_dialog_item, items);
		
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
					startActivityForResult(Intent.createChooser(
							intent, getString(R.string.complete_using)), PICK_FROM_FILE);
				}
			}
		});

		mChooseDialog = builder.create();
    }
    
    /**
     * Shows the AlertDialog for the image selection
     */
	private void showChooseImage() {
		mChooseDialog.show();
	}
    


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		
		// From file
		if (requestCode == PICK_FROM_FILE) {
			Uri mImageCaptureUri = data.getData();
			// From Gallery
			path = UriUtils.getRealPathFromURI(mImageCaptureUri, this);
			// From File Manager
			if (path == null)
				path = mImageCaptureUri.getPath();

			if (path != null)
				bitmap = BitmapFactory.decodeFile(path);
		} else {
			// From camera
			Uri uri = data.getData();
			path = UriUtils.getRealPathFromURI(uri, this);
			bitmap = (Bitmap) data.getExtras().get("data");
		}

		mImage.setImageBitmap(bitmap);
		mAttach.setVisibility(View.GONE);
		mImage.setVisibility(View.VISIBLE);
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
