package com.example.interviewpractice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale

// AI-free prototype: answers are checked using predefined keywords.
data class Q(val question: String, val answer: String, val keywords: List<String>)

class MainActivity : Activity() {
    private val questions = listOf(
        Q("What is Selenium WebDriver?", "Selenium WebDriver is an API used to automate web browsers.", listOf("selenium", "webdriver", "automate", "browser")),
        Q("What is an XPath?", "XPath is a locator used to identify elements in an HTML or XML document.", listOf("xpath", "locator", "element")),
        Q("What is an explicit wait?", "An explicit wait waits for a specific condition before continuing.", listOf("explicit", "wait", "condition")),
        Q("What is TestNG?", "TestNG is a testing framework for Java used to organize and execute automated tests.", listOf("testng", "testing", "framework", "java")),
        Q("What is Page Object Model?", "POM is a design pattern that keeps page locators and page actions in page classes.", listOf("page", "object", "model", "locator", "action")),
        Q("What is SQL?", "SQL is a language used to query and manage data in relational databases.", listOf("sql", "query", "database")),
        Q("What is an API?", "An API is an interface that allows software systems to communicate with each other.", listOf("api", "interface", "communicate"))
    )

    private var index = 0
    private var total = 0
    private var answered = 0
    private var listening = false
    private var finishing = false
    private var spokenText = StringBuilder()
    private var recognizer: SpeechRecognizer? = null
    private lateinit var root: LinearLayout
    private lateinit var qText: TextView
    private lateinit var result: TextView
    private lateinit var score: TextView
    private lateinit var speak: Button
    private lateinit var done: Button

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        showHome()
    }

    private fun baseLayout(): LinearLayout {
        val l = LinearLayout(this)
        l.orientation = LinearLayout.VERTICAL
        l.setPadding(28, 28, 28, 28)
        l.setBackgroundColor(Color.rgb(245, 247, 255))
        return l
    }

    private fun title(text: String, size: Float = 26f): TextView = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(Color.rgb(35, 42, 85)); setPadding(0, 8, 0, 18)
        gravity = Gravity.CENTER
    }

    private fun button(text: String): Button = Button(this).apply {
        this.text = text; textSize = 16f; isAllCaps = false
        setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(70, 90, 210))
        setPadding(12, 10, 12, 10)
    }

    private fun showHome() {
        root = baseLayout()
        root.addView(title("🎯 Interview Practice", 28f))
        val info = title("Practice • Speak • Get Score", 18f)
        root.addView(info)
        val test = button("🎤 Start Test")
        val list = button("📚 All Questions & Answers")
        root.addView(test); root.addView(list)
        test.setOnClickListener { showTest() }
        list.setOnClickListener { showQuestionList() }
        setContentView(root)
    }

    private fun showTest() {
        root = baseLayout()
        val back = button("← Home")
        root.addView(back)
        score = title("Score: $total", 18f)
        root.addView(score)
        qText = title("", 22f)
        qText.gravity = Gravity.START
        root.addView(qText)

        speak = button("🎤 Start Speaking")
        done = button("✅ Done")
        done.isEnabled = false
        root.addView(speak); root.addView(done)

        result = TextView(this).apply { textSize = 17f; setTextColor(Color.DKGRAY); setPadding(0, 18, 0, 0) }
        root.addView(result)
        val next = button("➡️ Next Question")
        root.addView(next)
        setContentView(root)
        showQuestion()

        back.setOnClickListener { stopListening(); showHome() }
        speak.setOnClickListener { startListening() }
        done.setOnClickListener { finishAnswer() }
        next.setOnClickListener {
            stopListening(); index = (index + 1) % questions.size; result.text = ""; showQuestion()
        }
    }

    private fun showQuestionList() {
        val scroll = ScrollView(this)
        val list = baseLayout()
        val back = button("← Home")
        list.addView(back)
        list.addView(title("📚 Questions & Answers", 25f))
        questions.forEachIndexed { i, q ->
            val card = TextView(this).apply {
                text = "Q${i + 1}. ${q.question}\n\nAnswer: ${q.answer}\n\nKeywords: ${q.keywords.joinToString(", ")}"
                textSize = 16f; setTextColor(Color.rgb(35, 42, 60)); setPadding(20, 20, 20, 20)
                setBackgroundColor(if (i % 2 == 0) Color.WHITE else Color.rgb(232, 238, 255))
            }
            list.addView(card)
            val space = Space(this); list.addView(space, LinearLayout.LayoutParams(1, 12))
        }
        scroll.addView(list); setContentView(scroll)
        back.setOnClickListener { showHome() }
    }

    private fun showQuestion() {
        qText.text = "Q${index + 1}: ${questions[index].question}"
        score.text = "Score: $total  •  Answered: $answered/${questions.size}"
        speak.isEnabled = true; done.isEnabled = false
    }

    private fun startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 200)
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            result.text = "Speech recognition available nahi hai."
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) { result.text = "🎧 Sun raha hoon... Answer pura bolo. Jab finish ho, Done dabao." }
            override fun onBeginningOfSpeech() { listening = true }
            override fun onRmsChanged(r: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() { if (listening && !finishing) restartListening() }
            override fun onError(e: Int) { if (listening && !finishing) restartListening() }
            override fun onResults(data: Bundle?) {
                val text = data?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (text.isNotBlank()) { spokenText.append(" ").append(text) }
                if (listening && !finishing) restartListening()
            }
            override fun onPartialResults(data: Bundle?) {
                val text = data?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (text.isNotBlank()) result.text = "🎧 $text\n\nDone dabao jab answer complete ho."
            }
            override fun onEvent(t: Int, p: Bundle?) {}
        })
        finishing = false; listening = true; spokenText.clear(); done.isEnabled = true; speak.isEnabled = false
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
        recognizer!!.startListening(i)
    }

    private fun restartListening() {
        if (!listening || finishing) return
        try {
            recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
            })
        } catch (_: Exception) {}
    }

    private fun finishAnswer() {
        finishing = true; listening = false
        try { recognizer?.stopListening() } catch (_: Exception) {}
        speak.isEnabled = true; done.isEnabled = false
        evaluate(spokenText.toString().trim())
    }

    private fun stopListening() {
        finishing = true; listening = false
        try { recognizer?.stopListening(); recognizer?.destroy() } catch (_: Exception) {}
        recognizer = null
    }

    private fun evaluate(spoken: String) {
        val s = spoken.lowercase()
        val hits = questions[index].keywords.count { s.contains(it) }
        val pct = if (questions[index].keywords.isEmpty()) 0 else hits * 100 / questions[index].keywords.size
        val marks = when { pct >= 75 -> 10; pct >= 50 -> 7; pct >= 25 -> 4; else -> 1 }
        total += marks; answered++
        val verdict = if (marks >= 7) "✅ Good Answer" else if (marks >= 4) "⚠️ Partially Correct" else "❌ Need Improvement"
        result.text = "Your Answer:\n${if (spoken.isBlank()) "(No answer detected)" else spoken}\n\n$verdict\nMarks: $marks/10\n\nExpected Answer:\n${questions[index].answer}"
        score.text = "Score: $total  •  Answered: $answered/${questions.size}"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == 200 && results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED) startListening()
    }

    override fun onDestroy() { stopListening(); super.onDestroy() }
}
