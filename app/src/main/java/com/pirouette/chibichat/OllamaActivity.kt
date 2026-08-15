package com.pirouette.chibichat

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

class OllamaActivity : AppCompatActivity() {
    lateinit var etOllamaIPAdd: EditText
    lateinit var etOllamaPort: EditText
    lateinit var etOllamaModel: EditText
    lateinit var etOllamaPromptName: EditText
    lateinit var btnOllamaBack: Button
    lateinit var btnOllamaSave: Button
    lateinit var btnOllamaReset: Button
    lateinit var etOllamaSystemPrompt: EditText
    lateinit var spOllamaSystemPrompts: Spinner
    lateinit var tvSavedPrompts: TextView
    lateinit var ipAdd: String
    lateinit var port: String
    lateinit var model: String
    lateinit var ollamaPromptName: String
    lateinit var ollamaSystemPrompt: String
    var promptList = ArrayList<OllamaSavedPrompt>()


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
        etOllamaPromptName = findViewById<EditText>(R.id.etOllamaPromptName)
        etOllamaSystemPrompt= findViewById<EditText>(R.id.etOllamaSystemPrompt)
        spOllamaSystemPrompts = findViewById<Spinner>(R.id.spOllamaSystemPrompts)
        tvSavedPrompts = findViewById<TextView>(R.id.tvSavedPrompts)
        btnOllamaBack = findViewById<Button>(R.id.btnOllamaBack)
        btnOllamaSave = findViewById<Button>(R.id.btnOllamaSave)
        btnOllamaReset = findViewById<Button>(R.id.btnOllamaReset)
        spOllamaSystemPrompts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                etOllamaPromptName.setText(promptList[position].name)
                etOllamaSystemPrompt.setText(promptList[position].prompt)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    fun ResetDefaults() {
        etOllamaPort.setText("11434")
        etOllamaIPAdd.setText("127.0.0.1")
        etOllamaModel.setText("null")
        etOllamaPromptName.setText("")
        etOllamaSystemPrompt.setText("")
    }

    fun SaveData() {
        val sharedPrefs = getSharedPreferences("saved_ollama_settings", Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit();
        val currentPromptName = etOllamaPromptName.text.toString().trim()
        val currentPrompt = etOllamaSystemPrompt.text.toString()
        promptList.removeAll { it.name == currentPromptName || it.prompt == currentPrompt }
        if (currentPromptName.isNotEmpty() && currentPrompt.isNotEmpty()) {
            promptList.add(0, OllamaSavedPrompt(currentPromptName, currentPrompt))
        }
        editor.putString("IP_ADDRESS", etOllamaIPAdd.text.toString())
        editor.putString("PORT", etOllamaPort.text.toString())
        editor.putString("MODEL", etOllamaModel.text.toString())
        editor.putString("SYSTEM_PROMPT_NAME", currentPromptName)
        editor.putString("SYSTEM_PROMPT", currentPrompt)
        OllamaPromptStore.savePrompts(editor, promptList)
        editor.apply()
        LoadPromptSpinner(currentPromptName)
    }

    fun LoadData() {
        val sharedPrefs = getSharedPreferences("saved_ollama_settings", Context.MODE_PRIVATE)
        ipAdd = sharedPrefs.getString("IP_ADDRESS", "127.0.0.1").toString()
        port = sharedPrefs.getString("PORT", "11434").toString()
        model = sharedPrefs.getString("MODEL", null).toString()
        ollamaPromptName = sharedPrefs.getString("SYSTEM_PROMPT_NAME", "").orEmpty()
        ollamaSystemPrompt = sharedPrefs.getString("SYSTEM_PROMPT", "").orEmpty()
        promptList = OllamaPromptStore.loadPrompts(sharedPrefs)
        etOllamaIPAdd.setText(ipAdd)
        etOllamaPort.setText(port)
        etOllamaModel.setText(model)
        etOllamaPromptName.setText(ollamaPromptName)
        etOllamaSystemPrompt.setText(ollamaSystemPrompt)
        LoadPromptSpinner(ollamaPromptName)

    }

    fun LoadPromptSpinner(selectedPromptName: String) {
        if (promptList.isEmpty()) {
            spOllamaSystemPrompts.visibility = View.GONE
            tvSavedPrompts.visibility = View.GONE
            return
        }

        spOllamaSystemPrompts.visibility = View.VISIBLE
        tvSavedPrompts.visibility = View.VISIBLE
        val promptLabels = promptList.map { it.name }
        val promptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, promptLabels)
        promptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spOllamaSystemPrompts.adapter = promptAdapter
        val selectedIndex = promptList.indexOfFirst { it.name == selectedPromptName }
        if (selectedIndex >= 0) {
            spOllamaSystemPrompts.setSelection(selectedIndex)
        }
    }
}