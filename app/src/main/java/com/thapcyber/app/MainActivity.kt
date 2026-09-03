package com.thapcyber.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ThapCyberApp()
        }
    }
}

@Composable
fun ThapCyberApp() {

    var url by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var riskScore by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // App title
        Text(
            text = "🛡️ ThapCyber",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF29B6F6)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your Personal Cybersecurity Assistant",
            fontSize = 14.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Phishing URL Checker card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF18243A)
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Phishing URL Checker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Check a website URL for common phishing indicators before visiting it.",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // URL input
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        result = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Enter website URL")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Check URL button
                Button(
                    onClick = {

                        val cleanUrl = url.trim()
                        var score = 0

                        if (cleanUrl.isBlank()) {

                            result = "⚠️ Please enter a URL"
                            riskScore = 0

                        } else if (
                            !cleanUrl.startsWith("http://", ignoreCase = true) &&
                            !cleanUrl.startsWith("https://", ignoreCase = true)
                        ) {

                            result = "⚠️ Please enter a valid URL"
                            riskScore = 80

                        } else {

                            // URL length check
                            if (cleanUrl.length > 100) {
                                score += 15
                            }

                            // IP address check
                            if (
                                Regex(
                                    """https?://\d{1,3}(\.\d{1,3}){3}.*""",
                                    RegexOption.IGNORE_CASE
                                ).matches(cleanUrl)
                            ) {
                                score += 40
                            }

                            // @ symbol check
                            if (cleanUrl.contains("@")) {
                                score += 30
                            }

                            // Suspicious keyword check
                            val suspiciousWords = listOf(
                                "login",
                                "verify",
                                "account",
                                "password",
                                "secure",
                                "signin",
                                "bank",
                                "free"
                            )

                            if (
                                suspiciousWords.any {
                                    cleanUrl.contains(it, ignoreCase = true)
                                }
                            ) {
                                score += 25
                            }

                            // URL shortener check
                            val shortenerDomains = listOf(
                                "bit.ly",
                                "tinyurl.com",
                                "t.co",
                                "is.gd",
                                "ow.ly",
                                "shorturl.at"
                            )

                            if (
                                shortenerDomains.any {
                                    cleanUrl.contains(it, ignoreCase = true)
                                }
                            ) {
                                score += 20
                            }

                            // HTTP instead of HTTPS
                            if (cleanUrl.startsWith("http://", ignoreCase = true)) {
                                score += 20
                            }

                            // Limit score to 100
                            score = score.coerceAtMost(100)

                            riskScore = score
                            val reasons = mutableListOf<String>()

                            if (cleanUrl.length > 100) {
                                reasons.add("• URL is unusually long")
                            }

                            if (
                                Regex(
                                    """https?://\d{1,3}(\.\d{1,3}){3}.*""",
                                    RegexOption.IGNORE_CASE
                                ).containsMatchIn(cleanUrl)
                            ) {
                                reasons.add("• URL uses an IP address")
                            }

                            if (cleanUrl.contains("@")) {
                                reasons.add("• URL contains an @ symbol")
                            }

                            val foundSuspiciousWords = suspiciousWords.filter {
                                cleanUrl.contains(it, ignoreCase = true)
                            }

                            if (foundSuspiciousWords.isNotEmpty()) {
                                reasons.add(
                                    "• Suspicious keyword(s): ${foundSuspiciousWords.joinToString(", ")}"
                                )
                            }

                            if (
                                shortenerDomains.any {
                                    cleanUrl.contains(it, ignoreCase = true)
                                }
                            ) {
                                reasons.add("• URL uses a shortened link")
                            }

                            if (cleanUrl.startsWith("http://", ignoreCase = true)) {
                                reasons.add("• Uses HTTP instead of HTTPS")
                            }

                            if (reasons.isEmpty()) {
                                reasons.add("• No common phishing indicators detected")
                            }

                            result = when {
                                score >= 60 ->
                                    "🔴 High Risk URL\nRisk Score: $score/100"

                                score >= 30 ->
                                    "🟠 Medium Risk URL\nRisk Score: $score/100"

                                score > 0 ->
                                    "🟡 Low Risk URL\nRisk Score: $score/100"

                                else ->
                                    "🟢 URL looks safe\nRisk Score: 0/100"
                            } + "\n\nWhy?\n" + reasons.joinToString("\n")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check URL")
                }

                // Result
                if (result.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = result,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.startsWith("⚠️")) {
                            Color(0xFFFF5252)
                        } else {
                            Color(0xFF69F0AE)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "ThapCyber • Cybersecurity Project",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}