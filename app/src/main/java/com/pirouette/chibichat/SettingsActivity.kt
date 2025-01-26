package com.pirouette.chibichat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {
    lateinit var btnSettingsBack: Button
    lateinit var btnSettingsSave: Button
    lateinit var btnOllama: Button
    lateinit var btnKobold: Button
    lateinit var rbOllama: RadioButton
    lateinit var rbKobold: RadioButton
    var ollama = false
    var kobold = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        InitializeWidgets()
        LoadData()

        btnOllama.setOnClickListener {
            val ollamaPage = Intent(this, OllamaActivity::class.java);
            startActivity(ollamaPage);
        }
        btnKobold.setOnClickListener {
            val koboldPage = Intent(this, KoboldActivity::class.java);
            startActivity(koboldPage);
        }
        btnSettingsBack.setOnClickListener {
            finish()
        }
        btnSettingsSave.setOnClickListener {
            SaveData();
            Toast.makeText(applicationContext, "Data Saved!", Toast.LENGTH_SHORT).show()
        }
        rbKobold.setOnClickListener {
            Toast.makeText(applicationContext, "Kobold Server Chosen! Please Save.", Toast.LENGTH_SHORT).show()
        }
        rbOllama.setOnClickListener {
            Toast.makeText(applicationContext, "Ollama Server Chosen! Please Save.", Toast.LENGTH_SHORT).show()
        }
    }

    fun InitializeWidgets() {
        btnKobold = findViewById<Button>(R.id.btnKobold)
        btnOllama = findViewById<Button>(R.id.btnOllama)
        btnSettingsBack = findViewById<Button>(R.id.btnSettingsBack)
        btnSettingsSave = findViewById<Button>(R.id.btnSettingsSave)
        rbKobold = findViewById<RadioButton>(R.id.rbKobold)
        rbOllama = findViewById<RadioButton>(R.id.rbOllama)

    }

    fun ResetDefaults() {
        rbOllama.isChecked = true;
    }

    fun SaveData() {
        val sharedPrefs = getSharedPreferences("saved_server_settings", Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit();
        editor.putBoolean("OLLAMA", rbOllama.isChecked)
        editor.putBoolean("KOBOLD", rbKobold.isChecked)
        editor.apply()
    }

    fun LoadData() {
        val sharedPrefs = getSharedPreferences("saved_server_settings", Context.MODE_PRIVATE)
        ollama = sharedPrefs.getBoolean("OLLAMA", true)
        kobold = sharedPrefs.getBoolean("KOBOLD", false)
        rbOllama.isChecked = ollama
        rbKobold.isChecked = kobold
    }
}