package com.ganainy.gymmasterscompose.di


import android.content.Context
import androidx.room.Room
import com.ganainy.gymmasterscompose.Constants.FIREBASE_DATABASE_NAME
import com.ganainy.gymmasterscompose.ui.theme.networking.retrofit.ExerciseApi
import com.ganainy.gymmasterscompose.ui.theme.networking.retrofit.Secrets
import com.ganainy.gymmasterscompose.ui.theme.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ExerciseRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IExerciseRepository
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
import com.ganainy.gymmasterscompose.ui.theme.room.AppDatabase
import com.ganainy.gymmasterscompose.utils.ExerciseDataManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://exercisedb.p.rapidapi.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val interceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-rapidapi-host", "exercisedb.p.rapidapi.com")
                .addHeader("x-rapidapi-key", Secrets.RAPID_API_KEY)
                .build()
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "exercise-database"
        )  .fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideExerciseApi(retrofit: Retrofit): ExerciseApi {
        return retrofit.create(ExerciseApi::class.java)
    }

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
        database: FirebaseDatabase,
        appDatabase: AppDatabase,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): IAuthRepository {
        return AuthRepository(
            auth, database,appDatabase ,ioDispatcher,
        )
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
        database: FirebaseDatabase,
        appDatabase: AppDatabase,
    ): IUsersRepository {
        return UsersRepository(auth, database,appDatabase)
    }


    @Provides
    @Singleton
    fun provideWorkoutRepository(
        auth: FirebaseAuth,
        database: FirebaseDatabase,
        storage: FirebaseStorage
    ): IWorkoutRepository {
        return WorkoutRepository(database,storage)
    }


    @Provides
    @Singleton
    fun provideExerciseRepository(
        exerciseApi: ExerciseApi,
        appDatabase: AppDatabase,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): IExerciseRepository {
        return ExerciseRepository(
            appDatabase, exerciseApi,
            ioDispatcher = ioDispatcher
        )
    }


    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @IoDispatcher
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO


    @Provides
    @Singleton
    fun provideExerciseDataManager(
        exerciseRepository: IExerciseRepository,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): ExerciseDataManager = ExerciseDataManager(
        exerciseRepository,
        ioDispatcher
    )

}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher