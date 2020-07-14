package com.freewill.phayathaidetect.model

import com.google.gson.annotations.SerializedName

data class UpdateList2(

	@field:SerializedName("head")
	val head: Head? = null,

	@field:SerializedName("body")
	val body: Body? = null
)

data class ItagListItem(

	@field:SerializedName("mac_address")
	val macAddress: String? = null
)

data class Body(

	@field:SerializedName("itag_list")
	val itagList: List<ItagListItem?>? = null
)

data class Head(

	@field:SerializedName("code")
	val code: Int? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("version")
	val version: String? = null
)
