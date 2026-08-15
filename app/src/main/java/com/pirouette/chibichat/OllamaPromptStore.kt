package com.pirouette.chibichat

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class OllamaSavedPrompt(val name: String, val prompt: String)

object OllamaPromptStore {
    private const val PROMPT_LIST_KEY = "SYSTEM_PROMPT_LIST"

    fun loadPrompts(sharedPrefs: SharedPreferences): ArrayList<OllamaSavedPrompt> {
        val promptList = ArrayList<OllamaSavedPrompt>()
        val savedPromptList = sharedPrefs.getString(PROMPT_LIST_KEY, null)

        if (!savedPromptList.isNullOrEmpty()) {
            val jsonArray = JSONArray(savedPromptList)
            for (index in 0 until jsonArray.length()) {
                val jsonItem = jsonArray.opt(index)
                if (jsonItem is JSONObject) {
                    val name = jsonItem.optString("name").trim()
                    val prompt = jsonItem.optString("prompt")
                    if (name.isNotEmpty() && prompt.isNotEmpty()) {
                        promptList.add(OllamaSavedPrompt(name, prompt))
                    }
                } else {
                    val prompt = jsonArray.optString(index)
                    if (prompt.isNotEmpty()) {
                        promptList.add(OllamaSavedPrompt(buildPromptLabel(prompt), prompt))
                    }
                }
            }
        }

        val currentPrompt = sharedPrefs.getString("SYSTEM_PROMPT", "").orEmpty()
        val currentPromptName = sharedPrefs.getString("SYSTEM_PROMPT_NAME", "").orEmpty()
        if (currentPrompt.isNotEmpty() && currentPrompt != "null" && promptList.none { it.prompt == currentPrompt }) {
            val savedName = if (currentPromptName.isNotEmpty()) {
                currentPromptName
            } else {
                buildPromptLabel(currentPrompt)
            }
            promptList.add(0, OllamaSavedPrompt(savedName, currentPrompt))
        }

        return promptList
    }

    fun savePrompts(editor: SharedPreferences.Editor, promptList: List<OllamaSavedPrompt>) {
        val jsonArray = JSONArray()
        for (promptItem in promptList) {
            if (promptItem.name.isNotEmpty() && promptItem.prompt.isNotEmpty()) {
                val jsonObject = JSONObject()
                jsonObject.put("name", promptItem.name)
                jsonObject.put("prompt", promptItem.prompt)
                jsonArray.put(jsonObject)
            }
        }
        editor.putString(PROMPT_LIST_KEY, jsonArray.toString())
    }

    fun buildPromptLabel(prompt: String): String {
        if (prompt.isBlank()) {
            return "System Prompt"
        }
        val singleLinePrompt = prompt.replace("\n", " ").trim()
        return if (singleLinePrompt.length > 24) {
            singleLinePrompt.substring(0, 24) + "..."
        } else {
            singleLinePrompt
        }
    }
}