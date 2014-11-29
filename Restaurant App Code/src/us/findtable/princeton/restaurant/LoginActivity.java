package us.findtable.princeton.restaurant;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import us.findtable.princeton.restaurant.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	/** Keep track of the login task to ensure we can cancel it if requested. */
	private UserLoginTask mAuthTask = null;
	
	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;
	
	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	private static final int STATE_DEFAULT = 0;
	private static final int STATE_SIGN_IN = 1;
	private static final int STATE_IN_PROGRESS = 2;
	
	private static final int RC_SIGN_IN = 0;
	
	private static final int DIALOG_PLAY_SERVICES_ERROR = 0;
	
	private static final String SAVED_PROGRESS = "sign_in_progress";
	
	// GoogleApiClient wraps our service connection to Google Play services and
	// provides access to the users sign in state and Google's APIs.
	private GoogleApiClient mGoogleApiClient;
	
	// We use mSignInProgress to track whether user has clicked sign in.
	// mSignInProgress can be one of three values:
	//
	//       STATE_DEFAULT: The default state of the application before the user
	//                      has clicked 'sign in', or after they have clicked
	//                      'sign out'.  In this state we will not attempt to
	//                      resolve sign in errors and so will display our
	//                      Activity in a signed out state.
	//       STATE_SIGN_IN: This state indicates that the user has clicked 'sign
	//                      in', so resolve successive errors preventing sign in
	//                      until the user has successfully authorized an account
	//                      for our app.
	//   STATE_IN_PROGRESS: This state indicates that we have started an intent to
	//                      resolve an error, and so we should not start further
	//                      intents until the current intent completes.
	private int mSignInProgress;
	
	// Used to store the PendingIntent most recently returned by Google Play
	// services until the user clicks 'sign in'.
	private PendingIntent mSignInIntent;
	
	// Used to store the error code most recently returned by Google Play services
	// until the user clicks 'sign in'.
	private int mSignInError;
	
	private SignInButton mSignInButton;
	private Button mSignOutButton;
	private Button mRevokeButton;
	private TextView mStatus;
	private ListView mCirclesListView;
	private ArrayAdapter<String> mCirclesAdapter;
	private ArrayList<String> mCirclesList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		//String[] accounts = getAccountNames();
		// Set up the login form.
		mEmail = "";//accounts[0];//getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);
		
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});
		
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		try {
			//System.err.println(GoogleAuthUtil.getToken(this, accounts[0], "some scope"));
			updateToken(false);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if (savedInstanceState != null) {
			mSignInProgress = savedInstanceState
					.getInt(SAVED_PROGRESS, STATE_DEFAULT);
		}
	}
	
	/** Returns an array of account names stored on the device.
	 *  @return an array of account names stored on the device
	 */
	private String[] getAccountNames() {
		AccountManager mAccountManager = AccountManager.get(this);
		Account[] accounts = mAccountManager.getAccountsByType(
				GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		String[] names = new String[accounts.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = accounts[i].name;
		}
		return names;
	}
	
	/** Updates authentication token.
	 *  
	 *  @param invalidateToken
	 *  @return
	 */
	private String updateToken(boolean invalidateToken) {
		String authToken = "null";
		try {
			AccountManager am = AccountManager.get(getBaseContext());
			Account[] accounts = am.getAccountsByType("com.google");
			AccountManagerFuture<Bundle> accountManagerFuture;
			//if(this == null){ //this is used when calling from an interval thread
			//    accountManagerFuture = am.getAuthToken(accounts[0], "android", false, null, null);
			//} else {
			accountManagerFuture = am.getAuthToken(accounts[0], "android", null, this, null, null);
			//}
			Bundle authTokenBundle = accountManagerFuture.getResult();
			authToken = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN).toString();
			if(invalidateToken) {
				am.invalidateAuthToken("com.google", authToken);
				authToken = updateToken(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return authToken;
	}
	
	/** */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// "Recover lost password option in menu"
		super.onCreateOptionsMenu(menu);
		//getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	/** Attempts to sign in or register the account specified by the login form.
	 *  If there are form errors (invalid email, missing fields, etc.), the
	 *  errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}
		
		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);
		
		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		
		boolean cancel = false;
		View focusView = null;
		
		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}
		
		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}
		
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(MainActivity.pref);
			mAuthTask.execute((Void) null);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		
		super.onSaveInstanceState(savedInstanceState);
	}
	
	/** Shows the progress UI and hides the login form. */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);
			
			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginStatusView.setVisibility(show ? View.VISIBLE
							: View.GONE);
				}
			});
			
			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
			.alpha(show ? 0 : 1)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE
							: View.VISIBLE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	/** Represents an asynchronous login/registration task used to authenticate
	 *  the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        SharedPreferences prefs;

        UserLoginTask(SharedPreferences prefs) {
            this.prefs = prefs;
        }
        
		@Override
		protected Boolean doInBackground(Void... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://54.186.80.240/restaurant_login.php");
			HttpResponse response = null;
			HttpEntity entity = null;
			
			// Request parameters and other properties.
			List<NameValuePair> credentials = new ArrayList<NameValuePair>(2);
			
			credentials.add(new BasicNameValuePair("username", mEmail));
			credentials.add(new BasicNameValuePair("password", mPassword));
			
			try {
				httppost.setEntity(new UrlEncodedFormEntity(credentials, "UTF-8"));
			} catch (UnsupportedEncodingException e) { System.err.println("Parameter binding failed."); }
			
			try {
				response = httpclient.execute(httppost);
				entity = response.getEntity();
			}
			catch (Exception e) {
				System.err.println("POST request failed."); 
				AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
				builder.setTitle("Connection error")
				.setMessage("Unable to connect to server.")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//do things
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
			
			// Read in
			String s = null;
			try {
				InputStream in = entity.getContent();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[1000];
				int bytesRead;
				while((bytesRead = in.read(buf)) != -1)
					out.write(buf, 0, bytesRead);
				s = out.toString();
			}
			catch(Exception e) { System.err.println("Read-in from server failed."); }
			
			if (s != null) {
				// Parse
				boolean backslashFlag = false, parseErrorFlag = false;
				int prev = 0;
				String[] serverParams = new String[65];
				int paramIndex = 0;
				for (int i = 0; i < s.length(); i++) {
					if (paramIndex == 65) {
						parseErrorFlag = true;
						break;
					}
					if (s.charAt(i) == '\\') {
						backslashFlag = !backslashFlag;
						continue;
					}
					if (s.charAt(i) != '/' && backslashFlag)
						backslashFlag = !backslashFlag;
					if (s.charAt(i) == '/' && backslashFlag) {
						serverParams[paramIndex++] = s.substring(prev, i-1);
						prev = i+1;
					}
				}
				serverParams[paramIndex++] =  s.substring(prev, s.length());
				parseErrorFlag |= paramIndex < 64;
				
				System.err.println(parseErrorFlag);
				System.err.println(java.util.Arrays.toString(serverParams));
				
				// Write settings
				if (!parseErrorFlag) {
					SharedPreferences.Editor editor = MainActivity.pref.edit();
					
					editor.putString(MainActivity.context.getString(R.string.key_restaurant_id), serverParams[0]);
					editor.putString(MainActivity.context.getString(R.string.key_authentication_token), serverParams[1]);
					editor.putString(MainActivity.context.getString(R.string.key_restaurant_name), serverParams[2]);
					editor.putString(MainActivity.context.getString(R.string.key_restaurant_type), serverParams[3]);
					editor.putString(MainActivity.context.getString(R.string.key_restaurant_address), serverParams[4]);
					
					// TODO: add line for reservation link [3]
					// TODO: add line for waitlist [4]
					HashMap<String, String> map = new HashMap<String, String>();
					for (int i = 2; i <= 20; i++) {
						map.put("open_tables_X"+i, serverParams[ 5+i]); //  5--23
						map.put("open_tables_Y"+i, serverParams[24+i]); // 24--42
						map.put("open_tables_Z"+i, serverParams[43+i]); // 43--61
					}
					map.put("open_tables_BAR", serverParams[64]);
					
					for (int i = 0; i < SettingsFragment.NUM_TABLE_TYPE_MAX; i++)
						try {
							editor.remove(SettingsFragment.KEY_DISPLAY+i);
							editor.remove(SettingsFragment.KEY_TOT_CNT+i);
							editor.remove(SettingsFragment.KEY_SIZE+i);
							editor.remove(SettingsFragment.KEY_COUNTER+i);
							editor.remove(SettingsFragment.KEY_TYPE+i);
						}
						catch(Exception e) {
							break;
						}
					int tableId = 0;
					for (int i = 2; i <= 20; i++)
						if (Integer.parseInt(map.get("open_tables_X"+i)) > -2) {
							editor.putBoolean(SettingsFragment.KEY_DISPLAY+tableId,
									Integer.parseInt(map.get("open_tables_X"+i)) > -1);
							editor.putString(SettingsFragment.KEY_TOT_CNT+tableId,
									""+Integer.parseInt(map.get("open_tables_X"+i))/256);
							editor.putString(SettingsFragment.KEY_SIZE+tableId,
									""+i);
							editor.putInt(SettingsFragment.KEY_COUNTER+tableId,
									Integer.parseInt(map.get("open_tables_X"+i))%256);
							editor.putString(SettingsFragment.KEY_TYPE+tableId++,
									MainActivity.context.getString(R.string.table_type2));
						}
					for (int i = 2; i <= 20; i++)
						if (Integer.parseInt(map.get("open_tables_Y"+i)) > -2) {
							editor.putBoolean(SettingsFragment.KEY_DISPLAY+tableId,
									Integer.parseInt(map.get("open_tables_Y"+i)) > -1);
							editor.putString(SettingsFragment.KEY_TOT_CNT+tableId,
									""+Integer.parseInt(map.get("open_tables_Y"+i))/256);
							editor.putString(SettingsFragment.KEY_SIZE+tableId,
									""+i);
							editor.putInt(SettingsFragment.KEY_COUNTER+tableId,
									Integer.parseInt(map.get("open_tables_Y"+i))%256);
							editor.putString(SettingsFragment.KEY_TYPE+tableId++,
									MainActivity.context.getString(R.string.table_type0));
						}
					for (int i = 2; i <= 20; i++)
						if (Integer.parseInt(map.get("open_tables_Z"+i)) > -2) {
							editor.putBoolean(SettingsFragment.KEY_DISPLAY+tableId,
									Integer.parseInt(map.get("open_tables_Z"+i)) > -1);
							editor.putString(SettingsFragment.KEY_TOT_CNT+tableId,
									""+Integer.parseInt(map.get("open_tables_Z"+i))/256);
							editor.putString(SettingsFragment.KEY_SIZE+tableId,
									""+i);
							editor.putInt(SettingsFragment.KEY_COUNTER+tableId,
									Integer.parseInt(map.get("open_tables_Z"+i))%256);
							editor.putString(SettingsFragment.KEY_TYPE+tableId++,
									MainActivity.context.getString(R.string.table_type3));
						}
					if (Integer.parseInt(map.get("open_tables_BAR")) > -2) {
						editor.putBoolean(SettingsFragment.KEY_DISPLAY+tableId,
								Integer.parseInt(map.get("open_tables_BAR")) > -1);
						editor.putString(SettingsFragment.KEY_TOT_CNT+tableId,
								""+Integer.parseInt(map.get("open_tables_BAR"))/256);
						editor.putString(SettingsFragment.KEY_SIZE+tableId,
								"1");
						editor.putInt(SettingsFragment.KEY_COUNTER+tableId,
								Integer.parseInt(map.get("open_tables_BAR"))%256);
						editor.putString(SettingsFragment.KEY_TYPE+tableId++,
								MainActivity.context.getString(R.string.table_type1));
					}
					editor.putString(SettingsFragment.KEY_NUM_TABLE_TYPE,
							Integer.toString(tableId));
					
					editor.commit();
					new POST();
				}
				else {
					System.err.println("Login failed!");
					// TODO: Add in the code to restart the login process.
					// THIS IS IMPORTANT.
					return false;
				}
			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);
			
			if (success) {
				finish();
			} else {
				mPasswordView
				.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}
		
		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}