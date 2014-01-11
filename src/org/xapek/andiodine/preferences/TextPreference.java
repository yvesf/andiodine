package org.xapek.andiodine.preferences;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class TextPreference extends AbstractPreference {
	public TextPreference(PreferenceActivity preferenceActivity, String title, int helpMsgId, String key) {
		super(preferenceActivity, title, helpMsgId, key);
	}

	@Override
	protected View getListItemView(Context context) {
		final EditText view = new EditText(context);
		view.setSingleLine();
		view.setText(getAsString());
		view.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				persist(s.toString());
			}
		});
		return view;
	}
}
