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
import androidx.compose.runtime.*
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

data class NotifItem(
    val id: Int,
    val username: String,
    val action: String,
    val time: String,
    val icon: ImageVector,
    val iconColor: Color
)

val notifications = listOf(
    NotifItem(1, "أحمد السالم", "أعجب بسالفتك", "منذ 5 دقائق", Icons.Filled.Favorite, Color.Red),
    NotifItem(2, "نورة الشمري", "علق على سالفتك", "منذ 15 دقيقة", Icons.Filled.ModeComment, Color(0xFF4A90D9)),
    NotifItem(3, "خالد العتيبي", "بدأ يتابعك", "منذ ساعة", Icons.Filled.PersonAdd, Color(0xFF2ECC71)),
    NotifItem(4, "سارة المطيري", "أعجب بسالفتك", "منذ ساعتين", Icons.Filled.Favorite, Color.Red),
    NotifItem(5, "فهد القحطاني", "علق على سالفتك", "منذ 3 ساعات", Icons.Filled.ModeComment, Color(0xFF4A90D9)),
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
                text = "الاشعارات",
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
                NotifCard(notif = notif)
            }
        }
    }
}

@Composable
fun NotifCard(notif: NotifItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notif.username.first().toString(),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notif.username,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = notif.action,
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = notif.icon,
                    contentDescription = null,
                    tint = notif.iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(notif.time, color = TextGray, fontSize = 11.sp)
            }
        }
    }
}
