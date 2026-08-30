# Security Policy

## 🔒 Security Architecture

**Bouncer** is designed with security and privacy in mind:

- **Local-Only Communication:** All network calls are strictly between the Android device and the local router (`192.168.1.1`). No external telemetry, tracking, or cloud servers are used.
- **Hardware-Backed Credential Encryption:** Router admin credentials are stored exclusively inside Android's `EncryptedSharedPreferences` using AES-256 GCM encryption.
- **No Cloud Sync or Backups:** `allowBackup="false"` is enforced in `AndroidManifest.xml` to prevent credentials from leaking through ADB backup or cloud drive snapshots.
- **Zero Hardcoded Secrets:** No default passwords or sensitive tokens exist in the source code.

## 🛡️ Reporting a Vulnerability

If you discover a security issue or vulnerability in this repository, please report it privately:

1. **Email:** [ratnakirti03@gmail.com](mailto:ratnakirti03@gmail.com)
2. **Subject:** `[Security Vulnerability] Bouncer - <Brief Description>`
3. Please include steps to reproduce, impact assessment, and any relevant logs or code snippets.

We will acknowledge your report promptly and provide an estimated timeline for a fix. Please do not open public GitHub issues for security vulnerabilities.
