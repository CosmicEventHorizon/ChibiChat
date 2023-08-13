package com.pirouette.chibichat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ServerActivity : AppCompatActivity() {
    lateinit var btnTermux: Button
    lateinit var btnKobold: Button
    lateinit var btnServerBack: Button
    lateinit var tvTermux: TextView
    lateinit var tvKobold: TextView
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        InitializeWidgets()
        isTermuxInstalled(this)
        btnServerBack.setOnClickListener {
            finish()
        }
        btnTermux.setOnClickListener {
            val url = "https://github.com/termux/termux-app/releases/tag/v0.118.0"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        btnKobold.setOnClickListener {
            val runCommand: String = getString(R.string.run_command)
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", runCommand)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(applicationContext, "Copied Command To Clipboard", Toast.LENGTH_LONG).show()
        }
    }

    fun InitializeWidgets() {
        btnServerBack = findViewById<Button>(R.id.btnServerBack)
        btnTermux = findViewById<Button>(R.id.btnTermux)
        btnKobold = findViewById<Button>(R.id.btnKobold)
        tvTermux = findViewById<Button>(R.id.tvTermux)
        tvKobold = findViewById<Button>(R.id.tvKobold)
    }

    fun isTermuxInstalled(context: Context) {
        val packageManager = context.packageManager
        try {
            packageManager.getPackageInfo("com.termux",0)
            tvTermux.text = "Termux is installed"
            tvTermux.setTextColor(ContextCompat.getColor(this, R.color.green));
            btnTermux.visibility = View.INVISIBLE

        } catch (e: PackageManager.NameNotFoundException) {
            tvTermux.text = "Termux is not installed"
            tvTermux.setTextColor(ContextCompat.getColor(this, R.color.red));
            btnTermux.visibility = View.VISIBLE

        }
    }


}