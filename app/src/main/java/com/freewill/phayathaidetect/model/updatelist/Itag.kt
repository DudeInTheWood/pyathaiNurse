package fwg.mdc.btc.nursetrackingtest.model

import com.google.gson.annotations.SerializedName

data class Itag(

    @field:SerializedName("itag_list")
	var list: List<ListItem?>? = null,

    @field:SerializedName("version")
	var version: String? = null
)