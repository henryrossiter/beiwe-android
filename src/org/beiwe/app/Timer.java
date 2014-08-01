package org.beiwe.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class Timer {
	Context appContext;
	BroadcastReceiver broadcastReceiver;
	AlarmManager alarmManager;
	BackgroundProcess backgroundProcess;
	
	//public strings for matching to messages
	//TODO: should we move these to the android Strings resource file?
	public static final String ACCELEROMETER_OFF = "Accelerometer_OFF";
	public static final String ACCELEROMETER_ON = "Accelerometer On";
	public static final String BLUETOOTH_OFF = "Bluetooth_OFF";
	public static final String BLUETOOTH_ON = "Bluetooth On";
	public static final String GPS_OFF = "GPS_OFF";
	public static final String GPS_ON = "GPS On";
	public static final String POWER_STATE_OFF = "PowerState_OFF";
	public static final String POWER_STATE_ON = "PowerState On";
	public static final String SIGN_OUT = "Signout";
	
	// control message intents
	private Intent signoutIntent = null;
	private Intent accelerometerOffIntent = null;
	private Intent accelerometerOnIntent = null;
	private Intent bluetoothOffIntent = null;
	private Intent bluetoothOnIntent = null;
	private Intent GPSOffIntent = null;
	private Intent GPSOnIntent = null;
	private Intent powerStateOffIntent = null;
	private Intent powerStateOnIntent = null;
	
	// Intent filters
	public IntentFilter getSignoutIntentFilter() { return new IntentFilter( signoutIntent.getAction() ); 	}
	public IntentFilter getAccelerometerOffIntentFilter() { return new IntentFilter( accelerometerOffIntent.getAction() ); }
	public IntentFilter getAccelerometerOnIntentFilter() { return new IntentFilter( accelerometerOnIntent.getAction() ); }
	public IntentFilter getBluetoothOffIntentFilter() { return new IntentFilter( bluetoothOffIntent.getAction() ); }
	public IntentFilter getBluetoothOnIntentFilter() { return new IntentFilter( bluetoothOnIntent.getAction() ); }
	public IntentFilter getGPSIntentOffFilter() { return new IntentFilter( GPSOffIntent.getAction() ); }
	public IntentFilter getGPSIntentOnFilter() { return new IntentFilter( GPSOnIntent.getAction() ); }
	public IntentFilter getPowerStateOffIntentFilter() { return new IntentFilter( powerStateOffIntent.getAction() ); }
	public IntentFilter getPowerStateOnIntentFilter() { return new IntentFilter( powerStateOnIntent.getAction() ); }
	
	// Intent getters
	public Intent getSignoutIntent() { return signoutIntent; }
	public Intent getAccelerometerOffIntent() { return accelerometerOffIntent; }
	public Intent getAccelerometerOnIntent() { return accelerometerOnIntent; }
	public Intent getBluetoothOffIntent() { return bluetoothOffIntent; }
	public Intent getBluetoothOnIntent() { return bluetoothOnIntent; }
	public Intent getGPSOffIntent() { return GPSOffIntent; }
	public Intent getGPSOnIntent() { return GPSOnIntent; }
	public Intent getPowerStateOffIntent() { return powerStateOffIntent; }
	public Intent getPowerStateOnIntent() { return powerStateOnIntent; }

	// Constructor
	public Timer(BackgroundProcess backgroundProcess) {
		this.backgroundProcess = backgroundProcess;
		this.appContext = backgroundProcess.getApplicationContext();
		
		// Setting up custom intents for the filters in the background service
		this.signoutIntent = setupCustomIntent(SIGN_OUT);
		this.accelerometerOffIntent = setupCustomIntent(ACCELEROMETER_OFF);
		this.accelerometerOnIntent = setupCustomIntent(ACCELEROMETER_ON);
		this.bluetoothOffIntent = setupCustomIntent(BLUETOOTH_OFF);
		this.bluetoothOnIntent = setupCustomIntent(BLUETOOTH_ON);
		this.GPSOffIntent = setupCustomIntent(GPS_OFF);
		this.GPSOnIntent = setupCustomIntent(GPS_ON);
		this.powerStateOffIntent = setupCustomIntent(POWER_STATE_OFF);
		this.powerStateOnIntent = setupCustomIntent(POWER_STATE_ON);
	}

	// Setup custom intents to be sent to the listeners running in the background process
	private Intent setupCustomIntent(String action){
		Intent newIntent = new Intent();
		newIntent.setAction(action);
		return newIntent; }

	/**Actions in Android are saved as values in locations. For example ACTION_CALL is actually saved as android.intent.action.CALL.
	 * When using an IntentFilter as an "action filter", we will use this convention, and start the timer using the action 
	 * "org.beiwe.app.START_TIMER". This will be registered in the background process, and will start the PendingIntent 
	 * (an intent waiting for something to call it) in the timer. */
	public void setupAlarm(int milliseconds, Intent customIntent, boolean repeating) {

		//create the intent, and then the pending intent from it		
		Intent eliIntentSteve = new Intent();
		eliIntentSteve.setAction("org.beiwe.app.steve"); // Eli's intent
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, eliIntentSteve, 0);
		//create and then register the broadcastreceiver with the background process
		broadcastReceiver = broadcastReceiverCreator(milliseconds, pendingIntent, customIntent, repeating);
		backgroundProcess.registerReceiver( broadcastReceiver, new IntentFilter( eliIntentSteve.getAction() ) );

		//and then start the alarm manager to actually trigger that intent it X seconds

		// These two lines actually wake up the broadcast receiver to check for incoming intents
		// http://developer.android.com/reference/android/app/AlarmManager.html#ELAPSED_REALTIME
		alarmManager = (AlarmManager)( backgroundProcess.getSystemService(Context.ALARM_SERVICE ));
		long nextTriggerTime = System.currentTimeMillis() + milliseconds;
		alarmManager.set( AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingIntent); 
		Log.i("alarm manager", "alarm started");
	}

	// TODO: make SetupNonRepeatingAlarm
	// TODO: make absoluteTimeAlarm (At 14:15, 15:15, etc...)

	public BroadcastReceiver broadcastReceiverCreator(final int milliseconds, final PendingIntent pendingIntent,
			final Intent customIntent, final boolean repeating) {		

		return new BroadcastReceiver() {
			@Override
			public void onReceive(Context appContext, Intent intent) {
				Toast.makeText(appContext, "Receive broadcast, reseting timer...", Toast.LENGTH_SHORT).show();
				backgroundProcess.unregisterReceiver(this);

				appContext.sendBroadcast( customIntent );
				if (repeating) {setupAlarm( milliseconds , customIntent, true); }	}
		}; }
}