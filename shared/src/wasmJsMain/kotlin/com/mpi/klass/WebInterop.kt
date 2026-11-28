package com.mpi.klass

@JsFun("(key) => { try { return window.localStorage.getItem(key); } catch (e) { return null; } }")
internal external fun jsStorageGet(key: String): String?

@JsFun("(key, value) => { try { window.localStorage.setItem(key, value); } catch (e) {} }")
internal external fun jsStorageSet(key: String, value: String)

@JsFun("(key) => { try { window.localStorage.removeItem(key); } catch (e) {} }")
internal external fun jsStorageRemove(key: String)

@JsFun("() => Date.now()")
internal external fun jsNowMillis(): Double

@JsFun("() => new Date().getDay()")
internal external fun jsDayOfWeek(): Int

@JsFun("() => new Date().getFullYear()")
internal external fun jsYear(): Int

@JsFun("() => new Date().getMonth() + 1")
internal external fun jsMonth(): Int

@JsFun("() => new Date().getDate()")
internal external fun jsDayOfMonth(): Int

@JsFun("(message) => console.log(message)")
internal external fun jsLog(message: String)

internal object WebBackendUnavailable :
    UnsupportedOperationException("This feature is not available on the web version yet.")

internal fun dayNameFromJs(): String = when (jsDayOfWeek()) {
    0 -> "Sunday"
    1 -> "Monday"
    2 -> "Tuesday"
    3 -> "Wednesday"
    4 -> "Thursday"
    5 -> "Friday"
    6 -> "Saturday"
    else -> "Sunday"
}
