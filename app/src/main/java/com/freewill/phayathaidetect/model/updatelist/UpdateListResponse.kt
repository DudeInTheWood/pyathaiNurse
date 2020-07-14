package fwg.mdc.btc.nursetrackingtest.model

import com.freewill.phayathaidetect.model.updatelist.Androidbox
import com.google.gson.annotations.SerializedName

data class UpdateListResponse(

	@field:SerializedName("itag")
	var itag: Itag? = null,

	@field:SerializedName("androidbox")
	var androidbox: Androidbox? = null
)