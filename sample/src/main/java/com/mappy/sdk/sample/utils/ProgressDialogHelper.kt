package com.mappy.sdk.sample.utils

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import com.mappy.sdk.sample.R

class ProgressDialogHelper(val context: Context) {

    private var progressDialog: ProgressDialog? = null
    private var message: String? = null

    private var attachedToWindow = false

    private var showCount = 0

    fun withDefaultMessage() {
        setMessage(R.string.loading)
    }

    @Synchronized
    fun show(listener: DialogInterface.OnCancelListener?) {
        showCount++
        if (!isShowing()) {
            val progressDialog = object : ProgressDialog(
                context,
                if (!message.isNullOrEmpty()) 0 else R.style.MappyTheme_ProgressDialog
            ) {
                override fun onDetachedFromWindow() {
                    super.onDetachedFromWindow()
                    attachedToWindow = false
                }
            }

            progressDialog.setCancelable(true)
            progressDialog.isIndeterminate = true
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setOnCancelListener(listener)

            if (!message.isNullOrEmpty()) {
                setMessage(message!!)
                if (!progressDialog.isShowing) {
                    progressDialog.show()
                }
            } else {
                if (!progressDialog.isShowing) {
                    progressDialog.show()
                }
                progressDialog.setContentView(R.layout.utils_dialog_progress)
            }
            this.progressDialog = progressDialog
            attachedToWindow = true
        }
    }

    @Synchronized
    fun show() {
        show(null)
    }

    @Synchronized
    fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) =
        progressDialog?.setOnCancelListener(listener)

    @Synchronized
    fun dismiss() {
        if (attachedToWindow && isShowing()) {
            if (--showCount == 0) {
                progressDialog?.dismiss()
            }
        } else {
            showCount = 0
        }
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun setMessage(messageId: Int) = setMessage(context.getString(messageId))

    fun isShowing() = progressDialog?.isShowing == true
}