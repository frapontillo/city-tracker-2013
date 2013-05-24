package it.auh.citytracker.activity;

import java.text.DateFormat;
import java.util.Date;

import it.auh.citytracker.Consts;
import it.auh.citytracker.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

public class DetailActivity extends SherlockActivity {
	
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO: This leads to crashing, investigate
		/*
		getSupportMenuInflater().inflate(R.menu.detail, menu);
		
		MenuItem shareItem = menu.findItem(R.id.menu_share);
        ShareActionProvider actionProvider = (ShareActionProvider) shareItem.getActionProvider();
		actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		actionProvider.setShareIntent(createShareIntent());
		*/
		return true;
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
    
    /**
     * Creates a sharing {@link Intent} with the given text.
     *
     * @return The sharing intent.
     */
    private Intent createShareIntent() {    	
        String shareString = String.format(
        		getString(R.string.share_issue));
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);
        return shareIntent;
    }
}
