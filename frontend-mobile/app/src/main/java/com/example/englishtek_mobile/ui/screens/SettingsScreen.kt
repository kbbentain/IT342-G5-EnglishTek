package com.example.englishtek_mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.englishtek_mobile.R
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.auth.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    tokenManager: TokenManager,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onAbout: () -> Unit,
    apiClient: ApiClient? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var soundEnabled by remember { mutableStateOf(true) }
    var isDownloading by remember { mutableStateOf(false) }
    var doNothingClicks by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val funnyMessages = listOf(
        "Yep, still doing nothing...",
        "This button is as productive as a sloth on vacation",
        "Achievement Unlocked: Clicked a Useless Button!",
        "Poof! Nothing happened, as promised",
        "Plot twist: This button actually... does nothing",
        "Welcome to the Circus of Non-Functionality",
        "Congratulations! You've discovered our most useless feature",
        "Rolling dice... Result: Still nothing!",
        "Painting masterpiece... with invisible paint",
        "Loading next level... Just kidding!"
    )
    
    // Background pattern
    val patternPainter = painterResource(id = R.drawable.bg_pattern)
    
    // Show delete account confirmation dialog if needed
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { 
                Text("Warning: This action cannot be undone. All your data will be permanently deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        // Delete account
                        scope.launch {
                            try {
                                val response = apiClient?.apiService?.deleteUser()
                                if (response?.isSuccessful == true) {
                                    tokenManager.clearToken()
                                    Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3498DB)
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background with pattern and gradient overlay
            Box(modifier = Modifier.fillMaxSize()) {
                // Pattern background
                Image(
                    painter = patternPainter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.05f
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Settings",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Customize your app experience and manage your account settings.",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            lineHeight = 20.sp
                        )
                    }
                }
                
                // Account settings section
                SettingsSection("Account Settings") {
                    SettingItem(
                        icon = Icons.Default.Person,
                        label = "Edit Profile",
                        onClick = onEditProfile
                    )
                    
                    SettingItem(
                        icon = Icons.Default.Lock,
                        label = "Change Password",
                        onClick = onChangePassword
                    )
                    
                    SettingItem(
                        icon = Icons.Default.Download,
                        label = "Download User Report",
                        onClick = {
                            // Download report functionality
                            Toast.makeText(context, "Downloading report...", Toast.LENGTH_SHORT).show()
                        },
                        rightComponent = if (isDownloading) {
                            { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF999999)) }
                        } else null
                    )
                    
                    // Delete Account Button - Red warning style
                    SettingItem(
                        icon = Icons.Default.Delete,
                        label = "Delete Account",
                        onClick = { showDeleteDialog = true },
                        textColor = Color(0xFFFF6B6B)
                    )
                }
                
                // App settings section
                SettingsSection("App Settings") {
                    SettingItem(
                        icon = Icons.Default.VolumeUp,
                        label = "Sound Effects",
                        onClick = { },
                        rightComponent = {
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { soundEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4ECDC4),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFF767577)
                                )
                            )
                        }
                    )
                }
                
                // Extra stuff section
                SettingsSection("Extra Stuff") {
                    SettingItem(
                        icon = Icons.Outlined.Help,
                        label = "About EnglishTek",
                        onClick = onAbout
                    )
                    
                    SettingItem(
                        icon = Icons.Outlined.Info,
                        label = "I do nothing ",
                        onClick = {
                            val message = funnyMessages[doNothingClicks % funnyMessages.size]
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            doNothingClicks++
                        }
                    )
                }
                
                // Logout button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            scope.launch {
                                tokenManager.clearToken()
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFE8E8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFFF6B6B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Logout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF6B6B)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 8.dp, start = 16.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    rightComponent: (@Composable () -> Unit)? = null,
    textColor: Color = Color(0xFF333333)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF3498DB)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = label,
                fontSize = 16.sp,
                color = textColor
            )
        }
        
        rightComponent?.invoke()
    }
    
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF0F0F0),
        thickness = 1.dp
    )
}
