package com.pirouette.chibichat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {
    lateinit var etIPAdd: EditText
    lateinit var etPort: EditText
    lateinit var etMaxContextLength: EditText
    lateinit var etMaxLength: EditText
    lateinit var etN: EditText
    lateinit var etTemperature: EditText
    lateinit var etTypical: EditText
    lateinit var etTopP: EditText
    lateinit var etTopK: EditText
    lateinit var etTopA: EditText
    lateinit var etRepPen: EditText
    lateinit var etRepPenRange: EditText
    lateinit var etRepPenSlope: EditText
    lateinit var etTfs: EditText
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
    lateinit var port: String
    lateinit var tvIPAdd: TextView
    var maxContextLength = 0
    var maxLength = 0
    var n = 0
    var temp = 0.0f
    var typical = 0
    var top_p = 0.0f
    var top_k = 0
    var top_a = 0
    var rep_pen = 0.0f
    var rep_pen_range = 0
    var rep_pen_slope = 0.0f
    var tfs = 0
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
            Toast.makeText(applicationContext, "Data Saved!", Toast.LENGTH_SHORT).show()

        }
        btnReset.setOnClickListener {
            ResetDefaults();
            Toast.makeText(applicationContext, "Loaded Default Settings", Toast.LENGTH_SHORT).show()
        }

    }

    fun InitializeWidgets() {
        etIPAdd = findViewById<EditText>(R.id.etIPAdd)
        etPort = findViewById<EditText>(R.id.etPort)
        etMaxContextLength = findViewById<EditText>(R.id.etMaxContextLength)
        etMaxLength = findViewById<EditText>(R.id.etMaxLength)
        etN = findViewById<EditText>(R.id.etN)
        etTemperature = findViewById<EditText>(R.id.etTemperature)
        etTypical = findViewById<EditText>(R.id.etTypical)
        etTopP = findViewById<EditText>(R.id.etTopP)
        etTopK = findViewById<EditText>(R.id.etTopK)
        etTopA = findViewById<EditText>(R.id.etTopA)
        etRepPen = findViewById<EditText>(R.id.etRepPen)
        etRepPenRange = findViewById<EditText>(R.id.etRepPenRange)
        etRepPenSlope = findViewById<EditText>(R.id.etRepPenSlope)
        etTfs = findViewById<EditText>(R.id.etTfs)
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
        etPort.setText("5001")
        etMaxContextLength.setText("4096")
        etMaxLength.setText("512")
        etN.setText("1")
        etTemperature.setText("0.7")
        etTypical.setText("1")
        etTopP.setText("0.92")
        etTopK.setText("0")
        etTopA.setText("0")
        etRepPen.setText("1.1")
        etRepPenRange.setText("300")
        etRepPenSlope.setText("0.7")
        etTfs.setText("1")
        etSystemPrompt.setText("")
        etContextPrompt.setText("")
        etStopToken.setText("<s>,</s>,[/INST],/n")
        etAIIdentifier.setText(" [/INST]")
        etUserIdentifier.setText(" <s> [INST] ")
        etSubstring.setText("</s>")

    }

    fun SaveData() {
        val sharedPrefs = getSharedPreferences("saved_settings", Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit();
        editor.putString("IP_ADDRESS", etIPAdd.text.toString())
        editor.putString("PORT", etPort.text.toString())
        editor.putInt("MAX_CONTEXT_LENGTH", Integer.parseInt(etMaxContextLength.text.toString()))
        editor.putInt("MAX_LENGTH", Integer.parseInt(etMaxLength.text.toString()))
        editor.putInt("N_VALUE", Integer.parseInt(etN.text.toString()))
        editor.putFloat("TEMPERATURE", etTemperature.text.toString().toFloat())
        editor.putInt("TYPICAL", Integer.parseInt(etTypical.text.toString()))
        editor.putFloat("TOP_P", etTopP.text.toString().toFloat())
        editor.putInt("TOP_K", Integer.parseInt(etTopK.text.toString()))
        editor.putInt("TOP_A", Integer.parseInt(etTopA.text.toString()))
        editor.putFloat("REP_PEN", etRepPen.text.toString().toFloat())
        editor.putInt("REP_PEN_RANGE", Integer.parseInt(etRepPenRange.text.toString()))
        editor.putFloat("REP_PEN_SLOPE", etRepPenSlope.text.toString().toFloat())
        editor.putInt("TFS_VALUE", Integer.parseInt(etTfs.text.toString()))
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
        port = sharedPrefs.getString("PORT", null).toString()
        maxContextLength = sharedPrefs.getInt("MAX_CONTEXT_LENGTH", 0).toInt()
        maxLength = sharedPrefs.getInt("MAX_LENGTH", 0).toInt()
        n = sharedPrefs.getInt("N_VALUE", 0).toInt()
        temp = sharedPrefs.getFloat("TEMPERATURE", 0.7f).toFloat()
        typical = sharedPrefs.getInt("TYPICAL", 0).toInt()
        top_p  = sharedPrefs.getFloat("TOP_P", 0.92f).toFloat()
        top_k = sharedPrefs.getInt("TOP_K", 0).toInt()
        top_a = sharedPrefs.getInt("TOP_A", 0).toInt()
        rep_pen = sharedPrefs.getFloat("REP_PEN", 1.1f).toFloat()
        rep_pen_range = sharedPrefs.getInt("REP_PEN_RANGE", 300).toInt()
        rep_pen_slope = sharedPrefs.getFloat("REP_PEN_SLOPE", 0.7f).toFloat()
        tfs = sharedPrefs.getInt("TFS_VALUE", 0).toInt()
        systemPrompt = sharedPrefs.getString("SYSTEM_PROMPT", null).toString()
        contextPrompt = sharedPrefs.getString("CONTEXT_PROMPT", null).toString()
        stopToken = sharedPrefs.getString("STOP_TOKEN", null).toString()
        userIdentifier = sharedPrefs.getString("USER_IDENTIFIER", null).toString()
        AIIdentifier = sharedPrefs.getString("AI_IDENTIFIER", null).toString()
        subString = sharedPrefs.getString("STOP_SUBSTRING", null).toString()
        etMaxContextLength.setText(maxContextLength.toString())
        etMaxLength.setText(maxLength.toString())
        etN.setText(n.toString())
        etTemperature.setText(temp.toString())
        etTypical.setText(typical.toString())
        etTopP.setText(top_p.toString())
        etTopK.setText(top_k.toString())
        etTopA.setText(top_a.toString())
        etRepPen.setText(rep_pen.toString())
        etRepPenRange.setText(rep_pen_range.toString())
        etRepPenSlope.setText(rep_pen_slope.toString())
        etTfs.setText(tfs.toString())
        etSystemPrompt.setText(systemPrompt)
        etContextPrompt.setText(contextPrompt)
        etStopToken.setText(stopToken)
        etUserIdentifier.setText(userIdentifier)
        etAIIdentifier.setText(AIIdentifier)
        etIPAdd.setText(ipAdd)
        etPort.setText(port)
        etSubstring.setText(subString)

    }
}