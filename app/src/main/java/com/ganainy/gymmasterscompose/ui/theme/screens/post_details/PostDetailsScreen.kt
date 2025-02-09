package com.ganainy.gymmasterscompose.ui.theme.screens.post_details

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.theme.components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost

@Composable
fun PostDetailsScreen(post: FeedPost,isLiked:Boolean) {

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

    if (postDetails.loadingPost) {
        LoadingIndicator()
    } else if (postDetails.error != null) {
        Text(text = postDetails.error!!.asString())
    } else {
        postDetails.feedPostWithLikesAndComments?.let { post ->

                    DetailedPostItem(
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




