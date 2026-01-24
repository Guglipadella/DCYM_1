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

    // Legge i prodotti forzando i numeri a Double per evitare crash
    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Product>()
                snapshot.children.forEach { child ->
                    try {
                        val p = Product(
                            id = child.child("id").getValue(Int::class.java) ?: 0,
                            name = child.child("name").getValue(String::class.java) ?: "",
                            description = child.child("description").getValue(String::class.java) ?: "",
                            // Questo trucco (Number) permette di leggere sia 15 che 15.0 senza crash
                            pricePurchase = (child.child("pricePurchase").value as? Number)?.toDouble() ?: 0.0,
                            priceRent = (child.child("priceRent").value as? Number)?.toDouble(),
                            imageResName = child.child("imageResName").getValue(String::class.java) ?: ""
                        )
                        if (p.id != 0) list.add(p)
                    } catch (e: Exception) { Log.e("Firebase", "Errore prodotto: ${e.message}") }
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        productsRef.addValueEventListener(listener)
        awaitClose { productsRef.removeEventListener(listener) }
    }

    // Legge le macchinette e l'inventario
    fun getMachines(): Flow<List<Machine>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Machine>()
                snapshot.children.forEach { child ->
                    try {
                        val inventory = mutableMapOf<String, Int>()
                        child.child("inventory").children.forEach { item ->
                            val qty = (item.value as? Number)?.toInt() ?: 0
                            inventory[item.key ?: ""] = qty
                        }

                        list.add(Machine(
                            id = child.key ?: "",
                            name = child.child("name").getValue(String::class.java) ?: "",
                            lat = (child.child("lat").value as? Number)?.toDouble() ?: 0.0,
                            lng = (child.child("lng").value as? Number)?.toDouble() ?: 0.0,
                            inventory = inventory
                        ))
                    } catch (e: Exception) { Log.e("Firebase", "Errore macchina: ${e.message}") }
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        machinesRef.addValueEventListener(listener)
        awaitClose { machinesRef.removeEventListener(listener) }
    }

    // Salvataggio ordine (Richiede la dipendenza play-services-target aggiunta sopra)
    suspend fun saveOrder(order: Order): Boolean {
        return try {
            ordersRef.child(order.id).setValue(order).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Ascolta lo stato dell'ordine per il playback
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

    // 5. LISTA ORDINI COMPLETA (Per la cronologia)
    fun getOrders(): Flow<List<Order>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Order>()
                snapshot.children.forEach { child ->
                    try {
                        val order = child.getValue(Order::class.java)
                        if (order != null) list.add(order)
                    } catch (e: Exception) {
                        Log.e("FirebaseRepo", "Err Order List Parse: ${e.message}")
                    }
                }
                // Li ordiniamo per data (dal più recente)
                list.sortByDescending { it.purchaseTimestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ordersRef.addValueEventListener(listener)
        awaitClose { ordersRef.removeEventListener(listener) }
    }
}