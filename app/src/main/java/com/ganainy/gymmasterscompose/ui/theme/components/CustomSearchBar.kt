import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(
    onQueryChange: (String) -> Unit,
    searchQuery: String
) {
    SearchBar(
        query = searchQuery,
        onQueryChange = onQueryChange,
        onSearch = { /* Handle search */ },
        active = false, // Not using the active state in this case
        onActiveChange = { /* Handle active state if needed */ },
        placeholder = {
            Text(
                text = stringResource(R.string.search_for_a_user),
                fontSize = 16.sp
            )
        },
        leadingIcon = { /* No leading icon */ },
        trailingIcon = {
            IconButton(onClick = { /* Handle search click */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search icon")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp) // Adjust padding to control spacing
    ) {
        // No content inside, since this SearchBar doesn't open
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewSearchBar() {
    CustomSearchBar({}, "")
}