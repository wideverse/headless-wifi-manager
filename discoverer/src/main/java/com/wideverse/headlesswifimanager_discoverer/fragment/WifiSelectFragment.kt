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

package com.wideverse.headlesswifimanager_discoverer.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wideverse.headlesswifimanager.data.WifiScanResult
import com.wideverse.headlesswifimanager.helper.WifiHelper
import com.wideverse.headlesswifimanager_discoverer.R
import com.wideverse.headlesswifimanager_discoverer.viewmodel.WifiViewModel
import kotlinx.android.synthetic.main.fragment_wifi_select.view.*
import kotlinx.android.synthetic.main.view_item_wifi.view.*
import org.jetbrains.anko.okButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.alert

class WifiSelectFragment : BaseFragment(), OnFragmentWifiSelected {
    lateinit var viewModel: WifiViewModel
    private var wifiListener: OnFragmentWifiSelected? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(WifiViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_wifi_select, container, false)

        view.cancelButton.onClick {
            fragmentListener?.onFragmentInteraction("cancelButton")
        }

        viewModel.recyclerAdapter = WifiViewAdapter(viewModel.wifiList, wifiListener!!)
        viewModel.recyclerLayout = LinearLayoutManager(inflater.context)

        view.wifiRecycler.layoutManager = viewModel.recyclerLayout
        view.wifiRecycler.adapter = viewModel.recyclerAdapter

        // Inflate the layout for this fragment
        return view
    }


    override fun onFragmentWifiSelected(result: WifiScanResult?) {
        if (result == null) {
            wifiListener?.onFragmentWifiSelected(null)
        } else {
            viewModel.selectedWifi = result
            val passwordDialog = PasswordDialogFragment()
            passwordDialog.wifiSelected = result

            if (result.protected) {
                passwordDialog.show(fragmentManager, "passwordFragment")
            } else {
                wifiListener?.onFragmentWifiSelected(result)
            }
        }
    }

    fun setWifiScanList(wifiList: List<WifiScanResult>) {
        if (wifiList.isEmpty()) {
            alert("No Wi-Fi network found. Try again later.") {
                okButton {
                    wifiListener?.onFragmentWifiSelected(null)
                }
            }.apply {
                isCancelable = false
                show()
            }
        }

        viewModel.wifiList = wifiList

        view?.wifiRecycler?.adapter =
            WifiViewAdapter(viewModel.wifiList, this)

        view?.wifiRecycler?.invalidate()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentWifiSelected) {
            wifiListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        wifiListener = null
    }

    /*override fun onDialogPositiveClick(dialog: DialogFragment, password: String) {
        selectedWifi?.password = password
        if (selectedWifi != null){
            wifiListener?.onFragmentWifiSelected(selectedWifi!!)
        }

        print("$TAG: $password")
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }*/
}

interface OnFragmentWifiSelected {
    fun onFragmentWifiSelected(result: WifiScanResult?)
}

class WifiViewAdapter(private val wifiList: List<WifiScanResult>, private val listener: OnFragmentWifiSelected) :
    RecyclerView.Adapter<WifiViewAdapter.WifiListHolder>() {

    class WifiListHolder(val view: View) : RecyclerView.ViewHolder(view)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): WifiListHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_item_wifi, parent, false)

        return WifiListHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: WifiListHolder, position: Int) {
        val currentWifi = wifiList[position]
        holder.view.wifiRssiIcon.setImageResource(WifiHelper.getDrawableFromRSSI(currentWifi.level, currentWifi.protected))
        holder.view.wifiTitle.text = wifiList[position].SSID

        holder.view.wifiItem.setOnClickListener {
             listener.onFragmentWifiSelected(wifiList[position])
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = wifiList.size
}