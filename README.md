---
page_type: sample
languages:
- android
products:
- azure
- azure-communication-services
---

# Create and manage Communication access tokens for Teams users in mobile applications

This code sample walks you through the process of acquiring a Communication Token Credential by exchanging an Azure AD token of a user with a Teams license for a valid Communication access token.

This sample application utilizes the [MSAL for Android](https://github.com/AzureAD/microsoft-authentication-library-for-android) library for authentication against the Azure AD and acquisition of a token with delegated permissions.
The token exchange itself is facilitated by the `azure-communication-identity` library.

To be able to use the token for Calling, use it to initialize the `CommunicationTokenCredential` from the `azure-communication-common` library.

## Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- An active Communication Services resource and connection string. [Create a Communication Services resource](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource/).
- Azure Active Directory tenant with users that have a Teams license.
- [Android Studio](https://developer.android.com/studio)

## Before running sample code

1. Complete the [Administrator actions](https://docs.microsoft.com/azure/communication-services/quickstarts/manage-teams-identity?pivots=programming-language-javascript#administrator-actions) from the [Manage access tokens for Teams users quickstart](https://docs.microsoft.com/azure/communication-services/quickstarts/manage-teams-identity).
    - Take a note of Fabrikam's Azure AD Tenant ID and Contoso's Azure AD App Client ID. You'll need the values in the following steps.
2. On the Authentication pane of your Azure AD App, add a new platform of the mobile and desktop application type Android.
3. Provide your application package name and [signature hash](https://developer.android.com/studio/publish/app-signing#generate-key).
4. Open an instance of Windows Terminal, PowerShell, or an equivalent command line and navigate to the directory that you'd like to clone the sample to.
5. `git clone https://github.com/AikoBB/communication-services-cte.git`
6. Open Android Studio and select `Open an Existing Project`.
7. Select folder `communication-services-cte`.
8. Copy MSAL Android configuration from Azure portal and paste to `communication-services-cte/app/src/main/res/raw/auth_config.json`.
9. Set value for `android:path` in `communication-services-cte/app/src/main/AndroidManifest.xml`(line 38) as your signature hash.
10. Set value for `connectionString` in `communication-services-cte/app/src/main/java/com.aykobb.cteapp/FirstFragment.kt`(line 65) as your Azure Communication Services Connection String.
11. Set value for `client ID` in `communication-services-cte/app/src/main/java/com.aykobb.cteapp/FirstFragment.kt`(line 163) as your Client ID of an Azure AD application.

## Run the code
 1. Build/Run in Android Studio

Click on the `Sing In` button and from the `Logcat' pane of the Android Studion you will see a login URL with a device code.
Navigate to the URL and paste the device code. You will be presented with the Azure AD login form.
If the authentication is successful, the application receives an Azure AD access token and the token will be exchanged for a Communication access token.

