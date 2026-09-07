package com.thapcyber.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ThapCyberApp()
        }
    }
}

@androidx.compose.runtime.Composable
fun ThapCyberApp() {

    var url by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var risk by remember { mutableStateOf("") }

    var malicious by remember { mutableStateOf(0) }
    var suspicious by remember { mutableStateOf(0) }
    var harmless by remember { mutableStateOf(0) }
    var undetected by remember { mutableStateOf(0) }

    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val backgroundColor = Color(0xFF071120)
    val cardColor = Color(0xFF182844)
    val purple = Color(0xFF7651C5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "🛡️ ThapCyber",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF29B6F6)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your Personal Cybersecurity Assistant",
            fontSize = 13.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
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
                    text = "Check a website URL using real VirusTotal threat intelligence before visiting it.",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Enter website URL")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {

                        if (url.isBlank()) {
                            errorMessage = "Please enter a URL"
                            result = ""
                            risk = ""
                            return@Button
                        }

                        loading = true
                        errorMessage = ""
                        result = ""
                        risk = ""

                        malicious = 0
                        suspicious = 0
                        harmless = 0
                        undetected = 0

                        Thread {

                            try {

                                val endpoint =
                                    URL("http://10.0.2.2:5001/check-url")

                                val connection =
                                    endpoint.openConnection() as HttpURLConnection

                                connection.requestMethod = "POST"
                                connection.connectTimeout = 10000
                                connection.readTimeout = 30000
                                connection.doOutput = true

                                connection.setRequestProperty(
                                    "Content-Type",
                                    "application/json"
                                )

                                val jsonRequest = JSONObject()
                                jsonRequest.put("url", url.trim())

                                connection.outputStream.use { output ->
                                    output.write(
                                        jsonRequest
                                            .toString()
                                            .toByteArray(Charsets.UTF_8)
                                    )
                                }

                                val responseCode = connection.responseCode

                                val responseText =
                                    if (responseCode in 200..299) {
                                        connection.inputStream
                                            .bufferedReader()
                                            .use { it.readText() }
                                    } else {
                                        connection.errorStream
                                            ?.bufferedReader()
                                            ?.use { it.readText() }
                                            ?: ""
                                    }

                                connection.disconnect()

                                val jsonResponse =
                                    if (responseText.isNotBlank()) {
                                        JSONObject(responseText)
                                    } else {
                                        JSONObject()
                                    }

                                if (responseCode in 200..299) {

                                    val returnedRisk =
                                        jsonResponse.optString(
                                            "risk",
                                            "Unknown"
                                        )

                                    val returnedMessage =
                                        jsonResponse.optString(
                                            "message",
                                            "Analysis completed"
                                        )

                                    val returnedMalicious =
                                        jsonResponse.optInt(
                                            "malicious",
                                            0
                                        )

                                    val returnedSuspicious =
                                        jsonResponse.optInt(
                                            "suspicious",
                                            0
                                        )

                                    val returnedHarmless =
                                        jsonResponse.optInt(
                                            "harmless",
                                            0
                                        )

                                    val returnedUndetected =
                                        jsonResponse.optInt(
                                            "undetected",
                                            0
                                        )

                                    Handler(Looper.getMainLooper()).post {

                                        risk = returnedRisk
                                        result = returnedMessage

                                        malicious =
                                            returnedMalicious

                                        suspicious =
                                            returnedSuspicious

                                        harmless =
                                            returnedHarmless

                                        undetected =
                                            returnedUndetected

                                        loading = false
                                    }

                                } else {

                                    val serverError =
                                        jsonResponse.optString(
                                            "error",
                                            "Server error"
                                        )

                                    Handler(Looper.getMainLooper()).post {

                                        errorMessage =
                                            "$serverError (HTTP $responseCode)"

                                        loading = false
                                    }
                                }

                            } catch (e: Exception) {

                                Handler(Looper.getMainLooper()).post {

                                    errorMessage =
                                        "Unable to connect to ThapCyber server"

                                    loading = false
                                }
                            }

                        }.start()
                    },

                    enabled = !loading,

                    modifier = Modifier.fillMaxWidth(),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = purple
                    )
                ) {

                    if (loading) {

                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )

                    } else {

                        Text(
                            text = "Check URL",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {

                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF5252),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (risk.isNotEmpty()) {

                    val riskColor = when {
                        risk.contains("High", ignoreCase = true) ->
                            Color(0xFFFF5252)

                        risk.contains("Medium", ignoreCase = true) ->
                            Color(0xFFFFC107)

                        risk.contains("Low", ignoreCase = true) ->
                            Color(0xFF69F0AE)

                        else ->
                            Color.LightGray
                    }

                    Text(
                        text = "🟢 $risk",
                        color = riskColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = result,
                        color = Color.White,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "VirusTotal Analysis",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AnalysisRow(
                        label = "Malicious",
                        value = malicious
                    )

                    AnalysisRow(
                        label = "Suspicious",
                        value = suspicious
                    )

                    AnalysisRow(
                        label = "Harmless",
                        value = harmless
                    )

                    AnalysisRow(
                        label = "Undetected",
                        value = undetected
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ThapCyber • Cybersecurity Project",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@androidx.compose.runtime.Composable
fun AnalysisRow(
    label: String,
    value: Int
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 13.sp
        )

        Text(
            text = value.toString(),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}