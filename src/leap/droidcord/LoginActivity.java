package leap.droidcord;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// UI references.
	private EditText mApiUrlView;
	private EditText mCdnUrlView;
	private EditText mGatewayUrlView;
	private CheckBox mUseGatewayView;
	private EditText mTokenView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Set up the login form.
		mApiUrlView = (EditText) findViewById(R.id.api_url);
		mCdnUrlView = (EditText) findViewById(R.id.cdn_url);
		mUseGatewayView = (CheckBox) findViewById(R.id.use_gateway);
		mGatewayUrlView = (EditText) findViewById(R.id.gateway_url);
		mTokenView = (EditText) findViewById(R.id.token);

		Button mLoginButton = (Button) findViewById(R.id.login_button);
		mLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mApiUrlView.setError(null);
		mCdnUrlView.setError(null);
		mGatewayUrlView.setError(null);
		mTokenView.setError(null);

		// Store values at the time of the login attempt.
		String api_url = mApiUrlView.getText().toString();
		String cdn_url = mCdnUrlView.getText().toString();
		Boolean use_gateway = mUseGatewayView.isChecked();
		String gateway_url = mGatewayUrlView.getText().toString();
		String token = mTokenView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(api_url)) {
			mApiUrlView.setError(getString(R.string.error_field_required));
			focusView = mApiUrlView;
			cancel = true;
		}

		if (TextUtils.isEmpty(cdn_url)) {
			mCdnUrlView.setError(getString(R.string.error_field_required));
			focusView = mCdnUrlView;
			cancel = true;
		}

		if (use_gateway && TextUtils.isEmpty(gateway_url)) {
			mGatewayUrlView.setError(getString(R.string.error_field_required));
			focusView = mGatewayUrlView;
			cancel = true;
		}

		if (TextUtils.isEmpty(token)) {
			mTokenView.setError(getString(R.string.error_field_required));
			focusView = mTokenView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			mAuthTask = new UserLoginTask(api_url, cdn_url, use_gateway,
					gateway_url, token);
			mAuthTask.call();
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	public void showProgress(final boolean show) {
		this.setProgressBarVisibility(show);
		this.setProgressBarIndeterminate(show);
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask implements Callable<Void> {

		private final String mApiUrl;
		private final String mCdnUrl;
		private final Boolean mUseGateway;
		private final String mGatewayUrl;
		private final String mToken;
		private Boolean success = false;

		UserLoginTask(String api_url, String cdn_url, Boolean use_gateway,
				String gateway_url, String token) {
			mApiUrl = api_url;
			mCdnUrl = cdn_url;
			mUseGateway = use_gateway;
			mGatewayUrl = gateway_url;
			mToken = token;
		}

		@Override
		public Void call() {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			final Handler handler = new Handler(Looper.getMainLooper());

			executor.execute(new Runnable() {
				@Override
				public void run() {
					// TODO: attempt authentication against a network service.

					try {
						// Simulate network access.
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						success = false;
					}

					handler.post(new Runnable() {
						@Override
						public void run() {
							mAuthTask = null;
							showProgress(false);

							if (success) {
								finish();
							} else {
								mTokenView
										.setError(getString(R.string.error_incorrect_password));
								mTokenView.requestFocus();
							}
						}
					});
				}
			});
			return null;
		}

	}
}
