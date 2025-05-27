package com.example.englishtek_mobile.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.englishtek_mobile.R
import com.example.englishtek_mobile.data.api.ApiClient
import com.example.englishtek_mobile.data.model.User
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

// Define colors to match React Native version
private object COLORS {
    val primary = Color(0xFF4ECDC4)
    val secondary = Color(0xFF4CD964)
    val background = Color(0xFFF8F9FA)
    val inputBg = Color(0xFFFFFFFF)
    val text = Color(0xFF333333)
    val textLight = Color(0xFF666666)
    val border = Color(0xFFE0E0E0)
    val error = Color(0xFFFF6B6B)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    apiClient: ApiClient,
    user: User?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Calculate avatar size based on screen width (20% of screen width like in React Native)
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val avatarSize = (screenWidth * 0.2f).dp
    
    // State variables
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var newAvatarFile by remember { mutableStateOf<File?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoadingProfile by remember { mutableStateOf(user == null) }
    var currentUser by remember { mutableStateOf(user) }
    var selectedPresetAvatar by remember { mutableStateOf<Int?>(null) }
    var showPresetDialog by remember { mutableStateOf(false) }
    
    // List of preset avatars
    val presetAvatars = listOf(
        R.drawable.character1,
        R.drawable.character2,
        R.drawable.character3,
        R.drawable.character4
    )
    
    // Fetch user profile if not provided
    LaunchedEffect(key1 = Unit) {
        if (user == null) {
            isLoadingProfile = true
            try {
                val response = apiClient.apiService.getUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    currentUser = response.body()
                    name = currentUser?.name ?: ""
                    email = currentUser?.email ?: ""
                    bio = currentUser?.bio ?: ""
                } else {
                    error = "Failed to load profile data"
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            } finally {
                isLoadingProfile = false
            }
        }
    }
    
    // Background pattern
    val patternPainter = painterResource(id = R.drawable.bg_pattern)
    
    // Check for unsaved changes
    val hasUnsavedChanges = remember(name, email, bio, avatarUri) {
        name != (currentUser?.name ?: "") ||
        email != (currentUser?.email ?: "") ||
        bio != (currentUser?.bio ?: "") ||
        avatarUri != null
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            avatarUri = it
            newAvatarFile = uriToFile(context, it)
        }
    }
    
    // Handle back navigation with unsaved changes check
    var showUnsavedDialog by remember { mutableStateOf(false) }
    
    // Handle image picking
    val handleImagePick = {
        imagePickerLauncher.launch("image/*")
    }
    
