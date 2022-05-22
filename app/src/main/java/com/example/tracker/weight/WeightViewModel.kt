package com.example.tracker.weight

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tracker.weight.FirestoreWeightRepository.from
import com.example.tracker.weight.FirestoreWeightRepository.getId
import com.example.tracker.weight.FirestoreWeightRepository.toMap
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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
        db.collection(FIRESTORE_COLLECTION_WEIGHTS).document(getId(dto))
            .delete()
            .addOnSuccessListener {
                Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener {
                Log.w(FIRESTORE_LOG_TAG, "Error deleting document", it)
            }
    }

    fun saveWeight(dto: WeightDto) {
        weightDtos.add(dto)
        weightDtos.sortWith(Collections.reverseOrder())
        weights.value = weightDtos
        db.collection(FIRESTORE_COLLECTION_WEIGHTS).document(getId(dto))
            .set(toMap(dto))
            .addOnSuccessListener { o: Void? ->
                Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot added with ID: " + getId(dto))
            }
            .addOnFailureListener { e: Exception? ->
                Log.w(FIRESTORE_LOG_TAG, "Error adding document", e)
            }
    }

    fun loadWeights() {
//        weights.setValue(restRepository.loadWeights());
        db.collection(FIRESTORE_COLLECTION_WEIGHTS)
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    weightDtos.clear()
                    for (document in task.result!!) {
                        weightDtos.add(from(document))
                        Log.d(FIRESTORE_LOG_TAG, document.id + " => " + document.data)
                    }
                    weightDtos.sortWith(Collections.reverseOrder())
                    weights.setValue(weightDtos)
                } else {
                    Log.w(FIRESTORE_LOG_TAG, "Error getting documents.", task.exception)
                }
            }
    }

    companion object {
        private const val FIRESTORE_LOG_TAG = "Firestore"
        private const val FIRESTORE_COLLECTION_WEIGHTS = "weights"
    }
}