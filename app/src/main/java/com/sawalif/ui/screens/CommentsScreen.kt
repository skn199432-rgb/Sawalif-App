package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.sawalif.data.models.Comment
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(postId: String, navController: NavController) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var commentText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(postId) {
        FirebaseRepository.getComments(postId) { list ->
            comments = list
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("التعليقات", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CardBg)
            )
        },
        bottomBar = {
            // حقل إدخال التعليق
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("اكتب تعليقاً...", color = TextDim) },
                    maxLines = 3,
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
                        if (commentText.isNotBlank() && !isSending) {
                            isSending = true
                            FirebaseRepository.addComment(postId, commentText.trim()) { success ->
                                isSending = false
                                if (success) commentText = ""
                            }
                        }
                    },
                    enabled = commentText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (commentText.isNotBlank()) Gold else BorderColor,
                            shape = CircleShape
                        )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "إرسال", tint = if (commentText.isNotBlank()) DarkBg else TextDim)
                    }
                }
            }
        },
        containerColor = DarkBg
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        } else if (comments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("لا توجد تعليقات بعد", color = TextSecondary, fontSize = 16.sp)
                    Text("كن أول من يعلّق!", color = TextDim, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(comments, key = { it.id }) { comment ->
                    CommentItem(comment = comment)
                    Divider(color = BorderColor, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        UserAvatar(
            name = comment.userName,
            avatarColor = comment.userAvatarColor,
            size = 38
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.userName,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = comment.userHandle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "· ${formatPostTime(comment.createdAt)}",
                    color = TextDim,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
