package fwg.mdc.btc.nursetrackingtest.model

import com.google.gson.annotations.SerializedName

data class ListItem(

	@field:SerializedName("distance")
	var distance: Double? = null,

	@field:SerializedName("mac_address")
	var macAddress: String? = null,

	@field:SerializedName("uuid")
	var uuid: String? = null
)