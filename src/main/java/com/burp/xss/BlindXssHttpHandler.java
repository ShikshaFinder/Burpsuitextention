package com.burp.xss;

import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlindXssHttpHandler implements HttpHandler {
    private final BlindXssInjector extension;
    private final Map<String, String> processedRequests;

    public BlindXssHttpHandler(BlindXssInjector extension) {
        this.extension = extension;
        this.processedRequests = new ConcurrentHashMap<>();
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        if (!extension.isEnabled()) {
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        String requestId = generateRequestId(requestToBeSent);
        if (processedRequests.containsKey(requestId)) {
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        HttpRequest request = requestToBeSent.request();
        List<String> payloads = extension.getPayloads();

        // Process URL parameters
        processUrlParameters(request, payloads);

        // Process POST body
        processPostBody(request, payloads);

        // Process headers
        processHeaders(request, payloads);

        // Process cookies
        processCookies(request, payloads);

        processedRequests.put(requestId, "");
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return ResponseReceivedAction.continueWith(responseReceived);
    }

    private void processUrlParameters(HttpRequest request, List<String> payloads) {
        String url = request.url();
        if (url.contains("?")) {
            String[] parts = url.split("\\?");
            String baseUrl = parts[0];
            String queryString = parts[1];

            for (String param : queryString.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String paramName = keyValue[0];
                    String originalValue = keyValue[1];

                    for (String payload : payloads) {
                        injectPayload(request, "URL Parameter: " + paramName, payload, originalValue);
                    }
                }
            }
        }
    }

    private void processPostBody(HttpRequest request, List<String> payloads) {
        String contentType = request.headerValue("Content-Type");
        if (contentType == null) return;

        if (contentType.contains("application/json")) {
            processJsonBody(request, payloads);
        } else if (contentType.contains("application/x-www-form-urlencoded")) {
            processFormUrlEncodedBody(request, payloads);
        } else if (contentType.contains("multipart/form-data")) {
            processMultipartBody(request, payloads);
        } else if (contentType.contains("application/xml")) {
            processXmlBody(request, payloads);
        }
    }

    private void processJsonBody(HttpRequest request, List<String> payloads) {
        try {
            String body = request.bodyToString();
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            processJsonObject(jsonObject, "", request, payloads);
        } catch (Exception e) {
            extension.logInjection("Error processing JSON body: " + e.getMessage());
        }
    }

    private void processJsonObject(JsonObject jsonObject, String prefix, HttpRequest request, List<String> payloads) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String originalValue = value.getAsString();
                for (String payload : payloads) {
                    injectPayload(request, "JSON Field: " + key, payload, originalValue);
                }
            } else if (value.isJsonObject()) {
                processJsonObject(value.getAsJsonObject(), key, request, payloads);
            }
        }
    }

    private void processFormUrlEncodedBody(HttpRequest request, List<String> payloads) {
        String body = request.bodyToString();
        for (String param : body.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String paramName = keyValue[0];
                String originalValue = keyValue[1];

                for (String payload : payloads) {
                    injectPayload(request, "Form Parameter: " + paramName, payload, originalValue);
                }
            }
        }
    }

    private void processMultipartBody(HttpRequest request, List<String> payloads) {
        // TODO: Implement multipart form data processing
    }

    private void processXmlBody(HttpRequest request, List<String> payloads) {
        // TODO: Implement XML body processing
    }

    private void processHeaders(HttpRequest request, List<String> payloads) {
        String[] targetHeaders = {
            "User-Agent", "Referer", "X-Forwarded-For", "Origin", "Accept-Language"
        };

        for (String headerName : targetHeaders) {
            String originalValue = request.headerValue(headerName);
            if (originalValue != null) {
                for (String payload : payloads) {
                    injectPayload(request, "Header: " + headerName, payload, originalValue);
                }
            }
        }
    }

    private void processCookies(HttpRequest request, List<String> payloads) {
        String cookieHeader = request.headerValue("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] keyValue = cookie.trim().split("=");
                if (keyValue.length == 2) {
                    String cookieName = keyValue[0];
                    String originalValue = keyValue[1];

                    for (String payload : payloads) {
                        injectPayload(request, "Cookie: " + cookieName, payload, originalValue);
                    }
                }
            }
        }
    }

    private void injectPayload(HttpRequest request, String location, String payload, String originalValue) {
        // Create different encoded versions of the payload
        String[] encodedPayloads = {
            payload, // Raw
            Base64.getEncoder().encodeToString(payload.getBytes()), // Base64
            URLEncoder.encode(payload, StandardCharsets.UTF_8), // URL encoded
            URLEncoder.encode(URLEncoder.encode(payload, StandardCharsets.UTF_8), StandardCharsets.UTF_8), // Double URL encoded
            URLEncoder.encode(URLEncoder.encode(URLEncoder.encode(payload, StandardCharsets.UTF_8), StandardCharsets.UTF_8), StandardCharsets.UTF_8) // Triple URL encoded
        };

        String[] encodingTypes = {
            "raw", "base64", "url", "double-url", "triple-url"
        };

        for (int i = 0; i < encodedPayloads.length; i++) {
            String encodedPayload = encodedPayloads[i];
            String encodingType = encodingTypes[i];

            // Log the injection attempt
            String message = String.format("[%s] Injected %s payload into %s: %s",
                    java.time.LocalDateTime.now(),
                    encodingType,
                    location,
                    encodedPayload);
            extension.logInjection(message);

            // Send Telegram notification
            extension.getExecutorService().submit(() -> {
                try {
                    extension.getTelegramNotifier().sendNotification(
                            extension.getTelegramToken(),
                            extension.getTelegramChatId(),
                            message
                    );
                } catch (Exception e) {
                    extension.logInjection("Error sending Telegram notification: " + e.getMessage());
                }
            });
        }
    }

    private String generateRequestId(HttpRequestToBeSent request) {
        return request.request().url() + "|" + request.request().method() + "|" + request.request().bodyToString();
    }
} 