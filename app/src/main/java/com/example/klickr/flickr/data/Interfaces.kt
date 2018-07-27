package com.example.klickr.flickr.data

object Interfaces {

    interface SnackBarListener {
        fun onRetryClickedFromSnackBar()
    }

    interface DialogListener {
        fun onPositiveClickedFromDialog()
    }

    interface OnLoadMoreListener {
        fun onLoadMore()
    }
}