package it.auh.citytracker.activity;

import java.text.DateFormat;
import java.util.Date;

import it.auh.citytracker.Consts;
import it.auh.citytracker.R;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class DetailActivity extends SherlockFragmentActivity {
	
	private ImageView mImageViewImportant;
	private TextView mTextViewUser;
	private TextView mTextViewDate;
	private TextView mTextViewDescription;
	private LinearLayout mLayoutImage;
	private ImageView mImageViewImage;
	
	private String mUser;
	private long mDate;
	private String mDescription;
	private String mUrl;
	private boolean isImportant;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		setContentView(R.layout.activity_report);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		mImageViewImportant = (ImageView) findViewById(R.id.image_important);
		mTextViewUser = (TextView) findViewById(R.id.text_user);
		mTextViewDate = (TextView) findViewById(R.id.text_date);
		mTextViewDescription = (TextView) findViewById(R.id.text_description);
		mLayoutImage = (LinearLayout) findViewById(R.id.layout_image);
		mImageViewImage = (ImageView) findViewById(R.id.image_attachment);
		
		Bundle b = getIntent().getExtras();
		mUser = b.getString(Consts.BUNDLE_USER);
		mDate = b.getLong(Consts.BUNDLE_DATE);
		mDescription = b.getString(Consts.BUNDLE_DESCRIPTION);
		mUrl = b.getString(Consts.BUNDLE_URL);
		isImportant = b.getBoolean(Consts.BUNDLE_IMPORTANT);
		
		bindModelToView();
	}
	
	private void bindModelToView() {
		mImageViewImportant.setVisibility(isImportant ? View.VISIBLE : View.GONE);
		mTextViewUser.setText(mUser);
		mTextViewDate.setText(DateFormat.getDateInstance().format(new Date(mDate)));
		mTextViewDescription.setText(mDescription);
		if (mUrl != null && !mUrl.equals("")) {
			mLayoutImage.setVisibility(View.VISIBLE);
			// TODO: set image view URL
		} else {
			mLayoutImage.setVisibility(View.GONE);
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
}