    // Handle save profile
    val handleSave: () -> Unit = {
        if (name.trim().isEmpty() || email.trim().isEmpty()) {
            error = "Please fill in your name and email!"
        } else {
            scope.launch {
                loading = true
                error = null
                
                try {
                    // Prepare multipart form data
                    val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                    val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
                    val bioBody = bio.toRequestBody("text/plain".toMediaTypeOrNull())
                    
                    // Prepare avatar part if selected
                    val avatarPart = newAvatarFile?.let {
                        MultipartBody.Part.createFormData(
                            name = "avatar",
                            filename = it.name,
                            body = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        )
                    }
                    
                    // Prepare preset avatar if selected
                    val presetAvatarBody = selectedPresetAvatar?.let {
                        val presetName = when (it) {
                            R.drawable.character1 -> "character1"
                            R.drawable.character2 -> "character2"
                            R.drawable.character3 -> "character3"
                            R.drawable.character4 -> "character4"
                            else -> null
                        }
                        presetName?.toRequestBody("text/plain".toMediaTypeOrNull())
                    }
                    
                    // Make API call
                    val response = apiClient.apiService.updateProfile(
                        nameBody, emailBody, bioBody, avatarPart, presetAvatarBody
                    )
                    
                    if (response.isSuccessful) {
                        // Show success message
                        Toast.makeText(
                            context,
                            "Yay! \ud83c\udf89 Your profile has been updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onNavigateBack()
                    } else {
                        error = response.errorBody()?.string() ?: "Failed to update profile."
                    }
                } catch (e: Exception) {
                    error = "Oops! Something went wrong. Please try again!"
                } finally {
                    loading = false
                }
            }
        }
    }
    
    // Show preset avatar selection dialog
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = { Text("Choose Avatar") },
            text = {
                Column {
                    Text(
                        "Select a preset avatar or upload your own image.",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        presetAvatars.forEach { avatarRes ->
                            val isSelected = selectedPresetAvatar == avatarRes
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) COLORS.primary else COLORS.border,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            selectedPresetAvatar = avatarRes
                                            avatarUri = null
                                            newAvatarFile = null
                                        }
                                ) {
                                    Image(
                                        painter = painterResource(id = avatarRes),
                                        contentDescription = "Preset Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                
                                if (isSelected) {
                                    Text(
                                        "Selected",
                                        color = COLORS.primary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { handleImagePick() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = COLORS.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Upload Photo",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Photo")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text("Done", color = COLORS.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedPresetAvatar = null
                    showPresetDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure you want to go back?") },
            confirmButton = {
                TextButton(onClick = { 
                    showUnsavedDialog = false
                    onNavigateBack() 
                }) {
                    Text("Go Back", color = COLORS.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Stay", color = COLORS.primary)
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (hasUnsavedChanges) {
                            showUnsavedDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = COLORS.primary
                )
            )
        },
        containerColor = COLORS.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background with pattern
            Image(
                painter = patternPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.05f
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar section
                Box(
                    modifier = Modifier
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Avatar image
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(COLORS.border)
                            .clickable { showPresetDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        // Display avatar image
                        if (avatarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(avatarUri),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (selectedPresetAvatar != null) {
                            Image(
                                painter = painterResource(id = selectedPresetAvatar!!),
                                contentDescription = "Preset Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (!currentUser?.avatarUrl.isNullOrEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(currentUser?.avatarUrl),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Placeholder image
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(avatarSize * 0.5f)
                                    .align(Alignment.Center),
                                tint = Color.White
                            )
                        }
                    }
                    
                    // Camera icon
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(COLORS.primary)
                            .border(2.dp, COLORS.background, CircleShape)
                            .align(Alignment.BottomEnd)
                            .clickable { handleImagePick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Change Picture",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Error message
                if (error != null) {
                    Text(
                        text = error ?: "",
                        color = COLORS.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // Name input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Name",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = COLORS.text,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Your name", color = COLORS.textLight) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(COLORS.inputBg, RoundedCornerShape(8.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = COLORS.primary,
                            unfocusedBorderColor = COLORS.border,
                            focusedContainerColor = COLORS.inputBg,
                            unfocusedContainerColor = COLORS.inputBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Email input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = COLORS.text,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Your email", color = COLORS.textLight) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(COLORS.inputBg, RoundedCornerShape(8.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = COLORS.primary,
                            unfocusedBorderColor = COLORS.border,
                            focusedContainerColor = COLORS.inputBg,
                            unfocusedContainerColor = COLORS.inputBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Bio input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Bio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = COLORS.text,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { 
                            // Hard limit to 20 characters
                            if (it.length <= 20) {
                                bio = it
                            }
                        },
                        placeholder = { Text("Tell us about yourself", color = COLORS.textLight) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(COLORS.inputBg, RoundedCornerShape(8.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = COLORS.primary,
                            unfocusedBorderColor = COLORS.border,
                            focusedContainerColor = COLORS.inputBg,
                            unfocusedContainerColor = COLORS.inputBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4
                    )
                    
                    // Character counter
                    Text(
                        text = "${bio.length}/20",
                        fontSize = 12.sp,
                        color = COLORS.textLight,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Save button
                Button(
                    onClick = handleSave,
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = COLORS.primary,
                        disabledContainerColor = COLORS.primary.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Save Changes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// Utility: Convert Uri to File (for image upload)
fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("avatar", ".jpg", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        tempFile
    } catch (e: Exception) {
        null
    }
}
