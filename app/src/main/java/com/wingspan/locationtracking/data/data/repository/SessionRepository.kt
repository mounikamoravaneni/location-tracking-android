package com.wingspan.locationtracking.data.data.repository

import com.wingspan.locationtracking.data.data.local.SessionDao
import com.wingspan.locationtracking.data.data.local.Session
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