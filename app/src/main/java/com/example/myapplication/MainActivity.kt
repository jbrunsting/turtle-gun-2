package com.example.myapplication

import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


const val FPS: Float = 60.0f
const val NARWHAL_SPEED: Float = 1000.0f
const val NARWHAL_SIDE_MARGIN: Float = 100.0f
const val NARWHAL_BOTTOM_MARGIN: Float = 150.0f
const val PIRANHA_SPEED: Float = 1200.0f
const val TURTLE_SIDE_MARGIN: Float = 100.0f
const val TURTLE_START_DISTANCE: Float = 700.0f
const val PIRANHA_SIZE = 100.0f
const val OBJECT_SPEED: Float = 800.0f
const val X = 0
const val Y = 1

open class Entity(v: View, minx: Float, miny: Float, maxx: Float, maxy: Float) {
    var v = v
    var min = floatArrayOf(minx, miny)
    var max = floatArrayOf(maxx, maxy)

    fun getCenterCoord(dim: Int): Float {
        if (dim == X) {
            return this.v.x + this.v.width / 2.0f
        }
        return this.v.y + this.v.height / 2.0f
    }

    fun setCenterCoord(dim: Int, value: Float) {
        if (getCenterCoord(dim) == value) {
            return;
        }
        if (dim == X) {
            this.v.x = value - this.v.width / 2.0f
        }
        this.v.y = value - this.v.height / 2.0f
    }
}

enum class WrapType { REVERSE, WRAP, HIDE }

class AnimatedEntity(
    v: View,
    stepx: Float,
    stepy: Float,
    minx: Float,
    miny: Float,
    maxx: Float,
    maxy: Float,
    type: WrapType
) : Entity(v, minx, miny, maxx, maxy) {
    var step = floatArrayOf(stepx, stepy)
    var dir = intArrayOf(1, 1)
    var type = type

    fun step() {
        for (dim in 0..1) {
            setCenterCoord(dim, getCenterCoord(dim) + this.step[dim] * this.dir[dim])
            if (getCenterCoord(dim) < this.min[dim]) {
                if (type == WrapType.REVERSE) {
                    setCenterCoord(dim, this.min[dim])
                    this.dir[dim] *= -1;
                } else if (type == WrapType.WRAP) {
                    setCenterCoord(dim, this.max[dim])
                } else if (type == WrapType.HIDE) {
                    v.visibility = View.GONE
                }
            } else if (getCenterCoord(dim) > this.max[dim]) {
                if (type == WrapType.REVERSE) {
                    setCenterCoord(dim, this.max[dim])
                    this.dir[dim] *= -1;
                } else if (type == WrapType.WRAP) {
                    setCenterCoord(dim, this.min[dim])
                } else if (type == WrapType.HIDE) {
                    v.visibility = View.GONE
                }
            }
        }
    }
}

class MainActivity : AppCompatActivity() {
    var sWidth: Float = 0.0f
    var sHeight: Float = 0.0f
    var narwhalEntity: AnimatedEntity? = null
    var turtleEntity: Entity? = null
    var piranhaEntities: List<AnimatedEntity> = listOf()
    var background1Entity: AnimatedEntity? = null
    var background2Entity: AnimatedEntity? = null

    private fun frame() {
        narwhalEntity?.step()
        for (p in piranhaEntities) {
            p.step()
        }
        background1Entity?.step()
        background2Entity?.step()
    }

    private fun shootPiranha() {
        if (turtleEntity == null) {
            return;
        }

        val piranhaView = ImageView(this)
        piranhaView.setImageResource(R.drawable.piranha)
        val layoutParams = FrameLayout.LayoutParams(100, 100)
        main_layout.addView(piranhaView, layoutParams)
        piranhaView.rotation = -90.0f
        piranhaView.bringToFront()
        piranhaView.x = 0.0f;
        piranhaView.y = 0.0f;

        piranhaEntities += AnimatedEntity(
            piranhaView,
            0.0f,
            -PIRANHA_SPEED / FPS,
            0.0f,
            0.0f,
            sWidth,
            sHeight,
            WrapType.HIDE
        )

        for (dim in 0..1) {
            piranhaView.x = (turtleEntity?.getCenterCoord(X) ?: 0.0f) - PIRANHA_SIZE / 2.0f
            piranhaView.y = (turtleEntity?.getCenterCoord(Y) ?: 0.0f) - PIRANHA_SIZE / 2.0f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainHandler = Handler(Looper.getMainLooper())

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        sWidth = displayMetrics.widthPixels.toFloat()
        sHeight = displayMetrics.heightPixels.toFloat()

        background1.post {
            val bgCenterY = background1.height.toFloat() / 2.0f - 5.0f
            val bgCenterX = background1.width.toFloat() / 2.0f

            background1Entity = AnimatedEntity(
                background1,
                0.0f,
                OBJECT_SPEED / FPS,
                bgCenterX,
                -bgCenterY,
                bgCenterX,
                3 * bgCenterY,
                WrapType.WRAP
            )
            background1Entity?.setCenterCoord(Y, -bgCenterY)

            background2Entity = AnimatedEntity(
                background2,
                0.0f,
                OBJECT_SPEED / FPS,
                bgCenterX,
                -bgCenterY,
                bgCenterX,
                3 * bgCenterY,
                WrapType.WRAP
            )
            background2Entity?.setCenterCoord(Y, bgCenterY)
        }

        narwhal.post(Runnable {
            narwhal.x = sWidth / 2.0f
            narwhal.y = sHeight - narwhal.height - NARWHAL_BOTTOM_MARGIN

            val narwhalY = sHeight - narwhal.height / 2 - NARWHAL_BOTTOM_MARGIN
            narwhalEntity = AnimatedEntity(
                narwhal,
                NARWHAL_SPEED / FPS,
                0.0f,
                NARWHAL_SIDE_MARGIN.toFloat(),
                narwhalY,
                sWidth - NARWHAL_SIDE_MARGIN.toFloat(),
                narwhalY,
                WrapType.REVERSE
            )
        })

        turtle.post(Runnable {
            turtleEntity =
                Entity(turtle, TURTLE_SIDE_MARGIN, 0.0f, sWidth - TURTLE_SIDE_MARGIN, sHeight)
            turtleEntity?.setCenterCoord(X, sWidth / 2.0f)
            turtleEntity?.setCenterCoord(Y, sHeight - TURTLE_START_DISTANCE)
        })

        mainHandler.post(object : Runnable {
            override fun run() {
                frame()
                mainHandler.postDelayed(this, (1000 / FPS).toLong())
            }
        })

        scene_click.setOnClickListener { shootPiranha() }
    }
}
