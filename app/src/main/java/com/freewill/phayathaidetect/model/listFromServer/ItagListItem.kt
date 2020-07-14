package com.freewill.phayathaidetect.model.listFromServer

import com.google.gson.annotations.SerializedName

data class ItagListItem(

	@field:SerializedName("mac_address")
	val macAddress: String? = null,

	@field:SerializedName("uuid")
	val uuid: String? = null
)