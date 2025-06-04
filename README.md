# Burp Suite Blind XSS Injector Extension

A Burp Suite extension that automates blind XSS payload injection and notifies via Telegram.

## Features

- Automatically injects XSS payloads into various injection points:
  - URL parameters
  - POST body (JSON, form-urlencoded, multipart/form-data, XML)
  - HTTP headers (User-Agent, Referer, X-Forwarded-For, Origin, Accept-Language)
  - Cookies
- Multiple encoding techniques for each payload:
  - Raw payload
  - Base64 encoded
  - URL encoded
  - Double URL encoded
  - Triple URL encoded
- Real-time Telegram notifications for each injection attempt
- User-friendly UI for configuration
- Non-blocking operation
- Comprehensive logging

## Requirements

- Burp Suite Professional 2023.11.1 or later
- Java 11 or later
- Telegram Bot Token (create one via @BotFather)

## Installation

1. Build the extension:
   ```bash
   mvn clean package
   ```

2. In Burp Suite:
   - Go to Extensions > Extensions
   - Click "Add"
   - Select "Java" as the extension type
   - Click "Select file" and choose the generated JAR file
   - Click "Next" and then "Close"

## Configuration

1. In Burp Suite, go to the "Blind XSS Injector" tab
2. Configure the following settings:
   - Enable/Disable the injector
   - Add your Telegram Bot Token
   - Add your Telegram Chat ID
   - Add your XSS payloads (one per line)

## Usage

1. Configure your Telegram bot and chat ID
2. Add your XSS payloads
3. Enable the injector
4. Browse your target application normally
5. The extension will automatically:
   - Inject payloads into all in-scope requests
   - Send Telegram notifications for each injection
   - Log all activities in the UI

## Default XSS Payloads

Here are some example payloads you can use:

```
<script>alert(1)</script>
<img src=x onerror=alert(1)>
"><script>alert(1)</script>
javascript:alert(1)
```

## Security Notice

This tool is for authorized security testing only. Always:
- Obtain proper authorization before testing
- Follow responsible disclosure practices
- Respect privacy and data protection laws
- Use in controlled environments

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 