package com.example.papirus

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout_notifs)
        val llNotifs = findViewById<LinearLayout>(R.id.ll_notifications_container)
        val llMessages = findViewById<LinearLayout>(R.id.ll_messages_container)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    llNotifs.visibility = View.VISIBLE
                    llMessages.visibility = View.GONE
                } else {
                    llNotifs.visibility = View.GONE
                    llMessages.visibility = View.VISIBLE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}