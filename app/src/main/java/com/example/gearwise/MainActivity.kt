package com.example.gearwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gearwise.ui.navigation.GearWiseNavGraph
import com.example.gearwise.ui.theme.GearWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GearWiseTheme {
                GearWiseNavGraph()
            }
        }
    }
}
