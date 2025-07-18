
package me.shetj.mp3recorder

import android.Manifest.permission
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.os.Bundle
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.shetj.base.BaseKit
import me.shetj.base.fix.FixPermission
import me.shetj.base.ktx.hasPermission
import me.shetj.base.ktx.setAppearance
import me.shetj.base.mvvm.viewbind.BaseBindingActivity
import me.shetj.base.mvvm.viewbind.BaseViewModel
import me.shetj.mp3recorder.databinding.ActivityMainTestBinding
import me.shetj.mp3recorder.record.activity.mix.RecordActivity
import me.shetj.mp3recorder.record.utils.AudioManagerX
import me.shetj.recorder.core.AudioUtils

class MainActivity : BaseBindingActivity<ActivityMainTestBinding, BaseViewModel>() {
    private var splashScreen:  SplashScreen? =null
    private var isKeep = true
    override fun onCreate(savedInstanceState: Bundle?) {
        splashScreen = installSplashScreen()
        lifecycleScope.launch {
            delay(1000)
            isKeep = false
        }
        splashScreen!!.setKeepOnScreenCondition(SplashScreen.KeepOnScreenCondition {
            //指定保持启动画面展示的条件
            return@KeepOnScreenCondition isKeep
        })
        splashScreen!!.setOnExitAnimationListener { splashScreenViewProvider ->
            setAppearance(true)
            val splashScreenView = splashScreenViewProvider.view
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView,
                View.ALPHA,
                1f,
                0f,
            )
            slideUp.duration = 800
            slideUp.doOnEnd {
                splashScreenViewProvider.remove()
                setAppearance(true,Color.WHITE)
            }
            slideUp.start()
        }
        super.onCreate(savedInstanceState)
    }


    override fun initBaseView() {
        mBinding.btnDemo3.setOnClickListener {
            if (FixPermission.checkReadMediaFile(this, isRequest = true) && hasPermission(permission.RECORD_AUDIO, isRequest = true)) {
                startActivity(Intent(this,RecordActivity::class.java))
            }
        }

        val audioManagerX = AudioManagerX(this)
        val bestSampleRate = AudioUtils.getBestSampleRate(this@MainActivity)
        mBinding.msg.apply {
            text = audioManagerX.checkDevice()
            append("\n\n\n获取当前手机录音最佳参数：")
            append("\n最佳采样率：${bestSampleRate}\n")
            append("\n录音最小缓存大小($bestSampleRate,1,${AudioFormat.ENCODING_PCM_16BIT})：${AudioRecord.getMinBufferSize(
                bestSampleRate,
                1,  AudioFormat.ENCODING_PCM_16BIT
            )}\n")
            append("音频输出的缓冲：${getBestBufferSize()}\n")
        }

    }



    //
    private fun getBestBufferSize(): Int {
        val am = BaseKit.app.getSystemService(AUDIO_SERVICE) as? AudioManager
        val bufferSizeStr: String? = am?.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
        val bufferSize: Int = bufferSizeStr?.let { str ->
            Integer.parseInt(str).takeUnless { it == 0 }
        } ?: 256 // Use a default value if property not found
        return bufferSize
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
