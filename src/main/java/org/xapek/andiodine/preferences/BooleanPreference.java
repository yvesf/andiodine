package org.xapek.andiodine.preferences;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.xapek.andiodine.R;

public class BooleanPreference extends AbstractPreference {

    public BooleanPreference(PreferenceActivity preferenceActivity, String title, int helpMsgId, String key) {
        super(preferenceActivity, title, helpMsgId, key);
    }

    @Override
    protected View getListItemView(Context context) {
        CheckBox view = new CheckBox(context);
        view.setText(context.getString(R.string.enable) + " " + getTitle());
        Log.d(TAG, "Status: " + getTitle() + " = " + getAsBoolean());
        view.setChecked(getAsBoolean());
        view.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                persist(isChecked);
            }
        });
        return view;
    }

}
