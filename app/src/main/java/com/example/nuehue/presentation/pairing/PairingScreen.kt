package com.example.nuehue.presentation.pairing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nuehue.R
import com.example.nuehue.data.api.model.HueBridge
import com.example.nuehue.domain.model.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    viewModel: PairingViewModel = hiltViewModel()
) {
    val discoveryState by viewModel.discoveryState.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val pairingState by viewModel.pairingState.collectAsState()
    val pairingProgress by viewModel.pairingProgress.collectAsState()
    val credentialsState by viewModel.credentialsState.collectAsState()
    val manualIpInput by viewModel.manualIpInput.collectAsState()

    var selectedBridge by remember { mutableStateOf<HueBridge?>(null) }
    var showManualIpInput by remember { mutableStateOf(false) }
    var ipText by remember { mutableStateOf(TextFieldValue()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Discovery Section
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Discover Hue Bridge",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.discoverBridges()
                                    showManualIpInput = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Discover (mDNS)")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.discoverBridges(DiscoverBridgeUseCase.DiscoveryMethod.CLOUD)
                                    showManualIpInput = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cloud API")
                            }
                        }

                        Button(
                            onClick = { showManualIpInput = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.manual_ip_entry))
                        }
                    }
                }
            }

            // Discovery Results
            discoveryState?.let { state ->
                item {
                    when (state) {
                        is Resource.Loading -> {
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(state.message ?: stringResource(R.string.scanning_network))
                                }
                            }
                        }
                        is Resource.Success -> {
                            if (state.data.isEmpty()) {
                                Card {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(stringResource(R.string.no_bridges_found))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = { showManualIpInput = true }) {
                                            Text("Enter IP Manually")
                                        }
                                    }
                                }
                            } else {
                                Card {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Found ${state.data.size} bridge(s)",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        state.data.forEach { bridge ->
                                            BridgeItem(
                                                bridge = bridge,
                                                selected = selectedBridge == bridge,
                                                onSelect = {
                                                    selectedBridge = it
                                                    viewModel.validateConnection(it.ip)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.error_occurred),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(state.message)
                                    Button(
                                        onClick = { showManualIpInput = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Enter IP Manually")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Manual IP Input
            if (showManualIpInput) {
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.enter_bridge_ip),
                                style = MaterialTheme.typography.titleMedium
                            )
                            OutlinedTextField(
                                value = ipText,
                                onValueChange = {
                                    ipText = it
                                    viewModel.updateManualIp(it.text)
                                },
                                label = { Text("IP Address") },
                                placeholder = { Text("192.168.1.100") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (ipText.text.isNotBlank()) {
                                        selectedBridge = HueBridge(ip = ipText.text)
                                        viewModel.validateConnection(ipText.text)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = ipText.text.isNotBlank()
                            ) {
                                Text(stringResource(R.string.connect))
                            }
                        }
                    }
                }
            }

            // Validation State
            validationState?.let { state ->
                item {
                    when (state) {
                        is Resource.Loading -> {
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(stringResource(R.string.validating_connection))
                                }
                            }
                        }
                        is Resource.Success -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.connection_successful),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Bridge IP: ${selectedBridge?.ip}")
                                    Button(
                                        onClick = {
                                            selectedBridge?.let { bridge ->
                                                viewModel.pairWithBridge(bridge.ip)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Start Pairing")
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.connection_failed),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(state.message)
                                    Button(
                                        onClick = { viewModel.resetValidationState() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(R.string.retry))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pairing State
            pairingState?.let { state ->
                item {
                    when (state) {
                        is Resource.Loading -> {
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator()
                                    Text(stringResource(R.string.press_link_button))
                                    if (pairingProgress.isNotBlank()) {
                                        Text(pairingProgress)
                                    }
                                    Button(
                                        onClick = { viewModel.resetPairingState() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text(stringResource(R.string.cancel))
                                    }
                                }
                            }
                        }
                        is Resource.Success -> {
                            val username = state.data.success?.username
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.successfully_paired),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Bridge IP: ${selectedBridge?.ip}")
                                    if (username != null) {
                                        Text("Username: ${username.take(8)}...")
                                    }
                                    Button(
                                        onClick = {
                                            selectedBridge?.let { bridge ->
                                                if (username != null) {
                                                    viewModel.saveCredentials(
                                                        com.example.nuehue.data.api.model.HueCredentials(
                                                            bridgeIp = bridge.ip,
                                                            username = username
                                                        )
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Save Credentials")
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.error_occurred),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(state.message)
                                    Button(
                                        onClick = {
                                            selectedBridge?.let { bridge ->
                                                viewModel.pairWithBridge(bridge.ip)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(R.string.retry))
                                    }
                                    Button(
                                        onClick = { viewModel.resetPairingState() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(R.string.cancel))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Credentials State
            credentialsState?.let { state ->
                item {
                    when (state) {
                        is Resource.Success -> {
                            if (state.data != null) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Credentials Saved!",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        Text("Bridge IP: ${state.data.bridgeIp}")
                                        Text("You can now control your lights.")
                                        Button(
                                            onClick = { viewModel.clearCredentials() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(stringResource(R.string.reset))
                                        }
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Failed to save credentials",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(state.message)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun BridgeItem(
    bridge: HueBridge,
    selected: Boolean,
    onSelect: (HueBridge) -> Unit
) {
    Card(
        onClick = { onSelect(bridge) },
        colors = if (selected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = bridge.name ?: "Hue Bridge",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = bridge.ip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
