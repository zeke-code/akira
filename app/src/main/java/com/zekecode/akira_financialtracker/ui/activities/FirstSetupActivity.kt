package com.zekecode.akira_financialtracker.ui.activities

import com.zekecode.akira_financialtracker.R
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zekecode.akira_financialtracker.databinding.ActivityFirstSetupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirstSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstSetupBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFirstSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currencySpinner: Spinner = binding.currencySpinner
        val currencyOptions = resources.getStringArray(R.array.currency_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter

        sharedPreferences = getSharedPreferences("AkiraPrefs", MODE_PRIVATE)

        // Show welcome view and then switch to user input view
        switchViewWithAnimation(binding.welcomeView, binding.userInputView, 5000)

        binding.saveButton.setOnClickListener {
            val userName = binding.userNameEditText.text.toString().trim()
            val monthlyBudgetStr = binding.monthlyBudgetEditText.text.toString().trim()
            val selectedCurrency = binding.currencySpinner.selectedItem.toString()

            if (userName.isNotEmpty() && monthlyBudgetStr.isNotEmpty()) {
                try {
                    val monthlyBudget = monthlyBudgetStr.toFloat()
                    if (!isValidDecimal(monthlyBudgetStr)) {
                        Toast.makeText(this, "Please enter a number with at most 2 decimal numbers", Toast.LENGTH_SHORT).show()
                    } else {
                        // Save data in sharedPreferences and proceed to MainActivity after a short delay
                        CoroutineScope(Dispatchers.Main).launch{
                            switchViewWithAnimation(binding.userInputView, binding.readyView)
                            withContext(Dispatchers.IO) {
                                sharedPreferences.edit().apply {
                                    putString("Username", userName)
                                    putFloat("MonthlyBudget", monthlyBudget)
                                    putString("Currency", selectedCurrency)
                                    putBoolean("IsSetupComplete", true)
                                    apply()
                                }
                            }

                            delay(4000)

                            val intent = Intent(this@FirstSetupActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
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
