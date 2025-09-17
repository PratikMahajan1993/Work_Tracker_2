package com.example.worktracker.data.database.mappers

import com.example.worktracker.data.database.entity.ActivityCategory
import com.example.worktracker.data.database.entity.ComponentInfo
import com.example.worktracker.data.database.entity.OperatorInfo
import com.example.worktracker.data.database.entity.ProductionActivity
import com.example.worktracker.data.database.entity.TheBoysInfo
import com.example.worktracker.data.database.entity.WorkActivityLog
import com.google.firebase.firestore.DocumentSnapshot
import android.util.Log

private const val MAPPER_TAG = "FirestoreMappers"

// --- WorkActivityLog Mappers ---

fun WorkActivityLog.toFirestoreData(): Map<String, Any?> {
    return mapOf(
        // id is documentId in Firestore, not explicitly in map here
        "categoryName" to categoryName,
        "categoryId" to categoryId,
        "startTime" to startTime,
        "endTime" to endTime,
        "description" to description,
        "operatorId" to operatorId,
        "expenses" to expenses,
        "logDate" to logDate,
        "taskSuccessful" to taskSuccessful,
        "assignedBy" to assignedBy,
        "duration" to duration,
        "lastModified" to lastModified
        // componentIdsForSync and theBoyIdsForSync are not part of the direct entity fields here;
        // they are added to the map by the repository before uploading to Firestore if needed.
    )
}

