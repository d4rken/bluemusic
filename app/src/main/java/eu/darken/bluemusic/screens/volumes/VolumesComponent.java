package eu.darken.bluemusic.screens.volumes;

import dagger.Subcomponent;
import eu.darken.ommvplib.injection.PresenterComponent;
import eu.darken.ommvplib.injection.fragment.FragmentComponent;
import eu.darken.ommvplib.injection.fragment.FragmentComponentBuilder;


@VolumesScope
@Subcomponent(modules = {})
public interface VolumesComponent extends PresenterComponent<VolumesView, VolumesPresenter>, FragmentComponent<VolumesFragment> {
    @Subcomponent.Builder
    interface Builder extends FragmentComponentBuilder<VolumesFragment, VolumesComponent> {

    }
}
