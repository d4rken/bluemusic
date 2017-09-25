package eu.darken.bluemusic.core.database;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import eu.darken.bluemusic.core.bluetooth.BluetoothSource;
import eu.darken.bluemusic.core.bluetooth.SourceDevice;
import eu.darken.bluemusic.core.service.StreamHelper;
import eu.darken.bluemusic.util.dagger.ApplicationScope;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;


@ApplicationScope
public class DeviceManager {

    private BluetoothSource bluetoothSource;
    private final StreamHelper streamHelper;
    private final BehaviorSubject<Map<String, ManagedDevice>> deviceObs = BehaviorSubject.create();

    @Inject
    public DeviceManager(BluetoothSource bluetoothSource, StreamHelper streamHelper) {
        this.bluetoothSource = bluetoothSource;
        this.streamHelper = streamHelper;
    }

    private Realm getRealm() {
        return Realm.getDefaultInstance();
    }

    @NonNull
    public Observable<Map<String, ManagedDevice>> observe() {
        Observable.defer(() -> loadDevices(true).toObservable())
                .subscribeOn(Schedulers.computation())
                .subscribe();
        return deviceObs;
    }

    public Single<Map<String, ManagedDevice>> loadDevices(boolean notify) {
        return Single.zip(
                bluetoothSource.getConnectedDevices(),
                bluetoothSource.getPairedDevices(),
                (active, paired) -> {
                    final Map<String, ManagedDevice> result = new HashMap<>();

                    Realm realm = getRealm();
                    final RealmResults<DeviceConfig> deviceConfigs = realm.where(DeviceConfig.class).findAll();

                    realm.beginTransaction();
                    for (DeviceConfig config : deviceConfigs) {
                        if (!paired.containsKey(config.address)) {
                            if (bluetoothSource.isEnabled()) {
                                Timber.i("Deleted stale config from %s", config.address);
                                config.deleteFromRealm();
                            }
                            continue;
                        }

                        ManagedDevice managed = new ManagedDevice(paired.get(config.address), realm.copyFromRealm(config));
                        managed.setMaxMusicVolume(streamHelper.getMaxVolume(streamHelper.getMusicId()));
                        managed.setMaxVoiceVolume(streamHelper.getMaxVolume(streamHelper.getVoiceId()));
                        managed.setActive(active.containsKey(managed.getAddress()));

                        if (active.containsKey(config.address)) config.lastConnected = System.currentTimeMillis();

                        Timber.v("Loaded: %s", managed);
                        result.put(managed.getAddress(), managed);
                    }

                    realm.commitTransaction();
                    realm.close();
                    return result;
                })
                .doOnError(throwable -> Timber.e(throwable, null))
                .doOnSuccess(stringManagedDeviceMap -> {
                    if (notify) deviceObs.onNext(stringManagedDeviceMap);
                });
    }

    public Observable<Collection<ManagedDevice>> update(Collection<ManagedDevice> managedDevice) {
        return Observable.just(managedDevice)
                .subscribeOn(Schedulers.computation())
                .map(devices -> {
                    Realm realm = getRealm();
                    realm.beginTransaction();
                    for (ManagedDevice device : devices) {
                        Timber.d("Updated device: %s", device);
                        realm.copyToRealmOrUpdate(device.getDeviceConfig());
                    }
                    realm.commitTransaction();
                    realm.close();
                    return devices;
                });
    }

    public void notifyChanged(Collection<ManagedDevice> updatedDevs) {
        Map<String, ManagedDevice> updatedMap = deviceObs.getValue();
        if (updatedMap == null) updatedMap = new HashMap<>();
        for (ManagedDevice d : updatedDevs) {
            updatedMap.put(d.getAddress(), d);
        }
        deviceObs.onNext(updatedMap);
    }

    public Single<ManagedDevice> addNewDevice(SourceDevice device) {
        return Single.zip(
                bluetoothSource.getConnectedDevices(),
                bluetoothSource.getPairedDevices(),
                (active, paired) -> {

                    if (!paired.containsKey(device.getAddress())) {
                        Timber.e("Device isn't paired device: %s", device);
                        throw new IllegalArgumentException();
                    }

                    try (Realm realm = getRealm()) {

                        realm.beginTransaction();

                        DeviceConfig config = realm.where(DeviceConfig.class).equalTo("address", device.getAddress()).findFirst();
                        if (config != null) {
                            Timber.e("Trying to add already known device: %s (%s)", device, config);
                            throw new IllegalArgumentException();
                        }

                        config = realm.createObject(DeviceConfig.class, device.getAddress());
                        config.musicVolume = streamHelper.getVolumePercentage(streamHelper.getMusicId());
                        config.voiceVolume = streamHelper.getVolumePercentage(streamHelper.getVoiceId());

                        if (active.containsKey(config.address)) config.lastConnected = System.currentTimeMillis();

                        ManagedDevice newDevice = new ManagedDevice(device, realm.copyFromRealm(config));
                        newDevice.setMaxMusicVolume(streamHelper.getMaxVolume(streamHelper.getMusicId()));
                        newDevice.setMaxVoiceVolume(streamHelper.getMaxVolume(streamHelper.getVoiceId()));
                        newDevice.setActive(active.containsKey(newDevice.getAddress()));
                        Timber.v("Loaded: %s", newDevice);
                        realm.commitTransaction();
                        return newDevice;
                    }
                })
                .doOnError(throwable -> Timber.e(throwable, null));
    }

    public Completable removeDevice(ManagedDevice device) {
        return Completable
                .create(e -> {
                    try (Realm realm = getRealm()) {
                        DeviceConfig config = realm.where(DeviceConfig.class).equalTo("address", device.getAddress()).findFirst();
                        if (config != null) {
                            realm.beginTransaction();
                            config.deleteFromRealm();
                            realm.commitTransaction();
                        }
                    } finally {
                        e.onComplete();
                    }
                    loadDevices(true).subscribe();
                });
    }
}
