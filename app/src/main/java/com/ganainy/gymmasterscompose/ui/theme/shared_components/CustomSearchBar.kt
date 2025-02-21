
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(
    hint: String,
    onQueryChange: (String) -> Unit,
    searchQuery: String
) {
    SearchBar(
        query = searchQuery,
        onQueryChange = onQueryChange,
        onSearch = { /* Handle search */ },
        active = false,
        onActiveChange = { /* Handle active state if needed */ },
        placeholder = {
            Text(
                text = hint,
                fontSize = 16.sp
            )
        },
        leadingIcon = {  IconButton(onClick = { /* Handle search click */ }) {
            Icon(Icons.Default.Search, contentDescription = "Search icon")
        } },
        trailingIcon = {

        },
        shape = RoundedCornerShape(8.dp), // Set the shape directly
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 0.dp)
    ) {
        // No content inside, since this SearchBar doesn't open
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSearchBar() {
    CustomSearchBar("Search",{}, "")
}