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
        lateinit var btnDeleteLast: Button
        lateinit var recyclerview: RecyclerView
        lateinit var etPrompt: EditText
        lateinit var adapter: RvAdapter
        lateinit var ipAdd: String
        lateinit var port: String
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
        val stopTokenArray: ArrayList<String> = ArrayList()
        val subStringArray: ArrayList<String> = ArrayList()
        var msgData = ArrayList<Message>()
        var savedStoryData = ArrayList<SavedData>()
        var convArray = mutableListOf<String>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CreateFolder()
        btnSend = findViewById(R.id.btnSendText)
        btnDeleteLast = findViewById(R.id.btnDeleteLast)
        etPrompt = findViewById(R.id.etPrompt)
        //tvCheck = findViewById(R.id.tvOutput)
        recyclerview = findViewById<RecyclerView>(R.id.rvMessages)
        recyclerview.layoutManager = LinearLayoutManager(this)
        adapter = RvAdapter(msgData)
        recyclerview.adapter = adapter
        savedStoryData = loadArrayFromFile(this)
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
        btnDeleteLast.setOnClickListener()
        {
            msgData.removeLastOrNull()
            convArray.removeLastOrNull()
            adapter.notifyDataSetChanged()
            recyclerview.scrollToPosition(msgData.size - 1);
        }

    }
    fun LoadData(){
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
        val url = "http://"+ipAdd+":"+port+"/api/v1/generate"
        val initialPrompt = systemPrompt+contextPrompt
        val data = JsonClass(
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
            prompt = initialPrompt + convArray.joinToString(separator = ""),
            stopSequence = stopTokenArray)
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
        //val savedDataFile = loadArrayFromFile(this)
        container.layoutManager = LinearLayoutManager(this)
        val popupAdapter = RvLoadAdapter(savedStoryData)
        container.adapter = popupAdapter

        btnDeleteStory.setOnClickListener(){
            val position = popupAdapter.selectedPosition
            if (position != RecyclerView.NO_POSITION) {
                savedStoryData.removeAt(position)
                saveArrayToFile(this, savedStoryData)
                popupAdapter.selectedPosition = RecyclerView.NO_POSITION
                popupAdapter.notifyDataSetChanged()
            }
        }
        btnLoadStory.setOnClickListener(){
            val position = popupAdapter.selectedPosition
            if (position != RecyclerView.NO_POSITION) {
                msgData.clear()
                msgData.addAll(savedStoryData[position].savedDataArray)
                convArray.clear()
                convArray.addAll(savedStoryData[position].promptListSaved)
                adapter.notifyDataSetChanged()
                Toast.makeText(applicationContext, savedStoryData[position].name + " Loaded!", Toast.LENGTH_SHORT).show()

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
            ArrayList()
        }
    }


}


