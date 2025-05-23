package net.hycrafthd.minecraft_authenticator.microsoft.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.hycrafthd.minecraft_authenticator.Constants;
import net.hycrafthd.minecraft_authenticator.microsoft.api.MinecraftHasPurchasedResponse;
import net.hycrafthd.minecraft_authenticator.microsoft.api.MinecraftLauncherLoginPayload;
import net.hycrafthd.minecraft_authenticator.microsoft.api.MinecraftLauncherLoginResponse;
import net.hycrafthd.minecraft_authenticator.microsoft.api.MinecraftProfileResponse;
import net.hycrafthd.minecraft_authenticator.microsoft.api.OAuthErrorResponse;
import net.hycrafthd.minecraft_authenticator.microsoft.api.OAuthTokenResponse;
import net.hycrafthd.minecraft_authenticator.microsoft.api.XBLAuthenticatePayload;
import net.hycrafthd.minecraft_authenticator.microsoft.api.XBLAuthenticateResponse;
import net.hycrafthd.minecraft_authenticator.microsoft.api.XBoxProfileResponse;
import net.hycrafthd.minecraft_authenticator.microsoft.api.XBoxResponse;
import net.hycrafthd.minecraft_authenticator.microsoft.api.XSTSAuthorizeErrorResponse;
import net.hycrafthd.minecraft_authenticator.microsoft.api.XSTSAuthorizePayload;
import net.hycrafthd.minecraft_authenticator.microsoft.api.XSTSAuthorizeResponse;
import net.hycrafthd.minecraft_authenticator.util.ConnectionUtil;
import net.hycrafthd.minecraft_authenticator.util.ConnectionUtil.TimeoutValues;
import net.hycrafthd.minecraft_authenticator.util.HttpPayload;
import net.hycrafthd.minecraft_authenticator.util.HttpResponse;
import net.hycrafthd.minecraft_authenticator.util.Parameters;

public class MicrosoftService {
	
	public static URL oAuthLoginUrl() {
		return oAuthLoginUrl(Constants.MICROSOFT_CLIENT_ID, Constants.MICROSOFT_OAUTH_REDIRECT_URL);
	}
	
	public static URL oAuthLoginUrl(String clientId, String redirectUrl) {
		return oAuthLoginUrl(clientId, redirectUrl, null);
	}
	
	public static URL oAuthLoginUrl(String clientId, String redirectUrl, String clientSecret) {
		final Parameters parameters = Parameters.create() //
				.add("client_id", clientId) //
				.add("response_type", "code") //
				.add("scope", "XboxLive.signin offline_access") //
				.add("redirect_uri", redirectUrl);
		
		if (clientSecret != null) {
			parameters.add("client_secret", clientSecret);
		}
		
		try {
			return ConnectionUtil.urlBuilder(Constants.MICROSOFT_OAUTH_SERVICE, Constants.MICROSOFT_OAUTH_ENDPOINT_AUTHORIZE, parameters);
		} catch (final MalformedURLException ex) {
			throw new AssertionError("This url should never be malformed.", ex);
		}
	}
	
	public static MicrosoftResponse<OAuthTokenResponse, OAuthErrorResponse> oAuthTokenFromCode(String authorizationCode, TimeoutValues timeoutValues) {
		return oAuthTokenFromCode(Constants.MICROSOFT_CLIENT_ID, Constants.MICROSOFT_OAUTH_REDIRECT_URL, authorizationCode, timeoutValues);
	}
	
	public static MicrosoftResponse<OAuthTokenResponse, OAuthErrorResponse> oAuthTokenFromCode(String clientId, String redirectUrl, String authorizationCode, TimeoutValues timeoutValues) {
		return oAuthTokenFromCode(clientId, redirectUrl, null, authorizationCode, timeoutValues);
	}
	
