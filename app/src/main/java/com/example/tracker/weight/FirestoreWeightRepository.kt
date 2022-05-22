package com.example.tracker.weight

import com.google.firebase.firestore.QueryDocumentSnapshot

object FirestoreWeightRepository {

    @JvmStatic
    fun from(document: QueryDocumentSnapshot): WeightDto {
        val timeInMillis = document.data["timeInMillis"] as Long
        val weightInKgs = document.data["weight in kilograms"] as Double
        return WeightDto(timeInMillis, weightInKgs)
    }

    @JvmStatic
    fun toMap(dto: WeightDto): Map<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        map["timeInMillis"] = dto.timeInMillis
        map["weight in kilograms"] = dto.weightInKgs
        return map
    }

    @JvmStatic
    fun getId(dto: WeightDto): String {
        return dto.timeInMillis.toString() + "_" + dto.weightInKgs
    }
}