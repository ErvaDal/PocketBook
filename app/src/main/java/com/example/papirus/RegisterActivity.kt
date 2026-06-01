package com.example.papirus

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import com.example.papirus.data.RegisterRequest
import com.example.papirus.data.ApiResponse

import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)


        //kenar boşlukları için
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density

            // boşluk devam
            val paddingPx = (32 * density).toInt()

            v.setPadding(
                systemBars.left + paddingPx,
                systemBars.top + paddingPx,
                systemBars.right + paddingPx,
                systemBars.bottom + paddingPx
            )
            insets
        }

        // doğum tarihi takvim için
        val etBirthDate = findViewById<TextInputEditText>(R.id.et_BirthDate)

        etBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format(
                        "%02d/%02d/%04d",
                        selectedDay,
                        selectedMonth + 1,
                        selectedYear
                    )
                    etBirthDate.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        // tema değiştirme
        val ivThemeToggle = findViewById<ImageView>(R.id.iv_theme_toggle)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            ivThemeToggle.setImageResource(R.drawable.ic_sun)
        } else {
            ivThemeToggle.setImageResource(R.drawable.ic_moon)
        }

        ivThemeToggle.setOnClickListener {
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }


        val btnLoginRedirect = findViewById<TextView>(R.id.btn_register_login_redirect)
        btnLoginRedirect.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val username = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_Username)
        val email = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_Email)
        val birthDate = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_BirthDate)
        val password = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_PasswordHash)
        val passwordConfirm = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_PasswordConfirm)
        val btnRegister = findViewById<MaterialButton>(R.id.btn_register_submit)


        btnRegister.setOnClickListener {

            val request = RegisterRequest(
                username.text.toString(),
                email.text.toString(),
                password.text.toString(),
                birthDate.text.toString()
            )

            RetrofitClient.api.register(request).enqueue(object : Callback<ApiResponse> {

                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    if (response.isSuccessful) {

                        Log.d("REGISTER", "BAŞARILI 🎉")

                        startActivity(
                            Intent(this@RegisterActivity, LoginActivity::class.java)
                        )
                        finish()

                    } else {
                        Log.e("REGISTER", "HATA: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("REGISTER", "NETWORK ERROR: ${t.message}")
                }
            })
        }
    }
}