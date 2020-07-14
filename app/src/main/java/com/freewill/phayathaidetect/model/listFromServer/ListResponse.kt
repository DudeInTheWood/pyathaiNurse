package com.freewill.phayathaidetect.model.listFromServer

import com.google.gson.annotations.SerializedName

data class ListResponse(

	@field:SerializedName("head")
	val head: Head? = null,

	@field:SerializedName("body")
	val body: Body? = null
)