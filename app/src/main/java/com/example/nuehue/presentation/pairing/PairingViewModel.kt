package com.example.nuehue.presentation.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuehue.data.api.model.HueAuthResponse
import com.example.nuehue.data.api.model.HueBridge
import com.example.nuehue.data.api.model.HueCredentials
import com.example.nuehue.domain.model.Resource
import com.example.nuehue.domain.usecase.ClearCredentialsUseCase
import com.example.nuehue.domain.usecase.DiscoverBridgeUseCase
import com.example.nuehue.domain.usecase.GetStoredCredentialsUseCase
import com.example.nuehue.domain.usecase.PairWithBridgeUseCase
import com.example.nuehue.domain.usecase.SaveCredentialsUseCase
import com.example.nuehue.domain.usecase.ValidateBridgeConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val discoverBridgeUseCase: DiscoverBridgeUseCase,
    private val validateBridgeConnectionUseCase: ValidateBridgeConnectionUseCase,
    private val pairWithBridgeUseCase: PairWithBridgeUseCase,
    private val saveCredentialsUseCase: SaveCredentialsUseCase,
    private val getStoredCredentialsUseCase: GetStoredCredentialsUseCase,
    private val clearCredentialsUseCase: ClearCredentialsUseCase
) : ViewModel() {

    private val _discoveryState = MutableStateFlow<Resource<List<HueBridge>>?>(null)
    val discoveryState: StateFlow<Resource<List<HueBridge>>?> = _discoveryState.asStateFlow()

    private val _validationState = MutableStateFlow<Resource<Boolean>?>(null)
    val validationState: StateFlow<Resource<Boolean>?> = _validationState.asStateFlow()

    private val _pairingState = MutableStateFlow<Resource<HueAuthResponse>?>(null)
    val pairingState: StateFlow<Resource<HueAuthResponse>?> = _pairingState.asStateFlow()

    private val _pairingProgress = MutableStateFlow("")
    val pairingProgress: StateFlow<String> = _pairingProgress.asStateFlow()

    private val _credentialsState = MutableStateFlow<Resource<HueCredentials>?>(null)
    val credentialsState: StateFlow<Resource<HueCredentials>?> = _credentialsState.asStateFlow()

    private val _manualIpInput = MutableStateFlow("")
    val manualIpInput: StateFlow<String> = _manualIpInput.asStateFlow()

    fun discoverBridges(method: DiscoverBridgeUseCase.DiscoveryMethod = DiscoverBridgeUseCase.DiscoveryMethod.MDNS) {
        viewModelScope.launch {
            _discoveryState.value = Resource.Loading("Scanning local network...")
            _discoveryState.value = discoverBridgeUseCase(method)
        }
    }

    fun updateManualIp(ip: String) {
        _manualIpInput.value = ip
    }

    fun validateConnection(ip: String) {
        viewModelScope.launch {
            _validationState.value = Resource.Loading("Validating connection...")
            _validationState.value = validateBridgeConnectionUseCase(ip)
        }
    }

    fun pairWithBridge(ip: String) {
        viewModelScope.launch {
            _pairingState.value = Resource.Loading("Waiting for link button...")
            _pairingState.value = pairWithBridgeUseCase(
                ip = ip,
                onProgress = { progress ->
                    _pairingProgress.value = progress
                }
            )
        }
    }

    fun saveCredentials(credentials: HueCredentials) {
        viewModelScope.launch {
            val result = saveCredentialsUseCase(credentials)
            if (result is Resource.Success) {
                _credentialsState.value = Resource.Success(credentials)
            } else {
                _credentialsState.value = result as Resource.Error
            }
        }
    }

    fun loadCredentials() {
        viewModelScope.launch {
            getStoredCredentialsUseCase().collect { resource ->
                _credentialsState.value = resource
            }
        }
    }

    fun clearCredentials() {
        viewModelScope.launch {
            _credentialsState.value = Resource.Loading("Clearing credentials...")
            _credentialsState.value = clearCredentialsUseCase()
        }
    }

    fun resetDiscoveryState() {
        _discoveryState.value = null
    }

    fun resetValidationState() {
        _validationState.value = null
    }

    fun resetPairingState() {
        _pairingState.value = null
        _pairingProgress.value = ""
    }
}
