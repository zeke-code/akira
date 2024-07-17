package com.zekecode.akira_financialtracker.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zekecode.akira_financialtracker.databinding.ActivityFirstSetupBinding

class FirstSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstSetupBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFirstSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AkiraPrefs", MODE_PRIVATE)

        // Show welcome view and then switch to user input view
        switchViewWithAnimation(binding.welcomeView, binding.userInputView, 5000)

        binding.saveButton.setOnClickListener {
            val userName = binding.userNameEditText.text.toString().trim()
            val monthlyBudgetStr = binding.monthlyBudgetEditText.text.toString().trim()

            if (userName.isNotEmpty() && monthlyBudgetStr.isNotEmpty()) {
                try {
                    val monthlyBudget = monthlyBudgetStr.toFloat()
                    if (!isValidDecimal(monthlyBudgetStr)) {
                        Toast.makeText(this, "Please enter a number with at most 2 decimal numbers", Toast.LENGTH_SHORT).show()
                    } else {

                        switchViewWithAnimation(binding.userInputView, binding.readyView)

                        // Save data in sharedPreferences and proceed to the home activity after a short delay
                        binding.readyView.postDelayed({
                            with(sharedPreferences.edit()) {
                                putString("Username", userName)
                                putFloat("MonthlyBudget", monthlyBudget)
                                putBoolean("IsSetupComplete", true)
                                apply()
                            }
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }, 4000)
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Please enter a valid number for the budget", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Checks if a number has equal or less than two decimal digits
    private fun isValidDecimal(numberStr: String): Boolean {
        val parts = numberStr.split(".")
        return parts.size <= 1 || parts[1].length <= 2
    }

    private fun switchViewWithAnimation(fromView: View, toView: View, delay: Long = 0) {
        if (delay > 0) {
            fromView.postDelayed({
                fadeOut(fromView) {
                    fromView.visibility = View.GONE
                    fadeIn(toView)
                    toView.visibility = View.VISIBLE
                }
            }, delay)
        } else {
            fadeOut(fromView) {
                fromView.visibility = View.GONE
                fadeIn(toView)
                toView.visibility = View.VISIBLE
            }
        }
    }

    private fun fadeIn(view: View, duration: Long = 500) {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            this.duration = duration
            fillAfter = true
        }
        view.startAnimation(fadeIn)
    }

    private fun fadeOut(view: View, duration: Long = 500, onAnimationEnd: (() -> Unit)? = null) {
        val fadeOut = AlphaAnimation(1f, 0f).apply {
            this.duration = duration
            fillAfter = true
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    onAnimationEnd?.invoke()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        view.startAnimation(fadeOut)
    }
}
