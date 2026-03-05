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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sawalif.data.models.*
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavController, onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var userRole by remember { mutableStateOf(UserRole.USER) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getCurrentUser { user -> currentUser = user }
        FirebaseRepository.getUserRole { role -> userRole = role }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                userRole = userRole
            )
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 1) {
                FloatingActionButton(
                    onClick = { navController.navigate("compose") },
                    containerColor = Color.Transparent,
                    contentColor = DarkBg,
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(listOf(Gold, Purple)),
                            shape = CircleShape
                        )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "سالفة جديدة", tint = DarkBg)
                }
            }
        },
        containerColor = DarkBg
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> FeedTab(navController = navController, currentUser = currentUser)
                1 -> ExploreTab(navController = navController)
                2 -> NotificationsTab()
                3 -> MessagesTab(navController = navController)
                4 -> ProfileTab(navController = navController, currentUser = currentUser, onLogout = onLogout)
                5 -> AdminScreen(navController = navController)
            }
        }
    }
}

// ===== شريط التنقل السفلي =====
@Composable
fun AppBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userRole: UserRole
) {
    val tabs = buildList {
        add(Triple(Icons.Outlined.Home, Icons.Filled.Home, "الرئيسية"))
        add(Triple(Icons.Outlined.Search, Icons.Filled.Search, "استكشاف"))
        add(Triple(Icons.Outlined.Notifications, Icons.Filled.Notifications, "إشعارات"))
        add(Triple(Icons.Outlined.MailOutline, Icons.Filled.Mail, "رسائل"))
        add(Triple(Icons.Outlined.Person, Icons.Filled.Person, "حسابي"))
        if (userRole == UserRole.ADMIN || userRole == UserRole.MODERATOR) {
            add(Triple(Icons.Outlined.AdminPanelSettings, Icons.Filled.AdminPanelSettings, "إدارة"))
        }
    }

    NavigationBar(
        containerColor = CardBg,
        tonalElevation = 0.dp
    ) {
        tabs.forEachIndexed { index, (outlinedIcon, filledIcon, label) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == index) filledIcon else outlinedIcon,
                        contentDescription = label,
                        tint = if (selectedTab == index) Gold else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (selectedTab == index) Gold else TextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Purple.copy(alpha = 0.2f)
                )
            )
        }
    }
}

