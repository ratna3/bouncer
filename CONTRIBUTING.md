# Contributing to Bouncer

Thank you for your interest in contributing to **Bouncer**! Contributions from the community are welcomed and appreciated.

---

## 🛠️ Development Workflow

1. **Fork the repository** on GitHub.
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/your-username/bouncer.git
   cd bouncer/bouncer-app
   ```
3. **Create a feature branch**:
   ```bash
   git checkout -b feature/my-new-feature
   ```
4. **Make your changes** following our code guidelines:
   - Follow Kotlin and Jetpack Compose best practices.
   - Keep router-specific scraping logic modular inside the `data` package so adapters for other router models can be easily added.
   - Never hardcode credentials, tokens, or IP addresses.
5. **Run tests to verify changes**:
   ```bash
   ./gradlew testDebugUnitTest
   ```
6. **Commit your changes**:
   ```bash
   git commit -m "Add feature: my new feature"
   ```
7. **Push to your branch**:
   ```bash
   git push origin feature/my-new-feature
   ```
8. **Open a Pull Request** against `main`.

---

## 🧪 Router Adapters & Testing

If you have a different router model or firmware revision and would like to contribute an adapter:
1. Implement the [`RouterRepository`](file:///c:/Users/Ratna/Desktop/bouncer/bouncer-app/app/src/main/java/com/example/bouncer/data/RouterRepository.kt) interface.
2. Document the HTML selectors and endpoints used for login, DHCP scraping, and MAC filtering.
3. Include unit tests with mock HTML payloads to ensure parsing stability.

---

## 💬 Community & Questions

Feel free to open an issue or connect on [Discord](https://discord.gg/VRPSujmH).
