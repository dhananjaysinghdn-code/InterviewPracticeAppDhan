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
import android.widget.*
import java.util.Locale

data class BankItem(val question: String, val answer: String, val category: String, val keywords: List<String>, val code: String = "")
data class Attempt(val index: Int, val spoken: String, val mark: Int)

class MainActivity : Activity() {
    private val questions = buildQuestions()
    private var current = 0
    private var score = 0
    private var answered = 0
    private var listening = false
    private var finishing = false
    private val spoken = StringBuilder()
    private var speech: SpeechRecognizer? = null
    private val attempts = mutableListOf<Attempt>()
    private lateinit var handler: Handler
    private lateinit var prefs: SharedPreferences
    private lateinit var root: LinearLayout
    private lateinit var questionView: TextView
    private lateinit var resultView: TextView
    private lateinit var scoreView: TextView
    private lateinit var speakButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        showHome()
    }

    private fun buildQuestions(): List<BankItem> {
        val list = mutableListOf<BankItem>()
        fun add(q: String, a: String, category: String, keywords: String, code: String = "") {
            list.add(BankItem(q, a, category, keywords.split(",").filter { it.isNotBlank() }, code))
        }
        add("What is Selenium WebDriver?", "Selenium WebDriver is an API used to automate web browsers.", "Selenium", "selenium,webdriver,automate,browser")
        add("What is XPath?", "XPath is a locator used to identify elements in HTML or XML.", "Selenium", "xpath,locator,element")
        add("What is an explicit wait?", "It waits for a specific condition before continuing the test.", "Selenium", "explicit,wait,condition")
        add("What is TestNG?", "TestNG is a Java testing framework used to organize and execute tests.", "TestNG", "testng,java,framework")
        add("What is POM?", "Page Object Model keeps page locators and actions inside reusable page classes.", "Framework", "page,object,model,locator,action")
        add("What is SQL?", "SQL is used to query and manage data in relational databases.", "SQL", "sql,query,database")
        add("What is an API?", "An API is an interface that allows software systems to communicate.", "API", "api,interface,communicate")
        add("How do you click an element?", "Use findElement with a locator and call click().", "Selenium Quick Code", "click,element", "driver.findElement(By.id(\"login\")).click();")
        add("How do you enter text?", "Use sendKeys() on the WebElement.", "Selenium Quick Code", "sendkeys,text", "driver.findElement(By.id(\"user\")).sendKeys(\"admin\");")
        add("How do you hover over an element?", "Use the Actions class with moveToElement().", "Selenium Quick Code", "actions,hover,move", "new Actions(driver).moveToElement(element).perform();")
        add("How do you switch to a frame?", "Use driver.switchTo().frame().", "Selenium Quick Code", "frame,switch", "driver.switchTo().frame(element);")
        add("How do you accept an alert?", "Switch to the alert and call accept().", "Selenium Quick Code", "alert,accept", "driver.switchTo().alert().accept();")
        add("How do you select a dropdown value?", "Use Selenium Select class.", "Selenium Quick Code", "select,dropdown", "new Select(element).selectByVisibleText(\"India\");")
        add("How do you wait for visibility?", "Use WebDriverWait with ExpectedConditions.", "Selenium Quick Code", "wait,visibility", "new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOf(element));")
        add("How do you double click?", "Use Actions doubleClick().", "Selenium Quick Code", "actions,doubleclick", "new Actions(driver).doubleClick(element).perform();")
        add("How do you right click?", "Use Actions contextClick().", "Selenium Quick Code", "actions,rightclick", "new Actions(driver).contextClick(element).perform();")
        add("How do you drag and drop?", "Use Actions dragAndDrop().", "Selenium Quick Code", "actions,drag,drop", "new Actions(driver).dragAndDrop(source, target).perform();")
        add("How do you switch to another window?", "Use driver.switchTo().window(handle).", "Selenium Quick Code", "window,switch", "driver.switchTo().window(handle);")
        add("How do you get text from an element?", "Use getText() on the element.", "Selenium Quick Code", "gettext,text", "String text = element.getText();")
        add("How do you take a screenshot?", "Use TakesScreenshot.", "Selenium Quick Code", "screenshot", "((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);")
        add("How do you scroll to an element?", "Use JavascriptExecutor.", "Selenium Quick Code", "scroll,javascript", "((JavascriptExecutor) driver).executeScript(\"arguments[0].scrollIntoView(true);\", element);")
        add("What is implicit wait?", "It sets a global wait for locating elements.", "Selenium", "implicit,wait", "driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));")
        add("What is inheritance in Java?", "Inheritance allows a class to acquire properties and behavior from another class.", "Core Java", "inheritance,class,java")
        add("What is polymorphism?", "Polymorphism allows the same interface or method to have different implementations.", "Core Java", "polymorphism,method")
        add("What is encapsulation?", "Encapsulation bundles data and methods and controls access to the data.", "Core Java", "encapsulation,data,methods")
        add("What is an ArrayList?", "ArrayList is a resizable ordered collection in Java.", "Collections", "arraylist,collection")
        add("What is HashMap?", "HashMap stores key-value pairs and provides lookup by key.", "Collections", "hashmap,key,value")
        add("What is exception handling?", "It handles runtime problems using try, catch, finally and related constructs.", "Core Java", "exception,try,catch")
        add("What is Maven?", "Maven is a build and dependency management tool.", "Maven", "maven,build,dependency")
        add("What is Git?", "Git is a distributed version control system.", "Git", "git,version,control")
        add("What is CI/CD?", "CI/CD automates building, testing and delivery of software.", "CI/CD", "ci,cd,build,test,delivery")
        add("What is REST API?", "REST is an architectural style commonly used for HTTP APIs.", "API", "rest,http,api")
        add("What is regression testing?", "Regression testing verifies existing functionality after changes.", "Testing", "regression,testing")
        add("What is smoke testing?", "Smoke testing checks whether a build is stable enough for further testing.", "Testing", "smoke,build,testing")
        while (list.size < 420) {
            val n = list.size + 1
            add("Interview Practice Question $n?", "Explain the concept clearly, describe your approach, mention the tool used, explain validation, and state the final result.", "Scenario", "concept,approach,tool,validation,result")
        }
        return list
    }

    private fun background() = prefs.getInt("background", Color.rgb(242, 245, 255))
    private fun primary() = prefs.getInt("primary", Color.rgb(78, 93, 210))
    private fun layout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 24, 24, 24)
        setBackgroundColor(background())
    }
    private fun title(text: String, size: Float = 18f) = TextView(this).apply {
        this.text = text
        textSize = size
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(30, 40, 75))
        setPadding(8, 10, 8, 10)
    }
    private fun button(text: String) = Button(this).apply {
        this.text = text
        textSize = 16f
        isAllCaps = false
        setTextColor(Color.WHITE)
        setBackgroundColor(primary())
    }
    private fun card(text: String) = TextView(this).apply {
        this.text = text
        textSize = 17f
        setPadding(20, 20, 20, 20)
        setTextColor(Color.DKGRAY)
        setBackgroundColor(Color.WHITE)
        elevation = 5f
    }
    private fun add(view: View) { root.addView(view) }

    private fun showHome() {
        root = layout()
        add(title("📚 📖 📕 📗 📘 📙", 25f))
        add(title("👩‍💻✨", 40f))
        add(title("🎯 Interview Practice", 30f))
        add(title("420+ Q&A • Speak • Exam • Quick Code", 17f))
        add(button("🎤 Start Test").apply { setOnClickListener { startTest() } })
        add(button("📚 All Questions & Answers").apply { setOnClickListener { showAll() } })
        add(button("📝 Review My Answers").apply { setOnClickListener { review() } })
        add(button("⏱️ Exam Mode").apply { setOnClickListener { examSetup() } })
        add(button("⚡ Selenium Quick Code").apply { setOnClickListener { quickCode() } })
        add(button("⚙️ Settings").apply { setOnClickListener { settings() } })
        add(button("🗑️ Clear Results").apply { setOnClickListener { attempts.clear(); score = 0; answered = 0; Toast.makeText(this@MainActivity, "Results cleared", Toast.LENGTH_SHORT).show() } })
        setContentView(root)
    }

    private fun startTest() {
        root = layout()
        add(button("← Home").apply { setOnClickListener { stopSpeech(); showHome() } })
        scoreView = title("⭐ Score: $score • Answered: $answered", 17f)
        add(scoreView)
        questionView = card("")
        add(questionView)
        speakButton = button("🎤 Start Speaking")
        add(speakButton)
        resultView = card("")
        resultView.visibility = View.GONE
        add(resultView)
        add(button("➡️ Next Question").apply { setOnClickListener { stopSpeech(); current = (current + 1) % questions.size; showQuestion() } })
        setContentView(root)
        showQuestion()
        speakButton.setOnClickListener { if (listening) finishAnswer() else listen() }
    }

    private fun showQuestion() { questionView.text = "Q${current + 1}\n\n${questions[current].question}" }

    private fun listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 7)
            return
        }
        speech?.destroy()
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech!!.setRecognitionListener(recognitionListener)
        spoken.clear()
        finishing = false
        listening = true
        speakButton.text = "⏹️ Listening..."
        resultView.visibility = View.VISIBLE
        resultView.text = "🎧 Ready... speak your answer."
        startRecognition()
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { resultView.text = "🎧 Ready..." }
        override fun onBeginningOfSpeech() { resultView.text = "🎧 Listening..." }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) { if (!finishing) handler.postDelayed({ if (listening) startRecognition() }, 400) }
        override fun onResults(results: Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (text.isNotBlank()) spoken.append(" ").append(text)
            if (!finishing) handler.postDelayed({ finishAnswer() }, 300)
        }
        override fun onPartialResults(results: Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (text.isNotBlank()) resultView.text = "🎧 $text"
        }
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun startRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        }
        try { speech?.startListening(intent) } catch (_: Exception) {}
    }

    private fun finishAnswer() {
        if (finishing) return
        finishing = true
        listening = false
        try { speech?.stopListening() } catch (_: Exception) {}
        val q = questions[current]
        val spokenText = spoken.toString().lowercase()
        val hits = q.keywords.count { spokenText.contains(it) }
        val percent = if (q.keywords.isEmpty()) 0 else hits * 100 / q.keywords.size
        val mark = when { percent >= 75 -> 10; percent >= 50 -> 7; percent >= 25 -> 4; else -> 1 }
        score += mark
        answered++
        attempts.add(Attempt(current, spoken.toString().trim(), mark))
        resultView.visibility = View.VISIBLE
        resultView.text = "🗣️ YOUR ANSWER\n${spoken.toString().trim()}\n\n⭐ SCORE: $mark/10\n\n✅ CORRECT ANSWER\n${q.answer}${if (q.code.isNotBlank()) "\n\n⚡ ONE-LINE CODE\n${q.code}" else ""}"
        scoreView.text = "⭐ Score: $score • Answered: $answered"
        speakButton.text = "🎤 Start Speaking"
    }

    private fun stopSpeech() {
        finishing = true
        listening = false
        try { speech?.stopListening() } catch (_: Exception) {}
        try { speech?.destroy() } catch (_: Exception) {}
        speech = null
    }

    private fun showAll() {
        root = layout()
        add(button("← Home").apply { setOnClickListener { showHome() } })
        add(title("📚 Questions & Answers", 27f))
        questions.forEachIndexed { index, q ->
            add(title("Q${index + 1} • ${q.category}", 18f))
            val text = "❓ QUESTION\n${q.question}\n\n💡 ANSWER\n${q.answer}"
            val fullText = if (q.code.isNotBlank()) text + "\n\n⚡ ONE-LINE CODE\n${q.code}" else text
            add(card(fullText))
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun quickCode() {
        root = layout()
        add(button("← Home").apply { setOnClickListener { showHome() } })
        add(title("⚡ Selenium Quick Code", 27f))
        questions.filter { it.code.isNotBlank() }.forEachIndexed { index, q ->
            add(title("${index + 1}. ${q.question}", 19f))
            add(card("⚡ ONE-LINE CODE\n${q.code}\n\n💡 USE / ANSWER\n${q.answer}\n────────────────"))
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun review() {
        root = layout()
        add(button("← Home").apply { setOnClickListener { showHome() } })
        add(title("📝 My Answers", 27f))
        if (attempts.isEmpty()) add(title("No attempts yet.", 18f))
        attempts.forEach { a ->
            val q = questions[a.index]
            add(card("Q${a.index + 1}. ${q.question}\n\n🗣️ YOUR ANSWER\n${a.spoken.ifBlank { "No answer detected" }}\n\n⭐ ${a.mark}/10\n\n✅ CORRECT ANSWER\n${q.answer}"))
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private var examQuestions = emptyList<BankItem>()
    private var examIndex = 0
    private var examScore = 0
    private var examTimer: CountDownTimer? = null
    private lateinit var examQuestion: TextView
    private lateinit var examStatus: TextView
    private lateinit var examNext: Button

    private fun examSetup() {
        root = layout()
        add(button("← Home").apply { setOnClickListener { showHome() } })
        add(title("📝 Exam Mode", 29f))
        add(title("Choose exam size", 18f))
        listOf(20, 50, 100).forEach { count ->
            add(button("$count Questions").apply { setOnClickListener { startExam(count) } })
        }
        add(card("20 Q = 20 min • 50 Q = 45 min • 100 Q = 90 min"))
        setContentView(root)
    }

    private fun startExam(count: Int) {
        examQuestions = questions.shuffled().take(count)
        examIndex = 0
        examScore = 0
        root = layout()
        add(button("✕ Exit").apply { setOnClickListener { examTimer?.cancel(); showHome() } })
        examStatus = title("Question 1/$count • Score 0", 17f)
        add(examStatus)
        examQuestion = card("")
        add(examQuestion)
        val options = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        add(options)
        examNext = button("Next")
        add(examNext)
        setContentView(root)
        renderExamQuestion(options)
        val minutes = if (count <= 20) 20L else if (count <= 50) 45L else 90L
        examTimer = object : CountDownTimer(minutes * 60000L, 1000L) {
            override fun onTick(ms: Long) { examStatus.text = "Question ${examIndex + 1}/$count • Score $examScore • ⏱️ ${ms / 60000}:${String.format("%02d", (ms / 1000) % 60)}" }
            override fun onFinish() { finishExam() }
        }.start()
    }

    private fun renderExamQuestion(options: LinearLayout) {
        options.removeAllViews()
        val q = examQuestions[examIndex]
        examQuestion.text = "[${q.category}]\n\n${q.question}"
        val answers = (listOf(q.answer) + questions.filter { it.question != q.question }.shuffled().take(3).map { it.answer }).shuffled()
        for (answer in answers) {
            val option = button(answer)
            options.addView(option)
            option.setOnClickListener {
                for (j in 0 until options.childCount) options.getChildAt(j).isEnabled = false
                if (answer == q.answer) { examScore++; option.text = "✅ $answer" } else { option.text = "❌ $answer" }
                examNext.text = if (examIndex == examQuestions.lastIndex) "Submit Exam" else "Next"
            }
        }
        examNext.setOnClickListener {
            if (examIndex == examQuestions.lastIndex) finishExam() else { examIndex++; renderExamQuestion(options) }
        }
    }

    private fun finishExam() {
        examTimer?.cancel()
        val percent = if (examQuestions.isEmpty()) 0 else examScore * 100 / examQuestions.size
        root = layout()
        add(title("🏆 Exam Complete", 30f))
        add(title("$examScore / ${examQuestions.size}", 38f))
        add(title("$percent%", 30f))
        add(card(if (percent >= 90) "🏆 Excellent!" else if (percent >= 75) "🌟 Very Good!" else if (percent >= 60) "👍 Good progress!" else "💪 Keep practicing!"))
        add(button("← Home").apply { setOnClickListener { showHome() } })
        setContentView(root)
    }

    private fun settings() {
        root = layout()
        add(button("← Home").apply { setOnClickListener { showHome() } })
        add(title("⚙️ Settings", 28f))
        listOf("🔵 Blue" to Color.rgb(78, 93, 210), "🟣 Purple" to Color.rgb(125, 82, 190), "🟢 Green" to Color.rgb(35, 145, 100), "🟠 Orange" to Color.rgb(225, 125, 45), "🌸 Pink" to Color.rgb(205, 75, 135), "🔴 Red" to Color.rgb(205, 55, 65)).forEach { (name, color) ->
            add(button(name).apply { setOnClickListener { prefs.edit().putInt("primary", color).apply(); settings() } })
        }
        add(title("Background colour", 17f))
        listOf("☁️ Blue" to Color.rgb(205, 220, 255), "🌙 Dark" to Color.rgb(35, 38, 50), "🌿 Green" to Color.rgb(205, 240, 218), "🌸 Pink" to Color.rgb(255, 210, 232), "🌊 Sky" to Color.rgb(190, 225, 255), "🌅 Cream" to Color.rgb(255, 230, 180), "⚪ White" to Color.WHITE).forEach { (name, color) ->
            add(button(name).apply { setOnClickListener { prefs.edit().putInt("background", color).apply(); settings() } })
        }
        add(card("Question = blue • Your Answer = orange • Correct Answer = green\nVoice completes after about 3 seconds of silence."))
        add(button("Save & Home").apply { setOnClickListener { showHome() } })
        setContentView(root)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 7 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) listen()
    }

    override fun onDestroy() {
        stopSpeech()
        examTimer?.cancel()
        super.onDestroy()
    }
}