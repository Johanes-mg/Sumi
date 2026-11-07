package com.sumijojoh.mg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sumijojoh.mg.data.AppSettingsRepository
import com.sumijojoh.mg.data.PersonRepository
import com.sumijojoh.mg.ui.SumiApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = PersonRepository(applicationContext)
        val settingsRepository = AppSettingsRepository(applicationContext)
        setContent {
            SumiApp(repository, settingsRepository)
        }
    }
}
