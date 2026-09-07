package com.thapcyber.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Context
import org.json.JSONArray
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ScanResult(
    val url: String,
    val risk: String,
    val malicious: Int,
    val suspicious: Int,
    val harmless: Int,
    val undetected: Int
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ThapCyberApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThapCyberApp() {
    val context = LocalContext.current

    var url by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ScanResult?>(null) }
    var error by remember { mutableStateOf("") }

    var history by remember {
        mutableStateOf<List<ScanResult>>(emptyList())
    }


    fun checkUrl() {

        val cleanUrl = url.trim()

        if (cleanUrl.isEmpty()) {
            error = "Please enter a website URL."
            result = null
            return
        }

        if (!cleanUrl.startsWith("http://") &&
            !cleanUrl.startsWith("https://")) {
            error = "Enter a valid URL starting with http:// or https://"
            result = null
            return
        }

        loading = true
        error = ""
        result = null

        Thread {

            try {

                val connection = URL(
                    "https://thapcyber.onrender.com/check-url"
                ).openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 30000

                val json = JSONObject()
                json.put("url", cleanUrl)

                connection.outputStream.use { output ->
                    output.write(json.toString().toByteArray())
                }

                val responseCode = connection.responseCode

                if (responseCode !in 200..299) {
                    throw Exception("Server returned error $responseCode")
                }

                val response =
                    connection.inputStream.bufferedReader().use {
                        it.readText()
                    }

                val data = JSONObject(response)

                val scan = ScanResult(
                    url = data.optString("url", cleanUrl),
                    risk = data.optString("risk", "Unknown"),
                    malicious = data.optInt("malicious", 0),
                    suspicious = data.optInt("suspicious", 0),
                    harmless = data.optInt("harmless", 0),
                    undetected = data.optInt("undetected", 0)
                )

                Handler(Looper.getMainLooper()).post {

                    result = scan
                    loading = false

                    history = listOf(scan) +
                            history.filter {
                                it.url != scan.url
                            }.take(9)

                }

            } catch (e: Exception) {

                Handler(Looper.getMainLooper()).post {

                    loading = false
                    error =
                        "Unable to connect to ThapCyber server. Make sure the backend is running."
                }
            }

        }.start()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ThapCyber",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cybersecurity URL Scanner",
                            fontSize = 12.sp
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "Stay Safe Online",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Check a website before visiting it using real threat intelligence from VirusTotal."
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = url,
                            onValueChange = {
                                url = it
                                error = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Website URL")
                            },
                            placeholder = {
                                Text("https://example.com")
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { checkUrl() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading
                        ) {

                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(20.dp),
                                    strokeWidth = 2.dp
                                )

                                Spacer(
                                    modifier = Modifier.width(10.dp)
                                )

                                Text("Analyzing...")
                            } else {
                                Text("CHECK URL")
                            }
                        }
                    }
                }
            }

            if (error.isNotEmpty()) {

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            result?.let { scan ->

                item {

                    Text(
                        text = "Security Analysis",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {

                            Text(
                                text = scan.risk,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "VirusTotal analysis completed",
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = scan.url,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Divider()

                            Spacer(modifier = Modifier.height(14.dp))

                            ScanStat(
                                "Malicious",
                                scan.malicious
                            )

                            ScanStat(
                                "Suspicious",
                                scan.suspicious
                            )

                            ScanStat(
                                "Harmless",
                                scan.harmless
                            )

                            ScanStat(
                                "Undetected",
                                scan.undetected
                            )
                        }
                    }
                }
            }

            if (history.isNotEmpty()) {

                item {

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Recent Scans",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(history) { scan ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = scan.url,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                Text(
                                    text = scan.risk,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            Text(
                                text = "${scan.malicious} threats",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "About ThapCyber",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ThapCyber is a cybersecurity application designed to help users identify potentially malicious or suspicious website URLs before visiting them."
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Powered by real-time VirusTotal threat intelligence."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ScanStat(
    title: String,
    value: Int
) {



    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(text = title)

        Text(
            text = value.toString(),
            fontWeight = FontWeight.Bold
        )
    }
}
