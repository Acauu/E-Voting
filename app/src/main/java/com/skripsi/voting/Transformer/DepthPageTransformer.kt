package com.skripsi.voting.Transformer

import android.view.View
import androidx.viewpager.widget.ViewPager

private const val MIN_SCALE = 0.75f

class DepthPageTransformer : ViewPager.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            when {
                position < -1 -> { // [-Infinity,-1)
                    // membuat halaman way off-screen ke kiri.
                    alpha = 0f
                }
                position <= 0 -> { // [-1,0]
                    // menggunakan default slide transisi untuk berpindah ke kiri.
                    alpha = 1f
                    translationX = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> { // (0,1]
                    // Fade out halamanya.
                    alpha = 1 - position

                    // Counteract the default slide transisi
                    translationX = pageWidth * -position

                    // Scale halaman ke bawah (diantara MIN_SCALE and 1)
                    val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position)))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> { // (1,+Infinity]
                    // halaman ini way off-screen ke kanan.
                    alpha = 0f
                }
            }
        }
    }
}