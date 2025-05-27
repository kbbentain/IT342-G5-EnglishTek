package com.example.englishtek_mobile.ui.components

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin

/**
 * A composable that renders Markdown text using Markwon library
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    onLinkClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Build Markwon instance with plugins
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .build()
    }
    
    val textColor = if (color != Color.Unspecified) {
        color.toArgb()
    } else {
        MaterialTheme.colorScheme.onSurface.toArgb()
    }
    
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            TextView(ctx).apply {
                // Apply text styling
                setTextColor(textColor)
                
                if (fontSize != TextUnit.Unspecified) {
                    textSize = fontSize.value
                }
                if (textAlign != null) {
                    this.textAlignment = when (textAlign) {
                        TextAlign.Center -> TextView.TEXT_ALIGNMENT_CENTER
                        TextAlign.End -> TextView.TEXT_ALIGNMENT_VIEW_END
                        TextAlign.Start -> TextView.TEXT_ALIGNMENT_VIEW_START
                        else -> TextView.TEXT_ALIGNMENT_TEXT_START
                    }
                }
                if (lineHeight != TextUnit.Unspecified) {
                    setLineSpacing(lineHeight.value, 1f)
                }
            }
        },
        update = { textView ->
            // Render markdown
            markwon.setMarkdown(textView, markdown)
        }
    )
}
