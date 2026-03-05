package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.sawalif.data.models.PostType
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(navController: NavController) {
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PostType.TEXT) }
    var externalLink by remember { mutableStateOf("") }
    var pollOptions by remember { mutableStateOf(mutableListOf("", "")) }
    var isPosting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val maxLength = 500

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("سالفة جديدة", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextPrimary)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            errorMessage = ""
                            when {
                                content.isBlank() -> errorMessage = "اكتب شيئاً أولاً"
                                content.length > maxLength -> errorMessage = "تجاوزت الحد الأقصى"
                                selectedType == PostType.VIDEO && externalLink.isBlank() ->
                                    errorMessage = "أضف رابط الفيديو"
                                selectedType == PostType.POLL && pollOptions.count { it.isNotBlank() } < 2 ->
                                    errorMessage = "أضف خيارين على الأقل"
                                else -> {
                                    isPosting = true
                                    val validPollOptions = if (selectedType == PostType.POLL)
                                        pollOptions.filter { it.isNotBlank() }
                                    else emptyList()

                                    FirebaseRepository.addPost(
                                        content = content.trim(),
                                        type = selectedType,
                                        externalLink = if (selectedType == PostType.VIDEO) externalLink.trim() else null,
                                        pollOptions = validPollOptions
                                    ) { success ->
                                        isPosting = false
                                        if (success) navController.popBackStack()
                                        else errorMessage = "حدث خطأ، حاول مجدداً"
                                    }
                                }
                            }
                        },
                        enabled = content.isNotBlank() && !isPosting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gold,
                            disabledContainerColor = Gold.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("نشر", color = DarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = CardBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // حقل النص الرئيسي
            OutlinedTextField(
                value = content,
                onValueChange = { if (it.length <= maxLength) content = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                placeholder = { Text("شاركنا سالفتك...", color = TextDim, fontSize = 16.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Gold
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // عداد الأحرف
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "${content.length}/$maxLength",
                    color = if (content.length > maxLength * 0.9) ErrorRed else TextDim,
                    fontSize = 12.sp
                )
            }

            // اختيار نوع المنشور
            Text("نوع السالفة", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PostTypeChip(
                    icon = Icons.Default.TextFields,
                    label = "نص",
                    isSelected = selectedType == PostType.TEXT,
                    onClick = { selectedType = PostType.TEXT }
                )
                PostTypeChip(
                    icon = Icons.Default.VideoLibrary,
                    label = "فيديو",
                    isSelected = selectedType == PostType.VIDEO,
                    onClick = { selectedType = PostType.VIDEO }
                )
                PostTypeChip(
                    icon = Icons.Default.Poll,
                    label = "تصويت",
                    isSelected = selectedType == PostType.POLL,
                    onClick = { selectedType = PostType.POLL }
                )
            }

            // حقل رابط الفيديو
            if (selectedType == PostType.VIDEO) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("رابط الفيديو", color = TextSecondary, fontSize = 14.sp)
                    OutlinedTextField(
                        value = externalLink,
                        onValueChange = { externalLink = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("رابط يوتيوب أو تيك توك...", color = TextDim) },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Gold) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = Gold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    // تلميح
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextDim, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("يدعم روابط يوتيوب وتيك توك", color = TextDim, fontSize = 12.sp)
                    }
                }
            }

            // خيارات التصويت
            if (selectedType == PostType.POLL) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("خيارات التصويت", color = TextSecondary, fontSize = 14.sp)
                    pollOptions.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = option,
                                onValueChange = { newValue ->
                                    pollOptions = pollOptions.toMutableList().also { it[index] = newValue }
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("الخيار ${index + 1}", color = TextDim) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
                                    unfocusedBorderColor = BorderColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = Gold
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (pollOptions.size > 2) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    pollOptions = pollOptions.toMutableList().also { it.removeAt(index) }
                                }) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "حذف", tint = ErrorRed)
                                }
                            }
                        }
                    }
                    if (pollOptions.size < 5) {
                        TextButton(
                            onClick = { pollOptions = pollOptions.toMutableList().also { it.add("") } }
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة خيار", color = Gold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // رسالة الخطأ
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun PostTypeChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = if (isSelected) Purple.copy(alpha = 0.3f) else CardBg,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Gold else BorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Gold else TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            color = if (isSelected) Gold else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
