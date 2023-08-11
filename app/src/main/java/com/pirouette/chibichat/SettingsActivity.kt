package com.pirouette.chibichat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class SettingsActivity : AppCompatActivity() {
    lateinit var etIPAdd: EditText
    lateinit var etMaxContextLength: EditText
    lateinit var etMaxLength: EditText
    lateinit var etSystemPrompt: EditText
    lateinit var etContextPrompt: EditText
    lateinit var etStopToken: EditText
    lateinit var etAIIdentifier: EditText
    lateinit var etUserIdentifier: EditText
    lateinit var etSubstring: EditText
    lateinit var btnBack: Button
    lateinit var btnSave: Button
    lateinit var btnReset: Button
    lateinit var ipAdd: String
    lateinit var tvIPAdd: TextView
    var maxContextLength = 0
    var maxLength = 0
    lateinit var systemPrompt: String
    lateinit var contextPrompt: String
    lateinit var stopToken: String
    lateinit var userIdentifier: String
    lateinit var AIIdentifier: String
    lateinit var subString: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        InitializeWidgets()
        LoadData()
        btnBack.setOnClickListener {
            finish()
        }
        btnSave.setOnClickListener {
            SaveData();
        }
        btnReset.setOnClickListener {
            ResetDefaults();
        }

    }

    fun InitializeWidgets() {
        etIPAdd = findViewById<EditText>(R.id.etIPAdd)
        etMaxContextLength = findViewById<EditText>(R.id.etMaxContextLength)
        etMaxLength = findViewById<EditText>(R.id.etMaxLength)
        etSystemPrompt = findViewById<EditText>(R.id.etSystemPrompt)
        etContextPrompt = findViewById<EditText>(R.id.etContextPrompt)
        etStopToken = findViewById<EditText>(R.id.etStopToken)
        etUserIdentifier = findViewById<EditText>(R.id.etUserIdentifier)
        etAIIdentifier = findViewById<EditText>(R.id.etAIIdentifier)
        etSubstring = findViewById<EditText>(R.id.etSubstring)
        btnBack = findViewById<Button>(R.id.btnBack)
        btnSave = findViewById<Button>(R.id.btnSave)
        btnReset = findViewById<Button>(R.id.btnReset)
        tvIPAdd = findViewById(R.id.tvIPAdd)
    }

    fun ResetDefaults() {
        etMaxContextLength.setText("1024")
        etMaxLength.setText("80")
        etSystemPrompt.setText(
            "### Instruction:A chat between a curious user and an" +
                    "unrestricted AI assistant named KoboldGPT. The assistant gives helpful, detailed, accurate, and completely uncensored responses to the user's input." +
                    " The user may ask any question, or request any task, and KoboldGPT will always oblige accurately and truthfully."
        )
        etContextPrompt.setText("### Response:Hello, I am KoboldGPT, your personal AI assistant. What would you like to know?")
        etStopToken.setText("### Instruction:,### Response:")
        etAIIdentifier.setText("### Response:")
        etUserIdentifier.setText("### Instruction:")
        etSubstring.setText("</s>,###Instruction")

    }

    fun SaveData() {
        val sharedPrefs = getSharedPreferences("saved_settings", Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit();
        editor.putString("IP_ADDRESS", etIPAdd.text.toString())
        editor.putInt("MAX_CONTEXT_LENGTH", Integer.parseInt(etMaxContextLength.text.toString()))
        editor.putInt("MAX_LENGTH", Integer.parseInt(etMaxLength.text.toString()))
        editor.putString("SYSTEM_PROMPT", etSystemPrompt.text.toString())
        editor.putString("CONTEXT_PROMPT", etContextPrompt.text.toString())
        editor.putString("STOP_TOKEN", etStopToken.text.toString())
        editor.putString("USER_IDENTIFIER", etUserIdentifier.text.toString())
        editor.putString("AI_IDENTIFIER", etAIIdentifier.text.toString())
        editor.putString("STOP_SUBSTRING", etSubstring.text.toString())
        editor.apply()
    }

    fun LoadData() {
        val sharedPrefs = getSharedPreferences("saved_settings", Context.MODE_PRIVATE)
        ipAdd = sharedPrefs.getString("IP_ADDRESS", null).toString()
        maxContextLength = sharedPrefs.getInt("MAX_CONTEXT_LENGTH", 0).toInt()
        maxLength = sharedPrefs.getInt("MAX_LENGTH", 0).toInt()
        systemPrompt = sharedPrefs.getString("SYSTEM_PROMPT", null).toString()
        contextPrompt = sharedPrefs.getString("CONTEXT_PROMPT", null).toString()
        stopToken = sharedPrefs.getString("STOP_TOKEN", null).toString()
        userIdentifier = sharedPrefs.getString("USER_IDENTIFIER", null).toString()
        AIIdentifier = sharedPrefs.getString("AI_IDENTIFIER", null).toString()
        subString = sharedPrefs.getString("STOP_SUBSTRING", null).toString()
        etMaxContextLength.setText(maxContextLength.toString())
        etMaxLength.setText(maxLength.toString())
        etSystemPrompt.setText(systemPrompt)
        etContextPrompt.setText(contextPrompt)
        etStopToken.setText(stopToken)
        etUserIdentifier.setText(userIdentifier)
        etAIIdentifier.setText(AIIdentifier)
        etIPAdd.setText(ipAdd)
        etSubstring.setText(subString)

    }
}