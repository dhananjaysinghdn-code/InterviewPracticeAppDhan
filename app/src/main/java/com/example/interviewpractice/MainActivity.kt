package com.example.interviewpractice

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import java.util.Locale

data class Q(val question: String, val answer: String, val keywords: List<String>)
data class Attempt(val questionIndex: Int, val spoken: String, val marks: Int)

class MainActivity : Activity() {
    private val questions = listOf(
        Q("What is Selenium WebDriver?", "Selenium WebDriver is an API used to automate web browsers.", listOf("selenium", "webdriver", "automate", "browser")),
        Q("What is XPath?", "XPath is a locator used to identify elements in an HTML or XML document.", listOf("xpath", "locator", "element")),
        Q("What is an explicit wait?", "An explicit wait waits for a specific condition before continuing.", listOf("explicit", "wait", "condition")),
        Q("What is TestNG?", "TestNG is a Java testing framework used to organize and execute automated tests.", listOf("testng", "testing", "framework", "java")),
        Q("What is Page Object Model?", "POM keeps page locators and page actions inside page classes.", listOf("page", "object", "model", "locator", "action")),
        Q("What is SQL?", "SQL is a language used to query and manage relational database data.", listOf("sql", "query", "database")),
        Q("What is an API?", "An API is an interface that allows software systems to communicate.", listOf("api", "interface", "communicate"))
    )
    private var index = 0
    private var total = 0
    private var answered = 0
    private var listening = false
    private var finishing = false
    private var spokenText = StringBuilder()
    private var recognizer: SpeechRecognizer? = null
    private val attempts = mutableListOf<Attempt>()
    private lateinit var prefs: SharedPreferences
    private lateinit var root: LinearLayout
    private lateinit var qText: TextView
    private lateinit var result: TextView
    private lateinit var score: TextView
    private lateinit var speak: Button
    private lateinit var progress: ProgressBar

    override fun onCreate(b: Bundle?) { super.onCreate(b); prefs = getSharedPreferences("settings", MODE_PRIVATE); showHome() }
    private fun primary() = prefs.getInt("primary", Color.rgb(78,93,210))
    private fun background() = prefs.getInt("background", Color.rgb(242,245,255))

