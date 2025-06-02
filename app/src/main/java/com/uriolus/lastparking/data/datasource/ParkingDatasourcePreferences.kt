package com.uriolus.lastparking.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import arrow.core.Either
import arrow.core.raise.either
import com.uriolus.lastparking.domain.model.AppError
import com.uriolus.lastparking.domain.model.Parking
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "parking_prefs")

class ParkingDatasourcePreferences(
    private val context: Context
) : ParkingDatasource {

    private companion object {
        val PARKING_KEY = stringPreferencesKey("last_parking_data")
    }

    private val jsonParser = Json { ignoreUnknownKeys = true }

    override suspend fun saveParking(parking: Parking): Either<AppError, Unit> = either {
        try {
            val jsonString = jsonParser.encodeToString(parking)
            context.dataStore.edit { preferences ->
                preferences[PARKING_KEY] = jsonString
            }
        } catch (e: SerializationException) {
            raise(AppError.ErrorSaving("Failed to serialize parking data: ${e.message}"))
        } catch (e: IOException) {
            raise(AppError.ErrorSaving("Failed to write parking data to DataStore: ${e.message}"))
        } catch (e: Exception) {
            raise(AppError.ErrorSaving("An unexpected error occurred while saving parking: ${e.message}"))
        }
    }

    override suspend fun getLastParking(): Either<AppError, Parking> = either {
        val preferences = try {
            context.dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }.first()
        } catch (e: IOException) {
            raise(AppError.ErrorLoading("Failed to read parking data from DataStore: ${e.message}"))
        }

        val jsonString = preferences[PARKING_KEY]
            ?: raise(AppError.ErrorNoPreviousParking)

        Either.catch {
            jsonParser.decodeFromString<Parking>(jsonString)
        }.mapLeft { e ->
            when (e) {
                is SerializationException -> AppError.ErrorLoading("Failed to deserialize parking data: ${e.message}")
                else -> AppError.ErrorLoading("An unexpected error occurred while loading parking: ${e.message}")
            }
        }.bind()
    }
}
