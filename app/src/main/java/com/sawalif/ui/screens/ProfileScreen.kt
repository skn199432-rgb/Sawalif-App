package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawalif.ui.theme.GoldPrimary
import com.sawalif.ui.theme.SurfaceDark
import com.sawalif.ui.theme.TextGray

val myPosts = listOf(
    Post(1, "أنت", "@you", "أهلاً بالجميع في سوالف! 👋", 42, 8, "منذ يوم"),
    Post(2, "أنت", "@you", "هل جربتم القهوة الإثيوبية؟ تجربة لا تُنسى ☕", 67, 12, "منذ يومين"),
    Post(3, "أنت", "@you", "الرياضة الصباحية تغير حياتك 💪", 89, 19, "منذ أسبوع"),
)

@Composable
fun ProfileScreen() {
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
                text = "حسابي",
                color = GoldPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Divider(color = Color(0xFF1A1A2E), thickness = 1.dp)

        LazyColumn {
            item {
                // Profile Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("أ", color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("المستخدم", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("@user", color = TextGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("3", "سوالف")
                        StatItem("128", "متابع")
                        StatItem("64", "يتابع")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تعديل الملف الشخصي", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                Divider(color = Color(0xFF1A1A2E), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(myPosts) { post ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    PostCard(post = post)
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextGray, fontSize = 13.sp)
    }
}
