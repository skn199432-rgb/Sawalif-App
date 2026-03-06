package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawalif.ui.theme.GoldPrimary
import com.sawalif.ui.theme.SurfaceDark
import com.sawalif.ui.theme.TextGray

data class Notification(val icon: ImageVector, val iconColor: Color, val text: String, val time: String)

val notifications = listOf(
    Notification(Icons.Filled.Favorite, Color.Red, "أحمد السالم أعجب بسالفتك", "منذ 5 دقائق"),
    Notification(Icons.Filled.ModeComment, Color(0xFF4FC3F7), "سارة المطيري ردت على سالفتك", "منذ 15 دقيقة"),
    Notification(Icons.Filled.PersonAdd, GoldPrimary, "خالد العتيبي بدأ يتابعك", "منذ ساعة"),
    Notification(Icons.Filled.Favorite, Color.Red, "نورة الشمري أعجبت بسالفتك", "منذ ساعتين"),
    Notification(Icons.Filled.ModeComment, Color(0xFF4FC3F7), "فهد القحطاني ردّ على تعليقك", "منذ 3 ساعات"),
    Notification(Icons.Filled.Favorite, Color.Red, "محمد الغامدي أعجب بسالفتك", "أمس"),
    Notification(Icons.Filled.PersonAdd, GoldPrimary, "ريم العنزي بدأت تتابعك", "أمس"),
)

@Composable
fun NotificationsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "الإشعارات",
                color = GoldPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Divider(color = Color(0xFF1A1A2E), thickness = 1.dp)

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notif ->
                NotificationItem(notif)
            }
        }
    }
}

@Composable
fun NotificationItem(notif: Notification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(notif.iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(notif.icon, contentDescription = null, tint = notif.iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notif.text, color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(notif.time, color = TextGray, fontSize = 12.sp)
            }
        }
    }
}
