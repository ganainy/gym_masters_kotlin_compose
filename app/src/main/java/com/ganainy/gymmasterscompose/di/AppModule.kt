package com.ganainy.gymmasterscompose.di

import com.ganainy.gymmasterscompose.AppConstants.FIREBASE_DATABASE_NAME
import com.ganainy.gymmasterscompose.ui.theme.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.DataRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance(FIREBASE_DATABASE_NAME)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): AuthRepository {
        return AuthRepository(auth, database)
    }

    @Provides
    @Singleton
    fun provideDataRepository(
        authRepo: AuthRepository,
        database: FirebaseDatabase
    ): DataRepository {
        return DataRepository(authRepo, database)
    }

}
