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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sawalif.data.models.Conversation
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.theme.*

@Composable
fun MessagesScreen(navController: NavController) {
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getConversations { list ->
            conversations = list
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // رأس الصفحة
        Box(
            modifier = Modifier.fillMaxWidth().background(CardBg).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("الرسائل", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Divider(color = BorderColor, thickness = 0.5.dp)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
            return@Column
        }

        if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Message, contentDescription = null, tint = TextDim, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد محادثات بعد", color = TextSecondary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("ابدأ محادثة من صفحة المستخدم", color = TextDim, fontSize = 13.sp)
                }
            }
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(conversations, key = { it.otherUserId }) { conversation ->
                ConversationRow(
                    conversation = conversation,
                    onClick = { navController.navigate("chat/${conversation.otherUserId}") }
                )
                Divider(color = BorderColor, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun ConversationRow(conversation: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // صورة المستخدم
        Box {
            UserAvatar(
                name = conversation.otherUserName,
                avatarColor = conversation.otherUserAvatarColor,
                size = 50
            )
            if (conversation.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .background(Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString(),
                        color = DarkBg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.otherUserName,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = formatPostTime(conversation.lastMessageTime),
                    color = TextDim,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = conversation.lastMessage,
                color = if (conversation.unreadCount > 0) TextSecondary else TextDim,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}
