package com.freewill.phayathaidetect.model.listFromServer

import com.google.gson.annotations.SerializedName

data class Head(

	@field:SerializedName("code")
	val code: Int? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("version")
	val version: String? = null
)