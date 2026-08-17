package ke.co.smartroundclinic.patient.presentation.common.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ke.co.smartroundclinic.patient.common.passwordRules
import ke.co.smartroundclinic.patient.presentation.theme.StatusSuccess

/**
 * Live checklist of the password policy, shown under a password field. Each rule ticks green as it
 * is satisfied, so the requirements are visible while typing rather than only after a failed
 * submit.
 *
 * [visible] normally tracks focus or non-empty input — the list stays out of the way until the
 * field is actually in use.
 */
@Composable
fun PasswordRequirements(
    password: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        // Full width with an explicit Start alignment — the sign-up form centres its children, and
        // a wrap-content list would inherit that and sit in the middle of the screen.
        modifier = modifier.fillMaxWidth(),
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 6.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            password.passwordRules().forEach { rule ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (rule.isMet) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = if (rule.isMet) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = rule.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (rule.isMet) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
