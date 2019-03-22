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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.wideverse.headlesswifimanager.data.WifiScanResult
import com.wideverse.headlesswifimanager_discoverer.R
import kotlinx.android.synthetic.main.dialog_password.view.*

class PasswordDialogFragment: DialogFragment(){
    lateinit var listener: OnFragmentWifiSelected
    var wifiSelected: WifiScanResult? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as OnFragmentWifiSelected
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view =  inflater.inflate(R.layout.dialog_password, null)
            view.passwordLabel.text = "Type the password for the network ${wifiSelected?.SSID}"
            view.passwordCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    // hide password
                    view.passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    view.passwordEditText.moveCursorToVisibleOffset()
                } else {
                    // show password
                    view.passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                    view.passwordEditText.moveCursorToVisibleOffset()
                }
            }
                builder.setView(view)
                // Add action button
                .setPositiveButton("Confirm"
                ) { dialog, id ->
                    var password = view.passwordEditText.text.toString()
                    wifiSelected?.password = password
                    listener.onFragmentWifiSelected(wifiSelected!!)


                    // sign in the user ...
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialog, id ->
                    getDialog().cancel()
                    //fragmentListener.onDialogNegativeClick(this)

                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}