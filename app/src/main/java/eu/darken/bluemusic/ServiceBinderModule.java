package eu.darken.bluemusic;

import android.app.Service;

import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.android.ServiceKey;
import dagger.multibindings.IntoMap;
import eu.darken.bluemusic.core.service.BlueMusicService;
import eu.darken.bluemusic.core.service.BlueMusicServiceComponent;

@Module(subcomponents = {BlueMusicServiceComponent.class})
abstract class ServiceBinderModule {

    @Binds
    @IntoMap
    @ServiceKey(BlueMusicService.class)
    abstract AndroidInjector.Factory<? extends Service> blueService(BlueMusicServiceComponent.Builder impl);
}