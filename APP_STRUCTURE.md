## Core Components & Repositories

*   **`AuthRepository`**: Manages user authentication via Firebase Auth and creates user profile documents in Firestore (`/users/{userId}`).
*   **`UserRepository`**: Handles fetching and updating user profile documents from Firestore, including profile picture uploads/URL updates (Storage -> Firestore). Provides user data flows (`getUserFlow`).
*   **`UsersRepository`**: Fetches lists of users and user following information from Firestore collections (`/users`, `/follows`), providing real-time flows.
*   **`PostRepository`**: Creates post documents in Firestore (`/posts/{postId}`), uploads associated images to Firebase Storage, updates user post counts (in `/users/{userId}` via `FieldValue.increment`), updates user-specific post indices (e.g., `/users/{userId}/user_posts/{postId}` subcollection), and manages hashtags via `HashtagRepository` using atomic batches. Fetches post metrics.
*   **`FeedRepository`**: Fetches a paginated feed of posts from the Firestore `/posts` collection based on followed user IDs using `whereIn` queries.
*   **`CommentsRepository`**: Manages comment documents in Firestore (`/comments/{commentId}`), updates post comment counts atomically.
*   **`LikeRepository`**: Handles toggling like status for posts, comments, and workouts. Updates like documents (in `/post_likes`, `/comment_likes`, `/workout_likes`) and metric counts in Firestore using atomic Transactions/Batches. Uses the Room database (`CachedLike` table) for optimistic UI updates and syncing pending likes.
*   **`SocialRepository`**: Manages following/unfollowing users, updating follow documents (`/follows/{followId}`) and follower/following counts in user documents (`/users/{userId}`) atomically using Firestore batches/transactions.
*   **`ExerciseRepository`**:
    *   On first launch (or forced refresh), fetches *all* exercises from the external API via pagination, processes images (saves to local device storage, stores *path* in Room), and saves everything to the Room database (`Exercise`, `BodyPart`, `Equipment`, `TargetMuscle` tables). Tracks completion using SharedPreferences.
    *   Provides methods to get cached data (`getCached*`) directly from Room.
    *   Allows users to mark exercises as "saved locally" (persisted via `isSavedLocally` flag in Room).
    *   Original `get*` methods using `fetchWithCache` still exist for fetching specific items if needed (e.g., by ID) and will attempt network if cache is missing.
*   **`WorkoutRepository`**: Manages workout documents (`/workouts/{workoutId}`) and workout saves (`/workout_saves/{saveId}`) in Firestore. Handles workout cover image uploads/deletions (Storage -> Firestore URL). Manages workout likes and saves counts in Firestore atomically. Interacts with Room DB for locally saved workouts (`saveWorkoutLocally`, `getLocalWorkoutsFlow`, etc.).
*   **`HashtagRepository`**: Manages hashtag documents (`/hashtags/{tag}`) in Firestore, updating usage counts and timestamps atomically as part of larger batches (e.g., during post creation).

## Database Structure

### Remote (Firebase)

*   **Firebase Authentication:** Stores user credentials (email/password, UID).
*   **Cloud Firestore:** NoSQL document database. Key collections include:
    *   `/users/{userId}`: Stores `User` documents (profile info, stats sub-map).
        *   `/users/{userId}/user_posts/{postId}`: (Subcollection Pattern) Stores `PostByUserEntry` for efficient user feed lookup.
    *   `/posts/{postId}`: Stores `FeedPost` documents (creator info, text, image URLs, tags, timestamp, metrics sub-map).
    *   `/comments/{commentId}`: Stores `Comment` documents (creator info, text, postId, timestamp, likesCount field).
    *   `/hashtags/{tag}`: Stores `Hashtag` documents (tag name as ID, use count, last used timestamp).
    *   `/post_likes/{likeId}`: Stores `PostLike` documents (linking `userId` and `postId`). `likeId` is likely `{userId}_{postId}`.
    *   `/comment_likes/{likeId}`: Stores `CommentLike` documents (linking `userId`, `commentId`, `postId`). `likeId` is likely `{userId}_{commentId}`.
    *   `/workout_likes/{likeId}`: Stores `WorkoutLike` documents (linking `userId`, `workoutId`). `likeId` is likely `{userId}_{workoutId}`.
    *   `/workout_saves/{saveId}`: Stores `WorkoutSave` documents (linking `userId`, `workoutId`). `saveId` is likely `{userId}_{workoutId}`.
    *   `/follows/{followId}`: Stores `Follow` documents (linking `followerId` and `followedId`). `followId` is likely `{followerId}_{followedId}`.
    *   `/workouts/{workoutId}`: Stores `Workout` documents (creator info, name, description, exercises, image URL, tags, timestamp, metrics sub-map).
