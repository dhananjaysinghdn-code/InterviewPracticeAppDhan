package com.example.interviewpractice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

data class Attempt(val questionIndex: Int, val spokenAnswer: String, val score: Int)

class MainActivity : Activity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var handler: Handler
    private lateinit var root: LinearLayout
    private lateinit var questionView: TextView
    private lateinit var answerView: TextView
    private lateinit var scoreView: TextView
    private lateinit var speakButton: Button
    private val questions = QuestionBank.create()
    private val attempts = mutableListOf<Attempt>()
    private var questionIndex = 0
    private var totalScore = 0
    private var answeredCount = 0
    private var speech: SpeechRecognizer? = null
    private var listening = false
    private var finishing = false
    private val spoken = StringBuilder()
    private var examQuestions = emptyList<InterviewQuestion>()
    private var examIndex = 0
    private var examScore = 0
    private var examTimer: CountDownTimer? = null
    private lateinit var examQuestion: TextView
    private lateinit var examStatus: TextView
    private lateinit var examNext: Button

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        handler = Handler(Looper.getMainLooper())
        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        showHome()
    }

    private fun bg() = prefs.getInt("background", Color.rgb(242, 245, 255))
    private fun primary() = prefs.getInt("primary", Color.rgb(78, 93, 210))

    private fun layout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(20, 20, 20, 20)
        setBackgroundColor(bg())
    }

    private fun title(text: String, size: Float = 20f) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(Color.rgb(25, 35, 70))
        setPadding(10, 12, 10, 12)
        gravity = Gravity.CENTER
    }

    private fun button(text: String) = Button(this).apply {
        this.text = text
        textSize = 16f
        isAllCaps = false
        setTextColor(Color.WHITE)
        setBackgroundColor(primary())
    }

    private fun card(text: String, color: Int = Color.WHITE) = TextView(this).apply {
        this.text = text
        textSize = 16f
        setTextColor(Color.DKGRAY)
        setPadding(20, 18, 20, 18)
        setBackgroundColor(color)
        elevation = 6f
    }

    private fun add(v: View) {
        root.addView(v)
        v.alpha = 0f
        v.animate().alpha(1f).setDuration(220).start()
    }

    private fun character() = title("👩‍💻✨", 40f).also {
        if (prefs.getBoolean("animation", true)) {
            val a = TranslateAnimation(-25f, 25f, 0f, 0f)
            a.duration = 1300
            a.repeatMode = Animation.REVERSE
            a.repeatCount = Animation.INFINITE
            it.startAnimation(a)
        }
    }

    private fun showHome() {
        root = layout()
        add(title("📚  📖  📕  📗  📘", 24f))
        add(character())
        add(title("🎯 Interview Practice", 29f))
        add(title("420+ Q&A • Speaking • Exam • Quick Code", 16f))
        add(button("🎤 Speaking Test").apply { setOnClickListener { showTest() } })
        add(button("📚 All Questions & Answers").apply { setOnClickListener { showAll() } })
        add(button("📝 Review My Answers").apply { setOnClickListener { showReview() } })
        add(button("📝 Exam Mode").apply { setOnClickListener { examSetup() } })
        add(button("⚡ Selenium Quick Code").apply { setOnClickListener { quickCode() } })
        add(button("⚙️ Settings").apply { setOnClickListener { settings() } })
        add(button("🗑️ Clear Results").apply {
            setOnClickListener {
                attempts.clear(); totalScore = 0; answeredCount = 0
                Toast.makeText(this@MainActivity, "Results cleared", Toast.LENGTH_SHORT).show()
            }
        })
        setContentView(root)
    }

    private fun showTest() {
        root = layout()
        add(button("← Home").apply { setOnClickListener { stopVoice(); showHome() } })
        scoreView = title("⭐ Score: $totalScore • Answered: $answeredCount", 17f); add(scoreView)
        questionView = card(""); add(questionView)
        speakButton = button("🎤 Start Speaking"); add(speakButton)
        answerView = card(""); answerView.visibility = View.GONE; add(answerView)
        add(button("➡️ Next Question").apply {
            setOnClickListener { stopVoice(); questionIndex = (questionIndex + 1) % questions.size; showQuestion() }
        })
        setContentView(root)
        showQuestion()
        speakButton.setOnClickListener { if (listening) finishVoice() else startVoice() }
    }

    private fun showQuestion() {
        questionView.text = "Q${questionIndex + 1}\n\n${questions[questionIndex].question}"
        speakButton.text = "🎤 Start Speaking"
        answerView.visibility = View.GONE
    }

    private fun startVoice() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 7); return
        }
        try {
            speech?.destroy()
            speech = SpeechRecognizer.createSpeechRecognizer(this)
            speech?.setRecognitionListener(listener)
            spoken.clear(); finishing = false; listening = true
            speakButton.text = "⏹️ Listening..."
            answerView.visibility = View.VISIBLE
            answerView.text = "🎧 Ready... speak your answer."
            startRecognition()
        } catch (_: Exception) {
            answerView.visibility = View.VISIBLE
            answerView.text = "Voice recognition could not start."
        }
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(p: Bundle?) { answerView.text = "🎧 Ready..." }
        override fun onBeginningOfSpeech() { answerView.text = "🎧 Listening..." }
        override fun onRmsChanged(v: Float) {}
        override fun onBufferReceived(v: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(e: Int) { if (listening && !finishing) handler.postDelayed({ startRecognition() }, 350) }
        override fun onResults(b: Bundle?) {
            val text = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (text.isNotBlank()) spoken.append(" ").append(text)
            if (!finishing) handler.postDelayed({ finishVoice() }, 400)
        }
        override fun onPartialResults(b: Bundle?) {
            val text = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (text.isNotBlank()) answerView.text = "🎧 $text"
        }
        override fun onEvent(t: Int, p: Bundle?) {}
    }

    private fun startRecognition() {
        try {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            }
            speech?.startListening(i)
        } catch (_: Exception) { answerView.text = "Voice recognition unavailable." }
    }

    private fun finishVoice() {
        if (finishing) return
        finishing = true; listening = false
        try { speech?.stopListening() } catch (_: Exception) {}
        val q = questions[questionIndex]
        val spokenAnswer = spoken.toString().trim()
        val lower = spokenAnswer.lowercase()
        val hits = q.keywords.count { lower.contains(it) }
        val percent = if (q.keywords.isEmpty()) 0 else hits * 100 / q.keywords.size
        val mark = when { percent >= 75 -> 10; percent >= 50 -> 7; percent >= 25 -> 4; else -> 1 }
        totalScore += mark; answeredCount++
        attempts.add(Attempt(questionIndex, spokenAnswer, mark))
        val code = if (q.code.isNotBlank()) "\n\n⚡ ONE-LINE CODE\n━━━━━━━━━━━━━━\n${q.code}\n━━━━━━━━━━━━━━" else ""
        answerView.text = "🗣️ YOUR ANSWER\n━━━━━━━━━━━━━━\n${spokenAnswer.ifBlank { "No answer detected" }}\n\n⭐ YOUR SCORE: $mark / 10\n\n✅ CORRECT ANSWER\n━━━━━━━━━━━━━━\n${q.answer}$code"
        scoreView.text = "⭐ Score: $totalScore • Answered: $answeredCount"
        speakButton.text = "🎤 Speak Again"
    }

    private fun stopVoice() {
        finishing = true; listening = false
        try { speech?.stopListening() } catch (_: Exception) {}
        try { speech?.destroy() } catch (_: Exception) {}
        speech = null
    }

    private fun showAll() {
        root = layout(); add(button("← Home").apply { setOnClickListener { showHome() } })
        add(title("📚 Questions & Answers", 27f))
        questions.forEachIndexed { index, q ->
            add(title("Q${index + 1}  •  ${q.category}", 18f))
            add(card("❓ QUESTION\n${q.question}\n\n💡 ANSWER\n${q.answer}" + if (q.code.isNotBlank()) "\n\n⚡ CODE\n${q.code}" else "")))
        }
        val scroll = ScrollView(this); scroll.addView(root); setContentView(scroll)
    }

    private fun quickCode() {
        root = layout(); add(button("← Home").apply { setOnClickListener { showHome() } })
        add(title("⚡ Selenium Quick Code", 28f))
        add(card("Each item is separated clearly: TOPIC → CODE → WHEN TO USE.\nNo mixed question/answer blocks."))
        val coded = questions.filter { it.code.isNotBlank() }
        coded.forEachIndexed { index, q ->
            add(title("${index + 1}. ${q.question}", 18f))
            add(card("🔵 TOPIC / QUESTION\n${q.question}\n\n🟡 ONE-LINE CODE\n━━━━━━━━━━━━━━\n${q.code}\n━━━━━━━━━━━━━━\n\n🟢 WHEN TO USE\n${q.answer}"))
        }
        val scroll = ScrollView(this); scroll.addView(root); setContentView(scroll)
    }

    private fun showReview() {
        root = layout(); add(button("← Home").apply { setOnClickListener { showHome() } })
        add(title("📝 My Answer Review", 27f))
        if (attempts.isEmpty()) add(card("No attempts yet. Start a speaking test first."))
        attempts.forEach { a ->
            val q = questions[a.questionIndex]
            add(title("Q${a.questionIndex + 1} • ${q.category}", 18f))
            add(card("🗣️ YOUR ANSWER\n${a.spokenAnswer.ifBlank { "No answer detected" }}\n\n⭐ SCORE\n${a.score}/10\n\n✅ CORRECT ANSWER\n${q.answer}"))
        }
        val scroll = ScrollView(this); scroll.addView(root); setContentView(scroll)
    }

    private fun examSetup() {
        root = layout(); add(button("← Home").apply { setOnClickListener { showHome() } }); add(character())
        add(title("📝 Exam Mode", 29f)); add(title("Choose exam size", 18f))
        listOf(20, 50, 100).forEach { n -> add(button("$n Questions").apply { setOnClickListener { startExam(n) } }) }
        add(card("20 Q = 20 min • 50 Q = 45 min • 100 Q = 90 min")); setContentView(root)
    }

    private fun startExam(count: Int) {
        examQuestions = questions.shuffled().take(count); examIndex = 0; examScore = 0
        root = layout(); add(button("✕ Exit").apply { setOnClickListener { examTimer?.cancel(); showHome() } })
        examStatus = title("Question 1/$count • Score 0", 17f); add(examStatus)
        examQuestion = card(""); add(examQuestion)
        val options = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; add(options)
        examNext = button("Next"); examNext.isEnabled = false; add(examNext)
        setContentView(root); renderExam(options)
        val minutes = if (count <= 20) 20L else if (count <= 50) 45L else 90L
        examTimer = object : CountDownTimer(minutes * 60_000L, 1000L) {
            override fun onTick(ms: Long) { examStatus.text = "Question ${examIndex + 1}/$count • Score $examScore • ⏱️ ${ms / 60000}:${String.format("%02d", (ms / 1000) % 60)}" }
            override fun onFinish() { finishExam() }
        }.start()
    }

    private fun renderExam(options: LinearLayout) {
        options.removeAllViews(); examNext.isEnabled = false
        val q = examQuestions[examIndex]
        examQuestion.text = "[${q.category}]\n\n${q.question}"
        val choices = (listOf(q.answer) + questions.filter { it.question != q.question }.shuffled().take(3).map { it.answer }).shuffled()
        for (answer in choices) {
            val b = button(answer); options.addView(b)
            b.setOnClickListener {
                for (j in 0 until options.childCount) options.getChildAt(j).isEnabled = false
                if (answer == q.answer) { examScore++; b.text = "✅ $answer" } else b.text = "❌ $answer"
                examNext.isEnabled = true
            }
        }
        examNext.text = if (examIndex == examQuestions.lastIndex) "Submit Exam" else "Next"
        examNext.setOnClickListener { if (examIndex == examQuestions.lastIndex) finishExam() else { examIndex++; renderExam(options) } }
    }

    private fun finishExam() {
        examTimer?.cancel()
        val percent = if (examQuestions.isEmpty()) 0 else examScore * 100 / examQuestions.size
        root = layout(); add(character()); add(title("🏆 Exam Complete", 30f)); add(title("$examScore / ${examQuestions.size}", 38f)); add(title("$percent%", 30f))
        add(card(if (percent >= 90) "🏆 Excellent!" else if (percent >= 75) "🌟 Very Good!" else if (percent >= 60) "👍 Good progress!" else "💪 Keep practicing!"))
        add(button("← Home").apply { setOnClickListener { showHome() } }); setContentView(root)
    }

    private fun settings() {
        root = layout(); add(button("← Home").apply { setOnClickListener { showHome() } }); add(title("⚙️ Settings", 28f))
        add(title("Button colour", 17f))
        listOf("🔵 Blue" to Color.rgb(78,93,210), "🟣 Purple" to Color.rgb(125,82,190), "🟢 Green" to Color.rgb(35,145,100), "🟠 Orange" to Color.rgb(225,125,45), "🌸 Pink" to Color.rgb(205,75,135), "🔴 Red" to Color.rgb(205,55,65)).forEach { (n,c) -> add(button(n).apply { setOnClickListener { prefs.edit().putInt("primary",c).apply(); settings() } }) }
        add(title("Background colour", 17f))
        listOf("☁️ Light Blue" to Color.rgb(205,220,255), "🌙 Dark" to Color.rgb(35,38,50), "🌿 Green" to Color.rgb(205,240,218), "🌸 Pink" to Color.rgb(255,210,232), "🌊 Sky" to Color.rgb(190,225,255), "🌅 Cream" to Color.rgb(255,230,180), "⚪ White" to Color.WHITE).forEach { (n,c) -> add(button(n).apply { setOnClickListener { prefs.edit().putInt("background",c).apply(); settings() } }) }
        add(Switch(this).apply { text = "✨ Animation + moving character"; isChecked = prefs.getBoolean("animation",true); setOnCheckedChangeListener { _,v -> prefs.edit().putBoolean("animation",v).apply() } })
        add(card("Question, your answer and correct answer are now visually separated. Quick Code uses separate TOPIC / CODE / WHEN TO USE cards."))
        add(button("Save & Home").apply { setOnClickListener { showHome() } }); setContentView(root)
    }

    override fun onRequestPermissionsResult(code: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(code, permissions, results)
        if (code == 7 && results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED) startVoice()
    }

    override fun onDestroy() { stopVoice(); examTimer?.cancel(); super.onDestroy() }
}
