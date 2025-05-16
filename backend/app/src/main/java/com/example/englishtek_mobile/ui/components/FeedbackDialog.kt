package com.example.englishtek_mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun FeedbackDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, feedbackKeyword: String, additionalFeedback: String) -> Unit
) {
    if (!visible) return
    
    var rating by remember { mutableStateOf(0) }
    var selectedKeyword by remember { mutableStateOf("") }
    var additionalFeedback by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val feedbackKeywords = listOf(
        "Engaging", "Informative", "Challenging", "Easy", "Difficult", 
        "Boring", "Confusing", "Clear", "Helpful", "Frustrating"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Chapter Feedback",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Error message if any
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                when (currentStep) {
                    0 -> {
                        // Rating step
                        Text(
                            text = "How would you rate this chapter?",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Rating slider
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (rating > 0) "$rating / 5" else "Select a rating",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Slider(
                                value = rating.toFloat(),
                                onValueChange = { rating = it.toInt() },
                                valueRange = 0f..5f,
                                steps = 4,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Poor", style = MaterialTheme.typography.bodySmall)
                                Text("Excellent", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    if (rating > 0) {
                                        currentStep = 1
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Please select a rating"
                                    }
                                }
                            ) {
                                Text("Next")
                            }
                        }
                    }
                    
                    1 -> {
                        // Keyword selection step
                        Text(
                            text = "Select one word that best describes this chapter:",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Keyword grid
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectableGroup()
                        ) {
                            for (i in feedbackKeywords.indices step 2) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    for (j in 0..1) {
                                        val index = i + j
                                        if (index < feedbackKeywords.size) {
                                            val keyword = feedbackKeywords[index]
                                            OutlinedButton(
                                                onClick = { selectedKeyword = keyword },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(4.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (selectedKeyword == keyword) 
                                                        MaterialTheme.colorScheme.primaryContainer 
                                                    else 
                                                        MaterialTheme.colorScheme.surface
                                                ),
                                                border = BorderStroke(
                                                    width = 1.dp,
                                                    color = if (selectedKeyword == keyword) 
                                                        MaterialTheme.colorScheme.primary 
                                                    else 
                                                        MaterialTheme.colorScheme.outline
                                                )
                                            ) {
                                                Text(keyword)
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { currentStep = 0 }) {
                                Text("Back")
                            }
                            
                            Button(
                                onClick = {
                                    if (selectedKeyword.isNotEmpty()) {
                                        currentStep = 2
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Please select a keyword"
                                    }
                                }
                            ) {
                                Text("Next")
                            }
                        }
                    }
                    
                    2 -> {
                        // Additional feedback step
                        Text(
                            text = "Any additional feedback? (Optional)",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = additionalFeedback,
                            onValueChange = { additionalFeedback = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Type your feedback here...") }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { currentStep = 1 }) {
                                Text("Back")
                            }
                            
                            Button(
                                onClick = {
                                    onSubmit(rating, selectedKeyword, additionalFeedback)
                                    onDismiss()
                                }
                            ) {
                                Text("Submit")
                            }
                        }
                    }
                }
            }
        }
    }
}