*   **Firebase Storage:**
    *   `user_images/`: Stores user profile pictures.
    *   `posts_collection/{userId}/`: Stores images associated with posts, organized by user.
    *   `workout_cover_images/`: Stores cover images for workouts.

### Local (Room Database)

*   **`Exercise` Table:** Caches *all* `Exercise` data after initial download from the external API. Includes fields like `id`, `name`, `bodyPart`, `equipment`, `target`, `gifUrl`, `instructions`, `isSavedLocally` (boolean flag), `screenshotPath` (local file path to processed image). Primary source for exercise lists after first launch.
*   **`BodyPart` Table:** Caches list of body parts from API.
*   **`Equipment` Table:** Caches list of equipment types from API.
*   **`TargetMuscle` Table:** Caches list of target muscles from API.
*   **`CachedLike` Table:** Caches the *intended* like status (`isLiked`) of Posts, Comments, and Workouts for the current user. Includes `isPending` flag for likes/unlikes attempted but not yet confirmed by Firestore. Used for optimistic UI updates and offline handling via `syncPendingLikes`.
*   **`WorkoutEntity` Table:** Caches/Saves `Workout` data locally, likely for user's explicit "Saved Workouts" list and potential offline access.

## Interactions & Synchronization

1.  **Initial Exercise Download:** On first app launch (or forced retry), `ExerciseRepository.fetchAllExercisesAndCache` loops through the external API using pagination (`limit=100`), downloads all exercises, processes associated GIF images into local files (storing paths), and saves everything to the Room database tables. A SharedPreferences flag tracks completion.
2.  **Exercise Data Access:** After initial download, `ExerciseListViewModel` and other features primarily use `ExerciseRepository.getCached*` methods to load data directly from the Room cache.
3.  **Auth Flow:** User signs up/in via `AuthRepository` -> Firebase Auth verifies -> `AuthRepository` creates a corresponding user profile document in Firestore (`/users/{userId}`).
4.  **Optimistic Updates & Syncing (Likes):** When a user likes/unlikes content:
    *   `LikeRepository` immediately updates the `CachedLike` table in Room with `isPending = true` and the new `isLiked` state. The UI observes Room for instant feedback.
    *   `LikeRepository` attempts a Firestore Transaction to read the current backend like state and atomically update the like document and metric count.
    *   On successful Firestore Transaction, `LikeRepository` updates the `CachedLike` entry in Room to `isPending = false`.
    *   If the Transaction fails, the `CachedLike` remains pending. `syncPendingLikes()` retries the `toggleLike` operation later.
5.  **Remote Fetches:** Repositories fetch data directly from Firestore (e.g., user profiles, posts, comments, workouts) when needed, often providing `Flow`s for real-time updates using Firestore listeners (`addSnapshotListener`).
6.  **Atomic Writes (Batches/Transactions):** Creating posts, comments, workouts, following users, or toggling saves involves Firestore `WriteBatch` or `Transaction` operations to ensure multiple documents/counters are updated atomically (e.g., creating a post document + updating user post count + updating hashtags).
7.  **Media Uploads:** Creating posts (`PostRepository`), updating profile pictures (`UserRepository`), or creating workouts with images (`WorkoutRepository`) involves uploading files to Firebase Storage first, then saving the resulting download URL in the corresponding Firestore document.
8.  **Local Saving (Exercises/Workouts):** Users can explicitly save exercises/workouts locally via their respective repositories. This toggles flags/saves data in the Room database.

