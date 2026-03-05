package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sawalif.data.models.Post
import com.sawalif.data.models.User
import com.sawalif.data.models.UserRole
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.theme.*

@Composable
fun AdminScreen(navController: NavController) {
    var selectedSection by remember { mutableStateOf(0) }
    var totalUsers by remember { mutableStateOf(0) }
    var totalPosts by remember { mutableStateOf(0) }
    var reportedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getAdminStats { users, posts ->
            totalUsers = users
            totalPosts = posts
            isLoading = false
        }
        FirebaseRepository.getReportedPosts { posts -> reportedPosts = posts }
        FirebaseRepository.getAllUsers { users -> allUsers = users }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // رأس الصفحة
        Box(
            modifier = Modifier.fillMaxWidth().background(CardBg).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("لوحة التحكم", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gold)
        }
        Divider(color = BorderColor, thickness = 0.5.dp)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // إحصائيات سريعة
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdminStatCard(
                        icon = Icons.Default.People,
                        label = "المستخدمون",
                        count = totalUsers,
                        color = Purple,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        icon = Icons.Default.Forum,
                        label = "السوالف",
                        count = totalPosts,
                        color = Gold,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        icon = Icons.Default.Report,
                        label = "بلاغات",
                        count = reportedPosts.size,
                        color = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // تبويبات الأقسام
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminTabButton(
                        label = "المستخدمون",
                        isSelected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    AdminTabButton(
                        label = "البلاغات",
                        isSelected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // محتوى القسم المختار
            if (selectedSection == 0) {
                // قائمة المستخدمين
                if (allUsers.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("لا يوجد مستخدمون", color = TextSecondary)
                        }
                    }
                } else {
                    items(allUsers, key = { it.id }) { user ->
                        AdminUserRow(
                            user = user,
                            onBan = { FirebaseRepository.banUser(user.id, !user.isBanned) {} },
                            onMakeMod = { FirebaseRepository.setUserRole(user.id, UserRole.MODERATOR) {} }
                        )
                    }
                }
            } else {
                // قائمة البلاغات
                if (reportedPosts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("لا توجد بلاغات", color = TextSecondary, fontSize = 15.sp)
                            }
                        }
                    }
                } else {
                    items(reportedPosts, key = { it.id }) { post ->
                        AdminReportedPostRow(
                            post = post,
                            onDelete = { FirebaseRepository.deletePost(post.id) {} },
                            onIgnore = { FirebaseRepository.clearPostReport(post.id) {} }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    icon: ImageVector,
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CardBg, RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        Text(
            text = if (count >= 1000) "${count / 1000}K" else count.toString(),
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
fun AdminTabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) Purple.copy(alpha = 0.3f) else CardBg,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Gold else BorderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Gold else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun AdminUserRow(
    user: User,
    onBan: () -> Unit,
    onMakeMod: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(10.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(name = user.name, avatarColor = user.avatarColor, size = 40)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (user.role != UserRole.USER) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (user.role == UserRole.ADMIN) "أدمن" else "مشرف",
                        color = Gold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (user.isBanned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "محظور",
                        color = ErrorRed,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .background(ErrorRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(user.handle, color = TextSecondary, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (user.role == UserRole.USER) {
                IconButton(onClick = onMakeMod, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = "مشرف", tint = Purple, modifier = Modifier.size(18.dp))
                }
            }
            IconButton(onClick = onBan, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (user.isBanned) Icons.Default.LockOpen else Icons.Default.Block,
                    contentDescription = if (user.isBanned) "رفع الحظر" else "حظر",
                    tint = if (user.isBanned) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AdminReportedPostRow(
    post: Post,
    onDelete: () -> Unit,
    onIgnore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(10.dp))
            .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(name = post.user.name, avatarColor = post.user.avatarColor, size = 32)
            Spacer(modifier = Modifier.width(8.dp))
            Text(post.user.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Flag, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
            Text(" ${post.reportsCount} بلاغ", color = ErrorRed, fontSize = 12.sp)
        }
        Text(
            text = post.content,
            color = TextSecondary,
            fontSize = 13.sp,
            maxLines = 3
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onIgnore,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("تجاهل", fontSize = 13.sp)
            }
            Button(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("حذف", fontSize = 13.sp, color = Color.White)
            }
        }
    }
}
