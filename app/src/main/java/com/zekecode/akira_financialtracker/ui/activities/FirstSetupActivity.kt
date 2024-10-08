package com.zekecode.akira_financialtracker.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.databinding.ActivityFirstSetupBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.FirstSetupViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FirstSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirstSetupBinding
    private val viewModel: FirstSetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFirstSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()

        viewModel.showReadyView.observe(this, Observer { showReady ->
            if (showReady) {
                switchViewWithAnimation(binding.userInputView, binding.readyView)
            } else {
                Toast.makeText(
                    this,
                    "Please enter a number bigger than 20 with at most 2 decimal digits",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        viewModel.isSetupComplete.observe(this, Observer { isComplete ->
            if (isComplete) {
                val intent = Intent(this@FirstSetupActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })

        binding.saveButton.setOnClickListener {
            val userName = binding.userNameEditText.text.toString().trim()
            val monthlyBudgetStr = binding.monthlyBudgetEditText.text.toString().trim()
            val selectedCurrency = binding.currencyAutoCompleteTextView.text.toString().trim()

            if (userName.isNotEmpty() && monthlyBudgetStr.isNotEmpty() && selectedCurrency.isNotEmpty()) {
                viewModel.saveUserData(userName, monthlyBudgetStr, selectedCurrency)
            } else {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI() {
        val currencyAutoCompleteTextView = binding.currencyAutoCompleteTextView
        val currencyOptions = resources.getStringArray(R.array.currency_options)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            currencyOptions
        )
        currencyAutoCompleteTextView.setAdapter(adapter)

        switchViewWithAnimation(binding.welcomeView, binding.userInputView, 5000)
    }

    private fun switchViewWithAnimation(fromView: View, toView: View, delay: Long = 0) {
        if (delay > 0) {
            fromView.postDelayed({
                fadeOut(fromView) {
                    fromView.visibility = View.GONE
                    fadeIn(toView)
                }
            }, delay)
        } else {
            fadeOut(fromView) {
                fromView.visibility = View.GONE
                fadeIn(toView)
            }
        }
    }

    private fun fadeIn(
        view: View,
        duration: Long = 500,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        view.visibility = View.VISIBLE

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            this.duration = duration
            fillAfter = true
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    // Disable interaction during animation
                    view.isEnabled = false
                    view.isClickable = false
                    view.isFocusable = false
                }

                override fun onAnimationEnd(animation: Animation?) {
                    // Enable interaction after animation ends
                    view.isEnabled = true
                    view.isClickable = true
                    view.isFocusable = true
                    onAnimationEnd?.invoke()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        view.startAnimation(fadeIn)
    }

    private fun fadeOut(
        view: View,
        duration: Long = 500,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        // Disable interaction before animation starts
        view.isEnabled = false
        view.isClickable = false
        view.isFocusable = false

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
