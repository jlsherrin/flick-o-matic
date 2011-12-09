package edu.ualr.swe.flickomatic.tasks;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.auth.Permission;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;

import edu.ualr.swe.flickomatic.FlickOMaticActivity;
import edu.ualr.swe.flickomatic.FlickrHelper;

/**
 * Represents the task to start the oauth process.
 * 
 * @author yayu
 * 
 */
public class OAuthTask extends AsyncTask<Void, Integer, String> {

	private static final Logger logger = LoggerFactory.getLogger(OAuthTask.class);
	private static final Uri OAUTH_CALLBACK_URI = Uri.parse(FlickOMaticActivity.CALLBACK_SCHEME	+ "://oauth"); //$NON-NLS-1$

	/**
	 * The context.
	 */
	private Context mContext;

	/**
	 * The progress dialog before going to the browser.
	 */
	private ProgressDialog mProgressDialog;

	/**
	 * Constructor.
	 * 
	 * @param context
	 */
	public OAuthTask(Context context) {
		super();
		this.mContext = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = ProgressDialog.show(mContext,
				"", "Generating the authorization request..."); //$NON-NLS-1$ //$NON-NLS-2$
		mProgressDialog.setCanceledOnTouchOutside(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			//@Override
			public void onCancel(DialogInterface dlg) {
				OAuthTask.this.cancel(true);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected String doInBackground(Void... params) {
		try {
			Flickr f = FlickrHelper.getInstance().getFlickr();
			logger.debug("flickr");
			OAuthToken oauthToken = f.getOAuthInterface().getRequestToken(OAUTH_CALLBACK_URI.toString());
			logger.debug("requestTokenComplete");
			saveTokenSecret(oauthToken.getOauthTokenSecret());
			URL oauthUrl = f.getOAuthInterface().buildAuthenticationUrl(Permission.WRITE, oauthToken);
			logger.debug("requestAccessTokenComplete token=" + oauthToken.getOauthToken() + " secret= " + oauthToken.getOauthTokenSecret());
			saveToken(oauthToken.getOauthToken(),oauthToken.getOauthTokenSecret());
			return oauthUrl.toString();
		} catch (Exception e) {
			logger.error("Error to oauth", e); //$NON-NLS-1$
			return "error:" + e.getMessage(); //$NON-NLS-1$
		}
	}

	private void saveToken(String token,String tokenSecret) {
		logger.debug("request token: " + token); //$NON-NLS-1$
		logger.debug("request token secret: " + tokenSecret); //$NON-NLS-1$
		FlickOMaticActivity act = (FlickOMaticActivity) mContext;
		act.saveOAuthToken(null, null, token, tokenSecret);
		logger.debug("oauth token saved: {}", token); //$NON-NLS-1$
		logger.debug("oauth token secret saved: {}", tokenSecret); //$NON-NLS-1$		
	}

	/**
	 * Saves the oauth token secrent.
	 * 
	 * @param tokenSecret
	 */
	private void saveTokenSecret(String tokenSecret) {
//		logger.debug("request token: " + tokenSecret); //$NON-NLS-1$
//		FlickOMaticActivity act = (FlickOMaticActivity) mContext;
//		act.saveOAuthToken(null, null, null, tokenSecret);
//		logger.debug("oauth token secret saved: {}", tokenSecret); //$NON-NLS-1$
//		GetOAuthTokenTask task = new GetOAuthTokenTask(act);
//		task.execute();
		
		logger.debug("request token: " + tokenSecret); //$NON-NLS-1$
		FlickOMaticActivity act = (FlickOMaticActivity) mContext;
		act.saveOAuthToken(null, null, null, tokenSecret);
		logger.debug("oauth token secret saved: {}", tokenSecret); //$NON-NLS-1$
	}

	@Override
	protected void onPostExecute(String result) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		logger.debug("postExe result= " + result);
		if (result != null && !result.startsWith("error") ) { //$NON-NLS-1$
			logger.debug("success");
			mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result)));
			logger.debug("return from intent");
		} else {
			Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
		}
	}
}
