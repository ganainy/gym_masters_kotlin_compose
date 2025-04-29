## Firebase Project Setup

To run this project, you need to connect it to your own Firebase project.

1.  **Create Firebase Project:** Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project (or use an existing one).
2.  **Register Android App:**
    *   Within your project, click "Add app" and select the Android icon (</>).
    *   Follow the instructions, providing your app's package name (e.g., `com.yourname.gymmasterscompose`).
    *   Download the `google-services.json` configuration file.
3.  **Add Config File:** Place the downloaded `google-services.json` file in your Android project's `app/` directory.
4.  **Add Firebase SDKs:** Ensure your `app/build.gradle` file includes the necessary Firebase dependencies (BOM recommended) as listed earlier in this README (Auth, Firestore, Storage). Make sure the Google Services Gradle plugin is applied in both the project-level and app-level `build.gradle` files.
5.  **Enable Firebase Services:** In the Firebase Console, navigate to the "Build" section and enable the following services:
    *   **Authentication:** Go to Authentication -> Sign-in method tab -> Enable "Email/Password".
    *   **Firestore Database:** Go to Firestore Database -> Click "Create database" -> Start in **Test mode** for initial development (allows open read/write access - **remember to configure Security Rules before production!**) -> Choose a location close to your users.
    *   **Storage:** Go to Storage -> Click "Get started" -> Follow prompts, using the default security rules initially (allow read/write if authenticated - **remember to configure Security Rules before production!**).
6.  **Configure Security Rules (Crucial for Production):** Before releasing your app, you **MUST** configure robust Security Rules for Firestore and Storage to protect user data and prevent unauthorized access. The default "test mode" or authenticated user rules are often too permissive for production.

## Cloud Firestore Indexes

Many queries in this application involve filtering on one field while ordering by another, or ordering/filtering on nested fields (e.g., `postCreator.id`, `workout_metrics.likes_count`). These types of queries require **composite indexes** in Cloud Firestore.

**Creating Indexes (Easy Way):**

1.  **Run the App:** Use features that trigger complex queries (view feed, user profiles, sorted workouts, post comments).
2.  **Check Logcat:** If a query needs an index, Firestore will log an error containing a URL.
3.  **Click URL:** The link leads directly to the Firebase Console's index creation page, pre-filled with the required index details.
4.  **Confirm & Create:** Review the pre-filled index and click "Create Index". Index building may take time depending on data size. Queries will fail until the index is active.

**Common Queries Requiring Indexes:**

*   Fetching user posts (filter by creator ID, order by time).
*   Fetching the feed (filter by multiple creator IDs, order by time).
*   Fetching post comments (filter by post ID, order by time).
*   Sorting workouts by like/save count (ordering by nested fields).
