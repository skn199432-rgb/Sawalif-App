package com.sawalif.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sawalif.data.models.*

object FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance("https://sawalif-e1fbe-default-rtdb.firebaseio.com")

    // ==================== المصادقة ====================

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onResult(true, "") }
            .addOnFailureListener { e -> onResult(false, e.message ?: "خطأ في تسجيل الدخول") }
    }

    fun register(email: String, password: String, name: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: run { onResult(false, "خطأ في إنشاء الحساب"); return@addOnSuccessListener }
                val handle = "@" + name.lowercase().replace(" ", "_") + uid.take(4)
                val avatarColors = listOf("#F4B942", "#7B2FBE", "#E8A87C", "#7EB8D4", "#A8D5A2", "#D4A5C9")
                val randomColor = avatarColors.random()
                val userData = mapOf(
                    "id" to uid,
                    "name" to name,
                    "handle" to handle,
                    "avatarColor" to randomColor,
                    "bio" to "",
                    "followersCount" to 0,
                    "followingCount" to 0,
                    "isVerified" to false,
                    "isBanned" to false,
                    "role" to "USER",
                    "createdAt" to System.currentTimeMillis()
                )
                db.getReference("users").child(uid).setValue(userData)
                    .addOnSuccessListener { onResult(true, "") }
                    .addOnFailureListener { e -> onResult(false, e.message ?: "خطأ في حفظ البيانات") }
            }
            .addOnFailureListener { e -> onResult(false, e.message ?: "خطأ في إنشاء الحساب") }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    // ==================== بيانات المستخدم ====================

    fun getCurrentUser(onResult: (User?) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onResult(null); return }
        db.getReference("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) { onResult(null); return }
                onResult(parseUser(snapshot))
            }
            override fun onCancelled(error: DatabaseError) { onResult(null) }
        })
    }

    fun getUserById(userId: String, onResult: (User?) -> Unit) {
        db.getReference("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) { onResult(null); return }
                onResult(parseUser(snapshot))
            }
            override fun onCancelled(error: DatabaseError) { onResult(null) }
        })
    }

    fun updateUserName(newName: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        db.getReference("users").child(uid).child("name").setValue(newName)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun updateUserHandle(newHandle: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        val handle = if (newHandle.startsWith("@")) newHandle else "@$newHandle"
        db.getReference("users").orderByChild("handle").equalTo(handle)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) { onResult(false); return }
                    db.getReference("users").child(uid).child("handle").setValue(handle)
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                }
                override fun onCancelled(error: DatabaseError) { onResult(false) }
            })
    }

    fun updateUserBio(newBio: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        db.getReference("users").child(uid).child("bio").setValue(newBio)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUserStats(userId: String, onResult: (posts: Int, followers: Int, following: Int) -> Unit) {
        db.getReference("posts").orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(postSnap: DataSnapshot) {
                    val postsCount = postSnap.childrenCount.toInt()
                    db.getReference("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnap: DataSnapshot) {
                            val followers = userSnap.child("followersCount").getValue(Int::class.java) ?: 0
                            val following = userSnap.child("followingCount").getValue(Int::class.java) ?: 0
                            onResult(postsCount, followers, following)
                        }
                        override fun onCancelled(error: DatabaseError) { onResult(postsCount, 0, 0) }
                    })
                }
                override fun onCancelled(error: DatabaseError) { onResult(0, 0, 0) }
            })
    }

    fun followUser(targetUserId: String, isFollowing: Boolean, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        val followRef = db.getReference("follows").child(uid).child(targetUserId)
        val targetUserRef = db.getReference("users").child(targetUserId)
        val currentUserRef = db.getReference("users").child(uid)

        if (isFollowing) {
            followRef.removeValue()
            targetUserRef.child("followersCount").get().addOnSuccessListener { snap ->
                val count = snap.getValue(Int::class.java) ?: 1
                targetUserRef.child("followersCount").setValue(maxOf(0, count - 1))
            }
            currentUserRef.child("followingCount").get().addOnSuccessListener { snap ->
                val count = snap.getValue(Int::class.java) ?: 1
                currentUserRef.child("followingCount").setValue(maxOf(0, count - 1))
            }
            onResult(true)
        } else {
            followRef.setValue(true)
            targetUserRef.child("followersCount").get().addOnSuccessListener { snap ->
                val count = snap.getValue(Int::class.java) ?: 0
                targetUserRef.child("followersCount").setValue(count + 1)
            }
            currentUserRef.child("followingCount").get().addOnSuccessListener { snap ->
                val count = snap.getValue(Int::class.java) ?: 0
                currentUserRef.child("followingCount").setValue(count + 1)
            }
            sendNotification(targetUserId, NotificationType.FOLLOW, null, null)
            onResult(true)
        }
    }

    fun isFollowing(targetUserId: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        db.getReference("follows").child(uid).child(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) { onResult(snapshot.exists()) }
                override fun onCancelled(error: DatabaseError) { onResult(false) }
            })
    }

    // ==================== المنشورات ====================

    fun addPost(
        content: String,
        type: PostType,
        externalLink: String? = null,
        pollOptions: List<String>? = null,
        onResult: (Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        db.getReference("users").child(uid).get().addOnSuccessListener { userSnap ->
            val postId = db.getReference("posts").push().key ?: run { onResult(false); return@addOnSuccessListener }
            val postData = mutableMapOf<String, Any>(
                "id" to postId,
                "userId" to uid,
                "userName" to (userSnap.child("name").getValue(String::class.java) ?: "مستخدم"),
                "userHandle" to (userSnap.child("handle").getValue(String::class.java) ?: "@user"),
                "userAvatarColor" to (userSnap.child("avatarColor").getValue(String::class.java) ?: "#F4B942"),
                "content" to content,
                "type" to type.name,
                "likesCount" to 0,
                "commentsCount" to 0,
                "repostsCount" to 0,
                "createdAt" to System.currentTimeMillis()
            )
            if (externalLink != null) postData["externalLink"] = externalLink
            if (pollOptions != null && pollOptions.isNotEmpty()) {
                val pollMap = pollOptions.mapIndexed { index, text ->
                    index.toString() to mapOf("id" to index.toString(), "text" to text, "votesCount" to 0)
                }.toMap()
                postData["pollOptions"] = pollMap
            }
            db.getReference("posts").child(postId).setValue(postData)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        }.addOnFailureListener { onResult(false) }
    }

    fun getPosts(onResult: (List<Post>) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: ""
        db.getReference("posts").orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = mutableListOf<Post>()
                    for (child in snapshot.children) {
                        val post = parsePost(child, currentUid)
                        if (post != null) posts.add(0, post)
                    }
                    onResult(posts)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getUserPosts(userId: String, onResult: (List<Post>) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: ""
        db.getReference("posts").orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = mutableListOf<Post>()
                    for (child in snapshot.children) {
                        val post = parsePost(child, currentUid)
                        if (post != null) posts.add(0, post)
                    }
                    onResult(posts)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun deletePost(postId: String, onResult: (Boolean) -> Unit) {
        db.getReference("posts").child(postId).removeValue()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun toggleLike(postId: String, isCurrentlyLiked: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val postRef = db.getReference("posts").child(postId)
        if (isCurrentlyLiked) {
            postRef.child("likes").child(uid).removeValue()
            postRef.child("likesCount").get().addOnSuccessListener { snap ->
                val count = snap.getValue(Int::class.java) ?: 1
                postRef.child("likesCount").setValue(maxOf(0, count - 1))
            }
        } else {
            postRef.child("likes").child(uid).setValue(true)
            postRef.child("likesCount").get().addOnSuccessListener { snap ->
                val count = snap.getValue(Int::class.java) ?: 0
                postRef.child("likesCount").setValue(count + 1)
                postRef.get().addOnSuccessListener { postSnap ->
                    val ownerId = postSnap.child("userId").getValue(String::class.java) ?: ""
                    val postContent = postSnap.child("content").getValue(String::class.java) ?: ""
                    sendNotification(ownerId, NotificationType.LIKE, postId, postContent)
                }
            }
        }
    }

    fun voteInPoll(postId: String, optionId: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        val optionRef = db.getReference("posts").child(postId).child("pollOptions").child(optionId)
        optionRef.child("voters").child(uid).get().addOnSuccessListener { snap ->
            if (snap.exists()) { onResult(false); return@addOnSuccessListener }
            optionRef.child("voters").child(uid).setValue(true)
            optionRef.child("votesCount").get().addOnSuccessListener { countSnap ->
                val count = countSnap.getValue(Int::class.java) ?: 0
                optionRef.child("votesCount").setValue(count + 1)
                onResult(true)
            }
        }.addOnFailureListener { onResult(false) }
    }

    // ==================== التعليقات ====================

    fun addComment(postId: String, content: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        db.getReference("users").child(uid).get().addOnSuccessListener { userSnap ->
            val commentId = db.getReference("comments").child(postId).push().key ?: run { onResult(false); return@addOnSuccessListener }
            val commentData = mapOf(
                "id" to commentId,
                "userId" to uid,
                "userName" to (userSnap.child("name").getValue(String::class.java) ?: "مستخدم"),
                "userHandle" to (userSnap.child("handle").getValue(String::class.java) ?: "@user"),
                "userAvatarColor" to (userSnap.child("avatarColor").getValue(String::class.java) ?: "#F4B942"),
                "content" to content,
                "createdAt" to System.currentTimeMillis()
            )
            db.getReference("comments").child(postId).child(commentId).setValue(commentData)
                .addOnSuccessListener {
                    db.getReference("posts").child(postId).child("commentsCount").get()
                        .addOnSuccessListener { snap ->
                            val count = snap.getValue(Int::class.java) ?: 0
                            db.getReference("posts").child(postId).child("commentsCount").setValue(count + 1)
                        }
                    db.getReference("posts").child(postId).get().addOnSuccessListener { postSnap ->
                        val ownerId = postSnap.child("userId").getValue(String::class.java) ?: ""
                        val postContent = postSnap.child("content").getValue(String::class.java) ?: ""
                        sendNotification(ownerId, NotificationType.COMMENT, postId, postContent)
                    }
                    onResult(true)
                }
                .addOnFailureListener { onResult(false) }
        }.addOnFailureListener { onResult(false) }
    }

    fun getComments(postId: String, onResult: (List<Comment>) -> Unit) {
        db.getReference("comments").child(postId).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val comments = mutableListOf<Comment>()
                    for (child in snapshot.children) {
                        comments.add(Comment(
                            id = child.child("id").getValue(String::class.java) ?: "",
                            userId = child.child("userId").getValue(String::class.java) ?: "",
                            userName = child.child("userName").getValue(String::class.java) ?: "مستخدم",
                            userHandle = child.child("userHandle").getValue(String::class.java) ?: "@user",
                            userAvatarColor = child.child("userAvatarColor").getValue(String::class.java) ?: "#F4B942",
                            content = child.child("content").getValue(String::class.java) ?: "",
                            createdAt = java.util.Date(child.child("createdAt").getValue(Long::class.java) ?: 0L)
                        ))
                    }
                    onResult(comments.reversed())
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun deleteComment(postId: String, commentId: String, onResult: (Boolean) -> Unit) {
        db.getReference("comments").child(postId).child(commentId).removeValue()
            .addOnSuccessListener {
                db.getReference("posts").child(postId).child("commentsCount").get()
                    .addOnSuccessListener { snap ->
                        val count = snap.getValue(Int::class.java) ?: 1
                        db.getReference("posts").child(postId).child("commentsCount").setValue(maxOf(0, count - 1))
                    }
                onResult(true)
            }
            .addOnFailureListener { onResult(false) }
    }

    // ==================== البحث ====================

    fun searchUsers(query: String, onResult: (List<User>) -> Unit) {
        if (query.isBlank()) { onResult(emptyList()); return }
        val q = query.lowercase().trim().removePrefix("@")
        db.getReference("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = mutableListOf<User>()
                for (child in snapshot.children) {
                    val name = child.child("name").getValue(String::class.java)?.lowercase() ?: ""
                    val handle = child.child("handle").getValue(String::class.java)?.lowercase()?.removePrefix("@") ?: ""
                    if (name.contains(q) || handle.contains(q)) {
                        val user = parseUser(child)
                        if (user != null) results.add(user)
                    }
                }
                onResult(results.take(20))
            }
            override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
        })
    }

    fun searchPosts(query: String, onResult: (List<Post>) -> Unit) {
        if (query.isBlank()) { onResult(emptyList()); return }
        val q = query.lowercase().trim()
        val currentUid = auth.currentUser?.uid ?: ""
        db.getReference("posts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = mutableListOf<Post>()
                for (child in snapshot.children) {
                    val content = child.child("content").getValue(String::class.java)?.lowercase() ?: ""
                    if (content.contains(q)) {
                        val post = parsePost(child, currentUid)
                        if (post != null) results.add(post)
                    }
                }
                onResult(results.take(30))
            }
            override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
        })
    }

    fun getSuggestedUsers(onResult: (List<User>) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: ""
        db.getReference("users").limitToFirst(20).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (child in snapshot.children) {
                    val id = child.child("id").getValue(String::class.java) ?: ""
                    if (id != currentUid) {
                        val user = parseUser(child)
                        if (user != null) users.add(user)
                    }
                }
                onResult(users.shuffled().take(10))
            }
            override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
        })
    }

    // ==================== الرسائل ====================

    fun sendMessage(receiverId: String, content: String, onResult: (Boolean) -> Unit) {
        val senderId = auth.currentUser?.uid ?: run { onResult(false); return }
        val chatId = if (senderId < receiverId) "${senderId}_$receiverId" else "${receiverId}_$senderId"
        val msgId = db.getReference("messages").child(chatId).push().key ?: run { onResult(false); return }
        val msgData = mapOf(
            "id" to msgId,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to content,
            "createdAt" to System.currentTimeMillis(),
            "isRead" to false
        )
        db.getReference("messages").child(chatId).child(msgId).setValue(msgData)
            .addOnSuccessListener {
                val lastMsgData = mapOf(
                    "lastMessage" to content,
                    "lastMessageTime" to System.currentTimeMillis()
                )
                db.getReference("chats").child(senderId).child(receiverId).updateChildren(lastMsgData)
                db.getReference("chats").child(receiverId).child(senderId).updateChildren(lastMsgData)
                sendNotification(receiverId, NotificationType.MESSAGE, null, content)
                onResult(true)
            }
            .addOnFailureListener { onResult(false) }
    }

    fun getMessages(otherUserId: String, onResult: (List<Message>) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: run { onResult(emptyList()); return }
        val chatId = if (currentUid < otherUserId) "${currentUid}_$otherUserId" else "${otherUserId}_$currentUid"
        db.getReference("messages").child(chatId).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Message>()
                    for (child in snapshot.children) {
                        messages.add(Message(
                            id = child.child("id").getValue(String::class.java) ?: "",
                            senderId = child.child("senderId").getValue(String::class.java) ?: "",
                            receiverId = child.child("receiverId").getValue(String::class.java) ?: "",
                            content = child.child("content").getValue(String::class.java) ?: "",
                            createdAt = java.util.Date(child.child("createdAt").getValue(Long::class.java) ?: 0L),
                            isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                        ))
                    }
                    onResult(messages)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getConversations(onResult: (List<Conversation>) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: run { onResult(emptyList()); return }
        db.getReference("chats").child(currentUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val conversations = mutableListOf<Conversation>()
                    val total = snapshot.childrenCount.toInt()
                    if (total == 0) { onResult(emptyList()); return }
                    var processed = 0
                    for (child in snapshot.children) {
                        val otherUserId = child.key ?: continue
                        val lastMsg = child.child("lastMessage").getValue(String::class.java) ?: ""
                        val lastTime = child.child("lastMessageTime").getValue(Long::class.java) ?: 0L
                        db.getReference("users").child(otherUserId).get().addOnSuccessListener { userSnap ->
                            val user = parseUser(userSnap) ?: User(id = otherUserId, name = "مستخدم", handle = "@user")
                            conversations.add(0, Conversation(
                                otherUserId = otherUserId,
                                otherUserName = user.name,
                                otherUserHandle = user.handle,
                                otherUserAvatarColor = user.avatarColor,
                                lastMessage = lastMsg,
                                lastMessageTime = java.util.Date(lastTime)
                            ))
                            processed++
                            if (processed >= total) onResult(conversations)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    // ==================== الإشعارات ====================

    fun sendNotification(receiverId: String, type: NotificationType, postId: String?, postContent: String?) {
        val senderUid = auth.currentUser?.uid ?: return
        if (senderUid == receiverId) return
        getCurrentUser { sender ->
            val notifId = db.getReference("notifications").child(receiverId).push().key ?: return@getCurrentUser
            val notifData = mapOf(
                "id" to notifId,
                "type" to type.name,
                "senderId" to (sender?.id ?: senderUid),
                "senderName" to (sender?.name ?: "مستخدم"),
                "senderHandle" to (sender?.handle ?: "@user"),
                "senderAvatarColor" to (sender?.avatarColor ?: "#F4B942"),
                "postId" to (postId ?: ""),
                "postContent" to (postContent ?: ""),
                "createdAt" to System.currentTimeMillis(),
                "isRead" to false
            )
            db.getReference("notifications").child(receiverId).child(notifId).setValue(notifData)
        }
    }

    fun getNotifications(onResult: (List<Notification>) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: run { onResult(emptyList()); return }
        db.getReference("notifications").child(currentUid).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications = mutableListOf<Notification>()
                    for (child in snapshot.children) {
                        val typeStr = child.child("type").getValue(String::class.java) ?: "LIKE"
                        val type = try { NotificationType.valueOf(typeStr) } catch (e: Exception) { NotificationType.LIKE }
                        notifications.add(0, Notification(
                            id = child.child("id").getValue(String::class.java) ?: "",
                            type = type,
                            fromUserId = child.child("senderId").getValue(String::class.java) ?: "",
                            fromUserName = child.child("senderName").getValue(String::class.java) ?: "مستخدم",
                            fromUserHandle = child.child("senderHandle").getValue(String::class.java) ?: "@user",
                            fromUserAvatarColor = child.child("senderAvatarColor").getValue(String::class.java) ?: "#F4B942",
                            postId = child.child("postId").getValue(String::class.java)?.ifEmpty { null },
                            postContent = child.child("postContent").getValue(String::class.java)?.ifEmpty { null },
                            createdAt = java.util.Date(child.child("createdAt").getValue(Long::class.java) ?: 0L),
                            isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                        ))
                    }
                    onResult(notifications)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun markNotificationsRead() {
        val currentUid = auth.currentUser?.uid ?: return
        db.getReference("notifications").child(currentUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.child("isRead").setValue(true)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ==================== الأدمن ====================

    fun getAllUsers(onResult: (List<User>) -> Unit) {
        db.getReference("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (child in snapshot.children) {
                    val user = parseUser(child)
                    if (user != null) users.add(user)
                }
                onResult(users)
            }
            override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
        })
    }

    fun setUserRole(userId: String, role: UserRole, onResult: (Boolean) -> Unit) {
        db.getReference("users").child(userId).child("role").setValue(role.name)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUserRole(onResult: (UserRole) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onResult(UserRole.USER); return }
        db.getReference("users").child(uid).child("role")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roleStr = snapshot.getValue(String::class.java) ?: "USER"
                    val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.USER }
                    onResult(role)
                }
                override fun onCancelled(error: DatabaseError) { onResult(UserRole.USER) }
            })
    }

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun getAdminStats(onResult: (users: Int, posts: Int) -> Unit) {
        db.getReference("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersCount = snapshot.childrenCount.toInt()
                db.getReference("posts").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(postsSnap: DataSnapshot) {
                        onResult(usersCount, postsSnap.childrenCount.toInt())
                    }
                    override fun onCancelled(error: DatabaseError) { onResult(usersCount, 0) }
                })
            }
            override fun onCancelled(error: DatabaseError) { onResult(0, 0) }
        })
    }

    fun getReportedPosts(onResult: (List<Post>) -> Unit) {
        val uid = auth.currentUser?.uid ?: ""
        db.getReference("posts").orderByChild("reportsCount").startAt(1.0)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = mutableListOf<Post>()
                    for (child in snapshot.children) {
                        parsePost(child, uid)?.let { posts.add(it) }
                    }
                    onResult(posts)
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun reportPost(postId: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: run { onResult(false); return }
        val postRef = db.getReference("posts").child(postId)
        postRef.child("reports").child(uid).setValue(true)
            .addOnSuccessListener {
                postRef.child("reportsCount").get().addOnSuccessListener { snap ->
                    val count = snap.getValue(Int::class.java) ?: 0
                    postRef.child("reportsCount").setValue(count + 1)
                    onResult(true)
                }
            }
            .addOnFailureListener { onResult(false) }
    }

    fun clearPostReport(postId: String, onResult: (Boolean) -> Unit) {
        db.getReference("posts").child(postId).child("reportsCount").setValue(0)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun banUser(userId: String, isBanned: Boolean, onResult: (Boolean) -> Unit) {
        db.getReference("users").child(userId).child("isBanned").setValue(isBanned)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // ==================== مساعدات خاصة ====================

    private fun parseUser(snapshot: DataSnapshot): User? {
        val id = snapshot.child("id").getValue(String::class.java) ?: return null
        val roleStr = snapshot.child("role").getValue(String::class.java) ?: "USER"
        val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.USER }
        return User(
            id = id,
            name = snapshot.child("name").getValue(String::class.java) ?: "مستخدم",
            handle = snapshot.child("handle").getValue(String::class.java) ?: "@user",
            avatarColor = snapshot.child("avatarColor").getValue(String::class.java) ?: "#F4B942",
            bio = snapshot.child("bio").getValue(String::class.java) ?: "",
            followersCount = snapshot.child("followersCount").getValue(Int::class.java) ?: 0,
            followingCount = snapshot.child("followingCount").getValue(Int::class.java) ?: 0,
            isVerified = snapshot.child("isVerified").getValue(Boolean::class.java) ?: false,
            isBanned = snapshot.child("isBanned").getValue(Boolean::class.java) ?: false,
            role = role
        )
    }

    private fun parsePost(snapshot: DataSnapshot, currentUid: String): Post? {
        val id = snapshot.child("id").getValue(String::class.java) ?: return null
        val typeStr = snapshot.child("type").getValue(String::class.java) ?: "TEXT"
        val type = try { PostType.valueOf(typeStr) } catch (e: Exception) { PostType.TEXT }

        val pollOptions = mutableListOf<PollOption>()
        val pollSnap = snapshot.child("pollOptions")
        if (pollSnap.exists()) {
            for (optChild in pollSnap.children) {
                pollOptions.add(PollOption(
                    id = optChild.child("id").getValue(String::class.java) ?: "",
                    text = optChild.child("text").getValue(String::class.java) ?: "",
                    votesCount = optChild.child("votesCount").getValue(Int::class.java) ?: 0,
                    hasVoted = optChild.child("voters").child(currentUid).exists()
                ))
            }
        }

        return Post(
            id = id,
            user = User(
                id = snapshot.child("userId").getValue(String::class.java) ?: "",
                name = snapshot.child("userName").getValue(String::class.java) ?: "مستخدم",
                handle = snapshot.child("userHandle").getValue(String::class.java) ?: "@user",
                avatarColor = snapshot.child("userAvatarColor").getValue(String::class.java) ?: "#F4B942"
            ),
            content = snapshot.child("content").getValue(String::class.java) ?: "",
            type = type,
            externalLink = snapshot.child("externalLink").getValue(String::class.java),
            pollOptions = if (pollOptions.isEmpty()) null else pollOptions,
            likesCount = snapshot.child("likesCount").getValue(Int::class.java) ?: 0,
            commentsCount = snapshot.child("commentsCount").getValue(Int::class.java) ?: 0,
            repostsCount = snapshot.child("repostsCount").getValue(Int::class.java) ?: 0,
            isLiked = snapshot.child("likes").child(currentUid).exists(),
            reportsCount = snapshot.child("reportsCount").getValue(Int::class.java) ?: 0,
            createdAt = java.util.Date(snapshot.child("createdAt").getValue(Long::class.java) ?: 0L)
        )
    }

    private fun formatTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60_000 -> "الآن"
            diff < 3_600_000 -> "${diff / 60_000}د"
            diff < 86_400_000 -> "${diff / 3_600_000}س"
            else -> "${diff / 86_400_000}ي"
        }
    }
}
