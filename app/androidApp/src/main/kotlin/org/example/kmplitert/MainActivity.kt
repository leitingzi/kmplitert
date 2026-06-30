package org.example.kmplitert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.example.kmplitert.app.core.App
import org.example.kmplitert.app.core.ResourceUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ResourceUtils.init(applicationContext)

        setContent {
            App()
        }
    }
}