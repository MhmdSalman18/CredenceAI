# CredenceAI 🪙

**CredenceAI** is a high-performance, privacy-first personal finance manager for Android. It leverages on-device intelligence to automate expense tracking by intercepting and parsing financial notifications, effectively eliminating the friction of manual data entry.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-1.7-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean_/_MVVM-green.svg?style=flat)]()
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 🚀 The Product Vision

Manual expense tracking has a 90% abandonment rate due to "input friction." **CredenceAI** solves this by turning every payment notification into a structured data point automatically. 

*   **Zero-Touch Logging:** Real-time parsing of UPI (GPay, PhonePe, Paytm), Bank SMS, and financial app alerts.
*   **Privacy-First:** All processing is done locally via a custom `NotificationListenerService`. No financial data ever leaves the device.
*   **Smart Budgeting:** Predictive "Auto-Distribute" logic that intelligently balances monthly limits across categories with fluid UI feedback.

---

## 🏗️ Architectural Excellence

The project follows **Clean Architecture** and **SOLID** principles, designed for maximum maintainability and testability.

### **Layers & Data Flow**
- **Presentation:** 100% Jetpack Compose using the **MVVM** pattern. State is managed via `StateFlow` and `collectAsStateWithLifecycle` to ensure UI stability, lifecycle awareness, and zero memory leaks.
- **Domain:** Pure Kotlin layer containing `UseCases` and `Repository` interfaces. Contains the core "Smart Budget" logic, isolated from Android frameworks.
- **Data:** Implements repositories, **Room** for local persistence, and **Jetpack DataStore** for reactive settings and configuration.
- **Service Layer:** A sophisticated `NotificationListenerService` that acts as a real-time data ingestion pipe, operating independently of the UI lifecycle.

---

## 🛠️ Engineering Highlights & Challenges

### **1. Intelligent Notification Parsing Engine**
Handling the fragmented Indian financial notification ecosystem was the primary technical challenge.
- **The Problem:** Diverse formats across apps (GPay, PhonePe, HDFC, SMS) make data extraction difficult and error-prone.
- **The Solution:** Developed a multi-pass **Regex Engine** with collision resolution. It distinguishes between "You paid" (Debit) and "You received" (Credit) even in complex, promotion-heavy notification strings.
- **Battery Optimization:** implemented a package allowlist and keyword pre-filtering to minimize CPU wake-ups, significantly preserving battery life.

### **2. Premium UI/UX Implementation**
The app's UI goes beyond standard Material components to provide a "Fintech-grade" experience:
- **State-Driven Animations:** Utilizes `Animatable` and `FastOutSlowInEasing` for high-60fps fluid transitions during budget allocation.
- **Custom Design System:** Developed bespoke `BasicTextField` wrappers for specialized currency and budget inputs, providing a superior UX compared to standard components.
- **Edge-to-Edge Design:** Full support for Window Insets (`imePadding`, `statusBarsPadding`) for a modern, immersive feel.

### **3. Scalability with Hilt & SOLID**
- **Dependency Injection:** Full **Hilt** integration for modularity and effortless unit testing.
- **Reactive Streams:** End-to-end `Flow` implementation from the DAO up to the UI, ensuring a "Single Source of Truth."

---

## 📊 Project Structure

```text
com.credenceai.app
├── core           # Design System (Brand Colors, Typography), Preferences, Utils
├── data           # Room Entities, DAOs, Repository Implementations, Mappers
├── di             # Hilt Dependency Modules (Database, Repository, UseCase Modules)
├── domain         # Business Logic: Models, UseCases, Repository Interfaces
└── presentation   # ViewModels & Screens (Dashboard, Smart Budget, Transaction List)
```

---

## 📈 Roadmap
- [ ] **On-Device ML:** Transitioning from Regex to NLP for smarter merchant categorization.
- [ ] **Visual Analytics:** Interactive spending trend charts and pie-graphs using Compose Canvas.
- [ ] **Data Export:** Automated PDF/CSV reports for financial audits.

---

## 🤝 Let's Connect
I am an Android Engineer passionate about building clean, high-performance applications that solve real-world problems.

**Author:** [Your Name]  
**LinkedIn:** [Your LinkedIn Profile]  
**Portfolio:** [Your Website Link]

---
*This repository serves as a portfolio piece demonstrating expertise in Modern Android Development (MAD) and scalable system design.*
