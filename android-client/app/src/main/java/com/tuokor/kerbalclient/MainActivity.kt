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
        pitch_yaw_stick.setFixedCenter(false)
        pitch_yaw_stick.setOnMoveListener { angle, strength ->
            val pitchStrength = Math.sin(Math.toRadians(angle.toDouble())) * strength

            val yaw = Math.cos(Math.toRadians(angle.toDouble())) * strength
            Log.d("JOY","$angle $strength pitch: $pitchStrength yaw: $yaw")
            vm.pitchAndYawData.postValue(Pair(pitchStrength.toFloat().div(100),yaw.toFloat().div(100)))


        }
        roll_stick.setOnMoveListener { angle, strength ->
            Log.d("JOY2", "strength $strength angle $angle")
            val sign = if(angle == 180) -1 else 1
            val roll = strength.toFloat().div(100).times(sign)
            Log.d("JOY2", "roll $roll")
            vm.rollData.postValue(roll)
        }
    }
}
