package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sawalif.data.models.Message
import com.sawalif.data.models.User
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(otherUserId: String, navController: NavController) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var otherUser by remember { mutableStateOf<User?>(null) }
    var messageText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val currentUserId = FirebaseRepository.getCurrentUserId()

    LaunchedEffect(otherUserId) {
        FirebaseRepository.getUserById(otherUserId) { user -> otherUser = user }
        FirebaseRepository.getMessages(otherUserId) { list ->
            messages = list
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        otherUser?.let { user ->
                            UserAvatar(name = user.name, avatarColor = user.avatarColor, size = 34)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(user.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CardBg)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("اكتب رسالة...", color = TextDim) },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Gold
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank() && !isSending) {
                            isSending = true
                            FirebaseRepository.sendMessage(otherUserId, messageText.trim()) { success ->
                                isSending = false
                                if (success) messageText = ""
                            }
                        }
                    },
                    enabled = messageText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (messageText.isNotBlank()) Gold else BorderColor,
                            shape = CircleShape
                        )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "إرسال", tint = if (messageText.isNotBlank()) DarkBg else TextDim)
                    }
                }
            }
        },
        containerColor = DarkBg
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                val isMyMessage = message.senderId == currentUserId
                MessageBubble(message = message, isMyMessage = isMyMessage)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMyMessage: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isMyMessage) Purple else CardBg,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMyMessage) 16.dp else 4.dp,
                        bottomEnd = if (isMyMessage) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatPostTime(message.createdAt),
                    color = TextDim,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
