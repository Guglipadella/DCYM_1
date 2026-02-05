package it.polito.did.dcym.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Order
import it.polito.did.dcym.data.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val database = FirebaseDatabase.getInstance()
    private val productsRef = database.getReference("products")
    private val ordersRef = database.getReference("orders")
    private val machinesRef = database.getReference("machines")

    // 1. LISTA PRODOTTI
    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Product>()
                snapshot.children.forEach { child ->
                    try {
                        val categoriesList = mutableListOf<String>()
                        child.child("categories").children.forEach { catChild ->
                            catChild.getValue(String::class.java)?.let { categoriesList.add(it) }
                        }

                        val p = Product(
                            id = child.child("id").getValue(Int::class.java) ?: 0,
                            name = child.child("name").getValue(String::class.java) ?: "",
                            description = child.child("description").getValue(String::class.java) ?: "",
                            pricePurchase = (child.child("pricePurchase").value as? Number)?.toDouble() ?: 0.0,
                            priceRent = (child.child("priceRent").value as? Number)?.toDouble(),
                            imageResName = child.child("imageResName").getValue(String::class.java) ?: "",
                            categories = categoriesList
                        )
                        if (p.id != 0) list.add(p)
                    } catch (e: Exception) {
                        Log.e("Firebase", "Errore prodotto: ${e.message}")
                    }
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        productsRef.addValueEventListener(listener)
        awaitClose { productsRef.removeEventListener(listener) }
    }

    // 2. LISTA MACCHINETTE
    fun getMachines(): Flow<List<Machine>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Machine>()
                snapshot.children.forEach { child ->
                    try {
                        val inventoryMap = mutableMapOf<String, Int>()
                        child.child("inventory").children.forEach { invChild ->
                            val key = invChild.key ?: ""
                            val value = invChild.getValue(Int::class.java) ?: 0
                            inventoryMap[key] = value
                        }

                        val m = Machine(
                            id = child.child("id").getValue(String::class.java) ?: "",
                            name = child.child("name").getValue(String::class.java) ?: "",
                            lat = child.child("lat").getValue(Double::class.java) ?: 0.0,
                            lng = child.child("lng").getValue(Double::class.java) ?: 0.0,
                            inventory = inventoryMap,
                            sede = child.child("sede").getValue(String::class.java) ?: "",
                            edificio = child.child("edificio").getValue(String::class.java) ?: "",
                            piano = child.child("piano").getValue(String::class.java) ?: "",
                            indirizzo = child.child("indirizzo").getValue(String::class.java) ?: ""
                        )
                        list.add(m)
                    } catch (e: Exception) {
                        Log.e("FirebaseRepo", "Err Machine Parse: ${e.message}")
                    }
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        machinesRef.addValueEventListener(listener)
        awaitClose { machinesRef.removeEventListener(listener) }
    }

    // 3. SINGOLO ORDINE (Per il Playback)
    fun getOrderFlow(orderId: String): Flow<Order?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Order::class.java))
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ordersRef.child(orderId).addValueEventListener(listener)
        awaitClose { ordersRef.child(orderId).removeEventListener(listener) }
    }

    // 4. LISTA ORDINI COMPLETA (Semplificata e Corretta)
    // Rimosso il tentativo di leggere i prodotti qui dentro che causava l'errore.
    fun getOrders(): Flow<List<Order>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Order>()
                snapshot.children.forEach { child ->
                    try {
                        // Firebase mappa automaticamente i campi che trova (id, productId, productName, etc.)
                        val order = child.getValue(Order::class.java)
                        if (order != null) {
                            list.add(order)
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseRepo", "Err Order List Parse: ${e.message}")
                    }
                }
                // Ordina dal più recente
                list.sortByDescending { it.purchaseTimestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ordersRef.addValueEventListener(listener)
        awaitClose { ordersRef.removeEventListener(listener) }
    }

    // 5. SALVATAGGIO ORDINE
    fun saveOrder(order: Order): Boolean {
        return try {
            ordersRef.child(order.id).setValue(order)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}