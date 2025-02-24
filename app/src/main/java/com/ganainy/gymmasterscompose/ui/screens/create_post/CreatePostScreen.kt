package com.ganainy.gymmasterscompose.ui.screens.create_post

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.screens.create_post.composables.HashtagMultiLineTextField
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.shared_components.ErrorComponent
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.shared_components.ProfileImageSmall
import com.ganainy.gymmasterscompose.utils.MockData.samplePost
import com.ganainy.gymmasterscompose.utils.MockData.sampleUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(onNavigateBack: () -> Unit) {
    val viewModel = hiltViewModel<CreatePostViewModel>()

    val uiState by viewModel.uiState.collectAsState()
    val selectedImages = uiState.selectedImages

    val onClickPublishPost: () -> Unit = { viewModel.publishPost { onNavigateBack() } }
    val onUpdatePostContent: (String) -> Unit = { viewModel.updatePostContent(it) }
    val onImageRemoved: (Uri) -> Unit = { viewModel.onImageRemoved(it) }
    val onImageSelected: (List<Uri>) -> Unit = { viewModel.onImageSelected(it) }
    CreatePostContent(
        uiState,
        selectedImages,
        onNavigateBack,
        onClickPublishPost,
        onUpdatePostContent,
        onImageSelected,
        onImageRemoved
    )
}

@Composable
fun CreatePostContent(
    uiState: CreatePostUiState,
    selectedImages: List<Uri>,
    onNavigateBack: () -> Unit,
    onPublishPost: () -> Unit,
    onUpdatePostContent: (String) -> Unit,
    onImageSelected: (List<Uri>) -> Unit,
    onImageRemoved: (Uri) -> Unit
) {
    // Request permission for image selection
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5), // Limit to 5 images
        onResult = { uris -> onImageSelected(uris) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding() // Handle keyboard padding
    ) {
        // Enhanced Top App Bar
        CustomTopAppBar(
            title = "Create Post",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onNavigateBack,
            actionIcons = listOf(
               Icons.Filled.PostAdd
            ),
            disabledActionIcons = if (!uiState.isPostButtonEnabled) listOf(Icons.Filled.PostAdd) else emptyList(), // Disable the PostAdd icon if post is empty
            onActionClicks = listOf {
                if (uiState.isPostButtonEnabled) onPublishPost()
            },
            modifier = Modifier
                .shadow(4.dp)
                .background(MaterialTheme.colorScheme.surface)
        )

        if (uiState.isLoading) {
            // Show loading indicator
             LoadingIndicator()
            return@Column
        }else if (uiState.error != null) {
            // Show error message
             ErrorComponent(text = stringResource( uiState.error))
            return@Column
        }

        CreatePostFields(
            uiState,
            onUpdatePostContent,
            selectedImages,
            multiplePhotoPickerLauncher,
            onImageRemoved
        )

    }
}

@Composable
private fun CreatePostFields(
    uiState: CreatePostUiState,
    onUpdatePostContent: (String) -> Unit,
    selectedImages: List<Uri>,
    multiplePhotoPickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>,
    onImageRemoved: (Uri) -> Unit
) {
    Column {
        // User Info Section with Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImageSmall(
                profilePictureUrl = uiState.user?.profilePictureUrl,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        CircleShape
                    ),
                onClick = { }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = uiState.user?.displayName ?: "Anonymous",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "@${uiState.user?.username ?: "user"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Content TextField with enhanced styling
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            HashtagMultiLineTextField(
                value = uiState.feedPost.content,
                onValueChange = onUpdatePostContent,
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = "What's on your mind?",
            )
        }

        // Image Selection Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Images (${selectedImages.size}/5)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            IconButton(
                onClick = {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Add Images",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Selected Images Preview
        if (selectedImages.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedImages) { uri ->
                    Card(
                        modifier = Modifier
                            .size(140.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Remove Button
                            IconButton(
                                onClick = { onImageRemoved(uri) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove Image",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Preview
@Composable
fun PreviewCreatePostScreen_Initial() {
    AppTheme {
        val uiState = CreatePostUiState(
            isLoading = false,
            feedPost = samplePost,
            user = sampleUser
        )
        CreatePostContent(
            uiState = uiState,
            selectedImages = emptyList(),
            onNavigateBack = { true },
            onUpdatePostContent = { },
            onImageSelected = {},
            onImageRemoved = {},
            onPublishPost = {},
        )
    }
}

@Preview
@Composable
fun PreviewCreatePostScreen_Loading() {
    AppTheme {
        val uiState = CreatePostUiState(
            isLoading = true,
            error = null,
            feedPost = samplePost,
            user = sampleUser
        )

        CreatePostContent(
            uiState = uiState,
            selectedImages = emptyList(),
            onNavigateBack = { true },
            onUpdatePostContent = { },
            onImageSelected = {},
            onImageRemoved = {},
            onPublishPost = {},
        )
    }
}

@Preview
@Composable
fun PreviewCreatePostScreen_Error() {
    AppTheme {
        val uiState = CreatePostUiState(
            isLoading = false,
            feedPost = samplePost,
            error = 1,
            user = sampleUser
        )
        CreatePostContent(
            uiState = uiState,
            selectedImages = emptyList(),
            onNavigateBack = { true },
            onUpdatePostContent = { },
            onImageSelected = {},
            onImageRemoved = {},
            onPublishPost = {},
        )
    }
}