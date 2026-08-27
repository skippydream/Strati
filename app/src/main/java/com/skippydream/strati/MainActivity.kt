package com.skippydream.strati

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.skippydream.strati.data.ProgressStore
import com.skippydream.strati.ui.StratiApp
import com.skippydream.strati.ui.theme.StratiTheme

// AppCompatActivity: sotto Android 13 il backport della lingua per-app applica la scelta
// solo alle Activity di AppCompat.
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ProgressStore.init(this)

        setContent {
            StratiTheme {
                StratiApp()
            }
        }
    }
}
