package com.ganainy.gymmasterscompose.di


import android.content.Context
import androidx.room.Room
import com.ganainy.gymmasterscompose.BuildConfig
import com.ganainy.gymmasterscompose.Constants.FIREBASE_DATABASE_NAME
import com.ganainy.gymmasterscompose.prefs.ExerciseDownloadPrefs
import com.google.firebase.firestore.FirebaseFirestore
import com.ganainy.gymmasterscompose.ui.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.repository.CommentsRepository
import com.ganainy.gymmasterscompose.ui.repository.ExerciseRepository
import com.ganainy.gymmasterscompose.ui.repository.FeedRepository
import com.ganainy.gymmasterscompose.ui.repository.HashtagRepository
import com.ganainy.gymmasterscompose.ui.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.repository.ICommentsRepository
import com.ganainy.gymmasterscompose.ui.repository.IExerciseRepository
import com.ganainy.gymmasterscompose.ui.repository.IFeedRepository
import com.ganainy.gymmasterscompose.ui.repository.IHashtagRepository
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.IPostRepository
import com.ganainy.gymmasterscompose.ui.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.repository.IUsersRepository
import com.ganainy.gymmasterscompose.ui.repository.IWorkoutRepository
import com.ganainy.gymmasterscompose.ui.repository.LikeRepository
import com.ganainy.gymmasterscompose.ui.repository.PostRepository
import com.ganainy.gymmasterscompose.ui.repository.SocialRepository
import com.ganainy.gymmasterscompose.ui.repository.UserRepository
import com.ganainy.gymmasterscompose.ui.repository.UsersRepository
import com.ganainy.gymmasterscompose.ui.repository.WorkoutRepository
import com.ganainy.gymmasterscompose.ui.retrofit.ExerciseApi
import com.ganainy.gymmasterscompose.ui.room.AppDatabase
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
                .addHeader("x-rapidapi-key", BuildConfig.RAPID_API_KEY)
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
        ).fallbackToDestructiveMigration().build()
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

    //currently unused but is left for future use with realtime chat feature
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance(FIREBASE_DATABASE_NAME)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }


    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        database: FirebaseFirestore,
    ): IAuthRepository {
        return AuthRepository(
            auth, database
        )
    }


    @Provides
    @Singleton
    fun providePostRepository(
        userRepository: IUserRepository,
        database: FirebaseFirestore,
        hashtagRepository: IHashtagRepository,
        storage: FirebaseStorage

    ): IPostRepository {
        return PostRepository(userRepository, database, hashtagRepository, storage)
    }

    @Provides
    @Singleton
    fun provideSocialRepository(
        auth: FirebaseAuth,
        database: FirebaseFirestore,
    ): ISocialRepository {
        return SocialRepository(auth, database)
    }

    @Provides
    @Singleton
    fun provideHashtagRepository(
        database: FirebaseFirestore
    ): IHashtagRepository {
        return HashtagRepository(database)
    }


    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        database: FirebaseFirestore,
        storage: FirebaseStorage
    ): IUserRepository {
        return UserRepository(auth, database, storage)
    }


    @Provides
    @Singleton
    fun provideUsersRepository(
        auth: FirebaseAuth,
        database: FirebaseFirestore,
    ): IUsersRepository {
        return UsersRepository(auth, database)
    }


    @Provides
    @Singleton
    fun provideWorkoutRepository(
        database: FirebaseFirestore,
        storage: FirebaseStorage,
        hashtagRepository: IHashtagRepository,
        appDatabase: AppDatabase
    ): IWorkoutRepository {
        return WorkoutRepository(database, storage, appDatabase, hashtagRepository)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }


    @Provides
    @Singleton
    fun provideFeedRepository(
        database: FirebaseFirestore,
    ): IFeedRepository {
        return FeedRepository(database,)
    }

    @Provides
    @Singleton
    fun provideExerciseDownloadPrefs(
        @ApplicationContext context: Context
    ): ExerciseDownloadPrefs {
        return ExerciseDownloadPrefs(context)
    }

    @Provides
    @Singleton
    fun provideExerciseRepository(
        exerciseApi: ExerciseApi,
        appDatabase: AppDatabase,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        context: Context,
        exerciseDownloadPrefs: ExerciseDownloadPrefs
    ): IExerciseRepository {
        return ExerciseRepository(
            appDatabase, exerciseApi,
            ioDispatcher = ioDispatcher,
            context = context,
            exerciseDownloadPrefs = exerciseDownloadPrefs
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


    @Provides
    @Singleton
    fun provideCommentsRepository(
        firestore: FirebaseFirestore,
    ): ICommentsRepository {
        return CommentsRepository(
            firestore = firestore,
        )
    }


    @Provides
    @Singleton
    fun provideLikeRepository(
        firestore: FirebaseFirestore,
        userRepository: IUserRepository,
        appDatabase: AppDatabase
    ): ILikeRepository {
        return LikeRepository(
            firestore = firestore,
            userRepository = userRepository,
            appDatabase = appDatabase
        )
    }


}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher