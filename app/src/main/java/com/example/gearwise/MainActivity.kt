package com.example.gearwise

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gearwise.ui.navigation.GearWiseNavGraph
import com.example.gearwise.ui.theme.GearWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 全局崩溃捕获——帮助定位问题
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("GearWise", "CRASH: ${throwable.javaClass.simpleName}: ${throwable.message}", throwable)
            // 写入文件方便读取
            try {
                openFileOutput("crash_log.txt", MODE_PRIVATE).use {
                    it.write("${throwable.javaClass.name}: ${throwable.message}\n\n".toByteArray())
                    throwable.stackTrace.forEach { frame ->
                        it.write("  at $frame\n".toByteArray())
                    }
                }
            } catch (_: Exception) {}
            // 继续用默认处理器（会显示"已停止运行"对话框而非静默闪退）
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }

        setContent {
            GearWiseTheme {
                GearWiseNavGraph()
            }
        }
    }
}
