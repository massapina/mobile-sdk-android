package com.jumio.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.jumio.core.enums.JumioDataCenter;
import com.jumio.core.exceptions.PlatformNotSupportedException;
import com.jumio.nv.*;

/**
 * Copyright 2017 Jumio Corporation All rights reserved.
 */
public class NetverifyFragment extends Fragment implements View.OnClickListener {
	private final static String TAG = "JumioSDK_Netverify";
	private static final int PERMISSION_REQUEST_CODE_NETVERIFY = 303;

	private String apiToken = null;
	private String apiSecret = null;
	Switch switchVerification;
	Switch switchFaceMatch;

	NetverifySDK netverifySDK;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		switchVerification = (Switch) rootView.findViewById(R.id.switchOptionOne);
		switchFaceMatch = (Switch) rootView.findViewById(R.id.switchOptionTwo);

		Bundle args = getArguments();

		switchVerification.setText(args.getString(MainActivity.KEY_SWITCH_ONE_TEXT));
		switchFaceMatch.setText(args.getString(MainActivity.KEY_SWITCH_TWO_TEXT));

		apiToken = args.getString(MainActivity.KEY_API_TOKEN);
		apiSecret = args.getString(MainActivity.KEY_API_SECRET);

		Button startSDK = (Button) rootView.findViewById(R.id.btnStart);
		startSDK.setText(args.getString(MainActivity.KEY_BUTTON_TEXT));
		startSDK.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onClick(View view) {
		//Since the NetverifySDK is a singleton internally, a new instance is not
		//created here.
		initializeNetverifySDK();
		((MainActivity) getActivity()).checkPermissionsAndStart(netverifySDK, PERMISSION_REQUEST_CODE_NETVERIFY);
	}

	private void initializeNetverifySDK() {
		try {
			// You can get the current SDK version using the method below.
			// NetverifySDK.getSDKVersion();

			// Call the method isSupportedPlatform to check if the device is supported.
			// netverifySDK.isSupportedPlatform();

			// Applications implementing the SDK shall not run on rooted devices. Use either the below
			// method or a self-devised check to prevent usage of SDK scanning functionality on rooted
			// devices.
			if (NetverifySDK.isRooted(getActivity()))
				Log.w(TAG, "Device is rooted");

			// To create an instance of the SDK, perform the following call as soon as your activity is initialized.
			// Make sure that your merchant API token and API secret are correct and specify an instance
			// of your activity. If your merchant account is created in the EU data center, use
			// JumioDataCenter.EU instead.
			netverifySDK = NetverifySDK.create(getActivity(), apiToken, apiSecret, JumioDataCenter.US);

			// Enable ID verification to receive a verification status and verified data positions (see Callback chapter).
			// Note: Not possible for accounts configured as Fastfill only.
			netverifySDK.setRequireVerification(switchVerification.isChecked());

			// You can specify issuing country (ISO 3166-1 alpha-3 country code) and/or ID types and/or document variant to skip
			// their selection during the scanning process.
			// Use the following method to convert ISO 3166-1 alpha-2 into alpha-3 country code.
			// String alpha3 = IsoCountryConverter.convertToAlpha3("AT");
			// netverifySDK.setPreselectedCountry("AUT");
			// ArrayList<NVDocumentType> documentTypes = new ArrayList<>();
			// documentTypes.add(NVDocumentType.PASSPORT);
			// netverifySDK.setPreselectedDocumentTypes(documentTypes);
			// netverifySDK.setPreselectedDocumentVariant(NVDocumentVariant.PLASTIC);

			// The merchant scan reference allows you to identify the scan (max. 100 characters).
			// Note: Must not contain sensitive data like PII (Personally Identifiable Information) or account login.
			// netverifySDK.setMerchantScanReference("YOURSCANREFERENCE");

			// Use the following property to identify the scan in your reports (max. 100 characters).
			// netverifySDK.setMerchantReportingCriteria("YOURREPORTINGCRITERIA");

			// You can also set a customer identifier (max. 100 characters).
			// Note: The customer ID should not contain sensitive data like PII (Personally Identifiable Information) or account login.
			// netverifySDK.setCustomerId("CUSTOMERID");

			// Callback URL for the confirmation after the verification is completed. This setting overrides your Jumio merchant settings.
			// netverifySDK.setCallbackUrl("YOURCALLBACKURL");

			// You can enable face match during the ID verification for a specific transaction.
			// netverifySDK.setRequireFaceMatch(switchFaceMatch.isChecked());

			// Use the following method to disable ePassport scanning.
			// netverifySDK.setEnableEpassport(false);

			// Use the following method to set the default camera position.
			// netverifySDK.setCameraPosition(JumioCameraPosition.FRONT);

			// Use the following method to only support IDs where data can be extracted on mobile only.
			// netverifySDK.setDataExtractionOnMobileOnly(true);

			// Additional information for this scan should not contain sensitive data like PII (Personally Identifiable Information) or account login
			// netverifySDK.setAdditionalInformation("YOURADDITIONALINFORMATION");

			// Use the following method to explicitly send debug-info to Jumio. (default: false)
			// Only set this property to true if you are asked by our Jumio support personnel.
			// netverifySDK.sendDebugInfoToJumio(true);

			// Use the following method to override the SDK theme that is defined in the Manifest with a custom Theme at runtime
			// netverifySDK.setCustomTheme(R.style.YOURCUSTOMTHEMEID);

			// Use the following method to initialize the SDK before displaying it
//			   netverifySDK.initiate(new NetverifyInitiateCallback() {
//			     @Override
//			     public void onNetverifyInitiateSuccess() {
//			     }
//			     @Override
//			     public void onNetverifyInitiateError(int errorCode, int errorDetail, String errorMessage, boolean retryPossible) {
//			     }
//			 });

		} catch (PlatformNotSupportedException e) {
			e.printStackTrace();
			Toast.makeText(getActivity().getApplicationContext(), "This platform is not supported", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == NetverifySDK.REQUEST_CODE) {
			if (data == null)
				return;
			if (resultCode == Activity.RESULT_OK) {
				String scanReference = (data == null) ? "" : data.getStringExtra(NetverifySDK.EXTRA_SCAN_REFERENCE);
				NetverifyDocumentData documentData = (data == null) ? null : (NetverifyDocumentData) data.getParcelableExtra(NetverifySDK.EXTRA_SCAN_DATA);
				NetverifyMrzData mrzData = documentData != null ? documentData.getMrzData() : null;
			} else if (resultCode == Activity.RESULT_CANCELED) {
				String errorMessage = data.getStringExtra(NetverifySDK.EXTRA_ERROR_MESSAGE);
				int errorCode = data.getIntExtra(NetverifySDK.EXTRA_ERROR_CODE, 0);
			}

			//At this point, the SDK is not needed anymore. It is highly advisable to call destroy(), so that
			//internal resources can be freed.
			if (netverifySDK != null) {
				netverifySDK.destroy();
				netverifySDK = null;
			}
		}
	}
}
