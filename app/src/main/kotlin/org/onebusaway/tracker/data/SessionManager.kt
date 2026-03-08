import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class SessionEvent {
    object SessionExpired : SessionEvent()
}

@Singleton
class SessionManager @Inject constructor(context: Context) {

    private val _sessionEvents = MutableSharedFlow<SessionEvent>()
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearSession() {
        sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    suspend fun notifySessionExpired() {
        _sessionEvents.emit(SessionEvent.SessionExpired)
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
}
