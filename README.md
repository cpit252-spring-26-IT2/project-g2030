# Secure File Transfer System (SFTS)

## Group Members
* **Motaz Alsayed** – 2339709
* **Abdulaziz Bukhari** – 2338742
* **Abdulmalik Aldahari** – 2337862

---

## Problem Description: Security Gaps in Medical Data Exchange
Healthcare collaboration is currently compromised by three critical issues:

1. **Insecure Transfer Methods:** Reliance on USB drives and public email makes patient data vulnerable to loss and interception.
2. **Lack of Access Control:** Existing systems fail to restrict data within teams, allowing unauthorized staff to view sensitive records.
3. **Cyber Vulnerabilities:** Unencrypted network transfers expose the hospital to Man-in-the-Middle attacks and legal penalties for data breaches.

---

## Proposed Solution & Core Features
A robust Web Application built on Java designed to provide a centralized, encrypted environment for medical collaboration through the following mechanisms:

* **Data Isolation:** Segregates records by department (e.g., Lab vs. ER) to prevent unauthorized cross-team access.
* **Identity & Accountability:** Enforces strict user verification and logs all file activities for full accountability.
* **Secure Networking:** Utilizes a central server with isolated processing threads to ensure stable and safe data transmission.
* **Controlled Operations:** Assigns unique IDs to documents and provides a secure interface for file management, ensuring data remains under authorized control.

---

## System Architecture & Design Patterns (Milestone 1)
To ensure a scalable, secure, and maintainable foundation, the core system architecture is built using the following Software Design Patterns:

1. **Proxy Pattern (`DepartmentProxy`):** Acts as a security gateway. It intercepts file processing requests and verifies user authorization before granting access to sensitive departments (e.g., Laboratory).
2. **Factory Pattern (`DepartmentFactory`):** Centralizes and encapsulates the creation of Department objects (Lab, ER), allowing the system to easily scale and add new hospital departments in the future without modifying existing client code.
3. **Builder Pattern (`FileBuilder`):** Provides a step-by-step approach to constructing complex `SecureFile` objects, ensuring all mandatory fields (like ID and Name) are set while allowing optional security configurations (like Encryption type and Urgency).
4. **Singleton Pattern (`AuditLogger`):** Guarantees that only one instance of the system logger exists across the entire application, preventing concurrent modification issues and ensuring all security events are recorded in a centralized, thread-safe manner.

---