// ===== تبويب الخلاصة الرئيسية =====
@Composable
fun FeedTab(navController: NavController, currentUser: User?) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getPosts { loadedPosts ->
            posts = loadedPosts
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // شريط العنوان
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "سوالف",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Gold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Divider(color = BorderColor, thickness = 0.5.dp)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        } else if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Forum, contentDescription = null, tint = TextDim, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد سوالف بعد", color = TextSecondary, fontSize = 16.sp)
                    Text("كن أول من يشارك سالفة!", color = TextDim, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn {
                items(posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        onLike = { FirebaseRepository.toggleLike(post.id, post.isLiked) },
                        onComment = { navController.navigate("comments/${post.id}") },
                        onUserClick = { navController.navigate("profile/${post.user.id}") }
                    )
                    Divider(color = BorderColor, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ===== بطاقة المنشور =====
@Composable
fun PostCard(
    post: Post,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onUserClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // رأس البطاقة - معلومات المستخدم
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // الصورة الرمزية
            UserAvatar(
                name = post.user.name,
                avatarColor = post.user.avatarColor,
                size = 44,
                onClick = onUserClick
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.user.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.clickable { onUserClick() }
                    )
                    if (post.user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
                    }
                }
                Text(
                    text = "${post.user.handle} · ${formatPostTime(post.createdAt)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // محتوى المنشور
        Text(
            text = post.content,
            color = TextPrimary,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        // محتوى إضافي حسب النوع
        when (post.type) {
            PostType.VIDEO -> {
                if (!post.externalLink.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    VideoLinkCard(link = post.externalLink)
                }
            }
            PostType.POLL -> {
                if (!post.pollOptions.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PollCard(postId = post.id, options = post.pollOptions)
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(12.dp))

        // أزرار التفاعل
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // إعجاب
            ActionButton(
                icon = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = post.likesCount,
                tint = if (post.isLiked) ErrorRed else TextSecondary,
                onClick = onLike
            )
            // تعليق
            ActionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                count = post.commentsCount,
                tint = TextSecondary,
                onClick = onComment
            )
            // إعادة نشر
            ActionButton(
                icon = Icons.Outlined.Repeat,
                count = post.repostsCount,
                tint = TextSecondary,
                onClick = {}
            )
            // مشاركة
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Share, contentDescription = "مشاركة", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, count: Int, tint: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }.padding(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        if (count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = count.toString(), color = TextSecondary, fontSize = 13.sp)
        }
    }
}

// ===== بطاقة رابط الفيديو =====
@Composable
fun VideoLinkCard(link: String) {
    val isYoutube = link.contains("youtube") || link.contains("youtu.be")
    val isTiktok = link.contains("tiktok")
    val platform = when {
        isYoutube -> "يوتيوب"
        isTiktok -> "تيك توك"
        else -> "فيديو"
    }
    val platformColor = when {
        isYoutube -> Color(0xFFFF0000)
        isTiktok -> Color(0xFF00F2EA)
        else -> Purple
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(platformColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = platformColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = "مقطع $platform", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(
                text = link,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ===== بطاقة التصويت =====
@Composable
fun PollCard(postId: String, options: List<PollOption>) {
    val totalVotes = options.sumOf { it.votesCount }
    val hasVoted = options.any { it.hasVoted }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val percentage = if (totalVotes > 0) (option.votesCount.toFloat() / totalVotes) else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BorderColor)
                    .clickable(enabled = !hasVoted) {
                        FirebaseRepository.voteInPoll(postId, option.id) {}
                    }
            ) {
                if (hasVoted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .background(
                                brush = Brush.linearGradient(listOf(Purple.copy(alpha = 0.5f), Gold.copy(alpha = 0.3f)))
                            )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = option.text, color = TextPrimary, fontSize = 14.sp)
                    if (hasVoted) {
                        Text(
                            text = "${(percentage * 100).toInt()}%",
                            color = Gold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Text(
            text = "$totalVotes صوت",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// ===== الصورة الرمزية =====
@Composable
fun UserAvatar(
    name: String,
    avatarColor: String,
    size: Int = 40,
    onClick: (() -> Unit)? = null
) {
    val initial = if (name.isNotEmpty()) name[0].toString() else "م"
    val color = try {
        Color(android.graphics.Color.parseColor(avatarColor))
    } catch (e: Exception) {
        Gold
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .background(color.copy(alpha = 0.2f), CircleShape)
            .border(1.5.dp, color, CircleShape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.4f).sp
        )
    }
}

// ===== تبويب الاستكشاف =====
@Composable
fun ExploreTab(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<User>>(emptyList()) }
    var postResults by remember { mutableStateOf<List<Post>>(emptyList()) }
    var suggestedUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getSuggestedUsers { suggestedUsers = it }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            isSearching = true
            FirebaseRepository.searchUsers(searchQuery) { users -> searchResults = users }
            FirebaseRepository.searchPosts(searchQuery) { posts -> postResults = posts; isSearching = false }
        } else {
            searchResults = emptyList()
            postResults = emptyList()
            isSearching = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // شريط البحث
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ابحث عن مستخدمين أو سوالف...", color = TextDim) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Gold) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
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
        Divider(color = BorderColor, thickness = 0.5.dp)

        LazyColumn {
            if (searchQuery.length < 2) {
                // اقتراحات للمتابعة
                item {
                    Text(
                        text = "مقترح للمتابعة",
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(suggestedUsers) { user ->
                    SuggestedUserRow(user = user, navController = navController)
                    Divider(color = BorderColor, thickness = 0.5.dp)
                }
            } else {
                if (isSearching) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Gold)
                        }
                    }
                } else {
                    if (searchResults.isNotEmpty()) {
                        item {
                            Text("مستخدمون", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(16.dp, 12.dp))
                        }
                        items(searchResults) { user ->
                            SuggestedUserRow(user = user, navController = navController)
                            Divider(color = BorderColor, thickness = 0.5.dp)
                        }
                    }
                    if (postResults.isNotEmpty()) {
                        item {
                            Text("سوالف", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(16.dp, 12.dp))
                        }
                        items(postResults) { post ->
                            PostCard(
                                post = post,
                                onLike = { FirebaseRepository.toggleLike(post.id, post.isLiked) },
                                onComment = { navController.navigate("comments/${post.id}") },
                                onUserClick = { navController.navigate("profile/${post.user.id}") }
                            )
                            Divider(color = BorderColor, thickness = 0.5.dp)
                        }
                    }
                    if (searchResults.isEmpty() && postResults.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("لا توجد نتائج لـ \"$searchQuery\"", color = TextSecondary, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestedUserRow(user: User, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("profile/${user.id}") }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(name = user.name, avatarColor = user.avatarColor, size = 44)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = user.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
                }
            }
            Text(text = user.handle, color = TextSecondary, fontSize = 13.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextDim)
    }
}

// ===== تبويب الإشعارات =====
@Composable
fun NotificationsTab() {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getNotifications { list ->
            notifications = list
            isLoading = false
        }
        FirebaseRepository.markNotificationsRead()
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = TextDim, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد إشعارات", color = TextSecondary, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn {
                items(notifications, key = { it.id }) { notif ->
                    NotificationRow(notif)
                    Divider(color = BorderColor, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun NotificationRow(notification: Notification) {
    val (icon, text) = when (notification.type) {
        NotificationType.LIKE -> Icons.Default.Favorite to "أعجب بسالفتك"
        NotificationType.COMMENT -> Icons.Default.ChatBubble to "علّق على سالفتك"
        NotificationType.FOLLOW -> Icons.Default.PersonAdd to "بدأ بمتابعتك"
        NotificationType.REPOST -> Icons.Default.Repeat to "أعاد نشر سالفتك"
        NotificationType.MENTION -> Icons.Default.AlternateEmail to "ذكرك في سالفة"
    }
    val iconColor = when (notification.type) {
        NotificationType.LIKE -> ErrorRed
        NotificationType.COMMENT -> Purple
        NotificationType.FOLLOW -> Gold
        NotificationType.REPOST -> SuccessGreen
        NotificationType.MENTION -> Gold
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.isRead) Purple.copy(alpha = 0.05f) else DarkBg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(notification.fromUserName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(" $text", color = TextSecondary, fontSize = 14.sp)
            }
            if (!notification.postContent.isNullOrBlank()) {
                Text(
                    text = notification.postContent,
                    color = TextDim,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatPostTime(notification.createdAt),
                color = TextDim,
                fontSize = 11.sp
            )
        }
    }
}

// ===== تبويب الرسائل =====
@Composable
fun MessagesTab(navController: NavController) {
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseRepository.getConversations { list ->
            conversations = list
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
        } else if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MailOutline, contentDescription = null, tint = TextDim, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لا توجد محادثات بعد", color = TextSecondary, fontSize = 16.sp)
                    Text("ابدأ محادثة من صفحة أي مستخدم", color = TextDim, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn {
                items(conversations, key = { it.otherUserId }) { conv ->
                    ConversationRow(
                        conversation = conv,
                        onClick = { navController.navigate("chat/${conv.otherUserId}") }
                    )
                    Divider(color = BorderColor, thickness = 0.5.dp)
                }
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(name = conversation.otherUserName, avatarColor = conversation.otherUserAvatarColor, size = 48)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(conversation.otherUserName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(formatPostTime(conversation.lastMessageTime), color = TextDim, fontSize = 11.sp)
            }
            Text(
                text = conversation.lastMessage,
                color = if (conversation.unreadCount > 0) TextPrimary else TextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (conversation.unreadCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(20.dp).background(Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(conversation.unreadCount.toString(), color = DarkBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ===== تبويب الملف الشخصي =====
@Composable
fun ProfileTab(navController: NavController, currentUser: User?, onLogout: () -> Unit) {
    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Gold)
        }
        return
    }
    ProfileScreen(userId = currentUser.id, navController = navController, isOwnProfile = true, onLogout = onLogout)
}

// ===== دوال مساعدة =====
fun formatPostTime(date: Date): String {
    val diff = System.currentTimeMillis() - date.time
    return when {
        diff < 60_000 -> "الآن"
        diff < 3_600_000 -> "${diff / 60_000}د"
        diff < 86_400_000 -> "${diff / 3_600_000}س"
        diff < 604_800_000 -> "${diff / 86_400_000}ي"
        else -> SimpleDateFormat("d MMM", Locale("ar")).format(date)
    }
}
