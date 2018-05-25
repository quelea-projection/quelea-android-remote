package org.quelea.mobileremote.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import org.quelea.mobileremote.R;

public class AboutActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView aboutText = findViewById(R.id.aboutText);
		aboutText.setText(String.format("%s\n\n%s\n\n%s\n\n%s", getResources().getString(R.string.msg_about_text_app), getResources().getString(R.string.msg_about_text_support), getResources().getString(R.string.msg_about_text_responsibility), getResources().getString(R.string.msg_about_source_code)));
		Linkify.addLinks(aboutText, Linkify.ALL);		
		aboutText.setMovementMethod(LinkMovementMethod.getInstance());
	    
	}

}
