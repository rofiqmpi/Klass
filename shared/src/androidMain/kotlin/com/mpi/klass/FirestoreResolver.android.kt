package com.mpi.klass

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.mpi.klass.theme.getAppContext

private const val RESOLVER_TAG = "FirestoreResolver"
const val AI_STUDIO_FIREBASE_PROJECT_ID = "gen-lang-client-0286475017"

object FirestoreResolver {
    @Volatile
    private var lastWorkingDbLabel: String? = null

    fun currentFirebaseProjectId(): String = try {
        FirebaseApp.getInstance().options.projectId?.trim().orEmpty()
    } catch (_: Exception) {
        ""
    }

    fun getCustomDatabaseId(): String? {
        val secretDbId = KlassRuntimeConfig.firestoreDatabaseId.trim()
        if (secretDbId.isNotEmpty() && secretDbId != "(default)") {
            return secretDbId
        }
        val ctx = getAppContext() ?: return null
        val projectId = currentFirebaseProjectId()
        // Only apply AI Studio's bundled firestore_database_id when running against the AI Studio project;
        // external Firebase projects (e.g., GitHub Release GOOGLE_SERVICES_JSON) typically use "(default)"
        // unless FIRESTORE_DATABASE_ID secret is explicitly provided.
        if (projectId.isNotEmpty() && projectId != AI_STUDIO_FIREBASE_PROJECT_ID) {
            return null
        }
        val resId = ctx.resources.getIdentifier("firestore_database_id", "string", ctx.packageName)
        if (resId == 0) return null
        val dbName = ctx.getString(resId).trim()
        return if (dbName.isEmpty() || dbName == "(default)") null else dbName
    }

    fun getFirestoreInstances(): List<Pair<FirebaseFirestore, String>> {
        val candidates = mutableListOf<Pair<FirebaseFirestore, String>>()
        val customDbId = getCustomDatabaseId()
        val isExternalProject = currentFirebaseProjectId().let { it.isNotEmpty() && it != AI_STUDIO_FIREBASE_PROJECT_ID }

        fun addCustom() {
            if (customDbId != null && candidates.none { it.second == customDbId }) {
                try {
                    candidates.add(FirebaseFirestore.getInstance(customDbId) to customDbId)
                } catch (e: Exception) {
                    Log.w(RESOLVER_TAG, "Custom DB unavailable ($customDbId): ${e.message}")
                }
            }
        }

        fun addDefault() {
            if (candidates.none { it.second == "(default)" }) {
                try {
                    candidates.add(FirebaseFirestore.getInstance() to "(default)")
                } catch (e: Exception) {
                    Log.w(RESOLVER_TAG, "Default DB unavailable: ${e.message}")
                }
            }
        }

        val cached = lastWorkingDbLabel
        if (cached == "(default)" || (isExternalProject && KlassRuntimeConfig.firestoreDatabaseId.isBlank())) {
            addDefault()
            addCustom()
        } else {
            addCustom()
            addDefault()
        }

        return candidates
    }

    fun markWorkingDatabase(label: String) {
        if (label.isNotBlank()) {
            lastWorkingDbLabel = label
        }
    }
}
