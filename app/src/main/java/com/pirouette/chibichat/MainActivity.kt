package com.pirouette.chibichat

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class MainActivity : AppCompatActivity() {
    companion object {
        private const val STATE_RAW_CONV = "state_raw_conv"
        private const val STATE_MSG_DATA = "state_msg_data"
    }

    //lateinit var tvCheck: TextView
    lateinit var btnSend: Button
    lateinit var btnDeleteLast: Button
    lateinit var btnSystemPrompt: Button
    lateinit var recyclerview: RecyclerView
    lateinit var etPrompt: EditText
    lateinit var adapter: RvAdapter
    lateinit var ipAdd: String
    lateinit var port: String
    lateinit var model: String
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
    var ollama = false
    var kobold = false
    lateinit var systemPrompt: String
    lateinit var systemPromptName: String
    lateinit var contextPrompt: String
    lateinit var stopToken: String
    lateinit var userIdentifier: String
    lateinit var AIIdentifier: String
    lateinit var subString: String
    val stopTokenArray: ArrayList<String> = ArrayList()
    val subStringArray: ArrayList<String> = ArrayList()
    var rawConv = ArrayList<String>()
    var msgData = ArrayList<Message>()
    var savedStoryData = ArrayList<SavedData>()
    var koboldConv = mutableListOf<String>()
    var ollamaConv = ArrayList<OllamaMessage>()
    var systemPromptPopup: PopupWindow? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CreateFolder()
        btnSend = findViewById(R.id.btnSendText)
        btnDeleteLast = findViewById(R.id.btnDeleteLast)
        btnSystemPrompt = findViewById(R.id.btnSystemPrompt)
        etPrompt = findViewById(R.id.etPrompt)
        //tvCheck = findViewById(R.id.tvOutput)
        recyclerview = findViewById<RecyclerView>(R.id.rvMessages)
        recyclerview.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        adapter = RvAdapter(msgData)
        recyclerview.adapter = adapter
        RestoreConversationState(savedInstanceState)
        savedStoryData = loadArrayFromFile(this)
        LoadData()
        UpdateSystemPromptButton()
        btnSend.setOnClickListener()
        {
            /* Clear the previous error before proceeding */
            if (msgData.isNotEmpty() && (msgData[msgData.size - 1]).msgContent == "System: ") {
                ClearError()
            }

            /*Load current user settings*/
            LoadData()

            /* Show user message on screen and scroll down*/
            msgData.add(Message("User: " + etPrompt.text.toString(), 0))
            adapter.notifyDataSetChanged()
            recyclerview.scrollToPosition(msgData.size - 1);

            /* Add the user's prompt to the rawConvArray */
            rawConv.add("u: " + etPrompt.text.toString())
            etPrompt.setText("")

            /* Choose where to send user's prompt */
            if (ollama) {
                RawToOllama()
                OllamaPOST()
            } else {
                RawToKobold()
                KoboldPOST()
            }
        }
        btnDeleteLast.setOnClickListener()
        {
            /* If the previous message was a system message remove that along with the unsent user message, otherwise remove
            * the assistant message and the corresponding user message
            * */
            if (msgData.isNotEmpty() && (msgData[msgData.size - 1]).msgContent == "System: ") {
                ClearError()
            } else {
                DeleteLastMessage()
            }
            adapter.notifyDataSetChanged()
            recyclerview.scrollToPosition(msgData.size - 1);
        }
        btnSystemPrompt.setOnClickListener {
            if (systemPromptPopup?.isShowing == true) {
                systemPromptPopup?.dismiss()
            } else {
                ShowSystemPromptPopup()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        LoadData()
        UpdateSystemPromptButton()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(STATE_RAW_CONV, rawConv)
        outState.putSerializable(STATE_MSG_DATA, ArrayList(msgData))
    }

    /*function to delete system messages*/
    fun ClearError(){
            msgData.removeLastOrNull() /*Remove the system message*/
            msgData.removeLastOrNull() /*Remove the user message */
            rawConv.removeLastOrNull() /*Remove the user message from rawConv*/
    }

    /* function to delete last message*/
    fun DeleteLastMessage(){
        msgData.removeLastOrNull() /*Remove the previous assistant message*/
        msgData.removeLastOrNull() /*Remove the previous user message*/
        rawConv.removeLastOrNull() /*Remove the previous assistant message from rawConv*/
        rawConv.removeLastOrNull() /*Remove the previous user message from rawConv*/
    }

    fun RestoreConversationState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            return
        }

        rawConv.clear()
        rawConv.addAll(savedInstanceState.getStringArrayList(STATE_RAW_CONV) ?: arrayListOf())

        @Suppress("UNCHECKED_CAST")
        val restoredMessages =
            savedInstanceState.getSerializable(STATE_MSG_DATA) as? ArrayList<Message> ?: arrayListOf()
        msgData.clear()
        msgData.addAll(restoredMessages)
        adapter.notifyDataSetChanged()
        ScrollToBottom()
    }

    fun LoadData() {

        /* The following is to load the default server settings */
        val sharedServerPrefs = getSharedPreferences("saved_server_settings", Context.MODE_PRIVATE)
        ollama = sharedServerPrefs.getBoolean("OLLAMA", true)
        kobold = sharedServerPrefs.getBoolean("KOBOLD", false)

        /* Load settings depending on choice of server */
        if (ollama) {
            /* The following is to load ollama settings */
            val sharedOllamaPrefs =
                getSharedPreferences("saved_ollama_settings", Context.MODE_PRIVATE)
            ipAdd = sharedOllamaPrefs.getString("IP_ADDRESS", null).toString()
            port = sharedOllamaPrefs.getString("PORT", null).toString()
            model = sharedOllamaPrefs.getString("MODEL", null).toString()
            systemPromptName = sharedOllamaPrefs.getString("SYSTEM_PROMPT_NAME", "").orEmpty()
            systemPrompt = sharedOllamaPrefs.getString("SYSTEM_PROMPT", "").orEmpty()
        } else {
            /* The following is to load kobold settings */
            val sharedKoboldPrefs =
                getSharedPreferences("saved_kobold_settings", Context.MODE_PRIVATE)
            ipAdd = sharedKoboldPrefs.getString("IP_ADDRESS", null).toString()
            port = sharedKoboldPrefs.getString("PORT", null).toString()
            maxContextLength = sharedKoboldPrefs.getInt("MAX_CONTEXT_LENGTH", 0).toInt()
            maxLength = sharedKoboldPrefs.getInt("MAX_LENGTH", 0).toInt()
            n = sharedKoboldPrefs.getInt("N_VALUE", 0).toInt()
            temp = sharedKoboldPrefs.getFloat("TEMPERATURE", 0.7f).toFloat()
            typical = sharedKoboldPrefs.getInt("TYPICAL", 0).toInt()
            top_p = sharedKoboldPrefs.getFloat("TOP_P", 0.92f).toFloat()
            top_k = sharedKoboldPrefs.getInt("TOP_K", 0).toInt()
            top_a = sharedKoboldPrefs.getInt("TOP_A", 0).toInt()
            rep_pen = sharedKoboldPrefs.getFloat("REP_PEN", 1.1f).toFloat()
            rep_pen_range = sharedKoboldPrefs.getInt("REP_PEN_RANGE", 300).toInt()
            rep_pen_slope = sharedKoboldPrefs.getFloat("REP_PEN_SLOPE", 0.7f).toFloat()
            tfs = sharedKoboldPrefs.getInt("TFS_VALUE", 0).toInt()
            systemPrompt = sharedKoboldPrefs.getString("SYSTEM_PROMPT", null).toString()
            contextPrompt = sharedKoboldPrefs.getString("CONTEXT_PROMPT", null).toString()
            stopToken = sharedKoboldPrefs.getString("STOP_TOKEN", null).toString()
            userIdentifier = sharedKoboldPrefs.getString("USER_IDENTIFIER", null).toString()
            AIIdentifier = sharedKoboldPrefs.getString("AI_IDENTIFIER", null).toString()
            subString = sharedKoboldPrefs.getString("STOP_SUBSTRING", null).toString()
            stopTokenArray.clear()
            subStringArray.clear()
            var tmp = ""
            stopToken = stopToken + ",";
            if (subString != "") {
                subString = subString + ",";
            }
            for (charIndex in stopToken.indices) {
                if (stopToken[charIndex] == ',') {
                    stopTokenArray.add(tmp)
                    tmp = ""
                    continue
                }
                tmp = tmp + stopToken[charIndex]
            }
            for (charIndex in subString.indices) {
                if (subString[charIndex] == ',') {
                    subStringArray.add(tmp)
                    tmp = ""
                    continue
                }
                tmp = tmp + subString[charIndex]
            }

        }
    }

    fun UpdateSystemPromptButton() {
        if (ollama) {
            btnSystemPrompt.visibility = View.VISIBLE
            btnSystemPrompt.text = if (systemPromptName.isNotEmpty()) {
                systemPromptName
            } else {
                OllamaPromptStore.buildPromptLabel(systemPrompt)
            }
        } else {
            btnSystemPrompt.visibility = View.GONE
            systemPromptPopup?.dismiss()
        }
    }

    fun ShowSystemPromptPopup() {
        val sharedPrefs = getSharedPreferences("saved_ollama_settings", Context.MODE_PRIVATE)
        val promptList = OllamaPromptStore.loadPrompts(sharedPrefs)

        if (promptList.isEmpty()) {
            Toast.makeText(applicationContext, "No saved system prompts", Toast.LENGTH_SHORT).show()
            return
        }

        val promptLabels = promptList.map { it.name }
        val listView = ListView(this)
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, promptLabels)
        listView.dividerHeight = 0

        val popupWindow = PopupWindow(
            listView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f
        popupWindow.setOnDismissListener {
            systemPromptPopup = null
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedPrompt = promptList[position]
            SaveSelectedSystemPrompt(selectedPrompt)
            popupWindow.dismiss()
        }

        listView.measure(
            View.MeasureSpec.makeMeasureSpec(resources.displayMetrics.widthPixels, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.UNSPECIFIED
        )
        val popupHeight = listView.measuredHeight
        popupWindow.showAsDropDown(btnSystemPrompt, 0, -(btnSystemPrompt.height + popupHeight))
        systemPromptPopup = popupWindow
    }

    fun SaveSelectedSystemPrompt(selectedPrompt: OllamaSavedPrompt) {
        val sharedPrefs = getSharedPreferences("saved_ollama_settings", Context.MODE_PRIVATE)
        val promptList = OllamaPromptStore.loadPrompts(sharedPrefs)
        val editor = sharedPrefs.edit()
        editor.putString("SYSTEM_PROMPT_NAME", selectedPrompt.name)
        editor.putString("SYSTEM_PROMPT", selectedPrompt.prompt)
        OllamaPromptStore.savePrompts(editor, promptList)
        editor.apply()
        systemPromptName = selectedPrompt.name
        systemPrompt = selectedPrompt.prompt
        UpdateSystemPromptButton()
        Toast.makeText(applicationContext, "System prompt selected", Toast.LENGTH_SHORT).show()
    }

    /* koboldConv and ollamaConve have to be reconstructed every send as the user may have switched
    servers in-between prompts */
    fun RawToKobold() {
        koboldConv.clear()
        for (index in rawConv.indices) {
            if (rawConv[index].substring(0, 3) == "u: ") {
                koboldConv.add(userIdentifier + rawConv[index].substring(3) + AIIdentifier)
            } else {
                koboldConv.add(rawConv[index].substring(4))
            }
        }
    }

    fun RawToOllama() {
        ollamaConv.clear()
        val ollamaSystemPrompt = OllamaMessage("system", systemPrompt)
        ollamaConv.add(ollamaSystemPrompt)
        for (index in rawConv.indices) {
            if (rawConv[index].substring(0, 3) == "u: ") {
                ollamaConv.add(OllamaMessage("user", rawConv[index].substring(3)))
            } else {
                ollamaConv.add(OllamaMessage("assistant", rawConv[index].substring(4)))
            }
        }
    }

    fun OllamaPOST() {
        val url = buildChatUrl(ipAdd, port)
        val data = OllamaJsonClass(
            model = model,
            messages = ollamaConv,
            stream = true
        )
        val gson = Gson()
        val jsonRaw = gson.toJson(data)
        Thread {
            var connection: HttpURLConnection? = null
            val result = StringBuilder()
            var hasStreamingMessage = false

            try {
                connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10000000
                    readTimeout = 10000000
                    doInput = true
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonRaw)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val responseStream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                } ?: throw IOException("HTTP $responseCode")

                BufferedReader(InputStreamReader(responseStream)).use { reader ->
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (line.isBlank()) {
                            continue
                        }

                        val chunk = JSONObject(line)
                        if (responseCode !in 200..299) {
                            throw IOException(chunk.optString("error", "HTTP $responseCode"))
                        }

                        val content = chunk.optJSONObject("message")?.optString("content").orEmpty()
                        if (content.isNotEmpty()) {
                            result.append(content)
                            if (!hasStreamingMessage) {
                                hasStreamingMessage = true
                                runOnUiThread {
                                    AddMessage("")
                                }
                            }
                            runOnUiThread {
                                UpdateLastAssistantMessage(result.toString())
                            }
                        }
                    }
                }

                runOnUiThread {
                    rawConv.add("ai: " + result.toString())
                    if (!hasStreamingMessage) {
                        AddMessage("")
                    }
                }
            } catch (error: Exception) {
                runOnUiThread {
                    msgData.add(Message("System: " + error.toString(), 2))
                    adapter.notifyDataSetChanged()
                    recyclerview.scrollToPosition(msgData.size - 1);
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    fun buildChatUrl(ipAdd: String, port: String?): String {
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
            "/api/chat",
            null,
            null
        ).toString()
    }

    fun KoboldPOST() {
        val volleyQueue = Volley.newRequestQueue(this)
        val url = "http://" + ipAdd + ":" + port + "/api/v1/generate"
        val initialPrompt = systemPrompt + contextPrompt
        val data = KoboldJsonClass(
            maxContextLength = maxContextLength,
            maxLength = maxLength,
            n = n,
            temperature = temp,
            typical = typical,
            topP = top_p,
            topA = top_a,
            topK = top_k,
            repPen = rep_pen,
            repPenRange = rep_pen_range,
            repPenSlope = rep_pen_slope,
            tfs = tfs,
            prompt = initialPrompt + koboldConv.joinToString(separator = ""),
            stopSequence = stopTokenArray
        )
        val gson = Gson()
        val jsonRaw = gson.toJson(data)
        val jsonObj = JSONObject(jsonRaw)
        var result = ""
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, jsonObj,
            { response ->
                val jsonResult = response.getJSONArray("results")
                val jsonIndex = jsonResult.getJSONObject(0)
                val jsonText = jsonIndex.get("text")
                result = jsonText.toString()
                if (subStringArray.isNotEmpty() && subStringArray.size > 0) {
                    for (item in subStringArray) {
                        result = result.substringBefore(item)
                    }
                }
                rawConv.add("ai: " + result)
                AddMessage(result)
            },
            { error ->
                msgData.add(Message("System: " + error.toString(), 2))
                adapter.notifyDataSetChanged()
                recyclerview.scrollToPosition(msgData.size - 1);
                // tvCheck.setTextColor(Color.parseColor("#FFFFFFFF"))
                // tvCheck.text = error.toString()
            }
        )
        jsonObjectRequest.setRetryPolicy(
            DefaultRetryPolicy(
                10000000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        volleyQueue.add(jsonObjectRequest);
    }


    fun AddMessage(message: String) {
        if (message.startsWith(" ")) {
            msgData.add(Message("Assistant:" + message, 1))
        } else {
            msgData.add(Message("Assistant: " + message, 1))
        }
        adapter.notifyDataSetChanged()
        ScrollToBottom()
    }

    fun UpdateLastAssistantMessage(message: String) {
        if (msgData.isEmpty()) {
            return
        }

        val updatedMessage = if (message.startsWith(" ")) {
            "Assistant:$message"
        } else {
            "Assistant: $message"
        }
        msgData[msgData.size - 1] = Message(updatedMessage, 1)
        adapter.notifyDataSetChanged()
        ScrollToBottom()
    }

    fun ScrollToBottom() {
        if (msgData.isEmpty()) {
            return
        }
        recyclerview.post {
            recyclerview.scrollToPosition(msgData.size - 1)
        }
    }

    fun CreateFolder() {
        val folder = File(Environment.getExternalStorageDirectory(), "ChibiChat")
        if (!folder.exists()) {
            folder.mkdir()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.opSettings -> {
                val settingsPage = Intent(this, SettingsActivity::class.java);
                startActivity(settingsPage);
                true
            }

            R.id.opServer -> {
                val serverPage = Intent(this, ServerActivity::class.java);
                startActivity(serverPage);
                true
            }

            R.id.opSave -> {
                ShowSavePopUp()
                true
            }

            R.id.opClear -> {
                msgData.clear()
                rawConv.clear()
                adapter.notifyDataSetChanged()
                true
            }

            R.id.opLoad -> {
                ShowLoadPopUp()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun ShowLoadPopUp() {
        val inflater = LayoutInflater.from(this)
        val popupView: View = inflater.inflate(R.layout.activity_load, null)
        val container: RecyclerView = popupView.findViewById<RecyclerView>(R.id.rvLoads)
        val btnDeleteStory: Button = popupView.findViewById<Button>(R.id.btnDeleteStory)
        val btnLoadStory: Button = popupView.findViewById<Button>(R.id.btnLoadStory)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        savedStoryData = loadArrayFromFile(this)
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        //val savedDataFile = loadArrayFromFile(this)
        container.layoutManager = LinearLayoutManager(this)
        val popupAdapter = RvLoadAdapter(savedStoryData)
        container.adapter = popupAdapter

        btnDeleteStory.setOnClickListener() {
            val position = popupAdapter.selectedPosition
            if (position != RecyclerView.NO_POSITION) {
                savedStoryData.removeAt(position)
                saveArrayToFile(this, savedStoryData)
                popupAdapter.selectedPosition = RecyclerView.NO_POSITION
                popupAdapter.notifyDataSetChanged()
            }
        }
        btnLoadStory.setOnClickListener() {
            val position = popupAdapter.selectedPosition
            if (position != RecyclerView.NO_POSITION) {
                msgData.clear()
                msgData.addAll(savedStoryData[position].onscreenMessage)
                rawConv.clear()
                rawConv = savedStoryData[position].savedDataArray
                adapter.notifyDataSetChanged()
                Toast.makeText(
                    applicationContext,
                    savedStoryData[position].name + " Loaded!",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

    }

    fun ShowSavePopUp() {
        val inflater = LayoutInflater.from(this)
        val popupSaveView: View = inflater.inflate(R.layout.activity_save, null)
        val btnSaveName: Button = popupSaveView.findViewById<Button>(R.id.btnSaveName)
        val etSaveName: EditText = popupSaveView.findViewById<EditText>(R.id.etSaveName)
        val popupWindow = PopupWindow(
            popupSaveView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.showAtLocation(popupSaveView, Gravity.CENTER, 0, 0)

        btnSaveName.setOnClickListener() {
            val savedDataObject = SavedData(etSaveName.text.toString(), rawConv, msgData)
            savedStoryData.add(savedDataObject)
            saveArrayToFile(this, savedStoryData)
            Toast.makeText(
                applicationContext,
                etSaveName.text.toString() + " Saved!",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    fun saveArrayToFile(context: Context, array: ArrayList<SavedData>) {
        val fileName = "array_data"

        try {
            val fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            ObjectOutputStream(fileOutputStream).use { outputStream ->
                outputStream.writeObject(array)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            msgData.add(Message("System: " + e.toString(), 2))
        }
    }

    fun loadArrayFromFile(context: Context): ArrayList<SavedData> {
        val fileName = "array_data"
        return try {
            val fileInputStream = context.openFileInput(fileName)
            ObjectInputStream(fileInputStream).use { inputStream ->
                inputStream.readObject() as ArrayList<SavedData>
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ArrayList()
        }
    }
}
