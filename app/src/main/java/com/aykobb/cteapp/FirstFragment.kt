package com.aykobb.cteapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aykobb.cteapp.databinding.FragmentFirstBinding
import com.azure.communication.identity.CommunicationIdentityClient
import com.azure.communication.identity.CommunicationIdentityClientBuilder
import com.azure.communication.identity.models.GetTokenForTeamsUserOptions
import com.microsoft.identity.client.*
import com.microsoft.identity.client.IPublicClientApplication.LoadAccountsCallback
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val TAG = "com.aykobb.cteapp.FirstFragment"
    private var _binding: FragmentFirstBinding? = null

    private var mMultipleAccountApp: IMultipleAccountPublicClientApplication? = null
    private var acsClient: CommunicationIdentityClient? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        initPublicClientApplication()
        initCommunicationIdentityClient()
        return binding.root
    }

    private fun initPublicClientApplication() {
        // Creates a PublicClientApplication object with res/raw/auth_config_single_account.json
        PublicClientApplication.createMultipleAccountPublicClientApplication(
            requireContext(),
            R.raw.auth_config,
            object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                    mMultipleAccountApp = application
                }

                override fun onError(exception: MsalException) {
                    Log.d(TAG, "IMultipleAccountPublicClientApplication init is failed: ${exception.message} ")
                }
            })
    }

    private fun initCommunicationIdentityClient() {
        var connectionString = "<paste your connection string here>"
        acsClient = CommunicationIdentityClientBuilder()
            .connectionString(connectionString)
            .buildClient()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            // feel free to extend the scopes
            // but ensure that the scopes you provide are for the same resources
            val scopes = listOf<String>("https://graph.microsoft.com/User.Read")
            mMultipleAccountApp?.acquireTokenWithDeviceCode(scopes, getAuthDeviceCodeCallback())
        }
    }

    /**
     * Callback used in for acquire token by device code flow.
     */
    private fun getAuthDeviceCodeCallback(): IPublicClientApplication.DeviceCodeFlowCallback {
        return object : IPublicClientApplication.DeviceCodeFlowCallback {
            override fun onUserCodeReceived(
                vUri: String,
                userCode: String,
                message: String,
                sessionExpirationDate: Date
            ) {
                Log.d(TAG, "onUserCodeReceived: ${vUri} ${userCode} ${message} ${sessionExpirationDate}")
            }

            override fun onTokenReceived(authResult: IAuthenticationResult) {
                Log.d(TAG, "Successfully authenticated, Teams User token: ${authResult.accessToken}")
                acquireTeamsTokenSilentlyForAcs()
            }


            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                Log.d(TAG,"Authentication failed: $exception")
            }
        }
    }

    private fun acquireTeamsTokenSilentlyForAcs(){
        mMultipleAccountApp!!.getAccounts(object : LoadAccountsCallback {
            override fun onTaskCompleted(result: List<IAccount>) {
                // You can use the account data to update your UI or your app database.
                Log.d(TAG,"getAccounts succeed: $result")

                val scopes = listOf<String>(
                    "https://auth.msft.communication.azure.com/Teams.ManageCalls",
                    "https://auth.msft.communication.azure.com/Teams.ManageChats"
                )
                var params = AcquireTokenSilentParameters.Builder()
                    .forAccount(result[0])
                    .fromAuthority("https://login.microsoftonline.com/common")
                    .withScopes(scopes)
                    .withCallback(getAuthSilentCallback())
                    .forceRefresh(true)
                    .build()

                mMultipleAccountApp?.acquireTokenSilentAsync(params)
            }

            override fun onError(exception: MsalException) {
                Log.d(TAG,"getAccounts failed: $exception")
            }
        })
    }

    /**
     * Callback used in for silent acquireToken calls.
     */
    private fun getAuthSilentCallback(): SilentAuthenticationCallback? {
        return object : SilentAuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                Log.d(TAG,"getAuthSilentCallback: Successfully authenticated")
                exchangeTeamsTokenToAcsToken(authenticationResult)
            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                Log.d(TAG,"Authentication failed: $exception")
                if (exception is MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception is MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                } else if (exception is MsalUiRequiredException) {
                    /* Tokens expired or no session, retry with interactive */
                }
            }
        }
    }

    private fun exchangeTeamsTokenToAcsToken(authenticationResult: IAuthenticationResult){
        val accountIds = authenticationResult.account.id.split(".")
        val options = GetTokenForTeamsUserOptions(
            authenticationResult.accessToken, "<paste the client ID>",
            accountIds[0]
        )
        var acsToken = acsClient?.getTokenForTeamsUser(options)
        Log.d(TAG, "Successfully authenticated, ACS User token: ${acsToken?.token}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}