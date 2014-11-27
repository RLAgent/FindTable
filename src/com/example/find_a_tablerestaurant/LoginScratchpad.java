import us.findtable.princeton.restaurant.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;


	
	/** onConnected is called when our Activity successfully connects to Google
	 *  Play services.  onConnected indicates that an account was selected on the
	 *  device, that the selected account has granted any requested permissions to
	 *  our app and that we were able to establish a service connection to Google
	 *  Play services.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		// Reaching onConnected means we consider the user signed in.
		Log.i(TAG, "onConnected");
		
		// Update the user interface to reflect that the user is signed in.
		mSignInButton.setEnabled(false);
		mSignOutButton.setEnabled(true);
		mRevokeButton.setEnabled(true);
		
		// Retrieve some profile information to personalize our app for the user.
		Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
		
		mStatus.setText(String.format(
				getResources().getString(R.string.signed_in_as),
				currentUser.getDisplayName()));
		
		Plus.PeopleApi.loadVisible(mGoogleApiClient, null)
		.setResultCallback(this);
		
		// Indicate that the sign in process is complete.
		mSignInProgress = STATE_DEFAULT;
	}

	
	private GoogleApiClient buildGoogleApiClient() {
		// When we build the GoogleApiClient we specify where connected and
		// connection failed callbacks should be returned, which Google APIs our
		// app uses and which OAuth 2.0 scopes our app requests.
		return new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(Plus.API, null)
		.addScope(Plus.SCOPE_PLUS_LOGIN)
		.build();
	}
	
	/** onConnectionFailed is called when our Activity could not connect to Google
	 *  Play services.  onConnectionFailed indicates that the user needs to select
	 *  an account, grant permissions or resolve an error in order to sign in.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes might
		// be returned in onConnectionFailed.
		Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
				+ result.getErrorCode());
		
		if (mSignInProgress != STATE_IN_PROGRESS) {
			// We do not have an intent in progress so we should store the latest
			// error resolution intent for use when the sign in button is clicked.
			mSignInIntent = result.getResolution();
			mSignInError = result.getErrorCode();
			
			if (mSignInProgress == STATE_SIGN_IN) {
				// STATE_SIGN_IN indicates the user already clicked the sign in button
				// so we should continue processing errors until the user is signed in
				// or they click cancel.
				resolveSignInError();
			}
		}
		
		// In this sample we consider the user signed out whenever they do not have
		// a connection to Google Play services.
		onSignedOut();
	}
	
	/** Starts an appropriate intent or dialog for user interaction to resolve
	 *  the current error preventing the user from being signed in.  This could
	 *  be a dialog allowing the user to select an account, an activity allowing
	 *  the user to consent to the permissions being requested by your app, a
	 *  setting to enable device networking, etc.
	 */
	private void resolveSignInError() {
		if (mSignInIntent != null) {
			// We have an intent which will allow our user to sign in or
			// resolve an error.  For example if the user needs to
			// select an account to sign in with, or if they need to consent
			// to the permissions your app is requesting.
			
			try {
				// Send the pending intent that we stored on the most recent
				// OnConnectionFailed callback.  This will allow the user to
				// resolve the error currently preventing our connection to
				// Google Play services.  
				mSignInProgress = STATE_IN_PROGRESS;
				startIntentSenderForResult(mSignInIntent.getIntentSender(),
						RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				Log.i(TAG, "Sign in intent could not be sent: "
						+ e.getLocalizedMessage());
				// The intent was canceled before it was sent.  Attempt to connect to
				// get an updated ConnectionResult.
				mSignInProgress = STATE_SIGN_IN;
				mGoogleApiClient.connect();
			}
		} else {
			// Google Play services wasn't able to provide an intent for some
			// error types, so we show the default Google Play services error
			// dialog which may still start an intent on our behalf if the
			// user can resolve the issue.
			showDialog(DIALOG_PLAY_SERVICES_ERROR);
		}  
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		switch (requestCode) {
			case RC_SIGN_IN:
				if (resultCode == RESULT_OK) {
					// If the error resolution was successful we should continue
					// processing errors.
					mSignInProgress = STATE_SIGN_IN;
				} else {
					// If the error resolution was not successful or the user canceled,
					// we should stop processing errors.
					mSignInProgress = STATE_DEFAULT;
				}
				
				if (!mGoogleApiClient.isConnecting()) {
					// If Google Play services resolved the issue with a dialog then
					// onStart is not called so we need to re-attempt connection here.
					mGoogleApiClient.connect();
				}
				break;
		}
	}
	
	@Override
	public void onResult(LoadPeopleResult peopleData) {
		if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
			mCirclesList.clear();
			PersonBuffer personBuffer = peopleData.getPersonBuffer();
			try {
				int count = personBuffer.getCount();
				for (int i = 0; i < count; i++) {
					mCirclesList.add(personBuffer.get(i).getDisplayName());
				}
			} finally {
				personBuffer.close();
			}
			
			mCirclesAdapter.notifyDataSetChanged();
		} else {
			Log.e(TAG, "Error requesting visible circles: " + peopleData.getStatus());
		}
	}
	
	private void onSignedOut() {
		// Update the UI to reflect that the user is signed out.
		mSignInButton.setEnabled(true);
		mSignOutButton.setEnabled(false);
		mRevokeButton.setEnabled(false);
		
		mStatus.setText(R.string.status_signed_out);
		
		mCirclesList.clear();
		mCirclesAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason.
		// We call connect() to attempt to re-establish the connection or get a
		// ConnectionResult that we can attempt to resolve.
		mGoogleApiClient.connect();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DIALOG_PLAY_SERVICES_ERROR:
				if (GooglePlayServicesUtil.isUserRecoverableError(mSignInError)) {
					return GooglePlayServicesUtil.getErrorDialog(
							mSignInError,
							this,
							RC_SIGN_IN, 
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									Log.e(TAG, "Google Play services resolution cancelled");
									mSignInProgress = STATE_DEFAULT;
									mStatus.setText(R.string.status_signed_out);
								}
							});
				} else {
					return new AlertDialog.Builder(this)
					.setMessage(R.string.play_services_error)
					.setPositiveButton(R.string.close,
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.e(TAG, "Google Play services error could not be "
									+ "resolved: " + mSignInError);
							mSignInProgress = STATE_DEFAULT;
							mStatus.setText(R.string.status_signed_out);
						}
					}).create();
				}
			default:
				return super.onCreateDialog(id);
		}
	}