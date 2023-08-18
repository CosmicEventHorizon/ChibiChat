package com.pirouette.chibichat

import android.content.Context
import android.content.Intent
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


class MainActivity : AppCompatActivity() {
        //lateinit var tvCheck: TextView
        lateinit var btnSend: Button
        lateinit var recyclerview: RecyclerView
        lateinit var etPrompt: EditText
        lateinit var adapter: RvAdapter
        lateinit var ipAdd: String
        var maxContextLength = 0
        var maxLength = 0
        lateinit var systemPrompt: String
        lateinit var contextPrompt: String
        lateinit var stopToken: String
        lateinit var userIdentifier: String
        lateinit var AIIdentifier: String
        lateinit var subString: String
        val stopTokenArray: ArrayList<String> = ArrayList()
        val subStringArray: ArrayList<String> = ArrayList()
        var msgData = ArrayList<Message>()
        val savedStoryData = ArrayList<SavedData>()
        var convArray = mutableListOf<String>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CreateFolder()
        btnSend = findViewById(R.id.btnSendText)
        etPrompt = findViewById(R.id.etPrompt)
        //tvCheck = findViewById(R.id.tvOutput)
        recyclerview = findViewById<RecyclerView>(R.id.rvMessages)
        recyclerview.layoutManager = LinearLayoutManager(this)
        adapter = RvAdapter(msgData)
        recyclerview.adapter = adapter
        btnSend.setOnClickListener()
        {
            LoadData()
            msgData.add(Message("User: " + etPrompt.text.toString(), 0))
            adapter.notifyDataSetChanged()
            convArray.add(userIdentifier+etPrompt.text.toString()+AIIdentifier)
            recyclerview.scrollToPosition(msgData.size - 1);
            GetJsonData()
            etPrompt.setText("")
        }

    }
    fun LoadData(){
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
        stopTokenArray.clear()
        subStringArray.clear()
        var tmp = ""
        stopToken = stopToken + ",";
        if(subString != "") {
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

     fun GetJsonData(){
        val volleyQueue = Volley.newRequestQueue(this)
        val url = "http://"+ipAdd+":5001/api/v1/generate"
        val initialPrompt = systemPrompt+contextPrompt
        val data = JsonClass(maxContextLength = maxContextLength, maxLength = maxLength, prompt = initialPrompt + convArray.joinToString(separator = ""), stopSequence = stopTokenArray)
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
                if (subStringArray.isNotEmpty() && subStringArray.size>0) {
                    for (item in subStringArray) {
                        result = result.substringBefore(item)
                    }
                }
                convArray.add(result)
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

    fun AddMessage (message: String)
    {
        if (message.first() == ' ') {
            msgData.add(Message("Kobold:" + message, 1))
        }else {
            msgData.add(Message("Kobold: " + message, 1))
        }
        adapter.notifyDataSetChanged()
        recyclerview.scrollToPosition(msgData.size - 1);
    }
    fun CreateFolder(){
        val folder = File(Environment.getExternalStorageDirectory(), "ChibiChat")
        if(!folder.exists()) {
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
            R.id.opServer ->{
                val serverPage = Intent(this, ServerActivity::class.java);
                startActivity(serverPage);
                true
            }
            R.id.opSave ->{
                ShowSavePopUp()
                true
            }
            R.id.opClear ->{
                msgData.clear()
                convArray.clear()
                adapter.notifyDataSetChanged()
                true
            }
            R.id.opLoad ->{
                ShowLoadPopUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun ShowLoadPopUp(){
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
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        val savedDataFile = loadArrayFromFile(this)
        container.layoutManager = LinearLayoutManager(this)
        val popupAdapter = RvLoadAdapter(savedDataFile)
        container.adapter = popupAdapter
        btnDeleteStory.setOnClickListener(){
            val position = popupAdapter.selectedPosition
            if (position != RecyclerView.NO_POSITION) {
                savedDataFile.removeAt(position)
                saveArrayToFile(this, savedDataFile)
                popupAdapter.selectedPosition = RecyclerView.NO_POSITION
                popupAdapter.notifyDataSetChanged()
            }
        }
        btnLoadStory.setOnClickListener(){
            val position = popupAdapter.selectedPosition
            if (position != RecyclerView.NO_POSITION) {
                msgData.clear()
                msgData.addAll(savedDataFile[position].savedDataArray)
                convArray.clear()
                convArray.addAll(savedDataFile[position].promptListSaved)
                adapter.notifyDataSetChanged()
                Toast.makeText(applicationContext, savedDataFile[position].name + " Loaded!", Toast.LENGTH_SHORT).show()

            }
        }
   
    }
    fun ShowSavePopUp(){
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

        btnSaveName.setOnClickListener(){
            val currentMsgData = ArrayList(msgData)
            val currentConversation = convArray.toMutableList()
            val savedDataObject = SavedData(etSaveName.text.toString(), currentMsgData, currentConversation)
            savedStoryData.add(savedDataObject)
            saveArrayToFile(this, savedStoryData)
            Toast.makeText(applicationContext, etSaveName.text.toString() + " Saved!", Toast.LENGTH_SHORT).show()
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
            msgData.add(Message("System: " + e.toString(), 2))
            ArrayList()
        }
    }


}


