package com.freewill.phayathaidetect.model.listFromServer

import com.google.gson.annotations.SerializedName

data class Body(

	@field:SerializedName("itag_list")
	val itagList: List<ItagListItem?>? = null
)