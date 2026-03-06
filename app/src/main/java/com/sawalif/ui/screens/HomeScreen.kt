package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawalif.ui.theme.GoldPrimary
import com.sawalif.ui.theme.SurfaceDark
import com.sawalif.ui.theme.TextGray

data class Post(
    val id: Int,
    val username: String,
    val handle: String,
    val content: String,
    val likes: Int,
    val replies: Int,
    val time: String
)

val samplePosts = listOf(
    Post(1, "أحمد السالم", "@ahmed_s", "الصبر مفتاح الفرج، وكل شيء له وقته المناسب 🌙", 142, 23, "منذ 5 دقائق"),
    Post(2, "سارة المطيري", "@sara_m", "اليوم جربت مطعم جديد وكان رهيب! أنصح الكل يجربه 🍕", 89, 15, "منذ 12 دقيقة"),
    Post(3, "خالد العتيبي", "@khalid_a", "من يعرف مكان جيد للكامبينق في المنطقة الشرقية؟ 🏕️", 67, 41, "منذ 30 دقيقة"),
    Post(4, "نورة الشمري", "@noura_sh", "الكتاب الجديد لـ رواية 'الأرض الطيبة' ممتاز جداً، أنهيته في يوم واحد 📚", 203, 37, "منذ ساعة"),
    Post(5, "فهد القحطاني", "@fahad_q", "ما أجمل الفجر في رمضان، سكون وهدوء لا يوصف ✨", 315, 52, "منذ ساعتين"),
)

@Composable
fun HomeScreen() {
    var posts by remember { mutableStateOf(samplePosts) }
    var showDialog by remember { mutableStateOf(false) }
    var newPostText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0F))) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "سوالف",
                    color = GoldPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Divider(color = Color(0xFF1A1A2E), thickness = 1.dp)

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts) { post ->
                    PostCard(post = post)
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = GoldPrimary,
            shape = RoundedCornerShape(50)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black)
                Text("ابدأ سالفة", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = SurfaceDark,
            title = { Text("سالفة جديدة", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPostText,
                    onValueChange = { newPostText = it },
                    placeholder = { Text("شاركنا سالفتك...", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF2A2A3E)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPostText.isNotBlank()) {
                            val newPost = Post(
                                id = posts.size + 1,
                                username = "أنت",
                                handle = "@you",
                                content = newPostText,
                                likes = 0,
                                replies = 0,
                                time = "الآن"
                            )
                            posts = listOf(newPost) + posts
                            newPostText = ""
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("نشر", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("إلغاء", color = TextGray)
                }
            }
        )
    }
}

@Composable
fun PostCard(post: Post) {
    var liked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(post.likes) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.username.first().toString(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(post.username, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(post.handle, color = TextGray, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(post.time, color = TextGray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(post.content, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            liked = !liked
                            likeCount = if (liked) likeCount + 1 else likeCount - 1
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "إعجاب",
                            tint = if (liked) Color.Red else TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text("$likeCount", color = TextGray, fontSize = 13.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.ModeComment, contentDescription = "رد", tint = TextGray, modifier = Modifier.size(20.dp))
                    Text("${post.replies}", color = TextGray, fontSize = 13.sp)
                }
            }
        }
    }
}
