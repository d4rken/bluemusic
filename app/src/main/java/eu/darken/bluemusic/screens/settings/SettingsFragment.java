package eu.darken.bluemusic.screens.settings;

import android.os.Bundle;
import android.view.MenuItem;

import eu.darken.bluemusic.R;
import eu.darken.bluemusic.screens.MainActivity;
import eu.darken.ommvplib.injection.ComponentPresenterPreferenceFragment;
import timber.log.Timber;


public class SettingsFragment extends ComponentPresenterPreferenceFragment<SettingsPresenter.View, SettingsPresenter, SettingsComponent> implements SettingsPresenter.View {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public SettingsComponent createComponent() {
        SettingsComponent.Builder builder = getComponentBuilder(this);
        return builder.build();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
        findPreference("core.delay.systemfudge").setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                Timber.i("Delay: %d", Long.parseLong((String) newValue));
                return true;
            } catch (Exception e) {
                Timber.e(e, null);
                return false;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //noinspection ConstantConditions
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
