package com.example.tracker.weight

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tracker.weight.FirestoreWeightRepository.from
import com.example.tracker.weight.FirestoreWeightRepository.getId
import com.example.tracker.weight.FirestoreWeightRepository.toMap
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList
import java.util.Collections

class WeightViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val weightDtos: MutableList<WeightDto> = ArrayList()
    private val weights = MutableLiveData<List<WeightDto>>()

    fun getWeights(): LiveData<List<WeightDto>> {
        return weights
    }

    fun deleteWeight(dto: WeightDto) {
        weightDtos.remove(dto)
        weights.value = weightDtos
        db.collection(FIRESTORE_COLLECTION_WEIGHTS).document(getId(dto)).delete()
    }

    fun saveWeight(dto: WeightDto) {
        weightDtos.add(dto)
        weightDtos.sortWith(Collections.reverseOrder())
        weights.value = weightDtos
        db.collection(FIRESTORE_COLLECTION_WEIGHTS).document(getId(dto)).set(toMap(dto))
    }

    fun loadWeights() {
        db.collection(FIRESTORE_COLLECTION_WEIGHTS)
            .get()
            .addOnSuccessListener { result ->
                weightDtos.clear()
                for (document in result) {
                    weightDtos.add(from(document))
                }
                weightDtos.sortWith(Collections.reverseOrder())
                weights.value = weightDtos
            }
    }

    companion object {
        private const val FIRESTORE_COLLECTION_WEIGHTS = "weights"
    }
}