package com.pirouette.chibichat

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class OllamaActivity : AppCompatActivity() {
    lateinit var etOllamaIPAdd: EditText
    lateinit var etOllamaPort: EditText
    lateinit var etOllamaModel: EditText
    lateinit var btnOllamaBack: Button
    lateinit var btnOllamaSave: Button
    lateinit var btnOllamaReset: Button
    lateinit var etOllamaSystemPrompt: EditText
    lateinit var ipAdd: String
    lateinit var port: String
    lateinit var model: String
    lateinit var ollamaSystemPrompt: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ollama)
        InitializeWidgets()
        LoadData()
        btnOllamaBack.setOnClickListener {
            finish()
        }
        btnOllamaSave.setOnClickListener {
            SaveData();
            Toast.makeText(applicationContext, "Data Saved!", Toast.LENGTH_SHORT).show()

        }
        btnOllamaReset.setOnClickListener {
            ResetDefaults();
            Toast.makeText(applicationContext, "Loaded Default Settings", Toast.LENGTH_SHORT).show()
        }

    }

    fun InitializeWidgets() {
        etOllamaIPAdd = findViewById<EditText>(R.id.etOllamaIPAdd)
        etOllamaPort = findViewById<EditText>(R.id.etOllamaPort)
        etOllamaModel = findViewById<EditText>(R.id.etOllamaModel)
        etOllamaSystemPrompt= findViewById<EditText>(R.id.etOllamaSystemPrompt)
        btnOllamaBack = findViewById<Button>(R.id.btnOllamaBack)
        btnOllamaSave = findViewById<Button>(R.id.btnOllamaSave)
        btnOllamaReset = findViewById<Button>(R.id.btnOllamaReset)
    }

    fun ResetDefaults() {
        etOllamaPort.setText("11434")
        etOllamaIPAdd.setText("127.0.0.1")
        etOllamaModel.setText("null")
        etOllamaSystemPrompt.setText("")
    }

    fun SaveData() {
        val sharedPrefs = getSharedPreferences("saved_ollama_settings", Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit();
        editor.putString("IP_ADDRESS", etOllamaIPAdd.text.toString())
        editor.putString("PORT", etOllamaPort.text.toString())
        editor.putString("MODEL", etOllamaModel.text.toString())
        editor.putString("SYSTEM_PROMPT", etOllamaSystemPrompt.text.toString())
        editor.apply()
    }

    fun LoadData() {
        val sharedPrefs = getSharedPreferences("saved_ollama_settings", Context.MODE_PRIVATE)
        ipAdd = sharedPrefs.getString("IP_ADDRESS", "127.0.0.1").toString()
        port = sharedPrefs.getString("PORT", "11434").toString()
        model = sharedPrefs.getString("MODEL", null).toString()
        ollamaSystemPrompt = sharedPrefs.getString("SYSTEM_PROMPT", null).toString()
        etOllamaIPAdd.setText(ipAdd)
        etOllamaPort.setText(port)
        etOllamaModel.setText(model)
        etOllamaSystemPrompt.setText(ollamaSystemPrompt)

    }
}