fun DocumentSnapshot.toWorkActivityLog(): WorkActivityLog? {
    return try {
        val workActivityLog = WorkActivityLog(
            id = this.id.toLongOrNull() ?: 0L, 
            categoryName = getString("categoryName") ?: "",
            categoryId = getLong("categoryId"),
            startTime = getLong("startTime") ?: 0L,
            endTime = getLong("endTime"),
            description = getString("description") ?: "",
            operatorId = getLong("operatorId")?.toInt(),
            expenses = getDouble("expenses"),
            logDate = getLong("logDate") ?: 0L,
            taskSuccessful = getBoolean("taskSuccessful"),
            assignedBy = getString("assignedBy"),
            duration = getLong("duration"),
            lastModified = getLong("lastModified") ?: System.currentTimeMillis()
        )
        workActivityLog.componentIdsForSync = (get("componentIds") as? List<*>)?.filterIsInstance<Long>()
        workActivityLog
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting DocumentSnapshot to WorkActivityLog: ${this.id}", e)
        null
    }
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toWorkActivityLog(): WorkActivityLog? {
    return try {
        val workActivityLog = WorkActivityLog(
            id = (this["firestore_document_id"] as? String)?.toLongOrNull() ?: (this["id"] as? Long ?: 0L),
            categoryName = this["categoryName"] as? String ?: "",
            categoryId = this["categoryId"] as? Long,
            startTime = this["startTime"] as? Long ?: 0L,
            endTime = this["endTime"] as? Long,
            description = this["description"] as? String ?: "",
            operatorId = (this["operatorId"] as? Long)?.toInt(),
            expenses = this["expenses"] as? Double,
            logDate = this["logDate"] as? Long ?: 0L,
            taskSuccessful = this["taskSuccessful"] as? Boolean,
            assignedBy = this["assignedBy"] as? String,
            duration = this["duration"] as? Long,
            lastModified = this["lastModified"] as? Long ?: System.currentTimeMillis()
        )
        // Read component IDs from the map, expecting a List<Long>
        workActivityLog.componentIdsForSync = (this["componentIds"] as? List<*>)?.filterIsInstance<Long>()
        workActivityLog
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting Map to WorkActivityLog: ${this["firestore_document_id"]}", e)
        null
    }
}

// --- OperatorInfo Mappers ---

fun OperatorInfo.toFirestoreData(): Map<String, Any?> {
    return mapOf(
        // operatorId is documentId
        "name" to name,
        "hourlySalary" to hourlySalary,
        "role" to role,
        "priority" to priority,
        "notes" to notes,
        "notesForAi" to notesForAi,
        "lastModified" to lastModified
    )
}

fun DocumentSnapshot.toOperatorInfo(): OperatorInfo? {
    return try {
        OperatorInfo(
            operatorId = this.id.toIntOrNull() ?: 0,
            name = getString("name") ?: "",
            hourlySalary = getDouble("hourlySalary") ?: 0.0,
            role = getString("role") ?: "",
            priority = getLong("priority")?.toInt() ?: 0,
            notes = getString("notes"),
            notesForAi = getString("notesForAi"),
            lastModified = getLong("lastModified") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting DocumentSnapshot to OperatorInfo: ${this.id}", e)
        null
    }
}

fun Map<String, Any?>.toOperatorInfo(): OperatorInfo? {
    return try {
        OperatorInfo(
            operatorId = (this["firestore_document_id"] as? String)?.toIntOrNull() ?: (this["operatorId"] as? Long)?.toInt() ?: 0,
            name = this["name"] as? String ?: "",
            hourlySalary = this["hourlySalary"] as? Double ?: 0.0,
            role = this["role"] as? String ?: "",
            priority = (this["priority"] as? Long)?.toInt() ?: 0,
            notes = this["notes"] as? String,
            notesForAi = this["notesForAi"] as? String,
            lastModified = this["lastModified"] as? Long ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting Map to OperatorInfo: ${this["firestore_document_id"]}", e)
        null
    }
}

// --- ActivityCategory Mappers ---

fun ActivityCategory.toFirestoreData(): Map<String, Any?> {
    return mapOf(
        "name" to name, 
        "lastModified" to lastModified
    )
}

fun DocumentSnapshot.toActivityCategory(): ActivityCategory? {
    return try {
        ActivityCategory(
            id = 0, 
            name = this.id, 
            lastModified = getLong("lastModified") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting DocumentSnapshot to ActivityCategory: ${this.id}", e)
        null
    }
}

fun Map<String, Any?>.toActivityCategory(): ActivityCategory? {
    return try {
        ActivityCategory(
            id = 0, 
            name = this["firestore_document_id"] as? String ?: this["name"] as? String ?: "",
            lastModified = this["lastModified"] as? Long ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting Map to ActivityCategory: ${this["firestore_document_id"]}", e)
        null
    }
}

// --- TheBoysInfo Mappers ---

fun TheBoysInfo.toFirestoreData(): Map<String, Any?> {
    return mapOf(
        "name" to name,
        "role" to role,
        "notes" to notes,
        "notesForAi" to notesForAi,
        "lastModified" to lastModified
    )
}

fun DocumentSnapshot.toTheBoysInfo(): TheBoysInfo? {
    return try {
        TheBoysInfo(
            boyId = this.id.toIntOrNull() ?: 0,
            name = getString("name") ?: "",
            role = getString("role") ?: "",
            notes = getString("notes"),
            notesForAi = getString("notesForAi"),
            lastModified = getLong("lastModified") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting DocumentSnapshot to TheBoysInfo: ${this.id}", e)
        null
    }
}

fun Map<String, Any?>.toTheBoysInfo(): TheBoysInfo? {
    return try {
        TheBoysInfo(
            boyId = (this["firestore_document_id"] as? String)?.toIntOrNull() ?: (this["boyId"] as? Long)?.toInt() ?: 0,
            name = this["name"] as? String ?: "",
            role = this["role"] as? String ?: "",
            notes = this["notes"] as? String,
            notesForAi = this["notesForAi"] as? String,
            lastModified = this["lastModified"] as? Long ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting Map to TheBoysInfo: ${this["firestore_document_id"]}", e)
        null
    }
}

// --- ProductionActivity Mappers ---

fun ProductionActivity.toFirestoreData(): Map<String, Any?> {
    return mapOf(
        "boyId" to boyId,
        "componentName" to componentName,
        "machineNumber" to machineNumber,
        "productionQuantity" to productionQuantity,
        "startTime" to startTime,
        "endTime" to endTime,
        "duration" to duration,
        "downtimeMinutes" to downtimeMinutes,
        "rejectionQuantity" to rejectionQuantity,
        "lastModified" to lastModified
    )
}

fun DocumentSnapshot.toProductionActivity(): ProductionActivity? {
    return try {
        ProductionActivity(
            id = this.id.toLongOrNull() ?: 0L,
            boyId = getLong("boyId")?.toInt() ?: 0,
            componentName = getString("componentName") ?: "",
            machineNumber = getLong("machineNumber")?.toInt() ?: 0,
            productionQuantity = getLong("productionQuantity")?.toInt() ?: 0,
            startTime = getLong("startTime") ?: 0L,
            endTime = getLong("endTime") ?: 0L,
            duration = getLong("duration") ?: 0L,
            downtimeMinutes = getLong("downtimeMinutes")?.toInt(),
            rejectionQuantity = getLong("rejectionQuantity")?.toInt(),
            lastModified = getLong("lastModified") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting DocumentSnapshot to ProductionActivity: ${this.id}", e)
        null
    }
}

fun Map<String, Any?>.toProductionActivity(): ProductionActivity? {
    return try {
        ProductionActivity(
            id = (this["firestore_document_id"] as? String)?.toLongOrNull() ?: (this["id"] as? Long ?: 0L),
            boyId = (this["boyId"] as? Long)?.toInt() ?: 0,
            componentName = this["componentName"] as? String ?: "",
            machineNumber = (this["machineNumber"] as? Long)?.toInt() ?: 0,
            productionQuantity = (this["productionQuantity"] as? Long)?.toInt() ?: 0,
            startTime = this["startTime"] as? Long ?: 0L,
            endTime = this["endTime"] as? Long ?: 0L,
            duration = this["duration"] as? Long ?: 0L,
            downtimeMinutes = (this["downtimeMinutes"] as? Long)?.toInt(),
            rejectionQuantity = (this["rejectionQuantity"] as? Long)?.toInt(),
            lastModified = this["lastModified"] as? Long ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting Map to ProductionActivity: ${this["firestore_document_id"]}", e)
        null
    }
}

// --- ComponentInfo Mappers ---

fun ComponentInfo.toFirestoreData(): Map<String, Any?> {
    return mapOf(
        "componentName" to componentName,
        "customer" to customer,
        "priorityLevel" to priorityLevel,
        "cycleTimeMinutes" to cycleTimeMinutes,
        "notesForAi" to notesForAi,
        "lastModified" to lastModified
    )
}

fun DocumentSnapshot.toComponentInfo(): ComponentInfo? {
    return try {
        ComponentInfo(
            id = 0, 
            componentName = this.id, 
            customer = getString("customer") ?: "",
            priorityLevel = getLong("priorityLevel")?.toInt() ?: 0,
            cycleTimeMinutes = getDouble("cycleTimeMinutes") ?: 0.0,
            notesForAi = getString("notesForAi"),
            lastModified = getLong("lastModified") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting DocumentSnapshot to ComponentInfo: ${this.id}", e)
        null
    }
}

fun Map<String, Any?>.toComponentInfo(): ComponentInfo? {
    return try {
        ComponentInfo(
            id = 0, 
            componentName = this["firestore_document_id"] as? String ?: this["componentName"] as? String ?: "",
            customer = this["customer"] as? String ?: "",
            priorityLevel = (this["priorityLevel"] as? Long)?.toInt() ?: 0,
            cycleTimeMinutes = this["cycleTimeMinutes"] as? Double ?: 0.0,
            notesForAi = this["notesForAi"] as? String,
            lastModified = this["lastModified"] as? Long ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(MAPPER_TAG, "Error converting Map to ComponentInfo: ${this["firestore_document_id"]}", e)
        null
    }
}
