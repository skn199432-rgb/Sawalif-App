package com.sawalif.data.models

import java.util.Date

// ===== أنواع المنشورات =====
enum class PostType {
    TEXT, VIDEO, POLL
}

// ===== أدوار المستخدمين =====
enum class UserRole {
    USER, MODERATOR, ADMIN
}

// ===== أنواع الإشعارات =====
enum class NotificationType {
    LIKE, COMMENT, FOLLOW, REPOST, MENTION
}

// ===== نموذج المستخدم =====
data class User(
    val id: String = "",
    val name: String = "",
    val handle: String = "",
    val avatarColor: String = "#F4B942",
    val bio: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isVerified: Boolean = false,
    val isBanned: Boolean = false,
    val role: UserRole = UserRole.USER
)

// ===== خيار التصويت =====
data class PollOption(
    val id: String = "",
    val text: String = "",
    val votesCount: Int = 0,
    val hasVoted: Boolean = false
)

// ===== نموذج المنشور =====
data class Post(
    val id: String = "",
    val user: User = User(),
    val content: String = "",
    val type: PostType = PostType.TEXT,
    val externalLink: String? = null,
    val pollOptions: List<PollOption>? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val repostsCount: Int = 0,
    val isLiked: Boolean = false,
    val reportsCount: Int = 0,
    val createdAt: Date = Date()
)

// ===== نموذج التعليق =====
data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val userAvatarColor: String = "#F4B942",
    val content: String = "",
    val createdAt: Date = Date()
)

// ===== نموذج الرسالة =====
data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val createdAt: Date = Date(),
    val isRead: Boolean = false
)

// ===== نموذج المحادثة =====
data class Conversation(
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserHandle: String = "",
    val otherUserAvatarColor: String = "#F4B942",
    val lastMessage: String = "",
    val lastMessageTime: Date = Date(),
    val unreadCount: Int = 0
)

// ===== نموذج الإشعار =====
data class Notification(
    val id: String = "",
    val type: NotificationType = NotificationType.LIKE,
    val fromUserId: String = "",
    val fromUserName: String = "",
    val fromUserHandle: String = "",
    val fromUserAvatarColor: String = "#F4B942",
    val postId: String? = null,
    val postContent: String? = null,
    val createdAt: Date = Date(),
    val isRead: Boolean = false
)
