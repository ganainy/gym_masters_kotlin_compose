package com.ganainy.gymmasterscompose.ui.screens.post_details

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator

@Composable
fun PostDetailsScreen(navigateBack: () ->  Unit, post: FeedPost, isLiked: Boolean) {

    val viewModel: PostDetailsViewModel = hiltViewModel()

    LaunchedEffect(key1 = Unit) {
        viewModel.apply {
            setPost(post)
            setLikeStatus(isLiked)
            observePostComments()
            observePostMetricsUpdates()
        }
    }


    val postDetails by viewModel.postDetailsUiData.collectAsState()

    Column {

        CustomTopAppBar(
            title = "Post Details",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = navigateBack,
        )

        if (postDetails.loadingPost) {
            LoadingIndicator()
        } else if (postDetails.error != null) {
            Text(text = postDetails.error!!.asString())
        } else {
            postDetails.feedPostWithLikesAndComments?.let { post ->

                DetailedPostContent(
                    feedPostWithLikesAndComments = post,
                    isLoadingComments = postDetails.loadingComments,
                    onProfileClick = { /*TODO*/ },
                    onPostLikeClick = viewModel::togglePostLike,
                    onCommentLikeClick = viewModel::toggleCommentLike,
                    onCommentSubmit = viewModel::submitComment,
                )
            }
        }

    }
}