	public static MicrosoftResponse<OAuthTokenResponse, OAuthErrorResponse> oAuthTokenFromCode(String clientId, String redirectUrl, String clientSecret, String authorizationCode, TimeoutValues timeoutValues) {
		final Parameters parameters = Parameters.create() //
				.add("client_id", clientId) //
				.add("code", authorizationCode) //
				.add("grant_type", "authorization_code") //
				.add("redirect_uri", redirectUrl);
		
		if (clientSecret != null) {
			parameters.add("client_secret", clientSecret);
		}
		
		return oAuthServiceRequest(parameters, timeoutValues);
	}
	
	public static MicrosoftResponse<OAuthTokenResponse, OAuthErrorResponse> oAuthTokenFromRefreshToken(String refreshToken, TimeoutValues timeoutValues) {
		return oAuthTokenFromRefreshToken(Constants.MICROSOFT_CLIENT_ID, Constants.MICROSOFT_OAUTH_REDIRECT_URL, refreshToken, timeoutValues);
	}
	
	public static MicrosoftResponse<OAuthTokenResponse, OAuthErrorResponse> oAuthTokenFromRefreshToken(String clientId, String redirectUrl, String refreshToken, TimeoutValues timeoutValues) {
		return oAuthTokenFromRefreshToken(clientId, redirectUrl, null, refreshToken, timeoutValues);
	}
	
	public static MicrosoftResponse<OAuthTokenResponse, OAuthErrorResponse> oAuthTokenFromRefreshToken(String clientId, String redirectUrl, String clientSecret, String refreshToken, TimeoutValues timeoutValues) {
		final Parameters parameters = Parameters.create() //
				.add("client_id", clientId) //
				.add("refresh_token", refreshToken) //
				.add("grant_type", "refresh_token") //
				.add("redirect_uri", redirectUrl);
		
		if (clientSecret != null) {
			parameters.add("client_secret", clientSecret);
		}
		
		return oAuthServiceRequest(parameters, timeoutValues);
		
	}
	
	private static MicrosoftResponse<OAuthTokenResponse, OAuthErrorResponse> oAuthServiceRequest(Parameters parameters, TimeoutValues timeoutValues) {
		final JsonElement responseElement;
		try {
			final URL url = ConnectionUtil.urlBuilder(Constants.MICROSOFT_OAUTH_SERVICE, Constants.MICROSOFT_OAUTH_ENDPOINT_TOKEN);
			final String responseString = ConnectionUtil.urlEncodedPostRequest(url, ConnectionUtil.JSON_CONTENT_TYPE, parameters, timeoutValues).getAsString();
			responseElement = new JsonParser().parse(responseString);
		} catch (final IOException | JsonParseException ex) {
			return MicrosoftResponse.ofException(ex);
		}
		
		try {
			final JsonObject responseObject = responseElement.getAsJsonObject();
			if (responseObject.has("error")) {
				return MicrosoftResponse.ofError(Constants.GSON.fromJson(responseObject, OAuthErrorResponse.class));
			}
			return MicrosoftResponse.ofResponse(Constants.GSON.fromJson(responseObject, OAuthTokenResponse.class));
		} catch (final Exception ex) {
			return MicrosoftResponse.ofException(ex);
		}
	}
	
	public static MicrosoftResponse<XBLAuthenticateResponse, Integer> xblAuthenticate(String accessToken, TimeoutValues timeoutValues) {
		final XBLAuthenticatePayload payload = new XBLAuthenticatePayload(new XBLAuthenticatePayload.Properties("RPS", "user.auth.xboxlive.com", "d=" + accessToken), "http://auth.xboxlive.com", "JWT");
		
		final JsonElement responseElement;
		try {
			final URL url = ConnectionUtil.urlBuilder(Constants.MICROSOFT_XBL_AUTHENTICATE_URL);
			final HttpResponse response = ConnectionUtil.jsonPostRequest(url, HttpPayload.fromGson(payload), timeoutValues);
			if (response.getResponseCode() >= 400) {
				return MicrosoftResponse.ofError(response.getResponseCode());
			}
			responseElement = new JsonParser().parse(response.getAsString());
		} catch (final IOException | JsonParseException ex) {
			return MicrosoftResponse.ofException(ex);
		}
		
		try {
			final XBLAuthenticateResponse response = Constants.GSON.fromJson(responseElement, XBLAuthenticateResponse.class);
			
			// Validate response
			if (response.getDisplayClaims().getXui().isEmpty()) {
				throw new IllegalStateException("Xui (user hashes) cannot be missing");
			}
			
			return MicrosoftResponse.ofResponse(response);
		} catch (final Exception ex) {
			return MicrosoftResponse.ofException(ex);
		}
	}
	
