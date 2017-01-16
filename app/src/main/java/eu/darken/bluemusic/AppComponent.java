package eu.darken.bluemusic;

import dagger.Component;
import eu.darken.bluemusic.core.BlueMusicServiceComponent;
import eu.darken.bluemusic.core.bluetooth.DeviceSourceModule;
import eu.darken.bluemusic.core.database.ManagedDeviceRepoModule;
import eu.darken.bluemusic.screens.volumes.VolumesComponent;
import eu.darken.bluemusic.util.AndroidModule;
import eu.darken.bluemusic.util.dagger.ApplicationScope;


@ApplicationScope
@Component(modules = {
        AndroidModule.class,
        DeviceSourceModule.class,
        ManagedDeviceRepoModule.class
})
public interface AppComponent {

    BlueMusicServiceComponent blueMusicServiceComponent();

    VolumesComponent volumeManagerComponent();
}
