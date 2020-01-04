package com.tuokor.kerbalclient

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuokor.kerbal.api.Control
import com.tuokor.kerbal.api.KerbalAPIGrpc
import com.tuokor.kerbal.api.VesselState
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KerbalViewModel : ViewModel() {

    private var channel: ManagedChannel? = null
    private var requestStream : StreamObserver<Control>? = null
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

                }catch (e: Exception) {
                    Log.e(this.javaClass.simpleName, "error connecting", e)
                }
            }

        }
    }
    fun sendPitch(pitch: Float) {
        requestStream?.onNext(Control.newBuilder().setPitch(pitch).build())
    }
}