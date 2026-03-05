package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sawalif.data.models.Notification
import com.sawalif.data.models.NotificationType
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.theme.*

@Composable
fun NotificationsScreen(navController: NavController) {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getNotifications { list ->
            notifications = list
            isLoading = false
        }
        FirebaseRepository.markNotificationsRead()
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // رأس الصفحة
        Box(
            modifier = Modifier.fillMaxWidth().background(CardBg).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("الإشعارات", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Divider(color = BorderColor, thickness = 0.5.dp)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            return@Column
        }

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = TextDim, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد إشعارات", color = TextSecondary, fontSize = 16.sp)
                }
            }
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notifications, key = { it.id }) { notification ->
                NotificationRow(
                    notification = notification,
                    onClick = {
                        notification.postId?.let { postId ->
                            navController.navigate("comments/$postId")
                        }
                    }
                )
                Divider(color = BorderColor, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun NotificationRow(notification: Notification, onClick: () -> Unit) {
    val (icon, iconColor) = when (notification.type) {
        NotificationType.LIKE -> Pair(Icons.Default.Favorite, ErrorRed)
        NotificationType.COMMENT -> Pair(Icons.Default.ChatBubble, Purple)
        NotificationType.FOLLOW -> Pair(Icons.Default.PersonAdd, Gold)
        NotificationType.REPOST -> Pair(Icons.Default.Repeat, SuccessGreen)
        NotificationType.MENTION -> Pair(Icons.Default.AlternateEmail, Gold)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.isRead) Purple.copy(alpha = 0.05f) else DarkBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // أيقونة النوع
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildNotificationText(notification),
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            notification.postContent?.let { content ->
                if (content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = content,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatPostTime(notification.createdAt),
                color = TextDim,
                fontSize = 11.sp
            )
        }
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Gold, CircleShape)
            )
        }
    }
}

private fun buildNotificationText(notification: Notification): String {
    return when (notification.type) {
        NotificationType.LIKE -> "${notification.fromUserName} أعجب بسالفتك"
        NotificationType.COMMENT -> "${notification.fromUserName} علّق على سالفتك"
        NotificationType.FOLLOW -> "${notification.fromUserName} بدأ بمتابعتك"
        NotificationType.REPOST -> "${notification.fromUserName} أعاد نشر سالفتك"
        NotificationType.MENTION -> "${notification.fromUserName} ذكرك في سالفة"
    }
}
