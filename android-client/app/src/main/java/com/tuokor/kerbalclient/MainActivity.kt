package com.tuokor.kerbalclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.cos

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val vm = ViewModelProvider(this).get(KerbalViewModel::class.java)
        vm.connect()
        joystickView.setOnMoveListener { angle, strength ->
            val pitchStrength = Math.sin(Math.toRadians(angle.toDouble())) * strength

            val yaw = Math.cos(Math.toRadians(angle.toDouble())) * strength
            Log.d("JOY","$angle $strength pitch: $pitchStrength yaw: $yaw")
            vm.sendControl(pitchStrength.toFloat().div(100), yaw.toFloat().div(100))

        }
    }
}
