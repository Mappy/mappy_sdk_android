package com.mappy.sdk.sample.utils

import com.mappy.webservices.resource.model.dao.MappyAddress

object AddressUtil {
    fun getFormattedGeoAddress(address: MappyAddress) =
        "${getFirstLineForQuery(address)}, ${getSecondLineForQuery(address)}"

    private fun getFirstLineForQuery(address: MappyAddress) = when {
        address.way.isNotEmpty() -> address.way
        address.town.isNotEmpty() -> address.town
        !address.subCountry.isNullOrEmpty() -> address.subCountry
        !address.country.isNullOrEmpty() -> address.country
        else -> null
    }

    private fun getSecondLineForQuery(address: MappyAddress) = when {
        !address.town.isEmpty() -> if (address.postalCode.isEmpty()) "${address.postalCode} ${address.town}" else address.town
        !address.subCountry.isNullOrEmpty() -> address.subCountry
        else -> null
    }
}