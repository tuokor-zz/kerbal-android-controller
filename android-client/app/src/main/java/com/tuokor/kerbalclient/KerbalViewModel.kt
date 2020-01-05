package com.tuokor.kerbalclient

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuokor.kerbal.api.Control
import com.tuokor.kerbal.api.KerbalAPIGrpc
import com.tuokor.kerbal.api.VesselState
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KerbalViewModel : ViewModel() {

    private var channel: ManagedChannel? = null
    private var requestStream : StreamObserver<Control>? = null
    private var intervalChannel : ReceiveChannel<Unit>? = null

    val pitchAndYawData : MutableLiveData<Pair<Float,Float>> = MutableLiveData<Pair<Float,Float>>()
    val rollData : MutableLiveData<Float> = MutableLiveData<Float>()
    fun connect() {
        viewModelScope.launch {


            withContext(Dispatchers.IO) {
                try {
                    channel = ManagedChannelBuilder.forAddress("192.168.1.111", 50051).usePlaintext().build()
                    val stub = KerbalAPIGrpc.newStub(channel)
                    requestStream = stub.vessel(object : StreamObserver<VesselState> {
                        override fun onNext(value: VesselState?) {
                            Log.d("VIEWMODEL", "received pitch: ${value?.pitch}")
                        }

                        override fun onError(t: Throwable?) {
                            Log.e("VIEWMODEL", "response stream error:", t)
                        }

                        override fun onCompleted() {
                            requestStream?.onCompleted()
                        }
                    })
                    startTicker()

                }catch (e: Exception) {
                    Log.e(this.javaClass.simpleName, "error connecting", e)
                }
            }

        }
    }
    private suspend fun startTicker() {
        viewModelScope.launch {
            val channel = ticker(100)
            intervalChannel = channel
            withContext(Dispatchers.IO) {
                for(unit in channel) {
                    val pitchYaw = pitchAndYawData.value
                    val roll = rollData.value
                    sendControl(pitchYaw?.first, pitchYaw?.second, roll)
                }
            }

        }

    }

    private fun sendControl(pitch: Float?, yaw: Float?, roll: Float?) {
        val control = Control.newBuilder()
        pitch?.let {
            control.setPitch(it)
        }
        yaw?.let {
            control.setYaw(it)
        }
        roll?.let {
            control.setRoll(it)
        }
        requestStream?.onNext(
            control.build()
        )
    }

    override fun onCleared() {
        super.onCleared()
        intervalChannel?.cancel()
    }
}