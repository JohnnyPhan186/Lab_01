package com.example.flashcardapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    lateinit var flashcardDatabase: FlashcardDatabase
    var allFlashcards = mutableListOf<Flashcard>()
    var currentCardDisplayedIndex = 0 //a variable to keep track of the index of the current card we're showing.

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        flashcardDatabase = FlashcardDatabase(this)
        allFlashcards = flashcardDatabase.getAllCards().toMutableList()

        val flashcardQuestion = findViewById<TextView>(R.id.flashcard_question)
        val flashcardAnswer = findViewById<TextView>(R.id.flashcard_answer)

        if (allFlashcards.isNotEmpty()) {
            flashcardQuestion.text = allFlashcards[0].question //first question of my database
            flashcardAnswer.text = allFlashcards[0].answer
        }

        flashcardQuestion.setOnClickListener {
            flashcardAnswer.visibility = View.VISIBLE
            flashcardQuestion.visibility = View.INVISIBLE

            Snackbar.make(
                flashcardQuestion, "Question button was clicked",
                Snackbar.LENGTH_SHORT
            ).show()
            val answerSideView = findViewById<View>(R.id.flashcard_answer)
            val questionSideView = findViewById<View>(R.id.flashcard_question)

// get the center for the clipping circle
            val cx = answerSideView.width / 2
            val cy = answerSideView.height / 2

// get the final radius for the clipping circle
            val finalRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()
// create the animator for this view (the start radius is zero)
            val anim = ViewAnimationUtils.createCircularReveal(answerSideView, cx, cy, 0f, finalRadius)
// hide the question and show the answer to prepare for playing the animation!
            questionSideView.visibility = View.INVISIBLE
            answerSideView.visibility = View.VISIBLE

            anim.duration = 3000
            anim.start()
        }

        flashcardAnswer.setOnClickListener {
            flashcardAnswer.visibility = View.INVISIBLE
            flashcardQuestion.visibility = View.VISIBLE
        }

        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                val data: Intent? = result.data //Question and answer Data

                if (data != null) {
                    val questionString = data.getStringExtra("QUESTION_KEY")
                    val answerString = data.getStringExtra("ANSWER_KEY")

                    flashcardQuestion.text = questionString
                    flashcardAnswer.text = answerString

                    if (!questionString.isNullOrEmpty() && !answerString.isNullOrEmpty()) //null check for Flashcard
                        flashcardDatabase.insertCard(Flashcard(questionString, answerString))
                    allFlashcards = flashcardDatabase.getAllCards().toMutableList()


                }
            }

        findViewById<ImageView>(R.id.add_question_button).setOnClickListener {
            val intent = Intent(this, AddCardActivity::class.java)
            resultLauncher.launch(intent)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }
        val nextButton = findViewById<ImageView>(R.id.add_next_button)
        nextButton.setOnClickListener {
            if (allFlashcards.isEmpty()) {
                return@setOnClickListener
            }

            // Loading the resource animation files
            val leftOutAnim = AnimationUtils.loadAnimation(it.context, R.anim.left_out)
            val rightInAnim = AnimationUtils.loadAnimation(it.context, R.anim.right_in)

            // Through listeners, play the two animations in sequence
            leftOutAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    // this method is called when the animation first starts
                    flashcardAnswer.visibility = View.INVISIBLE //hide the answer and show the question!
                    flashcardQuestion.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {
                    // this method is called when the animation is finished playing
                    flashcardQuestion.startAnimation(rightInAnim)

                    currentCardDisplayedIndex++

                    if (currentCardDisplayedIndex >= allFlashcards.size) {
                        currentCardDisplayedIndex = 0
                    }

                    allFlashcards = flashcardDatabase.getAllCards().toMutableList()

                    val question = allFlashcards[currentCardDisplayedIndex].question
                    val answer = allFlashcards[currentCardDisplayedIndex].answer

                    flashcardQuestion.text = question
                    flashcardAnswer.text = answer

                    flashcardAnswer.visibility =
                        View.INVISIBLE //hide the answer and show the question!
                    flashcardQuestion.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    // we don't need to worry about this method
                }
            })
            flashcardQuestion.startAnimation(leftOutAnim)
        }

    }
}