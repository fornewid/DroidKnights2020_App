package com.droidknights.app2020.di

import android.content.Context
import com.droidknights.app2020.MainApplication
import com.droidknights.app2020.data.Session
import dagger.Module
import dagger.Provides
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.json
import timber.log.Timber
import javax.inject.Singleton

@Module
class PrePackagedDbModule {
    @Singleton
    @Provides
    fun providePrePackagedDb(application: MainApplication): PrePackagedDb =
        PrePackagedDbImpl(application, "sessions.json")
}

interface PrePackagedDb {
    suspend fun getSessionList(): List<Session>
    suspend fun getSessionById(id: String): Session?
}

class PrePackagedDbImpl(
    private val context: Context,
    private val assetsName: String
) : PrePackagedDb {

    private val sessionList: List<Session>
    init {
        val jsonString = context.readJsonStringFromAsset(assetsName)
        sessionList = jsonString?.run(::createSessionList) ?: emptyList()
    }

    private fun createSessionList(jsonString: String): List<Session> {
        val json = Json(JsonConfiguration.Stable)
        return json.parse(PrePackagedSessionList.serializer(), jsonString)
            .session.map { it.toSession() }
    }

    override suspend fun getSessionList(): List<Session> {
        return sessionList
    }

    override suspend fun getSessionById(id: String): Session? {
        return sessionList.find { it.id == id }
    }

    private fun Context.readJsonStringFromAsset(assetsName: String): String? {
        return try {
            assets.open(assetsName)
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun PrePackagedSession.toSession(): Session {
        return Session(
            id = id,
            time = time,
            track = track,
            title = title,
            contents = contents,
            tag = tag
        )
    }

    @Serializable
    data class PrePackagedSessionList(
        val session: List<PrePackagedSession>
    )

    @Serializable
    data class PrePackagedSession(
        val id: String,
        val time: String,
        val track: Int,
        val title: String,
        val speaker: PrePackagedSpeaker,
        val contents: String,
        val tag: List<String>
    )

    @Serializable
    data class PrePackagedSpeaker(
        val name: String,
        val profileUrl: String,
        val description: String
    )
}
