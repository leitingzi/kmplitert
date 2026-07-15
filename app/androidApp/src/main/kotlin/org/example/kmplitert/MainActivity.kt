package org.example.kmplitert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.leitingzi.kmplitert.tool.LiteRTFileUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        LiteRTFileUtils.init(applicationContext)

        setContent {
            App()
        }
    }
}