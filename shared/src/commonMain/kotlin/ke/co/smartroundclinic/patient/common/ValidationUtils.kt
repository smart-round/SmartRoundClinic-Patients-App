package ke.co.smartroundclinic.patient.common

fun String.isValidEmail(): Boolean =
    contains("@") && substringAfter("@").contains(".") && length > 5

// ── Password policy ──────────────────────────────────────────────────────────
// Applied wherever a patient chooses a password: sign-up, the signed-out forgot
// password reset, and the in-app change under Profile. Kept identical to the doctor
// app's policy so a household using both apps meets one standard.
//
// This is a client-side guard for guidance, not enforcement — the API accepts whatever
// it is sent, so a weak password is still possible outside these screens.

const val MIN_PASSWORD_LENGTH = 8

/** One requirement and whether the candidate password currently satisfies it. */
data class PasswordRule(val label: String, val isMet: Boolean)

/** Every rule, in display order, evaluated against this string. */
fun String.passwordRules(): List<PasswordRule> = listOf(
    PasswordRule("Must be $MIN_PASSWORD_LENGTH characters long", length >= MIN_PASSWORD_LENGTH),
    PasswordRule("Must have an uppercase letter", any { it.isUpperCase() }),
    PasswordRule("Must have a lowercase letter", any { it.isLowerCase() }),
    PasswordRule("Must have a number", any { it.isDigit() }),
    PasswordRule("Must have a special symbol", any { !it.isLetterOrDigit() && !it.isWhitespace() }),
)

fun String.isValidPassword(): Boolean = passwordRules().all { it.isMet }

/** First unmet requirement, for screens that show a single inline error, or null once it passes. */
fun String.passwordErrorOrNull(): String? = passwordRules().firstOrNull { !it.isMet }?.label
