package me.diegomelo.minesweeper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class ConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConfigScreen(this, onSaveSettings = { rows, columns, mineCount ->

                Log.d("ConfigActivity", "rows: $rows, columns: $columns, mineCount: $mineCount")
                val resultIntent = Intent().apply {
                    putExtra("rows", rows)
                    putExtra("columns", columns)
                    putExtra("mineCount", mineCount)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }, onCancel = {
                Log.d("ConfigActivity", "onCancel")
                setResult(RESULT_CANCELED)
                finish()
            })
        }
    }
}