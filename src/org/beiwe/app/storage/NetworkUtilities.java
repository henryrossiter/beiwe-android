package org.beiwe.app.storage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.HttpPost;
import org.beiwe.app.DeviceInfo;
import org.beiwe.app.R;
import org.beiwe.app.ui.AsyncPostSender;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class NetworkUtilities {

	private Context appContext;

	/**Upload must be initialized with an appContext before they can access the wifi state or upload a _file_.
	 * @param some applicationContext */
	public NetworkUtilities(Context applicationContext) {
		this.appContext = applicationContext;
	}

	
	//#######################################################################################
	//#############################  WIFI STATE #############################################
	//#######################################################################################
	
	/**Return TRUE if WiFi is connected; FALSE otherwise
	 * @return boolean value of whether the wifi is on and connected. */
	//TODO: Josh.  Can you update the comment above: does it return wifi is on or wifi is on + internet is available.  (this may require a bit of testing on a phone?)
	public Boolean getWifiState() {
		ConnectivityManager connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected(); 
	}


	//#######################################################################################
	//################################## FILES ##############################################
	//#######################################################################################

	/**Loop through all files on the phone, and for each one, try to upload it
	 * to the server. If upload is successful, delete the file's local copy. */
	public void uploadAllFiles() {

		// Run the HTTP POST on a separate, non-blocking thread
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Callable<HttpPost> thread = new Callable<HttpPost>() {
			@Override
			public HttpPost call() {
				String[] files = TextFileManager.getAllUploadableFiles();

				for (String fileName : files) {
					try {
						Log.i("Upload.java", "Trying to upload file: " + fileName);
						tryToUploadAndThenDeleteFile(fileName);
					}
					catch (Exception e) {
						Log.i("Upload.java", "Failed to upload file: " + fileName);
						e.printStackTrace();
					}
				}
				Log.i("Upload.java", "Finished upload loop");				

				return null;
			}
		};
		executor.submit(thread);
	}


	/**Try to upload a file to the server, and if successful, delete the local
	 * (on-phone) copy of the file to save space, keep security, and not have
	 * to upload it again
	 * @param filename the short name (not the full path) of the file to upload */
	private void tryToUploadAndThenDeleteFile(String filename) {
		//TODO: Josh! Only try to upload if the WiFi is connected
		if (tryToUploadFile(filename)) {
			TextFileManager.delete(filename);
		}
	}


	/**Try to upload a file to the server
	 * @param filename the short name (not the full path) of the file to upload
	 * @return TRUE if the server reported "200 OK"; FALSE otherwise */
	private Boolean tryToUploadFile(String filename) {
		try {
			// Get the filePath, and the file
			String filePath = appContext.getFilesDir() + "/" + filename;
			File file = new File(filePath);

			// Try to upload the file via a Multipart POST request
			URL uploadUrl = new URL(appContext.getResources().getString(R.string.data_upload_url));
			PostRequestFileUpload postRequest = new PostRequestFileUpload();
			if (postRequest.sendPostRequest(file, uploadUrl) == 200) {
				// If the request was successful (returned "200 OK"), return TRUE
				return true;
			}
			else {
				// If the request failed (returned something other than 200), return FALSE
				return false;
			}
		}
		catch (IOException e) {
			// If the upload failed for any reason, return FALSE
			Log.i("Upload", "Failed to upload file " + filename + ". Raised exception " + e.getCause());
			e.printStackTrace();
			return false;
		}
	}

	//#######################################################################################
	//############################### UTILITY FUNCTIONS #####################################
	//#######################################################################################

	public static void pushDataToServer(String userID, String password) {		
		StringBuilder deviceInfo = getDeviceInfoString();
		String url = "http://beiwe.org/userinfo";
		String param = "patientID=" + userID + "&pwd=" + password + deviceInfo.toString();

		new AsyncPostSender().execute(param, url);
	}

	
	public static int checkPasswordsIdentical(final String userID, final String password) {
		Integer response;
		/* TODO: Dori. Determine which IDs to send
		 * 1. Device ID + password
		 * 2. Device ID + password + userID
		 * 3. UserID + password
		 * 4. Password only
		 */
		Callable<Integer> thread = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				StringBuilder deviceInfo = getDeviceInfoString();
				String param = "patientID=" + userID + "&pwd=" + password + deviceInfo.toString();
				return PostRequestFileUpload.sendPostRequest(param, new URL("http://beiwe.org/checkpasswords"));
			}
		};

		try {
			response = thread.call();
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	
	//#######################################################################################
	//############################### CONVENIENCE FUNCTIONS #################################
	//#######################################################################################

	private static StringBuilder getDeviceInfoString() {
		StringBuilder stringBuilder = new StringBuilder();

		String droidID = DeviceInfo.getAndroidID();
		String bluetoothMAC = DeviceInfo.getBlootoothMAC();
		stringBuilder.append("&droidID=" + droidID + "&btID=" + bluetoothMAC);

		return stringBuilder;
	}
}