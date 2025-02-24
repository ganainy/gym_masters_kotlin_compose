package com.ganainy.gymmasterscompose.ui.screens.post_details

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.shared_components.ErrorComponent
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.utils.MockData.samplePostWithLikesAndComments
import com.ganainy.gymmasterscompose.utils.UiText

@Composable
fun PostDetailsScreen(navigateBack: () ->  Unit, post: FeedPost) {

    val viewModel: PostDetailsViewModel = hiltViewModel()

    LaunchedEffect(key1 = Unit) {
        viewModel.apply {
            setPost(post)
            loadPostComments()
            loadPostMetrics()
        }
    }


    val postDetails by viewModel.postDetailsUiData.collectAsState()

    PostDetailsContent(navigateBack, postDetails, viewModel::togglePostLike, viewModel::toggleCommentLike, viewModel::addComment)
}

@Composable
private fun PostDetailsContent(
    navigateBack: () -> Unit,
    postDetails: PostDetailsUiData,
    togglePostLike: () -> Unit,
    toggleCommentLike: (String) -> Unit,
    submitComment: (String) -> Unit,
) {
    Column {

        CustomTopAppBar(
            title = "Post Details",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = navigateBack,
        )

        if (postDetails.loadingPost) {
            LoadingIndicator()
        } else if (postDetails.error != null) {
            ErrorComponent(text = postDetails.error.asString())
        } else {
            postDetails.feedPostWithLikesAndComments?.let { post ->
                DetailedPostContent(
                    feedPostWithLikesAndComments = post,
                    isLoadingComments = postDetails.loadingComments,
                    onProfileClick = { /*TODO*/ },
                    onPostLikeClick = togglePostLike,
                    onCommentLikeClick = toggleCommentLike,
                    onCommentSubmit = submitComment,
                )
            }
        }

    }
}



@Preview(showBackground = true)
@Composable
fun PostDetailsScreenPreview_Success() {
    AppTheme {
        PostDetailsContent(
            navigateBack = { },
            postDetails = PostDetailsUiData(
                loadingPost = false,
                loadingComments = false,
                feedPostWithLikesAndComments = samplePostWithLikesAndComments,
            ),
            togglePostLike = { },
            toggleCommentLike = { },
            submitComment = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PostDetailsScreenPreview_LoadingPost() {
    AppTheme {
    PostDetailsContent(
        navigateBack = { },
        postDetails = PostDetailsUiData(
            loadingPost = true,
            loadingComments = false,
        ),
        togglePostLike = { },
        toggleCommentLike = { },
        submitComment = { },
    )
}
}

@Preview(showBackground = true)
@Composable
fun PostDetailsScreenPreview_LoadingComments() {
    AppTheme {
    PostDetailsContent(
        navigateBack = { },
        postDetails = PostDetailsUiData(
            loadingPost = false,
            loadingComments = true,
            feedPostWithLikesAndComments = samplePostWithLikesAndComments,
        ),
        togglePostLike = { },
        toggleCommentLike = { },
        submitComment = { },
    )
}
}

@Preview(showBackground = true)
@Composable
fun PostDetailsScreenPreview_Error() {
    AppTheme {
    PostDetailsContent(
        navigateBack = { },
        postDetails = PostDetailsUiData(
            loadingPost = false,
            loadingComments = false,
            error = UiText.String("An error occurred"),
        ),
        togglePostLike = { },
        toggleCommentLike = { },
        submitComment = { },
    )
}
}

