package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sawalif.data.models.Post
import com.sawalif.data.models.User
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    var userResults by remember { mutableStateOf<List<User>>(emptyList()) }
    var postResults by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (query.length >= 2) {
            isSearching = true
            FirebaseRepository.searchUsers(query) { users ->
                userResults = users
                isSearching = false
            }
            FirebaseRepository.searchPosts(query) { posts ->
                postResults = posts
            }
        } else {
            userResults = emptyList()
            postResults = emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // شريط البحث
        Box(
            modifier = Modifier.fillMaxWidth().background(CardBg).padding(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ابحث عن مستخدم أو سالفة...", color = TextDim) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextDim) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "مسح", tint = TextDim)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Gold
                ),
                shape = RoundedCornerShape(24.dp)
            )
        }

        // تبويبات
        if (query.length >= 2) {
            Row(modifier = Modifier.fillMaxWidth().background(CardBg)) {
                listOf("مستخدمون", "سوالف").forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = title,
                                color = if (selectedTab == index) Gold else TextSecondary,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (selectedTab == index) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.width(40.dp).height(2.dp).background(Gold, RoundedCornerShape(1.dp)))
                            }
                        }
                    }
                }
            }
            Divider(color = BorderColor, thickness = 0.5.dp)
        }

        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (query.length < 2) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextDim, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("ابحث عن مستخدمين أو سوالف", color = TextSecondary, fontSize = 15.sp)
                        }
                    }
                }
            } else if (selectedTab == 0) {
                if (userResults.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("لا توجد نتائج", color = TextSecondary)
                        }
                    }
                } else {
                    items(userResults, key = { it.id }) { user ->
                        UserSearchRow(user = user, onClick = { navController.navigate("profile/${user.id}") })
                    }
                }
            } else {
                if (postResults.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("لا توجد نتائج", color = TextSecondary)
                        }
                    }
                } else {
                    items(postResults, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onLike = { FirebaseRepository.toggleLike(post.id, post.isLiked) },
                            onComment = { navController.navigate("comments/${post.id}") },
                            onProfile = { navController.navigate("profile/${post.user.id}") },
                            onReport = { FirebaseRepository.reportPost(post.id) {} }
                        )
                        Divider(color = BorderColor, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchRow(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(name = user.name, avatarColor = user.avatarColor, size = 44)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("✓", color = Gold, fontSize = 13.sp)
                }
            }
            Text(user.handle, color = TextSecondary, fontSize = 13.sp)
        }
    }
    Divider(color = BorderColor, thickness = 0.5.dp)
}
