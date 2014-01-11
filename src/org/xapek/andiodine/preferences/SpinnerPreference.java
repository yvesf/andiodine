package org.xapek.andiodine.preferences;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SpinnerPreference extends AbstractPreference {
	private final String[] mValues;

	public SpinnerPreference(PreferenceActivity preferenceActivity, String key, String title, int helpResId,
			String[] values) {
		super(preferenceActivity, title, helpResId, key);
		mValues = values;
	}

	@Override
	protected View getListItemView(Context context) {
		Spinner view = new Spinner(context);
		view.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mValues));

		int i = 0;
		for (final String value : mValues) {
			if (value != null && value.equals(getAsString())) {
				view.setSelection(i);
				break;
			}
			i++;
		}

		view.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String item = mValues[position];
				persist(item);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		return view;
	}
}