	public static MicrosoftResponse<XSTSAuthorizeResponse, XSTSAuthorizeErrorResponse> xstsAuthorize(String xblToken, String relyingParty, XBoxResponse.DisplayClaims displayClaims, TimeoutValues timeoutValues) {
		final XSTSAuthorizePayload payload = new XSTSAuthorizePayload(new XSTSAuthorizePayload.Properties("RETAIL", Arrays.asList(xblToken)), relyingParty, "JWT");
		
		final JsonElement responseElement;
		try {
			final URL url = ConnectionUtil.urlBuilder(Constants.MICROSOFT_XSTS_AUTHORIZE_URL);
			final String responseString = ConnectionUtil.jsonPostRequest(url, HttpPayload.fromGson(payload), timeoutValues).getAsString();
			responseElement = new JsonParser().parse(responseString);
		} catch (final IOException | JsonParseException ex) {
			return MicrosoftResponse.ofException(ex);
		}
		
		try {
			final JsonObject responseObject = responseElement.getAsJsonObject();
			if (responseObject.has("XErr")) {
				return MicrosoftResponse.ofError(Constants.GSON.fromJson(responseObject, XSTSAuthorizeErrorResponse.class));
			}
			
			final XSTSAuthorizeResponse response = Constants.GSON.fromJson(responseObject, XSTSAuthorizeResponse.class);
			
			// Validate response
			final var xblXui = displayClaims.getXui();
			final var xstsXui = response.getDisplayClaims().getXui();
			
			if (xblXui.containsAll(xstsXui)) {
				throw new IllegalStateException("Xui (user hashes) do match match");
			}
			
			return MicrosoftResponse.ofResponse(response);
		} catch (final Exception ex) {
			return MicrosoftResponse.ofException(ex);
		}
	}
	
	public static MicrosoftResponse<MinecraftLauncherLoginResponse, Integer> minecraftLaucherLogin(String xstsToken, XBoxResponse.DisplayClaims displayClaims, TimeoutValues timeoutValues) {
		final MinecraftLauncherLoginPayload payload = new MinecraftLauncherLoginPayload("XBL3.0 x=" + displayClaims.getXui().get(0).getUhs() + ";" + xstsToken, "PC_LAUNCHER");
		
		final JsonElement responseElement;
		try {
			final URL url = ConnectionUtil.urlBuilder(Constants.MICROSOFT_MINECRAFT_SERVICE, Constants.MICROSOFT_MINECRAFT_ENDPOINT_LAUNCHER_LOGIN);
			final HttpResponse response = ConnectionUtil.jsonPostRequest(url, HttpPayload.fromGson(payload), timeoutValues);
			if (response.getResponseCode() >= 400) {
				return MicrosoftResponse.ofError(response.getResponseCode());
			}
			responseElement = new JsonParser().parse(response.getAsString());
		} catch (final IOException | JsonParseException ex) {
			return MicrosoftResponse.ofException(ex);
		}
		
		try {
			final MinecraftLauncherLoginResponse response = Constants.GSON.fromJson(responseElement, MinecraftLauncherLoginResponse.class);
			return MicrosoftResponse.ofResponse(response);
		} catch (final Exception ex) {
			return MicrosoftResponse.ofException(ex);
		}
	}
	
