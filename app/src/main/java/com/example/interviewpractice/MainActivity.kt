package com.example.interviewpractice

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.*
import java.util.Locale

data class Q(val question:String, val answer:String, val keywords:List<String>)

class MainActivity: Activity() {
    private val questions = listOf(
        Q("What is Selenium WebDriver?","Selenium WebDriver is an API used to automate web browsers.", listOf("selenium","webdriver","automate","browser")),
        Q("What is an XPath?","XPath is a locator used to identify elements in an HTML or XML document.", listOf("xpath","locator","element")),
        Q("What is an explicit wait?","An explicit wait waits for a specific condition before continuing.", listOf("explicit","wait","condition")),
        Q("What is TestNG?","TestNG is a testing framework for Java used to organize and execute automated tests.", listOf("testng","testing","framework","java")),
        Q("What is Page Object Model?","POM is a design pattern that keeps page locators and page actions in page classes.", listOf("page","object","model","locator","action")),
        Q("What is SQL?","SQL is a language used to query and manage data in relational databases.", listOf("sql","query","database")),
        Q("What is an API?","An API is an interface that allows software systems to communicate with each other.", listOf("api","interface","communicate"))
    )
    private var index=0
    private var total=0
    private lateinit var qText:TextView
    private lateinit var result:TextView
    private lateinit var score:TextView

    override fun onCreate(b:Bundle?) {
        super.onCreate(b)
        val box=LinearLayout(this); box.orientation=LinearLayout.VERTICAL; box.setPadding(24,24,24,24)
        qText=TextView(this); qText.textSize=22f
        score=TextView(this); score.textSize=18f
        result=TextView(this); result.textSize=17f
        val speak=Button(this); speak.text="🎤 Answer Bolo"
        val next=Button(this); next.text="Next Question"
        box.addView(score); box.addView(qText); box.addView(speak); box.addView(result); box.addView(next)
        setContentView(box); showQuestion()
        speak.setOnClickListener { startSpeech() }
        next.setOnClickListener { index=(index+1)%questions.size; result.text=""; showQuestion() }
    }
    private fun showQuestion(){ qText.text="Q${index+1}: ${questions[index].question}"; score.text="Total Score: $total" }
    private fun startSpeech(){
        if(!SpeechRecognizer.isRecognitionAvailable(this)){ result.text="Speech recognition available nahi hai."; return }
        val i=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.ENGLISH)
        startActivityForResult(i,101)
    }
    override fun onActivityResult(r:Int,c:Int,d:Intent?){
        super.onActivityResult(r,c,d)
        if(r==101 && c==RESULT_OK){ evaluate(d?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()) }
    }
    private fun evaluate(spoken:String){
        val s=spoken.lowercase()
        val hits=questions[index].keywords.count{ s.contains(it) }
        val pct=hits*100/questions[index].keywords.size
        val marks=when { pct>=75->10; pct>=50->7; pct>=25->4; else->1 }
        total+=marks
        result.text="Aapka answer: $spoken\n\nMarks: $marks/10\n${if(marks>=7) "✅ Good" else if(marks>=4) "⚠️ Partial" else "❌ Need Improvement"}\n\nExpected Answer: ${questions[index].answer}"
        score.text="Total Score: $total"
    }
}
