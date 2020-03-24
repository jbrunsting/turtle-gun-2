package com.example.myapplication

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

const val FPS = 60;

class MainActivity : AppCompatActivity() {
    var s_width = 0
    var s_height = 0

    private fun test() {
        helloWorld.x = helloWorld.x + 10;
        if (helloWorld.x > s_width) {
            helloWorld.x = 0f
        }
        Log.e("hi", "Translation x is ${helloWorld.x}, width is ${s_width}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                test()
                mainHandler.postDelayed(this, 1000L / FPS)

                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)

                s_width = displayMetrics.widthPixels
                s_height = displayMetrics.heightPixels
            }
        })
    }
}
