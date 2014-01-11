package org.xapek.andiodine.preferences;

import android.content.Context;
import android.util.Log;
import android.view.View;

abstract class AbstractPreference {
    public static final String TAG = "Preference";
    private final String mKey;
    private final PreferenceActivity mPreferenceActivity;
    private final String mTitle;
    private final int mHelpMsgId;

    public AbstractPreference(PreferenceActivity preferenceActivity, String title, int helpResId, String key) {
        mPreferenceActivity = preferenceActivity;
        mTitle = title;
        mHelpMsgId = helpResId;
        mKey = key;
    }

    protected abstract View getListItemView(Context context);

    public void persist(final String value) {
        Log.d(TAG, String.format("persist String %s -> %s", mKey, value));
        mPreferenceActivity.getContentValues().put(mKey, value);
    }

    public void persist(final boolean value) {
        Log.d(TAG, String.format("persist boolean %s -> %s", mKey, "" + value));
        mPreferenceActivity.getContentValues().put(mKey, value ? 1 : 0);
    }

    public boolean getAsBoolean() {
        return mPreferenceActivity.getContentValues().getAsInteger(mKey) != null
                && mPreferenceActivity.getContentValues().getAsInteger(mKey) == 1;
    }

    public String getAsString() {
        return mPreferenceActivity.getContentValues().getAsString(mKey);
    }

    public String getTitle() {
        return mTitle;
    }

    public String getMessage() {
        return mPreferenceActivity.getResources().getString(mHelpMsgId);
    }
}
