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

package com.wideverse.headlesswifimanager_discoverer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.wideverse.headlesswifimanager_discoverer.fragment.*
import com.wideverse.headlesswifimanager_discoverer.helper.DiscovererViewPager.Companion.VIEW_WIFI_LIST


class MainPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val fragmentList: List<Fragment> = listOf(
        WelcomeFragment(),
        ScanningFragment(),
        AdvertiserConnectedFragment(),
        WifiSelectFragment(),
        WifiConnectingFragment(),
        DoneFragment(),
        ErrorFragment())

    fun getWifiFragment(): WifiSelectFragment = fragmentList[VIEW_WIFI_LIST] as WifiSelectFragment

    override fun getItem(position: Int) = fragmentList[position]

    override fun getCount() = 7
}
