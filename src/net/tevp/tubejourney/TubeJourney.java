package net.tevp.tubejourney;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.util.Log;

import net.tevp.journeyplannerparser.*;
import net.tevp.postcode.*;

public class TubeJourney extends Activity implements PostcodeListener {
	public static final String TAG = "TubeJourney";

	private LocationChooser locationStart, locationDest;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		setContentView(R.layout.main);

		locationStart = (LocationChooser) findViewById(R.id.locationStart);
		locationDest = (LocationChooser) findViewById(R.id.locationDest);

		final TubeJourney self = this;
		final Button button = (Button) findViewById(R.id.btnDoJourney);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				clearText();
				LocationType start = locationStart.location();
				LocationType dest = locationDest.location();

				JourneyPlannerParser jpp = new JourneyPlannerParser(false);
				JourneyParameters jp = new JourneyParameters();
				jp.speed = Speed.fast;
				Log.d(TAG, "Doing TFL lookup");
				appendText(jp.when.toString()+"\n");
				JourneyQuery jq = jpp.doAsyncJourney(start.create(locationStart.text()),dest.create(locationDest.text()), jp);
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
