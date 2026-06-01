package com.example.papirus

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView

class SearchActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "PapirusSettings"
    private val KEY_IS_DARK_MODE = "isDarkMode"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        setupWindowInsets()
        setupThemeToggle()
        setupSearchLogic()
        setupAIBubble()
        setupBottomNavigation()
        setupRecyclerViews()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottom_navigation_search).selectedItemId = R.id.nav_search
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_search)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupThemeToggle() {
        val ivThemeToggle = findViewById<ImageView>(R.id.iv_theme_toggle_search)
        val systemNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDarkMode = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, systemNightMode)

        ivThemeToggle.setImageResource(if (isDarkMode) R.drawable.ic_sun else R.drawable.ic_moon)

        ivThemeToggle.setOnClickListener {
            val newMode = !sharedPreferences.getBoolean(KEY_IS_DARK_MODE, systemNightMode)
            sharedPreferences.edit { putBoolean(KEY_IS_DARK_MODE, newMode) }

            if (newMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun setupSearchLogic() {
        val etSearchInput = findViewById<EditText>(R.id.et_search_input)
        val llHistory = findViewById<LinearLayout>(R.id.ll_search_history)
        val llResults = findViewById<LinearLayout>(R.id.ll_search_results)

        etSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    llHistory.visibility = View.VISIBLE
                    llResults.visibility = View.GONE
                } else {
                    llHistory.visibility = View.GONE
                    llResults.visibility = View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerViews() {
        val dummySearchHistory = mutableListOf(
            Book("1", "Siber Güvenlik", R.drawable.beyaz_logo),
            Book("2", "Yapay Zeka 101", R.drawable.beyaz_logo)
        )

        val rvSearchHistory = findViewById<RecyclerView>(R.id.rv_search_history)
        val historyAdapter = BookAdapter(dummySearchHistory) { secilenKitap ->
            Toast.makeText(this, "${secilenKitap.title} aranıyor...", Toast.LENGTH_SHORT).show()
        }
        rvSearchHistory.adapter = historyAdapter

        val tvClearHistory = findViewById<TextView>(R.id.tv_clear_history)
        val llHistory = findViewById<LinearLayout>(R.id.ll_search_history)

        tvClearHistory.setOnClickListener {
            historyAdapter.updateData(emptyList())
            llHistory.visibility = View.GONE
            Toast.makeText(this, "Arama geçmişi temizlendi", Toast.LENGTH_SHORT).show()
        }

        val rvSearchResults = findViewById<RecyclerView>(R.id.rv_search_results)
        rvSearchResults.adapter = BookAdapter(emptyList())
    }

    private fun setupAIBubble() {
        val cvAiBubble = findViewById<MaterialCardView>(R.id.cv_ai_bubble)

        cvAiBubble.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_ai_chat, null)
            bottomSheetDialog.setContentView(bottomSheetView)

            val rvChat = bottomSheetView.findViewById<RecyclerView>(R.id.rv_ai_chat)
            val etInput = bottomSheetView.findViewById<EditText>(R.id.et_ai_message_input)
            val btnSend = bottomSheetView.findViewById<ImageView>(R.id.iv_ai_send_btn)

            val chatHistory = mutableListOf(
                ChatMessage(
                    "Merhaba! Papirus veritabanındaki binlerce kitap arasından sana en uygun olanı bulabilirim. Ne tür şeyler okumaktan hoşlanırsın?",
                    false
                )
            )
            val chatAdapter = ChatAdapter(chatHistory)
            rvChat.adapter = chatAdapter

            btnSend.setOnClickListener {
                val userText = etInput.text.toString().trim()
                if (userText.isNotEmpty()) {
                    chatAdapter.addMessage(ChatMessage(userText, true))
                    etInput.text.clear()
                    rvChat.scrollToPosition(chatAdapter.itemCount - 1)

                    rvChat.postDelayed({
                        val aiResponse = "Senin için 'Stories' veritabanını tarıyorum... " +
                                "'$userText' konusuna benzeyen harika bir fantastik kitap buldum: 'Yıldızların Altında'. Okumak ister misin?"
                        chatAdapter.addMessage(ChatMessage(aiResponse, false))
                        rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                    }, 1000)
                }
            }
            bottomSheetDialog.show()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_search)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_search -> true
                R.id.nav_feed -> true
                R.id.nav_library -> { // 4. Simge: Kütüphaneye Geçiş
                    val intent = Intent(this, LibraryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}