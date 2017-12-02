package eu.darken.bluemusic.screens.settings;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.view.KeyEvent;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import eu.darken.bluemusic.R;
import eu.darken.bluemusic.core.settings.Settings;
import eu.darken.bluemusic.screens.MainActivity;
import eu.darken.bluemusic.util.ApiHelper;
import eu.darken.bluemusic.util.Preconditions;
import eu.darken.ommvplib.injection.ComponentPresenterPreferenceFragment;


public class SettingsFragment extends ComponentPresenterPreferenceFragment<SettingsPresenter.View, SettingsPresenter, SettingsComponent> implements SettingsPresenter.View {
    @Inject Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        Preconditions.checkNotNull(actionBar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.label_settings);
        actionBar.setSubtitle(null);
        findPreference(Settings.PREFKEY_VISIBLE_ADJUSTMENTS).setVisible(!ApiHelper.hasOreo());
    }

    @Override
    public void updatePremiumState(boolean isPremiumVersion) {

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (Settings.PREFKEY_AUTOPLAY_KEYCODE.equals(preference.getKey())) {
            List<Pair<Integer, String>> pairs = new ArrayList<>();

            pairs.add(Pair.create(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, "Play-Pause"));
            pairs.add(Pair.create(KeyEvent.KEYCODE_MEDIA_PLAY, "Play"));
            pairs.add(Pair.create(KeyEvent.KEYCODE_MEDIA_NEXT, "Next"));


            final int currentKeyCode = settings.getAutoplayKeycode();
            int selected = 0;
            String[] labels = new String[pairs.size()];
            for (int i = 0; i < pairs.size(); i++) {
                labels[i] = pairs.get(i).second;
                if (pairs.get(i).first == currentKeyCode) selected = i;
            }
            new AlertDialog.Builder(getContext())
                    .setSingleChoiceItems(labels, selected, (dialogInterface, pos) -> {
                        settings.setAutoplayKeycode(pairs.get(pos).first);
                        dialogInterface.dismiss();
                    })
                    .show();
            return true;
        } else if ("core.bugreporting.enabled".equals(preference.getKey())) {
            preference.setSummary(((CheckBoxPreference) preference).isChecked() ? ":)" : ":(");
            return true;
        } else return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Class<? extends SettingsPresenter> getTypeClazz() {
        return SettingsPresenter.class;
    }
}
