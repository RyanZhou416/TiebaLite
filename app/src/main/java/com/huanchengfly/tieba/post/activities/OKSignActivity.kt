package com.huanchengfly.tieba.post.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huanchengfly.tieba.post.utils.TiebaUtil

class OKSignActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TiebaUtil.startSign(this)
        finish()
    }
}
