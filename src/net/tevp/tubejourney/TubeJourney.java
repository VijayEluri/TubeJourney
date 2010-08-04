package net.tevp.tubejourney;

import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Spinner;
import android.view.View;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;

import net.tevp.JourneyPlannerParser.*;
import net.tevp.postcode.*;

public class TubeJourney extends Activity implements PostcodeListener {
	public static final String TAG = "TubeJourney";

	private Spinner typeStart, typeDest;
	private EditText textStart, textDest;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		setContentView(R.layout.main);

		final LinkedHashMap<String,Pair<LocationType, String>> types = new LinkedHashMap<String,Pair<LocationType, String>>();

		types.put("Postcode", new Pair<LocationType, String>(LocationType.Postcode, null));
		types.put("Stop", new Pair<LocationType, String>(LocationType.Stop, null));
		types.put("Address", new Pair<LocationType, String>(LocationType.Address, null));
		types.put("Place of Interest", new Pair<LocationType, String>(LocationType.PlaceOfInterest, null));
		// FIXME: remove hard code
		types.put("Home", new Pair<LocationType, String>(LocationType.Postcode, "E3 4AE"));

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types.keySet().toArray(new String[1]));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		textStart = (EditText) findViewById(R.id.textStart);
		typeStart = (Spinner) findViewById(R.id.typeStart);
		typeStart.setAdapter(adapter);
		textDest = (EditText) findViewById(R.id.textDest);
		typeDest = (Spinner) findViewById(R.id.typeDest);
		typeDest.setAdapter(adapter);

		typeStart.setOnItemSelectedListener(new TypesSetter(types, textStart));
		typeDest.setOnItemSelectedListener(new TypesSetter(types, textDest));

		final TubeJourney self = this;
		final Button button = (Button) findViewById(R.id.btnDoJourney);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				clearText();
				LocationType start = types.get((String)typeStart.getSelectedItem()).first();
				LocationType dest = types.get((String)typeDest.getSelectedItem()).first();

				JourneyPlannerParser jpp = new JourneyPlannerParser(false);
				JourneyParameters jp = new JourneyParameters();
				jp.speed = Speed.fast;
				Log.d(TAG, "Doing TFL lookup");
				appendText(jp.when.toString()+"\n");
				JourneyQuery jq = jpp.doAsyncJourney(start.create(textStart.getText().toString()),dest.create(textDest.getText().toString()), jp);
				new TubeJourneyTask(self).execute(jq);
			 }
		 });

		if (inState!=null && inState.containsKey("tflOutput"))
		{
			TextView tv = (TextView) findViewById(R.id.textLog);
			tv.setText(inState.getString("tflOutput"));
		}
	}

	protected void appendText(final String text)
	{
		TextView tv = (TextView) findViewById(R.id.textLog);
		tv.setText(tv.getText().toString()+text);
	}

	protected void clearText()
	{
		TextView tv = (TextView) findViewById(R.id.textLog);
		tv.setText("");
	}

	public void postcodeChange(final String postcode)
	{
		Log.d(TAG, "Postcode change to "+postcode);
		appendText("Got postcode " + postcode + "\n");
		JourneyPlannerParser jpp = new JourneyPlannerParser(false);
		JourneyParameters jp = new JourneyParameters();
		//jp.when = new GregorianCalendar(2010, 5, 10, 0, 23).getTime();
		jp.speed = Speed.fast;
		Log.d(TAG, "Doing TFL lookup");
		JourneyQuery jq = jpp.doAsyncJourney(LocationType.Postcode.create(postcode),LocationType.Postcode.create("E3 4AE"), jp);
		new TubeJourneyTask(this).execute(jq);
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		TextView tv = (TextView) findViewById(R.id.textLog);
		outState.putString("tflOutput", tv.getText().toString());
	}
}
