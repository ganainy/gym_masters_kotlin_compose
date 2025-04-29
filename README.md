# Gym Masters 

## Overview

The application manages various data types including user profiles, posts, comments, exercises, workouts, likes, and social connections. It employs a hybrid approach using:

1.  **Cloud Firestore:** Primary remote NoSQL database for dynamic, structured data like user information, posts, comments, likes, follows, workouts, and hashtags. Used for real-time updates where needed.
2.  **Firebase Authentication:** Handles user authentication (signup, sign-in, session management).
3.  **Firebase Storage:** Stores user-uploaded media like profile pictures, post images, and workout cover images.
4.  **Room Database:** Local persistence library for:
    *   Caching large datasets fetched initially (Exercises, Body Parts, Equipment, Targets).
    *   Managing offline data or user-specific saves (Saved Workouts, Saved Exercises - based on flags/tables).
    *   Handling optimistic UI updates and synchronization for actions like Likes (`CachedLike` table).
5.  **External API (ExerciseDB):** Used by `ExerciseRepository` to fetch the initial, comprehensive list of exercises during first app launch.

> **Note:** On instruction on how to setup the Firebase project, please refer to the [Firebase Project Setup](PROJECT_SETUP.md).  

> **Note:** To get a better understanding of how app and database is structured, please refer to [App Structure](APP_STRUCTURE.md).



## App Feature Showcase

This section highlights some of the core features of the Gym Masters app.

### 1. User Authentication
- Sign In to access your account.

  <img src="peview/screenshots/sign_in.png" width="250"/>

### 2. Discover New Users
- Explore new users in the community.

  <img src="peview/screenshots/discover.png" width="250"/>

### 3. Browse Exercises
- Browse the catalog of exercises.

  <img src="peview/screenshots/exercise_list.png" width="250"/>

### 4. Workout Details
- View detailed information about workouts

  <img src="peview/screenshots/workout_details_1.png" width="250"/>
  <img src="peview/screenshots/workout_details_2.png" width="250"/>

### 5. Exercise Details
- Explore detailed information about exercises.

  <img src="peview/screenshots/exercise.png" width="250"/>

### 6. User Profiles
- View profiles

  <img src="peview/screenshots/profile.png" width="250"/>

### 7. Explore Workouts
- Explore different workouts

  <img src="peview/screenshots/workouts.png" width="250"/>

### 8. Post Feed
- View workout feed.

  <img src="peview/screenshots/feed.png" width="250"/>

### 9. Interact with Posts
- Like and comment on posts.

  <img src="peview/screenshots/like_and_comment.png" width="250"/>
