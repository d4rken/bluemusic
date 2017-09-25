package eu.darken.bluemusic.screens.volumes;

import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.BindView;
import eu.darken.bluemusic.R;
import eu.darken.bluemusic.core.database.ManagedDevice;
import eu.darken.bluemusic.util.ui.BasicAdapter;
import eu.darken.bluemusic.util.ui.BasicViewHolder;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.NORMAL;


class VolumesAdapter extends BasicAdapter<VolumesAdapter.ManagedDeviceVH, ManagedDevice> {
    private final Callback callback;

    VolumesAdapter(Callback callback) {
        this.callback = callback;
    }

    @Override
    public ManagedDeviceVH onCreateBaseViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return new ManagedDeviceVH(parent, callback);
    }

    interface Callback {
        void onMusicVolumeAdjusted(ManagedDevice device, float percentage);

        void onVoiceVolumeAdjusted(ManagedDevice device, float percentage);

        void onDelete(ManagedDevice device);
    }

    static class ManagedDeviceVH extends BasicViewHolder<ManagedDevice> {
        @BindView(R.id.name) TextView name;
        @BindView(R.id.caption) TextView caption;
        @BindView(R.id.overflow_icon) View menu;
        @BindView(R.id.music_seekbar) SeekBar musicSeekbar;
        @BindView(R.id.music_counter) TextView musicCounter;
        @BindView(R.id.voice_seekbar) SeekBar voiceSeekbar;
        @BindView(R.id.voice_counter) TextView voiceCounter;
        private Callback callback;

        public ManagedDeviceVH(@NonNull ViewGroup parent, Callback callback) {
            super(parent, R.layout.viewholder_managed_device);
            this.callback = callback;
        }

        @Override
        public void bind(@NonNull ManagedDevice item) {
            super.bind(item);
            name.setText(item.getName());
            name.setTypeface(null, item.isActive() ? BOLD : NORMAL);

            String timeString;
            if (item.getLastConnected() > 0) {
                timeString = DateUtils.getRelativeDateTimeString(
                        getContext(),
                        item.getLastConnected(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.WEEK_IN_MILLIS,
                        0
                ).toString();
            } else {
                timeString = getString(R.string.time_tag_never);
            }
            caption.setText(
                    item.isActive() ?
                            getString(R.string.state_connected) :
                            getString(R.string.last_seen_x, timeString)
            );

            menu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(getContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_managed_device_item, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopMenuListener());
                popup.show();
            });

            musicSeekbar.setMax(item.getMaxMusicVolume());
            musicSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    musicCounter.setText(String.valueOf(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    callback.onMusicVolumeAdjusted(item, (float) seekBar.getProgress() / seekBar.getMax());
                }
            });
            musicSeekbar.setProgress(item.getRealMusicVolume());

            voiceSeekbar.setMax(item.getMaxVoiceVolume());
            voiceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    voiceCounter.setText(String.valueOf(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    callback.onVoiceVolumeAdjusted(item, (float) seekBar.getProgress() / seekBar.getMax());
                }
            });
            voiceSeekbar.setProgress(item.getRealVoiceVolume());
        }

        class PopMenuListener implements PopupMenu.OnMenuItemClickListener {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.delete:
                        callback.onDelete(getItem());
                        return true;
                    default:
                        return false;
                }
            }
        }
    }
}
