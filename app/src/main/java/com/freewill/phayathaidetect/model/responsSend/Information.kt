package com.fg.mdp.fwgfacilitiesfinder.model.responsSend


import com.google.gson.annotations.SerializedName

data class Information(

	@field:SerializedName("deviceId")
	val deviceId: String? = null ,


	@field:SerializedName("datetime")
	val datetime: String? = null
)