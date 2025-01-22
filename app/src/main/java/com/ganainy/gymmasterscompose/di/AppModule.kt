package com.ganainy.gymmasterscompose.di

import com.ganainy.gymmasterscompose.AppConstants.FIREBASE_DATABASE_NAME
import com.ganainy.gymmasterscompose.ui.theme.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUsersRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IWorkoutRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.PostRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.SocialRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.UserRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.UsersRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.WorkoutRepository
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
    ): IAuthRepository {
        return AuthRepository(auth, database)
    }


    @Provides
    @Singleton
    fun providePostRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): IPostRepository {
        return PostRepository(auth, database)
    }

    @Provides
    @Singleton
    fun provideSocialRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): ISocialRepository {
        return SocialRepository(auth, database)
    }


    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): IUserRepository {
        return UserRepository(auth, database)
    }


    @Provides
    @Singleton
    fun provideUsersRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): IUsersRepository {
        return UsersRepository(auth, database)
    }


    @Provides
    @Singleton
    fun provideWorkoutRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase
    ): IWorkoutRepository {
        return WorkoutRepository( database)
    }

}
