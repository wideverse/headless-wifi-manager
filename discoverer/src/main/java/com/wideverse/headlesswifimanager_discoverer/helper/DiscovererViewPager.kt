/*
 * Copyright 2019 Wideverse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wideverse.headlesswifimanager_discoverer.helper

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


class DiscovererViewPager : ViewPager {

    companion object {
        const val VIEW_WELCOME = 0
        const val VIEW_SCANNING = 1
        const val VIEW_ADVERTISER_CONNECTED = 2
        const val VIEW_WIFI_LIST = 3
        const val VIEW_WIFI_CONNECTING = 4
        const val VIEW_WIFI_DONE = 5
        const val VIEW_WIFI_ERROR = 6
    }

    fun setSlide(position: Int){
        this.currentItem = position
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }
}