	public static MicrosoftResponse<MinecraftHasPurchasedResponse, Integer> minecraftHasPurchased(String accessToken, UUID launcherClientId, TimeoutValues timeoutValues) {
		final JsonElement responseElement;
		try {
			final URL url = ConnectionUtil.urlBuilder(Constants.MICROSOFT_MINECRAFT_SERVICE, Constants.MICROSOFT_MINECRAFT_ENDPOINT_HAS_PURCHASED, Parameters.create().add("requestId", launcherClientId));
			final HttpResponse response = ConnectionUtil.bearerAuthorizationJsonGetRequest(url, accessToken, timeoutValues);
			if (response.getResponseCode() >= 400) {
				return MicrosoftResponse.ofError(response.getResponseCode());
			}
			responseElement = new JsonParser().parse(response.getAsString());
		} catch (final IOException | JsonParseException ex) {
			return MicrosoftResponse.ofException(ex);
		}
		
		try {
			final MinecraftHasPurchasedResponse response = Constants.GSON.fromJson(responseElement, MinecraftHasPurchasedResponse.class);
			return MicrosoftResponse.ofResponse(response);
		} catch (final Exception ex) {
			return MicrosoftResponse.ofException(ex);
		}
	}
	
	public static MicrosoftResponse<MinecraftProfileResponse, Integer> minecraftProfile(String accessToken, TimeoutValues timeoutValues) {
		final JsonElement responseElement;
		try {
			final URL url = ConnectionUtil.urlBuilder(Constants.MICROSOFT_MINECRAFT_SERVICE, Constants.MICROSOFT_MINECRAFT_ENDPOINT_PROFILE);
			final HttpResponse response = ConnectionUtil.bearerAuthorizationJsonGetRequest(url, accessToken, timeoutValues);
			if (response.getResponseCode() >= 400) {
				return MicrosoftResponse.ofError(response.getResponseCode());
			}
			responseElement = new JsonParser().parse(response.getAsString());
		} catch (final IOException | JsonParseException ex) {
			return MicrosoftResponse.ofException(ex);
		}
		
		try {
			final MinecraftProfileResponse response = Constants.GSON.fromJson(responseElement, MinecraftProfileResponse.class);
			return MicrosoftResponse.ofResponse(response);
		} catch (final Exception ex) {
			return MicrosoftResponse.ofException(ex);
		}
	}
	
	public static MicrosoftResponse<XBoxProfileResponse, Integer> xboxProfileSettings(String xstsToken, XBoxResponse.DisplayClaims displayClaims, TimeoutValues timeoutValues) {
		final String authorization = "XBL3.0 x=" + displayClaims.getXui().get(0).getUhs() + ";" + xstsToken;
		
		final JsonElement responseElement;
		try {
			final URL url = ConnectionUtil.urlBuilder(Constants.MICROSOFT_XBOX_PROFILE_SETTINGS_URL, Parameters.create().add("settings", "GameDisplayName,AppDisplayName,AppDisplayPicRaw,GameDisplayPicRaw,PublicGamerpic,Gamerscore,Gamertag,ModernGamertag,ModernGamertagSuffix,UniqueModernGamertag,AccountTier,XboxOneRep,Location,Bio,Watermarks,RealName,RealNameOverride,IsQuarantined"));
			final HttpResponse response = ConnectionUtil.authorizationJsonGetRequest(url, authorization, urlConnection -> {
				urlConnection.addRequestProperty("x-xbl-contract-version", "3");
			}, timeoutValues);
			if (response.getResponseCode() >= 400) {
				return MicrosoftResponse.ofError(response.getResponseCode());
			}
			responseElement = new JsonParser().parse(response.getAsString());
		} catch (final IOException | JsonParseException ex) {
			return MicrosoftResponse.ofException(ex);
		}
		
		try {
			final XBoxProfileResponse response = Constants.GSON.fromJson(responseElement, XBoxProfileResponse.class);
			return MicrosoftResponse.ofResponse(response);
		} catch (final Exception ex) {
			return MicrosoftResponse.ofException(ex);
		}
	}
	
}
