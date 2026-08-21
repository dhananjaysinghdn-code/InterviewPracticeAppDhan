package com.example.interviewpractice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale

data class Q(val question: String, val answer: String, val keywords: List<String>)
data class Attempt(val questionIndex: Int, val spoken: String, val marks: Int)

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
    private val attempts = mutableListOf<Attempt>()
    private val handler = Handler(Looper.getMainLooper())
    private var silenceRunnable: Runnable? = null
    private lateinit var root: LinearLayout
    private lateinit var qText: TextView
    private lateinit var result: TextView
    private lateinit var score: TextView
    private lateinit var speak: Button

    override fun onCreate(b: Bundle?) { super.onCreate(b); showHome() }

    private fun baseLayout(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(28, 28, 28, 28); setBackgroundColor(Color.rgb(245, 247, 255))
    }
    private fun title(text: String, size: Float = 26f): TextView = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(Color.rgb(35, 42, 85)); setPadding(0, 8, 0, 18); gravity = Gravity.CENTER
    }
    private fun button(text: String): Button = Button(this).apply {
        this.text = text; textSize = 16f; isAllCaps = false; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(70, 90, 210)); setPadding(12, 10, 12, 10)
    }

    private fun showHome() {
        root = baseLayout(); root.addView(title("🎯 Interview Practice", 28f)); root.addView(title("Practice • Speak • Review • Improve", 18f))
        val test = button("🎤 Start Test"); val list = button("📚 All Questions & Answers"); val review = button("📝 Review My Answers"); val clear = button("🗑️ Clear My Results")
        root.addView(test); root.addView(list); root.addView(review); root.addView(clear); setContentView(root)
        test.setOnClickListener { showTest() }; list.setOnClickListener { showQuestionList() }; review.setOnClickListener { showReview() }
        clear.setOnClickListener { attempts.clear(); total = 0; answered = 0; Toast.makeText(this, "Results cleared", Toast.LENGTH_SHORT).show(); showHome() }
    }

    private fun showTest() {
        root = baseLayout(); root.addView(button("← Home").also { it.setOnClickListener { stopListening(); showHome() } })
        score = title("Score: $total", 18f); root.addView(score); qText = title("", 22f); qText.gravity = Gravity.START; root.addView(qText)
        speak = button("🎤 Start Speaking"); root.addView(speak)
        result = TextView(this).apply { textSize = 17f; setTextColor(Color.DKGRAY); setPadding(0, 18, 0, 0) }; root.addView(result)
        val next = button("➡️ Next Question"); root.addView(next); setContentView(root); showQuestion()
        speak.setOnClickListener { if (listening) finishAnswer() else startListening() }
        next.setOnClickListener { stopListening(); index = (index + 1) % questions.size; result.text = ""; showQuestion() }
    }

    private fun showQuestionList() {
        val scroll = ScrollView(this); val list = baseLayout(); list.addView(button("← Home").also { it.setOnClickListener { showHome() } }); list.addView(title("📚 Questions & Answers", 25f))
        questions.forEachIndexed { i, q ->
            val card = TextView(this).apply { text = "Q${i + 1}. ${q.question}\n\nAnswer: ${q.answer}"; textSize = 16f; setTextColor(Color.rgb(35, 42, 60)); setPadding(20, 20, 20, 20); setBackgroundColor(if (i % 2 == 0) Color.WHITE else Color.rgb(232, 238, 255)); setOnClickListener { index = i; showTest() } }
            list.addView(card); list.addView(Space(this), LinearLayout.LayoutParams(1, 12))
        }
        scroll.addView(list); setContentView(scroll)
    }

    private fun showReview() {
        val scroll = ScrollView(this); val list = baseLayout(); list.addView(button("← Home").also { it.setOnClickListener { showHome() } }); list.addView(title("📝 My Answer Review", 25f))
        if (attempts.isEmpty()) list.addView(title("No answers yet. Start a test first.", 18f))
        attempts.forEach { a ->
            val q = questions[a.questionIndex]
            val card = TextView(this).apply { text = "Q${a.questionIndex + 1}. ${q.question}\n\n🗣️ Your Answer:\n${a.spoken.ifBlank { "(No answer detected)" }}\n\n⭐ Your Score: ${a.marks}/10\n\n✅ Correct Answer:\n${q.answer}"; textSize = 16f; setTextColor(Color.rgb(35, 42, 60)); setPadding(20, 20, 20, 20); setBackgroundColor(Color.WHITE) }
            list.addView(card); list.addView(Space(this), LinearLayout.LayoutParams(1, 14))
        }
        scroll.addView(list); setContentView(scroll)
    }

    private fun showQuestion() { qText.text = "Q${index + 1}: ${questions[index].question}"; score.text = "Score: $total  •  Answered: $answered/${questions.size}"; speak.isEnabled = true; speak.text = "🎤 Start Speaking" }

    private fun startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 200); return }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { result.text = "Speech recognition available nahi hai."; return }
        recognizer?.destroy(); recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) { result.text = "🎧 Sun raha hoon... Answer pura bolo." }
            override fun onBeginningOfSpeech() { listening = true; resetSilenceTimer() }
            override fun onRmsChanged(r: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() { if (listening && !finishing) restartListening() }
            override fun onError(e: Int) { if (listening && !finishing) restartListening() }
            override fun onResults(data: Bundle?) { val text = data?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty(); if (text.isNotBlank()) spokenText.append(" ").append(text); resetSilenceTimer(); if (listening && !finishing) restartListening() }
            override fun onPartialResults(data: Bundle?) { val text = data?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty(); if (text.isNotBlank()) { result.text = "🎧 $text\n\nKeep speaking..."; resetSilenceTimer() } }
            override fun onEvent(t: Int, p: Bundle?) {}
        })
        finishing = false; listening = true; spokenText.clear(); speak.text = "⏹️ Finish Answer"; result.text = "🎧 Listening..."
        startRecognition()
    }

    private fun startRecognition() { try { recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH); putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true); putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L); putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000L) }) } catch (_: Exception) {} }
    private fun restartListening() { if (!listening || finishing) return; handler.postDelayed({ if (listening && !finishing) startRecognition() }, 250) }
    private fun resetSilenceTimer() { silenceRunnable?.let { handler.removeCallbacks(it) }; silenceRunnable = Runnable { if (listening && !finishing) finishAnswer() }; handler.postDelayed(silenceRunnable!!, 3000) }

    private fun finishAnswer() {
        finishing = true; listening = false; silenceRunnable?.let { handler.removeCallbacks(it) }; try { recognizer?.stopListening() } catch (_: Exception) {}; speak.text = "🎤 Start Speaking"; evaluate(spokenText.toString().trim())
    }
    private fun stopListening() { finishing = true; listening = false; silenceRunnable?.let { handler.removeCallbacks(it) }; try { recognizer?.stopListening(); recognizer?.destroy() } catch (_: Exception) {}; recognizer = null }

    private fun evaluate(spoken: String) {
        val s = spoken.lowercase(); val hits = questions[index].keywords.count { s.contains(it) }; val pct = if (questions[index].keywords.isEmpty()) 0 else hits * 100 / questions[index].keywords.size
        val marks = when { pct >= 75 -> 10; pct >= 50 -> 7; pct >= 25 -> 4; else -> 1 }; total += marks; answered++; attempts.add(Attempt(index, spoken, marks))
        val verdict = if (marks >= 7) "✅ Good Answer" else if (marks >= 4) "⚠️ Partially Correct" else "❌ Need Improvement"
        result.text = "🗣️ Your Answer:\n${spoken.ifBlank { "(No answer detected)" }}\n\n$verdict\n⭐ How correct: $marks/10\n\n✅ Correct Answer:\n${questions[index].answer}"; score.text = "Score: $total  •  Answered: $answered/${questions.size}"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) { super.onRequestPermissionsResult(requestCode, permissions, results); if (requestCode == 200 && results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED) startListening() }
    override fun onDestroy() { stopListening(); super.onDestroy() }
}
