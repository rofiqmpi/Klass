package com.mpi.klass

expect fun loadCachedTranslations(): Map<String, String>
expect fun saveCachedTranslations(entries: Map<String, String>)
expect suspend fun fetchOnlineDictionaryFromCloud(): Map<String, String>
expect suspend fun fetchOnlineTranslations(texts: List<String>): Map<String, String>
