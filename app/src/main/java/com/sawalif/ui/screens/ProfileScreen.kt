package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sawalif.data.models.Post
import com.sawalif.data.models.User
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.theme.*

@Composable
fun ProfileScreen(
    userId: String,
    navController: NavController,
    isOwnProfile: Boolean = false,
    onLogout: (() -> Unit)? = null
) {
    var user by remember { mutableStateOf<User?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var postsCount by remember { mutableStateOf(0) }
    var followersCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var isFollowing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        FirebaseRepository.getUserById(userId) { u ->
            user = u
            isLoading = false
        }
        FirebaseRepository.getUserStats(userId) { posts, followers, following ->
            postsCount = posts
            followersCount = followers
            followingCount = following
        }
        FirebaseRepository.getUserPosts(userId) { userPosts ->
            posts = userPosts
        }
        if (!isOwnProfile) {
            FirebaseRepository.isFollowing(userId) { following ->
                isFollowing = following
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Gold)
        }
        return
    }

    val currentUser = user ?: return

    LazyColumn(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        item {
            // رأس الصفحة
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(brush = Brush.linearGradient(listOf(Purple.copy(alpha = 0.6f), Gold.copy(alpha = 0.3f))))
            ) {
                if (!isOwnProfile) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
                    }
                }
                if (isOwnProfile) {
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = TextPrimary)
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Default.Logout, contentDescription = "خروج", tint = TextPrimary)
                        }
                    }
                }
            }
        }

        item {
            // معلومات المستخدم
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBg)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // الصورة الرمزية
                    Box(modifier = Modifier.offset(y = (-30).dp)) {
                        UserAvatar(
                            name = currentUser.name,
                            avatarColor = currentUser.avatarColor,
                            size = 80
                        )
                    }

                    // زر المتابعة أو الرسالة
                    if (!isOwnProfile) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { navController.navigate("chat/${currentUser.id}") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(listOf(Gold, Gold))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("رسالة", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    FirebaseRepository.followUser(userId, isFollowing) { success ->
                                        if (success) {
                                            isFollowing = !isFollowing
                                            followersCount = if (isFollowing) followersCount + 1 else maxOf(0, followersCount - 1)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) BorderColor else Purple
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (isFollowing) "تتابعه" else "متابعة",
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // الاسم والمعرف
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentUser.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    if (currentUser.isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    }
                }
                Text(text = currentUser.handle, color = TextSecondary, fontSize = 14.sp)

                if (currentUser.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = currentUser.bio, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // الإحصائيات
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    StatItem(count = postsCount, label = "سالفة")
                    StatItem(count = followersCount, label = "متابع")
                    StatItem(count = followingCount, label = "يتابع")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = BorderColor, thickness = 0.5.dp)
            }
        }

        // منشورات المستخدم
        if (posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Forum, contentDescription = null, tint = TextDim, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد سوالف بعد", color = TextSecondary, fontSize = 15.sp)
                    }
                }
            }
        } else {
            items(posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onLike = { FirebaseRepository.toggleLike(post.id, post.isLiked) },
                    onComment = { navController.navigate("comments/${post.id}") },
                    onUserClick = {}
                )
                Divider(color = BorderColor, thickness = 0.5.dp)
            }
        }
    }

    // نافذة تعديل الملف الشخصي
    if (showEditDialog) {
        EditProfileDialog(
            currentUser = currentUser,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newBio ->
                FirebaseRepository.updateUserName(newName) {}
                FirebaseRepository.updateUserBio(newBio) {}
                user = currentUser.copy(name = newName, bio = newBio)
                showEditDialog = false
            }
        )
    }

    // نافذة تأكيد الخروج
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("تسجيل الخروج", color = TextPrimary) },
            text = { Text("هل تريد تسجيل الخروج من حسابك؟", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout?.invoke()
                }) {
                    Text("خروج", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
            containerColor = CardBg
        )
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (count >= 1000) "${count / 1000}K" else count.toString(),
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun EditProfileDialog(
    currentUser: User,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentUser.name) }
    var bio by remember { mutableStateOf(currentUser.bio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل الملف الشخصي", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Gold
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("نبذة عني", color = TextSecondary) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Gold
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name.trim(), bio.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("حفظ", color = DarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextSecondary)
            }
        },
        containerColor = CardBg
    )
}