    private fun baseLayout() = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(24,24,24,24); setBackgroundColor(background()) }
    private fun title(text:String,size:Float=26f)=TextView(this).apply{this.text=text;textSize=size;setTextColor(Color.rgb(31,41,84));setPadding(0,8,0,16);gravity=Gravity.CENTER}
    private fun card(text:String,size:Float=17f)=TextView(this).apply{this.text=text;textSize=size;setTextColor(Color.rgb(35,42,60));setPadding(22,22,22,22);background=GradientDrawable().apply{setColor(Color.WHITE);cornerRadius=28f;setStroke(2,Color.rgb(225,229,245))};elevation=7f}
    private fun button(text:String)=Button(this).apply{this.text=text;textSize=16f;isAllCaps=false;setTextColor(Color.WHITE);background=GradientDrawable().apply{setColor(primary());cornerRadius=28f};setPadding(14,10,14,10);elevation=5f}
    private fun animateIn(v:View){v.alpha=0f;v.translationY=22f;v.animate().alpha(1f).translationY(0f).setDuration(420).start()}
    private fun pulse(v:View){val a=AlphaAnimation(0.45f,1f);a.duration=650;a.repeatMode=Animation.REVERSE;a.repeatCount=Animation.INFINITE;v.startAnimation(a)}

    private fun showHome(){
        root=baseLayout();root.addView(title("🎯 Interview Practice",30f));root.addView(title("Practice • Speak • Review • Improve",18f))
        val test=button("🎤  Start Test");val list=button("📚  All Questions & Answers");val review=button("📝  Review My Answers");val settings=button("⚙️  Settings");val clear=button("🗑️  Clear My Results")
        listOf(test,list,review,settings,clear).forEach{root.addView(it);(it.layoutParams as LinearLayout.LayoutParams).setMargins(0,7,0,7);animateIn(it)}
        setContentView(root);test.setOnClickListener{showTest()};list.setOnClickListener{showQuestionList()};review.setOnClickListener{showReview()};settings.setOnClickListener{showSettings()}
        clear.setOnClickListener{attempts.clear();total=0;answered=0;Toast.makeText(this,"Results cleared",Toast.LENGTH_SHORT).show();showHome()}
    }

    private fun showSettings(){
        root=baseLayout();val home=button("←  Home");root.addView(home);home.setOnClickListener{showHome()};root.addView(title("⚙️ Settings",28f));root.addView(title("Choose app colour",18f))
        listOf("🔵 Blue" to Color.rgb(78,93,210),"🟣 Purple" to Color.rgb(125,82,190),"🟢 Green" to Color.rgb(35,145,100),"🟠 Orange" to Color.rgb(225,125,45),"🌸 Pink" to Color.rgb(205,75,135)).forEach{(name,color)->val b=button(name);b.setOnClickListener{prefs.edit().putInt("primary",color).apply();showSettings()};root.addView(b);animateIn(b)}
        val anim=Switch(this).apply{text="✨ Animated UI";textSize=17f;isChecked=prefs.getBoolean("animation",true);setPadding(0,20,0,20)};root.addView(anim);anim.setOnCheckedChangeListener{_,checked->prefs.edit().putBoolean("animation",checked).apply()}
        root.addView(card("🔊 Voice sound\n\nThe small microphone/listening beep is produced by Android's speech-recognition service. The app does not play an extra sound, so there is no extra app sound to turn down. If Android exposes a speech/assistant volume control on your phone, that controls this beep."))
        val done=button("✅  Save & Home");root.addView(done);done.setOnClickListener{showHome()};setContentView(root)
    }

    private fun showTest(){
        root=baseLayout();val home=button("←  Home");root.addView(home);home.setOnClickListener{stopListening();showHome()};score=title("⭐ Score: $total",18f);root.addView(score)
        progress=ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.max=questions.size;progress.progress=answered.coerceAtMost(questions.size);root.addView(progress)
        qText=card("");root.addView(qText);animateIn(qText);speak=button("🎤  Start Speaking");root.addView(speak);animateIn(speak);result=card("");root.addView(result);result.visibility=View.GONE
        val next=button("➡️  Next Question");root.addView(next);setContentView(root);showQuestion();speak.setOnClickListener{if(listening)finishAnswer() else startListening()};next.setOnClickListener{stopListening();index=(index+1)%questions.size;result.text="";result.visibility=View.GONE;showQuestion()}
    }
    private fun showQuestionList(){val scroll=ScrollView(this);val list=baseLayout();val home=button("←  Home");list.addView(home);home.setOnClickListener{showHome()};list.addView(title("📚 Questions & Answers",26f));questions.forEachIndexed{i,q->val c=card("Q${i+1}. ${q.question}\n\nAnswer: ${q.answer}");list.addView(c);animateIn(c);c.setOnClickListener{index=i;showTest()}};scroll.addView(list);setContentView(scroll)}
    private fun showReview(){val scroll=ScrollView(this);val list=baseLayout();val home=button("←  Home");list.addView(home);home.setOnClickListener{showHome()};list.addView(title("📝 My Answer Review",26f));if(attempts.isEmpty())list.addView(title("No answers yet. Start a test first.",18f));attempts.forEach{a->val q=questions[a.questionIndex];val c=card("Q${a.questionIndex+1}. ${q.question}\n\n🗣️ YOUR ANSWER\n${a.spoken.ifBlank{"(No answer detected)"}}\n\n⭐ SCORE: ${a.marks}/10\n\n✅ CORRECT ANSWER\n${q.answer}");list.addView(c);animateIn(c)};scroll.addView(list);setContentView(scroll)}
    private fun showQuestion(){qText.text="Q${index+1}\n\n${questions[index].question}";score.text="⭐ Score: $total   •   Answered: $answered/${questions.size}";progress.progress=answered.coerceAtMost(questions.size);speak.isEnabled=true;speak.text="🎤  Start Speaking"}

    private fun startListening(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),200);return}
        if(!SpeechRecognizer.isRecognitionAvailable(this)){result.visibility=View.VISIBLE;result.text="Speech recognition available nahi hai.";return}
        recognizer?.destroy();recognizer=SpeechRecognizer.createSpeechRecognizer(this)
        recognizer!!.setRecognitionListener(object:RecognitionListener{
            override fun onReadyForSpeech(p:Bundle?){result.visibility=View.VISIBLE;result.text="🎧  Ready... ab pura answer bolo.";if(prefs.getBoolean("animation",true))pulse(speak)}
            override fun onBeginningOfSpeech(){listening=true;result.text="🎧  Sun raha hoon... bolte raho."}
            override fun onRmsChanged(r:Float){};override fun onBufferReceived(b:ByteArray?){}
            override fun onEndOfSpeech(){result.text="⏳  Answer process ho raha hai..."}
            override fun onError(e:Int){if(finishing||!listening)return;android.os.Handler(mainLooper).postDelayed({if(listening&&!finishing)startRecognition()},350)}
            override fun onResults(data:Bundle?){val text=data?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty();if(text.isNotBlank())spokenText.append(" ").append(text);finishAnswer()}
            override fun onPartialResults(data:Bundle?){val text=data?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty();if(text.isNotBlank()){result.visibility=View.VISIBLE;result.text="🎧  $text\n\nKeep speaking..."}}
            override fun onEvent(t:Int,p:Bundle?){}}
        )
        finishing=false;listening=true;spokenText.clear();speak.text="⏹️  Listening...";result.visibility=View.VISIBLE;result.text="🎧  Ready...";if(prefs.getBoolean("animation",true))pulse(speak);startRecognition()
    }
    private fun startRecognition(){try{recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.ENGLISH);putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,3000L);putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,3000L);putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1)})}catch(_:Exception){}}
    private fun finishAnswer(){if(finishing)return;finishing=true;listening=false;speak.clearAnimation();speak.text="🎤  Start Speaking";try{recognizer?.stopListening()}catch(_:Exception){};evaluate(spokenText.toString().trim())}
    private fun stopListening(){finishing=true;listening=false;speak.clearAnimation();try{recognizer?.stopListening();recognizer?.destroy()}catch(_:Exception){};recognizer=null}
    private fun evaluate(spoken:String){val s=spoken.lowercase();val hits=questions[index].keywords.count{s.contains(it)};val pct=if(questions[index].keywords.isEmpty())0 else hits*100/questions[index].keywords.size;val marks=when{pct>=75->10;pct>=50->7;pct>=25->4;else->1};total+=marks;answered++;attempts.add(Attempt(index,spoken,marks));val verdict=if(marks>=7)"✅  Good Answer"else if(marks>=4)"⚠️  Partially Correct"else"❌  Need Improvement";result.visibility=View.VISIBLE;result.text="🗣️  YOUR ANSWER\n${spoken.ifBlank{"(No answer detected)"}}\n\n$verdict\n⭐  How correct: $marks/10\n\n✅  CORRECT ANSWER\n${questions[index].answer}";result.alpha=0f;result.animate().alpha(1f).setDuration(450).start();score.text="⭐ Score: $total   •   Answered: $answered/${questions.size}";progress.progress=answered.coerceAtMost(questions.size)}
    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,results:IntArray){super.onRequestPermissionsResult(requestCode,permissions,results);if(requestCode==200&&results.isNotEmpty()&&results[0]==PackageManager.PERMISSION_GRANTED)startListening()}
    override fun onDestroy(){stopListening();super.onDestroy()}
}
