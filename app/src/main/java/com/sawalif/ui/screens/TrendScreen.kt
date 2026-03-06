package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawalif.ui.theme.GoldPrimary
import com.sawalif.ui.theme.SurfaceDark
import com.sawalif.ui.theme.TextGray

val trendPosts = listOf(
    Post(1, "فهد القحطاني", "@fahad_q", "ما أجمل الفجر في رمضان، سكون وهدوء لا يوصف ✨", 315, 52, "منذ ساعتين"),
    Post(2, "نورة الشمري", "@noura_sh", "الكتاب الجديد لـ رواية 'الأرض الطيبة' ممتاز جداً، أنهيته في يوم واحد 📚", 203, 37, "منذ ساعة"),
    Post(3, "أحمد السالم", "@ahmed_s", "الصبر مفتاح الفرج، وكل شيء له وقته المناسب 🌙", 142, 23, "منذ 5 دقائق"),
    Post(4, "سارة المطيري", "@sara_m", "اليوم جربت مطعم جديد وكان رهيب! أنصح الكل يجربه 🍕", 89, 15, "منذ 12 دقيقة"),
    Post(5, "خالد العتيبي", "@khalid_a", "من يعرف مكان جيد للكامبينق في المنطقة الشرقية؟ 🏕️", 67, 41, "منذ 30 دقيقة"),
)

@Composable
fun TrendScreen() {
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
                text = "الترند 🔥",
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
            itemsIndexed(trendPosts) { index, post ->
                TrendPostCard(post = post, rank = index + 1)
            }
        }
    }
}

@Composable
fun TrendPostCard(post: Post, rank: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#$rank",
                color = GoldPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(post.username, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(post.content, color = Color(0xFFCCCCCC), fontSize = 14.sp, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("❤️ ${post.likes}", color = TextGray, fontSize = 12.sp)
                    Text("💬 ${post.replies}", color = TextGray, fontSize = 12.sp)
                }
            }
        }
    }
}
