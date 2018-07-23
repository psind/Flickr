package com.example.klickr.flickr.data

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.example.klickr.flickr.R
import kotlinx.android.synthetic.main.dialog_layout.view.*

/**
 * @author on 20/07/18.
 */
open class Utils {

    companion object {

        //Constants
        val FLICKR_API_KEY = "069ff1bb2477ad95d58d50dc993c2890"

        val URL = "https://api.flickr.com/services/rest?method=flickr.photos.search&api_key=$FLICKR_API_KEY&format=json&nojsoncallback=1"


        fun showSnackBar(context: Context?, snackBarListener: SnackBarListener?, message: String, coordinatorLayout: View,
                         showAction: Boolean) {
            if (context != null) {
                try {
                    val snackBar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)

                    if (showAction) {
                        snackBar.setAction(context.resources.getString(R.string.retry)) {
                            snackBarListener?.onRetryClickedFromSnackBar()
                        }
                        snackBar.setActionTextColor(Color.RED)
                    }

                    val sbView = snackBar.view

                    val textView = sbView.findViewById<View>(android.support.design.R.id.snackbar_text) as TextView

                    val snackBarAction = sbView
                            .findViewById<View>(android.support.design.R.id.snackbar_action) as TextView
                    textView.setTextColor(Color.RED)

                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                    snackBarAction.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)

                    snackBar.show()
                } catch (e: Exception) {

                }

            }
        }

        fun checkInternet(context: Context?): Boolean {
            return if (context != null) {
                val info = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
                info != null && info.isConnected
            } else {
                false
            }

        }

        fun hideKeyBoard(activity: Activity?) {
            if (activity != null) {
                val inputMethodManager = activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (activity.currentFocus != null) {
                    val f = activity.currentFocus
                    if (null != f && null != f.windowToken && EditText::class.java.isAssignableFrom(f.javaClass)) {
                        inputMethodManager.hideSoftInputFromWindow(f.windowToken, 0)
                    } else {
                        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                    }
                } else {
                    activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                }
            }
        }

        private var alertDialog: AlertDialog? = null

        fun showDialog(context: Context?, dialogListener: DialogListener?,
                       title: String, message: String, positiveButtonText: String,
                       negativeButtonText: String) {
            try {
                if (alertDialog != null && alertDialog?.isShowing!!)
                    alertDialog?.dismiss()
                if (context != null) {
                    val dialogBuilder = AlertDialog.Builder(context)
                    val inflater = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)

                    @SuppressLint("InflateParams") val dialogView = inflater.inflate(R.layout.dialog_layout, null)

                    if (TextUtils.isEmpty(negativeButtonText))
                        dialogView.cancelTV?.visibility = View.GONE
                    else
                        dialogView.cancelTV?.text = negativeButtonText

                    if (TextUtils.isEmpty(positiveButtonText))
                        dialogView.okTV?.visibility = View.GONE
                    else
                        dialogView.okTV?.text = positiveButtonText

                    dialogView.messageTV?.text = message

                    if (!TextUtils.isEmpty(title)) {
                        dialogView.titleTV?.visibility = View.VISIBLE
                        dialogView.titleTV?.text = title
                    } else {
                        dialogView.titleTV?.visibility = View.GONE
                    }

                    dialogBuilder.setView(dialogView)

                    alertDialog = dialogBuilder.create()
                    alertDialog?.setCancelable(false)
                    alertDialog?.setCanceledOnTouchOutside(false)
                    alertDialog?.show()


                    dialogView.cancelTV?.setOnClickListener {
                        if ((context as Activity).isFinishing)
                            return@setOnClickListener
                        if (alertDialog != null && alertDialog?.isShowing!!)
                            alertDialog?.dismiss()
                    }

                    dialogView.okTV?.setOnClickListener {
                        if ((context as Activity).isFinishing)
                            return@setOnClickListener
                        if (dialogListener != null) {

                            alertDialog?.dismiss()
                            dialogListener.onPositiveClickedFromDialog()
                        }

                        if (alertDialog != null && alertDialog?.isShowing!!)
                            alertDialog?.dismiss()
                    }
                }
            } catch (ignored: Exception) {

            }

        }
    }
}

interface SnackBarListener {
    fun onRetryClickedFromSnackBar()
}

interface DialogListener {
    fun onPositiveClickedFromDialog()
}

data class FlickrResponse(val constructedURL: String)

