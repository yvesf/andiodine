package org.xapek.andiodine.preferences;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.xapek.andiodine.R;

import java.util.ArrayList;
import java.util.List;

public abstract class PreferenceActivity extends ListActivity {
    private ArrayList<AbstractPreference> mPreferences;

    private ContentValues mContentValues = new ContentValues();

    private static class DialogPreferenceAdapter extends ArrayAdapter<AbstractPreference> {

        private final LayoutInflater mInflater;

        private static class HelpOnClickListener implements OnClickListener {
            private final AbstractPreference p;

            public HelpOnClickListener(AbstractPreference p) {
                this.p = p;
            }

            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())//
                        .setTitle(p.getTitle())//
                        .setMessage(p.getMessage())//
                        .create().show();
            }
        }

        public DialogPreferenceAdapter(Context context, List<AbstractPreference> preferences) {
            super(context, -1, preferences);
            mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AbstractPreference item = getItem(position);
            View rowView = mInflater.inflate(R.layout.rowlayout, parent, false);
            LinearLayout content = (LinearLayout) rowView.findViewById(R.id.rowlayout_content);
            ImageButton helpButton = (ImageButton) rowView.findViewById(R.id.rowlayout_help);
            TextView title = (TextView) rowView.findViewById(R.id.rowlayout_title);

            content.addView(item.getListItemView(getContext()));
            helpButton.setOnClickListener(new HelpOnClickListener(item));
            title.setText(item.getTitle());

            return rowView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = new ArrayList<AbstractPreference>();

        setListAdapter(new DialogPreferenceAdapter(this, mPreferences));
    }

    public void setContentValues(final ContentValues contentValues) {
        mContentValues = contentValues;
    }

    public ContentValues getContentValues() {
        return mContentValues;
    }

    public void addPreference(String key, String title, int helpMsgId, String defaultValue) {
        if (mContentValues.get(key) == null)
            mContentValues.put(key, defaultValue);
        mPreferences.add(new TextPreference(this, title, helpMsgId, key));
    }

    public void addPreference(String key, String title, int helpMsgId, boolean defaultValue) {
        BooleanPreference preference = new BooleanPreference(this, title, helpMsgId, key);
        if (mContentValues.get(key) == null)
            preference.persist(defaultValue);
        mPreferences.add(preference);
    }

    public void addPreference(String key, String title, int helpMsgId, String[] values, String defaultValue) {
        if (mContentValues.get(key) == null)
            mContentValues.put(key, defaultValue);
        mPreferences.add(new SpinnerPreference(this, key, title, helpMsgId, values));
    }
}
