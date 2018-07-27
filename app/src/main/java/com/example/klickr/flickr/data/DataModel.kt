package com.example.klickr.flickr.data

import android.text.TextUtils
import java.io.Serializable

object DataModel {
    data class FlickrResponse(val photos: PhotosModel?,
                              val stat: String?)

    data class PhotosModel(val photo: ArrayList<PhotoModel>?,
                           val pages: Int?)

    class PhotoModel : Serializable {
        private var id: String? = ""
        private var secret: String? = ""
        private var server: String? = ""
        private var farm: String? = ""
        private var constructedURL: String? = ""

        fun constructURL(): String {

            if (TextUtils.isEmpty(constructedURL) && !TextUtils.isEmpty(id)) {

                constructedURL = "https://farm" +
                        farm +
                        ".staticflickr.com/" +
                        server +
                        "/" +
                        id +
                        "_" +
                        secret +
                        ".jpg"
            }

            return constructedURL as String
        }
    }

}