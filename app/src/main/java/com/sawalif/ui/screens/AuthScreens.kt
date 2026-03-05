package com.sawalif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawalif.data.repository.FirebaseRepository
import com.sawalif.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // الشعار
            Text(
                text = "سوالف",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Gold
            )
            Text(
                text = "شارك سوالفك مع العالم",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // حقل البريد الإلكتروني
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("البريد الإلكتروني", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Gold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

            // حقل كلمة المرور
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("كلمة المرور", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Gold) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

            // رسالة الخطأ
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            // زر الدخول
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "يرجى ملء جميع الحقول"
                        return@Button
                    }
                    isLoading = true
                    FirebaseRepository.login(email.trim(), password) { success, error ->
                        isLoading = false
                        if (success) onLoginSuccess()
                        else errorMessage = error.ifEmpty { "بيانات الدخول غير صحيحة" }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(listOf(Gold, Purple)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(24.dp))
                    } else {
                        Text("تسجيل الدخول", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // رابط التسجيل
            TextButton(onClick = onNavigateToRegister) {
                Text("ليس لديك حساب؟ ", color = TextSecondary, fontSize = 14.sp)
                Text("سجّل الآن", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("إنشاء حساب", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gold)
            Text("انضم إلى مجتمع سوالف", fontSize = 14.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(8.dp))

            // الاسم
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("الاسم الكامل", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Gold) },
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

            // البريد الإلكتروني
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("البريد الإلكتروني", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Gold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

            // كلمة المرور
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("كلمة المرور", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Gold) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

            // تأكيد كلمة المرور
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("تأكيد كلمة المرور", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Gold) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = ErrorRed, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Button(
                onClick = {
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() ->
                            errorMessage = "يرجى ملء جميع الحقول"
                        name.trim().length < 3 ->
                            errorMessage = "الاسم يجب أن يكون 3 أحرف على الأقل"
                        password.length < 6 ->
                            errorMessage = "كلمة المرور يجب أن تكون 6 أحرف على الأقل"
                        password != confirmPassword ->
                            errorMessage = "كلمتا المرور غير متطابقتين"
                        else -> {
                            isLoading = true
                            FirebaseRepository.register(email.trim(), password, name.trim()) { success, error ->
                                isLoading = false
                                if (success) onRegisterSuccess()
                                else errorMessage = error.ifEmpty { "حدث خطأ أثناء إنشاء الحساب" }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(listOf(Gold, Purple)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(24.dp))
                    } else {
                        Text("إنشاء الحساب", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            TextButton(onClick = onNavigateToLogin) {
                Text("لديك حساب بالفعل؟ ", color = TextSecondary, fontSize = 14.sp)
                Text("سجّل دخولك", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
