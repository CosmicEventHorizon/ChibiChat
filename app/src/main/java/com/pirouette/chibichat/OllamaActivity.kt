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
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class OllamaActivity : AppCompatActivity() {
    lateinit var etOllamaIPAdd: EditText
    lateinit var etOllamaPort: EditText
    lateinit var etOllamaModel: EditText
    lateinit var etOllamaPromptName: EditText
    lateinit var btnOllamaBack: Button
    lateinit var btnOllamaSave: Button
    lateinit var btnOllamaReset: Button
    lateinit var btnOllamaPullModels: Button
    lateinit var btnOllamaApplyModel: Button
    lateinit var etOllamaSystemPrompt: EditText
    lateinit var spOllamaSystemPrompts: Spinner
    lateinit var spOllamaModels: Spinner
    lateinit var tvSavedPrompts: TextView
    lateinit var ipAdd: String
    lateinit var port: String
    lateinit var model: String
    lateinit var ollamaPromptName: String
    lateinit var ollamaSystemPrompt: String
    var promptList = ArrayList<OllamaSavedPrompt>()
    var modelList = ArrayList<String>()
    var selectedModel = ""


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
        btnOllamaPullModels.setOnClickListener {
            PullModels()
        }
        btnOllamaApplyModel.setOnClickListener {
            ApplySelectedModel()
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
        spOllamaModels = findViewById<Spinner>(R.id.spOllamaModels)
        tvSavedPrompts = findViewById<TextView>(R.id.tvSavedPrompts)
        btnOllamaBack = findViewById<Button>(R.id.btnOllamaBack)
        btnOllamaSave = findViewById<Button>(R.id.btnOllamaSave)
        btnOllamaReset = findViewById<Button>(R.id.btnOllamaReset)
        btnOllamaPullModels = findViewById<Button>(R.id.btnOllamaPullModels)
        btnOllamaApplyModel = findViewById<Button>(R.id.btnOllamaApplyModel)
        spOllamaSystemPrompts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                etOllamaPromptName.setText(promptList[position].name)
                etOllamaSystemPrompt.setText(promptList[position].prompt)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        spOllamaModels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedModel = modelList[position]
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
        ResetModelList()
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
        ResetModelList()
        LoadPromptSpinner(ollamaPromptName)

    }

    fun ResetModelList() {
        modelList.clear()
        selectedModel = ""
        spOllamaModels.adapter = null
        spOllamaModels.visibility = View.GONE
    }

    fun PullModels() {
        val ipAddress = etOllamaIPAdd.text.toString().trim()
        val portNumber = etOllamaPort.text.toString().trim()
        val url = buildTagsUrl(ipAddress, portNumber)

        Thread {
            var connection: HttpURLConnection? = null

            try {
                connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    doInput = true
                }

                val responseCode = connection.responseCode
                val responseStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                } ?: throw IOException("HTTP $responseCode")

                val responseBody = BufferedReader(InputStreamReader(responseStream)).use { reader ->
                    reader.readText()
                }

                if (responseCode !in 200..299) {
                    throw IOException(responseBody.ifEmpty { "HTTP $responseCode" })
                }

                val models = Gson().fromJson(responseBody, OllamaTagsJsonClass::class.java)
                    ?.models
                    ?.map { it.name }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()

                if (models.isEmpty()) {
                    throw IOException("No models found")
                }

                runOnUiThread {
                    modelList.clear()
                    modelList.addAll(models)
                    selectedModel = modelList[0]
                    val modelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modelList)
                    modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spOllamaModels.adapter = modelAdapter
                    spOllamaModels.visibility = View.VISIBLE
                    Toast.makeText(applicationContext, "Models Pulled!", Toast.LENGTH_SHORT).show()
                }
            } catch (error: Exception) {
                runOnUiThread {
                    Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    fun ApplySelectedModel() {
        if (modelList.isEmpty()) {
            Toast.makeText(applicationContext, "Pull models first", Toast.LENGTH_SHORT).show()
            return
        }

        selectedModel = spOllamaModels.selectedItem?.toString().orEmpty()
        if (selectedModel.isEmpty()) {
            Toast.makeText(applicationContext, "Choose a model first", Toast.LENGTH_SHORT).show()
            return
        }

        etOllamaModel.setText(selectedModel)
        SaveData()
        Toast.makeText(applicationContext, "Model Applied!", Toast.LENGTH_SHORT).show()
    }

    fun buildTagsUrl(ipAdd: String, port: String?): String {
        val base = if (ipAdd.startsWith("http://") || ipAdd.startsWith("https://")) {
            ipAdd
        } else {
            "http://$ipAdd"
        }

        val uri = try {
            URI(base)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid server address")
        }

        val portInt = port?.toIntOrNull() ?: uri.port

        return URI(
            uri.scheme,
            null,
            uri.host,
            portInt,
            "/api/tags",
            null,
            null
        ).toString()
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
