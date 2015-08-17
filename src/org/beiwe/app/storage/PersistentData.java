package org.beiwe.app.storage;
import java.util.List;

import org.beiwe.app.JSONUtils;
import org.beiwe.app.R;
import org.beiwe.app.ui.utils.AlertsManager;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**A class for managing patient login sessions.
 * Uses SharedPreferences in order to save username-password combinations.
 * @author Dor Samet, Eli Jones, Josh Zagorsky */
public class PersistentData {
	public static String NULL_ID = "NULLID";
	private static final long MAX_LONG = 9223372036854775807L;

	private static int PRIVATE_MODE = 0;
	private static boolean isInitialized = false;

	// Private things that are encapsulated using functions in this class 
	private static SharedPreferences pref; 
	private static Editor editor;
	private static Context appContext;
	
	/**  Editor key-strings */
	private static final String PREF_NAME = "BeiwePref";
	private static final String KEY_ID = "uid";
	private static final String KEY_PASSWORD = "password";
	private static final String IS_REGISTERED = "IsRegistered";
	private static final String LOGIN_EXPIRATION = "loginExpirationTimestamp";
	private static final String PCP_PHONE_KEY = "primary_care";
	private static final String PASSWORD_RESET_NUMBER_KEY = "reset_number";

	private static final String ACCELEROMETER = "accelerometer";
	private static final String GPS = "gps";
	private static final String CALLS = "calls";
	private static final String TEXTS = "texts";
	private static final String WIFI = "wifi";
	private static final String BLUETOOTH = "bluetooth";
	private static final String POWER_STATE = "power_state";

	private static final String ACCELEROMETER_OFF_DURATION_SECONDS = "accelerometer_off_duration_seconds";
	private static final String ACCELEROMETER_ON_DURATION_SECONDS = "accelerometer_on_duration_seconds";
	private static final String BLUETOOTH_ON_DURATION_SECONDS = "bluetooth_on_duration_seconds";
	private static final String BLUETOOTH_TOTAL_DURATION_SECONDS = "bluetooth_total_duration_seconds";
	private static final String BLUETOOTH_GLOBAL_OFFSET_SECONDS = "bluetooth_global_offset_seconds";
	private static final String CHECK_FOR_NEW_SURVEYS_FREQUENCY_SECONDS = "check_for_new_surveys_frequency_seconds";
	private static final String CREATE_NEW_DATA_FILES_FREQUENCY_SECONDS = "create_new_data_files_frequency_seconds";
	private static final String GPS_OFF_DURATION_SECONDS = "gps_off_duration_seconds";
	private static final String GPS_ON_DURATION_SECONDS = "gps_on_duration_seconds";
	private static final String SECONDS_BEFORE_AUTO_LOGOUT = "seconds_before_auto_logout";
	private static final String UPLOAD_DATA_FILES_FREQUENCY_SECONDS = "upload_data_files_frequency_seconds";
	private static final String VOICE_RECORDING_MAX_TIME_LENGTH_SECONDS = "voice_recording_max_time_length_seconds";
	private static final String WIFI_LOG_FREQUENCY_SECONDS = "wifi_log_frequency_seconds";
	private static final String SURVEY_IDS = "survey_ids";

	/*#####################################################################################
	################################### Initializing ######################################
	#####################################################################################*/

