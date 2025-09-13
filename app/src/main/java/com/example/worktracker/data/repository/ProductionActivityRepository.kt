package com.example.worktracker.data.repository

import com.example.worktracker.data.database.dao.ProductionActivityDao
import com.example.worktracker.data.database.entity.ProductionActivity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface ProductionActivityRepository {
    fun getAllProductionActivities(): Flow<List<ProductionActivity>>
    suspend fun getProductionActivityById(id: Long): ProductionActivity?
    fun getProductionActivitiesByBoyId(boyId: Int): Flow<List<ProductionActivity>>
    suspend fun insertProductionActivity(productionActivity: ProductionActivity)
    suspend fun updateProductionActivity(productionActivity: ProductionActivity)
    suspend fun deleteProductionActivity(productionActivity: ProductionActivity)
}

@Singleton
class ProductionActivityRepositoryImpl @Inject constructor(
    private val productionActivityDao: ProductionActivityDao
) : ProductionActivityRepository {

    override fun getAllProductionActivities(): Flow<List<ProductionActivity>> = productionActivityDao.getAllProductionActivities()

    override suspend fun getProductionActivityById(id: Long): ProductionActivity? = productionActivityDao.getProductionActivityById(id)

    override fun getProductionActivitiesByBoyId(boyId: Int): Flow<List<ProductionActivity>> = productionActivityDao.getProductionActivitiesByBoyId(boyId)

    override suspend fun insertProductionActivity(productionActivity: ProductionActivity) {
        productionActivityDao.insert(productionActivity)
    }

    override suspend fun updateProductionActivity(productionActivity: ProductionActivity) {
        productionActivityDao.update(productionActivity)
    }

    override suspend fun deleteProductionActivity(productionActivity: ProductionActivity) {
        productionActivityDao.delete(productionActivity)
    }
}
