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

data class InterviewQuestion(
    val question: String,
    val answer: String,
    val category: String,
    val keywords: List<String> = emptyList(),
    val code: String = ""
)

data class Attempt(
    val questionIndex: Int,
    val spokenAnswer: String,
    val score: Int
)

class MainActivity : Activity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var handler: Handler
    private lateinit var root: LinearLayout
    private lateinit var questionView: TextView
    private lateinit var answerView: TextView
    private lateinit var scoreView: TextView
    private lateinit var speakButton: Button

    private val questions = createQuestionBank()
    private val attempts = mutableListOf<Attempt>()
    private var questionIndex = 0
    private var totalScore = 0
    private var answeredCount = 0
    private var speechRecognizer: SpeechRecognizer? = null
    private var listening = false
    private var finishingAnswer = false
    private val spokenText = StringBuilder()

    private var examQuestions = emptyList<InterviewQuestion>()
    private var examIndex = 0
    private var examScore = 0
    private var examTimer: CountDownTimer? = null
    private lateinit var examQuestionView: TextView
    private lateinit var examStatusView: TextView
    private lateinit var examNextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        showHome()
    }

    private fun createQuestionBank(): List<InterviewQuestion> {
        val list = mutableListOf<InterviewQuestion>()

        fun add(q: String, a: String, category: String, keywords: String = "", code: String = "") {
            list.add(
                InterviewQuestion(
                    q,
                    a,
                    category,
                    keywords.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() },
                    code
                )
            )
        }

        add("What is Selenium WebDriver?", "Selenium WebDriver is an API used to automate web browsers.", "Selenium", "selenium,webdriver,automate,browser")
        add("What is XPath?", "XPath is a locator used to identify elements in HTML or XML.", "Selenium", "xpath,locator,element")
        add("What is CSS Selector?", "CSS Selector is a concise locator strategy for identifying web elements.", "Selenium", "css,selector,locator")
        add("What is an explicit wait?", "It waits for a specific condition before continuing the test.", "Selenium", "explicit,wait,condition")
        add("What is implicit wait?", "It applies a default wait while WebDriver searches for elements.", "Selenium", "implicit,wait")
        add("What is FluentWait?", "FluentWait lets us configure timeout, polling interval and ignored exceptions.", "Selenium", "fluent,wait,polling")
        add("How do you click an element?", "Locate the element and call click().", "Selenium", "click,element", "driver.findElement(By.id(\"login\")).click();")
        add("How do you enter text?", "Locate the input and call sendKeys().", "Selenium", "sendkeys,text", "driver.findElement(By.id(\"user\")).sendKeys(\"admin\");")
        add("How do you hover over an element?", "Use the Actions class and moveToElement().", "Selenium", "actions,hover,move", "new Actions(driver).moveToElement(element).perform();")
        add("How do you double click?", "Use Actions.doubleClick().", "Selenium", "actions,doubleclick", "new Actions(driver).doubleClick(element).perform();")
        add("How do you right click?", "Use Actions.contextClick().", "Selenium", "actions,rightclick", "new Actions(driver).contextClick(element).perform();")
        add("How do you drag and drop?", "Use Actions.dragAndDrop().", "Selenium", "actions,drag,drop", "new Actions(driver).dragAndDrop(source, target).perform();")
        add("How do you switch to a frame?", "Use driver.switchTo().frame().", "Selenium", "frame,switch", "driver.switchTo().frame(element);")
        add("How do you return from a frame?", "Use defaultContent() to return to the main document.", "Selenium", "frame,default", "driver.switchTo().defaultContent();")
        add("How do you switch windows?", "Use driver.switchTo().window(handle).", "Selenium", "window,switch", "driver.switchTo().window(handle);")
        add("How do you accept an alert?", "Switch to the alert and call accept().", "Selenium", "alert,accept", "driver.switchTo().alert().accept();")
        add("How do you dismiss an alert?", "Switch to the alert and call dismiss().", "Selenium", "alert,dismiss", "driver.switchTo().alert().dismiss();")
        add("How do you select a dropdown value?", "Use Selenium Select class.", "Selenium", "select,dropdown", "new Select(element).selectByVisibleText(\"India\");")
        add("How do you get text from an element?", "Use getText().", "Selenium", "gettext,text", "String text = element.getText();")
        add("How do you check visibility?", "Use isDisplayed().", "Selenium", "displayed,visibility", "element.isDisplayed();")
        add("How do you check enabled state?", "Use isEnabled().", "Selenium", "enabled", "element.isEnabled();")
        add("How do you check selection?", "Use isSelected().", "Selenium", "selected", "element.isSelected();")
        add("How do you take a screenshot?", "Use TakesScreenshot to capture the current browser screen.", "Selenium", "screenshot", "((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);")
        add("How do you scroll to an element?", "Use JavaScriptExecutor and scrollIntoView().", "Selenium", "scroll,javascript", "((JavascriptExecutor) driver).executeScript(\"arguments[0].scrollIntoView(true);\", element);")
        add("What is TestNG?", "TestNG is a Java testing framework used to organize and execute automated tests.", "TestNG", "testng,java,framework")
        add("What is @BeforeMethod?", "It runs before each test method.", "TestNG", "beforemethod,testng")
        add("What is @AfterMethod?", "It runs after each test method.", "TestNG", "aftermethod,testng")
        add("What is an assertion?", "An assertion compares actual behavior with expected behavior.", "TestNG", "assertion,expected,actual")
        add("What is POM?", "Page Object Model keeps page locators and actions inside dedicated page classes.", "Framework", "pom,page,object,model")
        add("Why use Page Factory?", "Page Factory can initialize page elements and reduce repeated element lookup code.", "Framework", "pagefactory,element")
        add("What is a data-driven framework?", "It separates test data from test logic so the same test can run with multiple datasets.", "Framework", "data,driven,framework")
        add("What is Maven?", "Maven is a build and dependency management tool for Java projects.", "Maven", "maven,build,dependency")
        add("What is Git?", "Git is a distributed version control system.", "Git", "git,version,control")
        add("What is CI/CD?", "CI/CD automates build, test and delivery activities.", "CI/CD", "ci,cd,build,test,delivery")
        add("What is REST API?", "REST is an architectural style commonly used to build HTTP APIs.", "API", "rest,http,api")
        add("What is GET in REST?", "GET is normally used to retrieve data.", "API", "get,retrieve,api")
        add("What is POST in REST?", "POST is normally used to create or submit data.", "API", "post,create,api")
        add("What is SQL?", "SQL is used to query and manage data in relational databases.", "SQL", "sql,database,query")
        add("What is INNER JOIN?", "INNER JOIN returns rows having matching values in both joined tables.", "SQL", "inner,join,sql")
        add("What is regression testing?", "Regression testing verifies that existing functionality still works after changes.", "Testing", "regression,testing")
        add("What is smoke testing?", "Smoke testing checks whether a build is stable enough for detailed testing.", "Testing", "smoke,build,testing")
        add("What is sanity testing?", "Sanity testing checks focused functionality after a small change or fix.", "Testing", "sanity,testing")
        add("What is STLC?", "STLC is the Software Testing Life Cycle followed to plan, design, execute and close testing activities.", "Testing", "stlc,testing")
        add("What is SDLC?", "SDLC is the Software Development Life Cycle used to develop and maintain software.", "Testing", "sdlc,development")
        add("What is an interface in Java?", "An interface defines a contract that implementing classes follow.", "Core Java", "interface,contract,java")
        add("What is inheritance?", "Inheritance allows a class to reuse properties and behavior from another class.", "Core Java", "inheritance,class,java")
        add("What is polymorphism?", "Polymorphism allows the same interface or method concept to have different implementations.", "Core Java", "polymorphism,method")
        add("What is encapsulation?", "Encapsulation bundles data and methods and controls access to the data.", "Core Java", "encapsulation,data,methods")
        add("What is an ArrayList?", "ArrayList is a resizable ordered collection in Java.", "Collections", "arraylist,collection")
        add("What is HashMap?", "HashMap stores key-value pairs and provides lookup using a key.", "Collections", "hashmap,key,value")
        add("What is exception handling?", "Exception handling manages runtime problems using constructs such as try, catch and finally.", "Core Java", "exception,try,catch")
        add("What is method overloading?", "Overloading means multiple methods have the same name with different parameter lists.", "Core Java", "overloading,method")
        add("What is method overriding?", "Overriding means a child class provides its own implementation of a parent method.", "Core Java", "overriding,method")
        add("What is a String immutable?", "Java String objects are immutable, so their value cannot be changed after creation.", "Core Java", "string,immutable")

        val topics = listOf("Core Java", "Selenium", "TestNG", "Framework", "API", "SQL", "Testing", "Maven", "Git", "CI/CD")
        var counter = 1
        while (list.size < 420) {
            val topic = topics[(list.size) % topics.size]
            val number = counter++
            add(
                "Scenario $number: explain an important $topic interview situation.",
                "Explain the problem, your approach, the tool or concept used, the validation performed and the final result. Keep the answer concise and project-focused.",
                topic,
                topic.lowercase()
            )
        }
        return list
    }

    private fun backgroundColor(): Int = prefs.getInt("background", Color.rgb(242, 245, 255))
    private fun primaryColor(): Int = prefs.getInt("primary", Color.rgb(78, 93, 210))

    private fun createLayout(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 24, 24, 24)
        setBackgroundColor(backgroundColor())
    }

    private fun label(text: String, size: Float = 18f): TextView = TextView(this).apply {
        this.text = text
        textSize = size
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(30, 40, 75))
        setPadding(8, 10, 8, 10)
    }

    private fun button(text: String): Button = Button(this).apply {
        this.text = text
        textSize = 16f
        isAllCaps = false
        setTextColor(Color.WHITE)
        setBackgroundColor(primaryColor())
    }

    private fun card(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 17f
        setPadding(20, 20, 20, 20)
        setTextColor(Color.DKGRAY)
        setBackgroundColor(Color.WHITE)
        elevation = 5f
    }

    private fun addViewAnimated(view: View) {
        root.addView(view)
        view.alpha = 0f
        view.animate().alpha(1f).setDuration(250).start()
    }

    private fun animatedGirl(): TextView {
        val girl = label("👩‍💻✨", 40f)
        if (prefs.getBoolean("animation", true)) {
            val animation = TranslateAnimation(-30f, 30f, 0f, 0f)
            animation.duration = 1400
            animation.repeatMode = Animation.REVERSE
            animation.repeatCount = Animation.INFINITE
            girl.startAnimation(animation)
        }
        return girl
    }

    private fun showHome() {
        root = createLayout()
        addViewAnimated(label("📚  📖  📕  📗  📘  📙", 25f))
        addViewAnimated(animatedGirl())
        addViewAnimated(label("🎯 Interview Practice", 30f))
        addViewAnimated(label("420+ Q&A • Speak • Exam • Quick Code", 17f))
        addViewAnimated(button("🎤 Start Test").apply { setOnClickListener { showTest() } })
        addViewAnimated(button("📚 All Questions & Answers").apply { setOnClickListener { showAllQuestions() } })
        addViewAnimated(button("📝 Review My Answers").apply { setOnClickListener { showReview() } })
        addViewAnimated(button("⏱️ Exam Mode").apply { setOnClickListener { showExamSetup() } })
        addViewAnimated(button("⚡ Selenium Quick Code").apply { setOnClickListener { showQuickCode() } })
        addViewAnimated(button("⚙️ Settings").apply { setOnClickListener { showSettings() } })
        addViewAnimated(button("🗑️ Clear Results").apply {
            setOnClickListener {
                attempts.clear()
                totalScore = 0
                answeredCount = 0
                Toast.makeText(this@MainActivity, "Results cleared", Toast.LENGTH_SHORT).show()
            }
        })
        setContentView(root)
    }

    private fun showTest() {
        root = createLayout()
        addViewAnimated(button("← Home").apply { setOnClickListener { stopListening(); showHome() } })
        scoreView = label("⭐ Score: $totalScore • Answered: $answeredCount", 17f)
        addViewAnimated(scoreView)
        questionView = card("")
        addViewAnimated(questionView)
        speakButton = button("🎤 Start Speaking")
        addViewAnimated(speakButton)
        answerView = card("")
        answerView.visibility = View.GONE
        addViewAnimated(answerView)
        addViewAnimated(button("➡️ Next Question").apply {
            setOnClickListener {
                stopListening()
                questionIndex = (questionIndex + 1) % questions.size
                showCurrentQuestion()
            }
        })
        setContentView(root)
        showCurrentQuestion()
        speakButton.setOnClickListener { if (listening) finishAnswer() else startListening() }
    }

    private fun showCurrentQuestion() {
        questionView.text = "Q${questionIndex + 1}\n\n${questions[questionIndex].question}"
        speakButton.text = "🎤 Start Speaking"
    }

    private fun startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 7)
            return
        }
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(recognitionListener)
            spokenText.clear()
            finishingAnswer = false
            listening = true
            speakButton.text = "⏹️ Listening..."
            answerView.visibility = View.VISIBLE
            answerView.text = "🎧 Ready... speak your answer."
            startRecognition()
        } catch (_: Exception) {
            answerView.visibility = View.VISIBLE
            answerView.text = "Voice could not start. Please try again."
            listening = false
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { answerView.text = "🎧 Ready..." }
        override fun onBeginningOfSpeech() { answerView.text = "🎧 Listening..." }
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onError(error: Int) {
            if (listening && !finishingAnswer) {
                handler.postDelayed({ if (listening && !finishingAnswer) startRecognition() }, 350)
            }
        }
        override fun onResults(results: Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (text.isNotBlank()) spokenText.append(" ").append(text)
            if (!finishingAnswer) handler.postDelayed({ finishAnswer() }, 500)
        }
        override fun onPartialResults(results: Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (text.isNotBlank()) answerView.text = "🎧 $text"
        }
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun startRecognition() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            }
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            listening = false
            answerView.text = "Voice recognition is unavailable."
        }
    }

    private fun finishAnswer() {
        if (finishingAnswer) return
        finishingAnswer = true
        listening = false
        try { speechRecognizer?.stopListening() } catch (_: Exception) { }

        val question = questions[questionIndex]
        val spoken = spokenText.toString().trim()
        val lower = spoken.lowercase()
        val hits = question.keywords.count { lower.contains(it) }
        val percentage = if (question.keywords.isEmpty()) 0 else hits * 100 / question.keywords.size
        val mark = when {
            percentage >= 75 -> 10
            percentage >= 50 -> 7
            percentage >= 25 -> 4
            else -> 1
        }
        totalScore += mark
        answeredCount++
        attempts.add(Attempt(questionIndex, spoken, mark))

        answerView.visibility = View.VISIBLE
        answerView.text = "🗣️ YOUR ANSWER\n${spoken.ifBlank { "No answer detected" }}\n\n⭐ $mark/10\n\n✅ CORRECT ANSWER\n${question.answer}" +
            if (question.code.isNotBlank()) "\n\n⚡ ONE-LINE CODE\n${question.code}" else ""
        scoreView.text = "⭐ Score: $totalScore • Answered: $answeredCount"
        speakButton.text = "🎤 Start Speaking"
    }

    private fun stopListening() {
        finishingAnswer = true
        listening = false
        try { speechRecognizer?.stopListening() } catch (_: Exception) { }
        try { speechRecognizer?.destroy() } catch (_: Exception) { }
        speechRecognizer = null
    }

    private fun showAllQuestions() {
        val scroll = ScrollView(this)
        root = createLayout()
        addViewAnimated(button("← Home").apply { setOnClickListener { showHome() } })
        addViewAnimated(label("📚 All Questions & Answers", 27f))
        questions.forEachIndexed { index, question ->
            val code = if (question.code.isNotBlank()) "\n\n⚡ ONE-LINE CODE\n${question.code}" else ""
            addViewAnimated(card("Q${index + 1}. ${question.question}\n\n${question.answer}$code"))
        }
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun showQuickCode() {
        val scroll = ScrollView(this)
        root = createLayout()
        addViewAnimated(button("← Home").apply { setOnClickListener { showHome() } })
        addViewAnimated(label("⚡ Selenium Quick Code", 27f))
        seleniumQuickCodes.forEach { item ->
            addViewAnimated(card("${item.topic}\n\n${item.code}\n\n${item.use}"))
        }
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun showReview() {
        val scroll = ScrollView(this)
        root = createLayout()
        addViewAnimated(button("← Home").apply { setOnClickListener { showHome() } })
        addViewAnimated(label("📝 My Answers", 27f))
        if (attempts.isEmpty()) addViewAnimated(label("No attempts yet.", 18f))
        attempts.forEach { attempt ->
            val question = questions[attempt.questionIndex]
            addViewAnimated(card("Q${attempt.questionIndex + 1}. ${question.question}\n\n🗣️ YOUR ANSWER\n${attempt.spokenAnswer.ifBlank { "No answer detected" }}\n\n⭐ ${attempt.score}/10\n\n✅ CORRECT ANSWER\n${question.answer}"))
        }
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun showExamSetup() {
        root = createLayout()
        addViewAnimated(button("← Home").apply { setOnClickListener { showHome() } })
        addViewAnimated(animatedGirl())
        addViewAnimated(label("📝 Exam Mode", 29f))
        addViewAnimated(label("Choose exam size", 18f))
        listOf(20, 50, 100).forEach { count ->
            addViewAnimated(button("$count Questions").apply { setOnClickListener { startExam(count) } })
        }
        addViewAnimated(card("20 Q = 20 min • 50 Q = 45 min • 100 Q = 90 min"))
        setContentView(root)
    }

    private fun startExam(count: Int) {
        examQuestions = questions.shuffled().take(count)
        examIndex = 0
        examScore = 0
        root = createLayout()
        addViewAnimated(button("✕ Exit").apply { setOnClickListener { examTimer?.cancel(); showHome() } })
        examStatusView = label("Question 1/$count • Score 0", 17f)
        addViewAnimated(examStatusView)
        examQuestionView = card("")
        addViewAnimated(examQuestionView)
        val options = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        addViewAnimated(options)
        examNextButton = button("Next")
        examNextButton.isEnabled = false
        addViewAnimated(examNextButton)
        setContentView(root)
        renderExamQuestion(options)

        val minutes = if (count <= 20) 20L else if (count <= 50) 45L else 90L
        examTimer = object : CountDownTimer(minutes * 60_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                val min = millisUntilFinished / 60_000L
                val sec = (millisUntilFinished / 1_000L) % 60L
                examStatusView.text = "Question ${examIndex + 1}/$count • Score $examScore • ⏱️ $min:${String.format("%02d", sec)}"
            }
            override fun onFinish() { finishExam() }
        }.start()
    }

    private fun renderExamQuestion(options: LinearLayout) {
        options.removeAllViews()
        examNextButton.isEnabled = false
        examNextButton.text = if (examIndex == examQuestions.lastIndex) "Submit Exam" else "Next"
        val question = examQuestions[examIndex]
        examQuestionView.text = "[${question.category}]\n\n${question.question}"

        val distractors = questions.filter { it.question != question.question }.shuffled().take(3).map { it.answer }
        val answers = (listOf(question.answer) + distractors).shuffled()

        for (answer in answers) {
            val optionButton = button(answer)
            options.addView(optionButton)
            optionButton.setOnClickListener {
                for (j in 0 until options.childCount) options.getChildAt(j).isEnabled = false
                optionButton.text = if (answer == question.answer) "✅ $answer" else "❌ $answer"
                if (answer == question.answer) examScore++
                examNextButton.isEnabled = true
            }
        }

        examNextButton.setOnClickListener {
            if (examIndex == examQuestions.lastIndex) finishExam() else {
                examIndex++
                renderExamQuestion(options)
            }
        }
    }

    private fun finishExam() {
        examTimer?.cancel()
        val percentage = if (examQuestions.isEmpty()) 0 else examScore * 100 / examQuestions.size
        root = createLayout()
        addViewAnimated(animatedGirl())
        addViewAnimated(label("🏆 Exam Complete", 30f))
        addViewAnimated(label("$examScore / ${examQuestions.size}", 38f))
        addViewAnimated(label("$percentage%", 30f))
        val message = when {
            percentage >= 90 -> "🏆 Excellent!"
            percentage >= 75 -> "🌟 Very Good!"
            percentage >= 60 -> "👍 Good progress!"
            else -> "💪 Keep practicing!"
        }
        addViewAnimated(card(message))
        addViewAnimated(button("← Home").apply { setOnClickListener { showHome() } })
        setContentView(root)
    }

    private fun showSettings() {
        root = createLayout()
        addViewAnimated(button("← Home").apply { setOnClickListener { showHome() } })
        addViewAnimated(animatedGirl())
        addViewAnimated(label("⚙️ Settings", 28f))
        addViewAnimated(label("Button colour", 17f))
        val primaryColors = listOf(
            "🔵 Blue" to Color.rgb(78, 93, 210),
            "🟣 Purple" to Color.rgb(125, 82, 190),
            "🟢 Green" to Color.rgb(35, 145, 100),
            "🟠 Orange" to Color.rgb(225, 125, 45),
            "🌸 Pink" to Color.rgb(205, 75, 135),
            "🔴 Red" to Color.rgb(205, 55, 65)
        )
        primaryColors.forEach { (name, color) ->
            addViewAnimated(button(name).apply { setOnClickListener { prefs.edit().putInt("primary", color).apply(); showSettings() } })
        }
        addViewAnimated(label("Background colour", 17f))
        val backgrounds = listOf(
            "☁️ Light Blue" to Color.rgb(205, 220, 255),
            "🌙 Dark" to Color.rgb(35, 38, 50),
            "🌿 Green" to Color.rgb(205, 240, 218),
            "🌸 Pink" to Color.rgb(255, 210, 232),
            "🌊 Sky" to Color.rgb(190, 225, 255),
            "🌅 Cream" to Color.rgb(255, 230, 180),
            "⚪ White" to Color.WHITE
        )
        backgrounds.forEach { (name, color) ->
            addViewAnimated(button(name).apply { setOnClickListener { prefs.edit().putInt("background", color).apply(); showSettings() } })
        }
        val animationSwitch = Switch(this).apply {
            text = "✨ Animation + moving character"
            isChecked = prefs.getBoolean("animation", true)
            setOnCheckedChangeListener { _, enabled -> prefs.edit().putBoolean("animation", enabled).apply() }
        }
        addViewAnimated(animationSwitch)
        addViewAnimated(card("Question, your answer and correct answer are shown separately. Voice automatically completes after about 3 seconds of silence."))
        addViewAnimated(button("Save & Home").apply { setOnClickListener { showHome() } })
        setContentView(root)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 7 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startListening()
    }

    override fun onDestroy() {
        stopListening()
        examTimer?.cancel()
        super.onDestroy()
    }
}
