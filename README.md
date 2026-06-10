# 🚗 CarLog – Smart Fleet & Driver Monitoring System

CarLog is an intelligent fleet management system designed to help companies monitor and evaluate driver behavior in real-time using OBD-II vehicle data.

The system collects live telemetry data from vehicles via Bluetooth OBD-II adapters, analyzes driving behavior, and generates performance scores for each trip.

---

## 🎯 Project Overview

CarLog was developed as a Graduation Project to solve real-world fleet management problems by enabling companies to:

- Monitor driver behavior in real time
- Improve road safety
- Reduce fuel consumption
- Evaluate trip performance automatically

---

## 📱 Key Features

### 🔗 OBD-II Integration
- Bluetooth connection with OBD-II device
- Real-time vehicle data streaming

### 📊 Live Vehicle Data
- Speed monitoring
- RPM tracking
- Fuel status
- Continuous trip updates

### 🚦 Driver Behavior Analysis
- Acceleration patterns detection
- Braking behavior analysis
- Idling time tracking
- Automated trip scoring system

### ⭐ Rating System
- Generates a score for each trip
- Evaluates driver performance
- Helps companies assess driving quality

### 🔐 Authentication System
- Secure login system for drivers

### 💬 Communication
- Chat between drivers and admin system

---

## 🧠 System Architecture

The application follows Clean Architecture principles:

```
Presentation Layer
│
├── UI (Fragments / Activities)
├── ViewModels
│
Domain Layer
│
├── Use Cases
├── Business Logic
│
Data Layer
│
├── Repository Implementation
├── Remote API (Retrofit)
```

---

## 🛠️ Tech Stack

- Kotlin
- XML
- MVVM Architecture
- Clean Architecture
- Coroutines
- Hilt (Dependency Injection)
- Retrofit
- LiveData
- Bluetooth Communication (OBD-II)
- ASP.NET Backend

---

## 🧑‍💻 My Role

I was responsible for the complete Android application development:

- Built full Android app from scratch
- Implemented Bluetooth communication with OBD-II devices
- Designed and developed UI screens
- Integrated REST APIs using Retrofit
- Implemented real-time data handling
- Built driver rating and analytics system
- Applied Clean Architecture principles

---

## 📸 Screenshots


| Login | Home | OBD Connection |
|------|------|------|
| 📷 | 📷 | 📷 |

---

## 📈 Impact

- Real-time fleet monitoring system
- Improved driver behavior evaluation
- Automated trip scoring mechanism
- Enhanced safety insights for companies

---

## 🔮 Future Improvements

- Migration to Jetpack Compose
- Offline-first support
- Advanced analytics dashboard
- GPS-based trip visualization
- Push notifications system

---

## 📌 Notes

This project was developed as a Graduation Project and represents early-stage implementation of a real-world fleet monitoring system using mobile and embedded vehicle data integration.

---

## 👨‍💻 Author

**Shehab Abdelhares**

- GitHub: https://github.com/SHEHAB7x