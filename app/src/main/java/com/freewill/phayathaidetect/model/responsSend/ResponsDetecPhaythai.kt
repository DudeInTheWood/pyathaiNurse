package com.fg.mdp.fwgfacilitiesfinder.model.responsSend


import com.google.gson.annotations.SerializedName


data class ResponsDetecPhaythai(

	@field:SerializedName("nurse")
	val nurse: List<NurseItem?>? = null,

	@field:SerializedName("information")
	val information: Information? = null
)