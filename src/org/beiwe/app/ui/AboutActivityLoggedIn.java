package org.beiwe.app.ui;

import org.beiwe.app.R;
import org.beiwe.app.session.SessionActivity;

import android.os.Bundle;

/**The about page!
 * @author Everyone! */
public class AboutActivityLoggedIn extends SessionActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}
}