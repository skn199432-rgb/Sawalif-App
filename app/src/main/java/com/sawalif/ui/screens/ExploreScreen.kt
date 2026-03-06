package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawalif.ui.theme.GoldPrimary
import com.sawalif.ui.theme.SurfaceDark

data class Category(val name: String, val emoji: String, val count: String, val color: Color)

val categories = listOf(
    Category("سوالف عامة", "💬", "1.2K سالفة", Color(0xFF1A2A4A)),
    Category("فضفضة", "🌙", "856 سالفة", Color(0xFF2A1A3A)),
    Category("ضحك", "😂", "2.1K سالفة", Color(0xFF1A3A2A)),
    Category("نقاشات", "🔥", "743 سالفة", Color(0xFF3A2A1A)),
    Category("رياضة", "⚽", "1.5K سالفة", Color(0xFF1A3A3A)),
    Category("تقنية", "💻", "934 سالفة", Color(0xFF2A2A1A)),
    Category("سفر", "✈️", "621 سالفة", Color(0xFF1A2A3A)),
    Category("طعام", "🍕", "1.1K سالفة", Color(0xFF3A1A2A)),
)

@Composable
fun ExploreScreen() {
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
                text = "استكشاف",
                color = GoldPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Divider(color = Color(0xFF1A1A2E), thickness = 1.dp)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                CategoryCard(category = category)
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = category.color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(category.emoji, fontSize = 32.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                category.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Text(
                category.count,
                color = Color(0xAAFFFFFF),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