	/**The publicly accessible initializing function for the LoginManager, initializes the internal variables.
	 * @param context */
	public static void initialize( Context context ) {
		if ( isInitialized ) { return; }
		appContext = context;
		pref = appContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE); //sets Shared Preferences private mode
		editor = pref.edit();
		editor.commit();
		isInitialized = true;
	} 

	/*#####################################################################################
	##################################### User State ######################################
	#####################################################################################*/

	/** Quick check for login. **/
	public static boolean isLoggedIn(){
		if (pref == null) Log.w("LoginManager", "FAILED AT ISLOGGEDIN");
		// If the current time is earlier than the expiration time, return TRUE; else FALSE
		return (System.currentTimeMillis() < pref.getLong(LOGIN_EXPIRATION, 0)); }

	/** Set the login session to expire a fixed amount of time in the future */
	public static void loginOrRefreshLogin() {
		editor.putLong(LOGIN_EXPIRATION, System.currentTimeMillis() + getMillisecondsBeforeAutoLogout());
		editor.commit(); }

	/** Set the login session to "expired" */
	public static void logout() {
		editor.putLong(LOGIN_EXPIRATION, 0);
		editor.commit(); }

	/**Getter for the IS_REGISTERED value.
	 * @param value */
	public static boolean isRegistered() { 
		if (pref == null) Log.w("LoginManager", "FAILED AT ISREGISTERED");
		return pref.getBoolean(IS_REGISTERED, false); }

	/**Setter for the IS_REGISTERED value.
	 * @param value */
	public static void setRegistered(boolean value) { 
		editor.putBoolean(IS_REGISTERED, value);
		editor.commit(); }

	/*######################################################################################
	##################################### Passwords ########################################
	######################################################################################*/

	/**Checks that an input matches valid password requirements. (this only checks length)
	 * Throws up an alert notifying the user if the password is not valid.
	 * @param input
	 * @param activity
	 * @return true or false based on password requirements.*/
	public static boolean passwordMeetsRequirements(String password, Activity currentActivity) {
		// If the password has too few characters, pop up an alert saying so
		int minPasswordLength = 1; // TODO: postproduction: set the minPasswordLength to something higher than 1
		if (password.length() < minPasswordLength) {
			String alertMessage = String.format(appContext.getString(R.string.password_too_short), minPasswordLength);
			AlertsManager.showAlert(alertMessage, currentActivity);
			return false;
		}
		return true;
	}

 	/**Takes an input string and returns a boolean value stating whether the input matches the current password.
	 * @param input
	 * @return */
	public static boolean checkPassword(String input){ return ( getPassword().equals( EncryptionEngine.safeHash(input) ) ); }

	/**Sets a password to a hash of the provided value.
	 * @param password */
	public static void setPassword(String password) {
		editor.putString(KEY_PASSWORD, EncryptionEngine.safeHash(password) );
		editor.commit();
	}

	
	/*#####################################################################################
	################################# Application State ###################################
	#####################################################################################*/

	public static boolean getAccelerometerEnabled(){ return pref.getBoolean(ACCELEROMETER, false); }
	public static boolean getGpsEnabled(){ return pref.getBoolean(GPS, false); }
	public static boolean getCallsEnabled(){ return pref.getBoolean(CALLS, false); }
	public static boolean getTextsEnabled(){ return pref.getBoolean(TEXTS, false); }
	public static boolean getWifiEnabled(){ return pref.getBoolean(WIFI, false); }
	public static boolean getBluetoothEnabled(){ return pref.getBoolean(BLUETOOTH, false); }
	public static boolean getPowerStateEnabled(){ return pref.getBoolean(POWER_STATE, false); }
	
	public static void setAccelerometerEnabled(boolean enabled) {
		editor.putBoolean(ACCELEROMETER, enabled);
		editor.commit(); }
	public static void setGpsEnabled(boolean enabled) {
		editor.putBoolean(GPS, enabled);
		editor.commit(); }
	public static void setCallsEnabled(boolean enabled) {
		editor.putBoolean(CALLS, enabled);
		editor.commit(); }
	public static void setTextsEnabled(boolean enabled) {
		editor.putBoolean(TEXTS, enabled);
		editor.commit(); }
	public static void setWifiEnabled(boolean enabled) {
		editor.putBoolean(WIFI, enabled);
		editor.commit(); }
	public static void setBluetoothEnabled(boolean enabled) {
		editor.putBoolean(BLUETOOTH, enabled);
		editor.commit(); }
	public static void setPowerStateEnabled(boolean enabled) {
		editor.putBoolean(POWER_STATE, enabled);
		editor.commit(); }
	
	/*#####################################################################################
	################################# Application State ###################################
	#####################################################################################*/

	// Default timings (only used if app doesn't download custom timings)
	private static final long DEFAULT_ACCELEROMETER_OFF_MINIMUM_DURATION = 10;
	private static final long DEFAULT_ACCELEROMETER_ON_DURATION = 10 * 60;
	private static final long DEFAULT_BLUETOOTH_ON_DURATION = 1 * 60;
	private static final long DEFAULT_BLUETOOTH_TOTAL_DURATION = 5 * 60;
	private static final long DEFAULT_BLUETOOTH_GLOBAL_OFFSET = 0 * 60;
	private static final long DEFAULT_CHECK_FOR_NEW_SURVEYS_PERIOD = 24 * 60 * 60;
	private static final long DEFAULT_CREATE_NEW_DATA_FILES_PERIOD = 15 * 60;
	private static final long DEFAULT_GPS_OFF_MINIMUM_DURATION = 5 * 60;
	private static final long DEFAULT_GPS_ON_DURATION = 5 * 60;
	private static final long DEFAULT_SECONDS_BEFORE_AUTO_LOGOUT = 5 * 60;
	private static final long DEFAULT_UPLOAD_DATA_FILES_PERIOD = 60;
	private static final long DEFAULT_VOICE_RECORDING_MAX_TIME_LENGTH = 4 * 60;
	private static final long DEFAULT_WIFI_LOG_FREQUENCY = 5 * 60;
	
	//FIXME: FEATURE. Eli/Josh. IMPLEMENT TOGGLES. Hook into these toggles during registration.
	public static long getAccelerometerOffDurationMilliseconds() { return 1000L * pref.getLong(ACCELEROMETER_OFF_DURATION_SECONDS, DEFAULT_ACCELEROMETER_OFF_MINIMUM_DURATION); }
	public static long getAccelerometerOnDurationMilliseconds() { return 1000L * pref.getLong(ACCELEROMETER_ON_DURATION_SECONDS, DEFAULT_ACCELEROMETER_ON_DURATION); }
	public static long getBluetoothOnDurationMilliseconds() { return 1000L * pref.getLong(BLUETOOTH_ON_DURATION_SECONDS, DEFAULT_BLUETOOTH_ON_DURATION); }
	public static long getBluetoothTotalDurationMilliseconds() { return 1000L * pref.getLong(BLUETOOTH_TOTAL_DURATION_SECONDS, DEFAULT_BLUETOOTH_TOTAL_DURATION); }
	public static long getBluetoothGlobalOffsetMilliseconds() { return 1000L * pref.getLong(BLUETOOTH_GLOBAL_OFFSET_SECONDS, DEFAULT_BLUETOOTH_GLOBAL_OFFSET); }
	public static long getCheckForNewSurveysFrequencyMilliseconds() { return 1000L * pref.getLong(CHECK_FOR_NEW_SURVEYS_FREQUENCY_SECONDS, DEFAULT_CHECK_FOR_NEW_SURVEYS_PERIOD); }
	public static long getCreateNewDataFilesFrequencyMilliseconds() { return 1000L * pref.getLong(CREATE_NEW_DATA_FILES_FREQUENCY_SECONDS, DEFAULT_CREATE_NEW_DATA_FILES_PERIOD); }
	public static long getGpsOffDurationMilliseconds() { return 1000L * pref.getLong(GPS_OFF_DURATION_SECONDS, DEFAULT_GPS_OFF_MINIMUM_DURATION); }
	public static long getGpsOnDurationMilliseconds() { return 1000L * pref.getLong(GPS_ON_DURATION_SECONDS, DEFAULT_GPS_ON_DURATION); }
	public static long getMillisecondsBeforeAutoLogout() { return 1000L * pref.getLong(SECONDS_BEFORE_AUTO_LOGOUT, DEFAULT_SECONDS_BEFORE_AUTO_LOGOUT); }
	public static long getUploadDataFilesFrequencyMilliseconds() { return 1000L * pref.getLong(UPLOAD_DATA_FILES_FREQUENCY_SECONDS, DEFAULT_UPLOAD_DATA_FILES_PERIOD); }
	public static long getVoiceRecordingMaxTimeLengthMilliseconds() { return 1000L * pref.getLong(VOICE_RECORDING_MAX_TIME_LENGTH_SECONDS, DEFAULT_VOICE_RECORDING_MAX_TIME_LENGTH); }
	public static long getWifiLogFrequencyMilliseconds() { return 1000L * pref.getLong(WIFI_LOG_FREQUENCY_SECONDS, DEFAULT_WIFI_LOG_FREQUENCY); }

	public static void setAccelerometerOffDurationSeconds(long seconds) {
		editor.putLong(ACCELEROMETER_OFF_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setAccelerometerOnDurationSeconds(long seconds) {
		editor.putLong(ACCELEROMETER_ON_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setBluetoothOnDurationSeconds(long seconds) {
		editor.putLong(BLUETOOTH_ON_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setBluetoothTotalDurationSeconds(long seconds) {
		editor.putLong(BLUETOOTH_TOTAL_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setBluetoothGlobalOffsetSeconds(long seconds) {
		editor.putLong(BLUETOOTH_GLOBAL_OFFSET_SECONDS, seconds);
		editor.commit(); }
	public static void setCheckForNewSurveysFrequencySeconds(long seconds) {
		editor.putLong(CHECK_FOR_NEW_SURVEYS_FREQUENCY_SECONDS, seconds);
		editor.commit(); }
	public static void setCreateNewDataFilesFrequencySeconds(long seconds) {
		editor.putLong(CREATE_NEW_DATA_FILES_FREQUENCY_SECONDS, seconds);
		editor.commit(); }
	public static void setGpsOffDurationSeconds(long seconds) {
		editor.putLong(GPS_OFF_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setGpsOnDurationSeconds(long seconds) {
		editor.putLong(GPS_ON_DURATION_SECONDS, seconds);
		editor.commit(); }
	public static void setSecondsBeforeAutoLogout(long seconds) {
		editor.putLong(SECONDS_BEFORE_AUTO_LOGOUT, seconds);
		editor.commit(); }
	public static void setUploadDataFilesFrequencySeconds(long seconds) {
		editor.putLong(UPLOAD_DATA_FILES_FREQUENCY_SECONDS, seconds);
		editor.commit(); }
	public static void setVoiceRecordingMaxTimeLengthSeconds(long seconds) {
		editor.putLong(VOICE_RECORDING_MAX_TIME_LENGTH_SECONDS, seconds);
		editor.commit(); }
	public static void setWifiLogFrequencySeconds(long seconds) {
		editor.putLong(WIFI_LOG_FREQUENCY_SECONDS, seconds);
		editor.commit(); }

	/*###########################################################################################
	################################### Text Strings ############################################
	###########################################################################################*/

	private static final String ABOUT_PAGE_TEXT_KEY = "about_page_text";
	private static final String CALL_CLINICIAN_BUTTON_TEXT_KEY = "call_clinician_button_text";
	private static final String CONSENT_FORM_TEXT_KEY = "consent_form_text";
	private static final String SURVEY_SUBMIT_SUCCESS_TOAST_TEXT_KEY = "survey_submit_success_toast_text";
	
	public static String getAboutPageText() {
		String defaultText = appContext.getString(R.string.default_about_page_text);
		return pref.getString(ABOUT_PAGE_TEXT_KEY, defaultText); }
	public static String getCallClinicianButtonText() {
		//TODO: Josh, got an NPE crash here when the app restarted on Debug Menu page; appContext was probably null
		String defaultText = appContext.getString(R.string.default_call_clinician_text);
		return pref.getString(CALL_CLINICIAN_BUTTON_TEXT_KEY, defaultText); }
	public static String getConsentFormText() {
		String defaultText = appContext.getString(R.string.default_consent_form_text);
		return pref.getString(CONSENT_FORM_TEXT_KEY, defaultText); }
	public static String getSurveySubmitSuccessToastText() {
		String defaultText = appContext.getString(R.string.default_survey_submit_success_message);
		return pref.getString(SURVEY_SUBMIT_SUCCESS_TOAST_TEXT_KEY, defaultText); }
	
	public static void setAboutPageText(String text) {
		editor.putString(ABOUT_PAGE_TEXT_KEY, text);
		editor.commit(); }
	public static void setCallClinicianButtonText(String text) {
		editor.putString(CALL_CLINICIAN_BUTTON_TEXT_KEY, text);
		editor.commit(); }
	public static void setConsentFormText(String text) {
		editor.putString(CONSENT_FORM_TEXT_KEY, text);
		editor.commit(); }
	public static void setSurveySubmitSuccessToastText(String text) {
		editor.putString(SURVEY_SUBMIT_SUCCESS_TOAST_TEXT_KEY, text);
		editor.commit(); }

	/*###########################################################################################
	################################### User Credentials ########################################
	###########################################################################################*/

	public static void setLoginCredentials( String userID, String password ) {
		if (editor == null) Log.e("LoginManager.java", "editor is null");
		editor.putString(KEY_ID, userID);
		setPassword(password);
		editor.commit();
	}

	public static String getPassword() { return pref.getString( KEY_PASSWORD, null ); }

	public static String getPatientID() { return pref.getString(KEY_ID, NULL_ID); }

	/*###########################################################################################
	#################################### Contact Numbers ########################################
	###########################################################################################*/

	public static String getPrimaryCareNumber() { return pref.getString(PCP_PHONE_KEY, ""); }
	public static void setPrimaryCareNumber( String phoneNumber) {
		editor.putString(PCP_PHONE_KEY, phoneNumber );
		editor.commit();
	}

	public static String getPasswordResetNumber() {
		return pref.getString(PASSWORD_RESET_NUMBER_KEY, "");
	}
	
	public static void setPasswordResetNumber( String phoneNumber ){
		editor.putString(PASSWORD_RESET_NUMBER_KEY, phoneNumber );
		editor.commit();
	}

	
	/*###########################################################################################
	###################################### Survey Info ##########################################
	###########################################################################################*/
	
	public static List<String> getSurveyIds() { return JSONUtils.jsonArrayToStringList(getSurveyIdsJsonArray()); }

	private static JSONArray getSurveyIdsJsonArray() {
		JSONArray jsonSurveyIdArray;
		String jsonString = pref.getString(SURVEY_IDS, "0");
		Log.d("persistant data", "getting ids: " + jsonString);
		if (jsonString == "0") { return new JSONArray(); } //return empty if the list is empty
		try { jsonSurveyIdArray = new JSONArray(jsonString); }
		catch (JSONException e) { throw new NullPointerException("getSurveyIds failed, json string was: " + jsonString ); }
		return jsonSurveyIdArray;
	}
		
	public static void addSurveyId(String surveyId) {
		List<String> list = JSONUtils.jsonArrayToStringList( getSurveyIdsJsonArray() );
		if ( !list.contains(surveyId) ) {
			list.add(surveyId);
			editor.putString(SURVEY_IDS, new JSONArray(list).toString() );
			editor.commit();
		}
		else { throw new NullPointerException("duplicate survey id added"); } //TODO: Eli/Josh.  I am unaware of how this code could ever possible run because we ensure uniqueness in the downloader.  thoughts?
	}
	
	private static void removeSurveyId(String surveyId) {
		List<String> list = JSONUtils.jsonArrayToStringList( getSurveyIdsJsonArray() );
		if ( list.contains(surveyId) ) {
			list.remove(surveyId);
			editor.putString(SURVEY_IDS, new JSONArray(list).toString() );
			editor.commit();
		}
		else { throw new NullPointerException("survey id does not exist"); } //TODO: Eli/Josh.  like in addSurveyId, I am unaware of how this code could ever possible run because we ensure uniqueness in the downloader.  thoughts?
	}
	
	public static String getSurveyTimes(String surveyId){ return pref.getString(surveyId + "-times", null); }
	public static String getSurveyContent(String surveyId){ return pref.getString(surveyId + "-content", null); }
	public static String getSurveyType(String surveyId){ return pref.getString(surveyId + "-type", null); }
	public static Boolean getSurveyNotificationState( String surveyId) { return pref.getBoolean(surveyId + "-notificationState", false ); }
	
	public static void createSurveyData(String surveyId, String content, String timings, String type){
		setSurveyContent(surveyId,  content);
		setSurveyTimes(surveyId, timings);
		setSurveyType(surveyId, type);
	}
	//individual setters
	public static void setSurveyContent(String surveyId, String content){
		editor.putString(surveyId + "-content", content);
		editor.commit(); }
	public static void setSurveyTimes(String surveyId, String times){
		editor.putString(surveyId + "-times", times);
		editor.commit(); }
	public static void setSurveyType(String surveyId, String type){
		editor.putString(surveyId + "-type", type);
		editor.commit(); }
	//survey state storage
	public static void setSurveyNotificationState(String surveyId, Boolean bool ) {
		editor.putBoolean(surveyId + "-notificationState", bool );
		editor.commit(); }
	
	public static void setMostRecentSurveyAlarmTime(String surveyId, long time) {
		editor.putLong(surveyId + "-prior_alarm", time);
		editor.commit(); }
	public static long getMostRecentSurveyAlarmTime(String surveyId) { return pref.getLong( surveyId + "-prior_alarm", MAX_LONG); }
	
	public static void deleteSurvey(String surveyId) {
		editor.remove(surveyId + "-content");
		editor.remove(surveyId + "-times");
		editor.remove(surveyId + "-type");
		editor.remove(surveyId + "-notificationState");
		editor.commit();
		removeSurveyId(surveyId);
	}
}