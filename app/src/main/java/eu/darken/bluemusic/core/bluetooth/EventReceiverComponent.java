package eu.darken.bluemusic.core.bluetooth;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import dagger.Subcomponent;
import eu.darken.ommvplib.injection.broadcastreceiver.BroadcastReceiverComponent;

@EventReceiverComponent.Scope
@Subcomponent(modules = {
})
public interface EventReceiverComponent extends BroadcastReceiverComponent<BluetoothEventReceiver> {

    @Subcomponent.Builder
    abstract class Builder extends BroadcastReceiverComponent.Builder<BluetoothEventReceiver, EventReceiverComponent> {

    }

    @javax.inject.Scope
    @Retention(RetentionPolicy.RUNTIME)
    @interface Scope {
    }
}
