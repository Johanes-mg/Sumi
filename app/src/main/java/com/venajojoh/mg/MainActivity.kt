package com.venajojoh.mg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.venajojoh.mg.data.PersonRepository
import com.venajojoh.mg.ui.VenaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = PersonRepository(applicationContext)
        setContent {
            VenaApp(repository)
        }
    }
}
