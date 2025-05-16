package com.example.englishtek_mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.model.ChangePasswordRequest
import kotlinx.coroutines.launch

// Define colors to match React Native version
private val COLORS = object {
    val primary = Color(0xFF4ECDC4)
    val secondary = Color(0xFF4ECDC4)
    val background = Color(0xFFF8F9FA)
    val inputBg = Color(0xFFFFFFFF)
    val text = Color(0xFF333333)
    val textLight = Color(0xFF666666)
    val border = Color(0xFFE0E0E0)
    val error = Color(0xFFFF6B6B)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    apiClient: ApiClient,
    onNavigateBack: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val isLengthValid = newPassword.length >= 6
    val isMatch = newPassword == confirmPassword && newPassword.isNotEmpty()
    val isFormValid = currentPassword.length >= 6 && isLengthValid && isMatch

    fun resetFields() {
        currentPassword = ""
        newPassword = ""
        confirmPassword = ""
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false; onNavigateBack() },
            title = { Text("Password Updated! 🎉") },
            text = { Text("Your secret code has been changed successfully!") },
            confirmButton = {
                TextButton(onClick = { showSuccess = false; onNavigateBack() }) { 
                    Text("Awesome! 😊", color = COLORS.primary) 
                }
            }
        )
    }
    if (error != null) {
        AlertDialog(
            onDismissRequest = { error = null },
            title = { Text("Oops! 😕") },
            text = { Text(error!!) },
            confirmButton = {
                TextButton(onClick = { error = null }) { 
                    Text("Let me try again! 💪", color = COLORS.primary) 
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = COLORS.background)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Box(Modifier.fillMaxWidth().background(COLORS.background).padding(20.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                loading = true
                                error = null
                                try {
                                    val req = ChangePasswordRequest(currentPassword, newPassword)
                                    val response = apiClient.apiService.changePassword(req)
                                    if (response.isSuccessful) {
                                        resetFields()
                                        showSuccess = true
                                    } else {
                                        val errorMsg = response.errorBody()?.string() ?: "Failed to change password"
                                        error = errorMsg
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: "An unknown error occurred"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = isFormValid && !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = COLORS.primary,
                            disabledContainerColor = COLORS.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Update Password",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Filled.Check, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(COLORS.background)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon and title section
            Box(
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 16.dp)
                    .size(80.dp)
                    .background(COLORS.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = "Reset Password",
                    tint = COLORS.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Text(
                text = "Let's Update Your Password! 🔐",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = COLORS.text,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Make sure to choose a strong password that you'll remember!",
                fontSize = 16.sp,
                color = COLORS.textLight,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Password fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Current Password
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    placeholder = { Text("Enter your current password", color = COLORS.textLight) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = COLORS.textLight)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showCurrent = !showCurrent }) {
                            Icon(
                                imageVector = if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showCurrent) "Hide Password" else "Show Password",
                                tint = COLORS.textLight
                            )
                        }
                    },
                    visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = COLORS.primary,
                        unfocusedBorderColor = COLORS.border,
                        focusedContainerColor = COLORS.inputBg,
                        unfocusedContainerColor = COLORS.inputBg
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // New Password
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    placeholder = { Text("Create a new password", color = COLORS.textLight) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = COLORS.textLight)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(
                                imageVector = if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showNew) "Hide Password" else "Show Password",
                                tint = COLORS.textLight
                            )
                        }
                    },
                    visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = COLORS.primary,
                        unfocusedBorderColor = COLORS.border,
                        focusedContainerColor = COLORS.inputBg,
                        unfocusedContainerColor = COLORS.inputBg
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    placeholder = { Text("Confirm your new password", color = COLORS.textLight) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = COLORS.textLight)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                imageVector = if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirm) "Hide Password" else "Show Password",
                                tint = COLORS.textLight
                            )
                        }
                    },
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = COLORS.primary,
                        unfocusedBorderColor = COLORS.border,
                        focusedContainerColor = COLORS.inputBg,
                        unfocusedContainerColor = COLORS.inputBg
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Password Requirements
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = COLORS.inputBg),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Password Requirements:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = COLORS.text,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(
                                imageVector = if (isLengthValid) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isLengthValid) COLORS.secondary else COLORS.textLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "At least 6 characters long",
                                fontSize = 14.sp,
                                fontWeight = if (isLengthValid) FontWeight.Medium else FontWeight.Normal,
                                color = if (isLengthValid) COLORS.secondary else COLORS.textLight
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(
                                imageVector = if (isMatch) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isMatch) COLORS.secondary else COLORS.textLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Passwords match",
                                fontSize = 14.sp,
                                fontWeight = if (isMatch) FontWeight.Medium else FontWeight.Normal,
                                color = if (isMatch) COLORS.secondary else COLORS.textLight
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
