/*
 *   ownCloud Android client application
 *
 *   @author David González Verdugo
 *   Copyright (C) 2020 ownCloud GmbH.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.presentation.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.owncloud.android.R
import info.hannes.logcat.BothLogsFragment
import info.hannes.logcat.LogcatFragment
import info.hannes.timber.fileLoggingTree

class LogHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logs)

        val toolbar = findViewById<Toolbar>(R.id.standard_toolbar).apply {
            setTitle(R.string.actionbar_logger)
            isVisible = true
        }
        findViewById<ConstraintLayout>(R.id.root_toolbar).isVisible = false

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Check that the activity is using the layout version with the fragment_container FrameLayout
        if (findViewById<View>(R.id.fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return
            }

            var logFragment: Fragment? = null

            fileLoggingTree()?.let {
                logFragment = BothLogsFragment.newInstance(
                    "${getString(R.string.app_name)}.log",
                    bothLogsSearchHint,
                    logCatSearchHint,
                    getString(R.string.mail_logger)
                )
            } ?: run {
                logFragment = LogcatFragment.newInstance(
                    logCatTargetFileName,
                    logCatSearchHint,
                    getString(R.string.mail_logger)
                )
            }

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            // firstFragment.arguments = intent.extras

            logFragment?.let {
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, it).commit()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var retval = true
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> retval = super.onOptionsItemSelected(item)
        }
        return retval
    }

    companion object {
        private const val logCatTargetFileName = "logfile.log"
        private const val logCatSearchHint = "search logcat"
        private const val bothLogsSearchHint = "search logfile"
    }
}
