package com.example.c001apk.compose.ui.component.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import com.example.c001apk.compose.ui.component.design.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.c001apk.compose.ui.component.design.contentCard
import androidx.compose.ui.unit.dp

/**
 * Created by bggRGjQaUbCoE on 2024/6/15
 */
@Composable
fun TextCard(
    modifier: Modifier = Modifier,
    text: String
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .contentCard(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
