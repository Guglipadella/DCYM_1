package it.polito.did.dcym.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.polito.did.dcym.data.model.Machine
import it.polito.did.dcym.data.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.MessageDigest
import kotlin.random.Random

class FirebaseRepository {

    // Riferimento al database
    private val database = FirebaseDatabase.getInstance()
    private val productsRef = database.getReference("products")
    private val ordersRef = database.getReference("orders")
    private val machinesRef = database.getReference("machines")

    // 1. LEGGE I PRODOTTI (Gestione sicura Lista/Mappa anche qui)
    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = mutableListOf<Product>()
                // Se Firebase lo vede come lista (array), snapshot.children itera sugli indici
                for (child in snapshot.children) {
                    try {
                        val product = child.getValue(Product::class.java)
                        if (product != null) {
                            productsList.add(product)
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseRepo", "Errore conversione prodotto: ${e.message}")
                    }
                }
                trySend(productsList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        productsRef.addValueEventListener(listener)
        awaitClose { productsRef.removeEventListener(listener) }
    }

    // 2. LEGGE LE MACCHINETTE (FIX PER IL TUO ERRORE)
    fun getMachines(): Flow<List<Machine>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val machinesList = mutableListOf<Machine>()

                for (child in snapshot.children) {
                    try {
                        // COSTRUZIONE MANUALE: Evita l'errore "Expected Map got ArrayList"
                        val id = child.child("id").getValue(String::class.java) ?: ""
                        val name = child.child("name").getValue(String::class.java) ?: ""
                        val lat = child.child("lat").getValue(Double::class.java) ?: 0.0
                        val lng = child.child("lng").getValue(Double::class.java) ?: 0.0

                        // Gestione intelligente dell'inventario (Lista o Mappa)
                        val inventorySnapshot = child.child("inventory")
                        val inventoryMap = mutableMapOf<String, Int>()

                        if (inventorySnapshot.value is List<*>) {
                            // CASO A: Firebase pensa sia una Lista (il tuo errore attuale)
                            val list = inventorySnapshot.value as List<*>
                            for (i in list.indices) {
                                // L'indice della lista corrisponde all'ID prodotto (es. index 1 = prodotto "1")
                                val qty = list[i].toString().toIntOrNull() ?: 0
                                if (qty > 0) inventoryMap[i.toString()] = qty
                            }
                        } else {
                            // CASO B: Firebase pensa sia una Mappa (Standard)
                            for (item in inventorySnapshot.children) {
                                val prodId = item.key ?: continue
                                val qty = item.getValue(Int::class.java) ?: 0
                                inventoryMap[prodId] = qty
                            }
                        }

                        // Creiamo l'oggetto Machine manualmente
                        val machine = Machine(id, name, lat, lng, inventoryMap)
                        machinesList.add(machine)

                    } catch (e: Exception) {
                        Log.e("FirebaseRepo", "Err Machine Manual Parse: ${e.message}")
                    }
                }
                trySend(machinesList)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        machinesRef.addValueEventListener(listener)
        awaitClose { machinesRef.removeEventListener(listener) }
    }

    // 3. LOGICA DI ACQUISTO (Invariata)
    fun buyProduct(productId: Int, machineId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val secretCode = Random.nextInt(1000, 9999).toString()
        val hashCode = hashString(secretCode)
        val orderId = ordersRef.push().key ?: return

        val orderData = mapOf(
            "hash_code" to hashCode,
            "product_id" to productId,
            "machine_id" to machineId,
            "status" to "PENDING",
            "timestamp" to System.currentTimeMillis()
        )

        ordersRef.child(orderId).setValue(orderData)
            .addOnSuccessListener { onSuccess(secretCode) }
            .addOnFailureListener { e -> onError(e.message ?: "Errore sconosciuto") }
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}