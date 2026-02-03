package com.wingspan.locationtracking.repository

import android.app.blob.BlobStoreManager
import com.wingspan.locationtracking.database.Session
import com.wingspan.locationtracking.database.SessionDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val dao: SessionDao
) {

    fun getAllSessions(): Flow<List<Session>> = dao.getAllSessions()

    suspend fun insertSession(session: Session) = dao.insertSession(session)

    suspend fun getSessionById(id: String): Session? = dao.getSessionById(id)
}