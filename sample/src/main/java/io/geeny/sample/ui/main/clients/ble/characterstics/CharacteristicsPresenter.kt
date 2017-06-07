package io.geeny.sample.ui.main.clients.ble.characterstics

import android.bluetooth.BluetoothGattCharacteristic
import io.geeny.sample.ui.common.presenter.BasePresenter
import io.geeny.sample.ui.common.presenter.BaseView
import io.geeny.sdk.GeenySdk
import io.geeny.sdk.clients.ble.BleClient
import io.geeny.sdk.clients.ble.GattResult
import io.reactivex.Observable
import io.reactivex.Scheduler

class CharacteristicsPresenter(
        val address: String,
        private val characteristicId: String,
        val sdk: GeenySdk,
        val ioScheduler: Scheduler,
        val mainScheduler: Scheduler) : BasePresenter<CharacteristicView>() {

    fun load() {
        add(
                sdk.clients.ble.getClient(address)
                        .switchIfEmpty(Observable.error(IllegalStateException("Couldn't find mqttClient: $address")))
                        .subscribe(
                                {
                                    view?.onConnectionLoaded(it)
                                    connectToCharacteristic(it)
                                },
                                view?.showError()
                        )
        )
    }

    private fun connectToCharacteristic(connection: BleClient) {
        view?.onCharacteristicLoaded(connection.characteristicById(characteristicId), connection)
        connectToValue(connection)
    }

    private fun connectToValue(connection: BleClient) {
        add(connection.value(characteristicId)
                .observeOn(mainScheduler)
                .subscribe { view?.onValueChanged(it) })
    }
}

interface CharacteristicView : BaseView {
    fun onConnectionLoaded(connection: BleClient)
    fun onCharacteristicLoaded(characteristic: BluetoothGattCharacteristic, connection: BleClient)
    fun onValueChanged(result: GattResult)
}
