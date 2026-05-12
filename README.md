# Secure File Transfer System (SFTS)

## Group Members
- **Motaz Alsayed** – 2339709
- **Abdulaziz Bukhari** – 2338742
- **Abdulmalik Aldahari** – 2337862

---

## Problem Description: Security Gaps in Medical Data Exchange
Healthcare collaboration is currently compromised by three critical issues:

1. **Insecure Transfer Methods:** Reliance on USB drives and public email makes patient data vulnerable to loss and interception.
2. **Lack of Access Control:** Existing systems fail to restrict data within teams, allowing unauthorized staff to view sensitive records.
3. **Cyber Vulnerabilities:** Unencrypted network transfers expose the hospital to Man-in-the-Middle attacks and legal penalties for data breaches. 

---

## Proposed Solution & Core Features
SFTS is a Java-based secure file transfer application designed to provide a centralized, encrypted environment for medical collaboration.

### Core Features
- **Data Isolation:** Segregates records by department such as Lab and ER to prevent unauthorized cross-team access. 
- **Identity & Accountability:** Enforces strict user verification and logs all file activities for accountability. 
- **Secure Networking:** Uses a central server with isolated processing threads to ensure stable and secure data transmission. 
- **Controlled Operations:** Assigns unique IDs to documents and provides secure file management workflows. 
- **Time-Limited Access:** Files can expire automatically after a configured duration. 
- **Maximum View Limits:** Files can be restricted to a specific number of views. 
- **View-Only Mode:** Files can be previewed securely without allowing direct download. 
- **Dynamic Watermarking:** Viewer identity can be displayed on protected file previews. 
- **View Tracking:** The system records file access events for auditing and monitoring. 
- **File Versioning:** The system supports version history for secure files. 
- **Recycle Bin & Recovery:** Deleted files can be moved to a recycle bin and restored later. 

---

## System Architecture & Design Patterns

To ensure a scalable, secure, and maintainable architecture, the system uses the following design patterns:

1. **Proxy Pattern (`DepartmentProxy`)**  
   Acts as a security gateway by intercepting file processing requests and validating user authorization before allowing access to restricted departments.

2. **Factory Pattern (`DepartmentFactory`)**  
   Centralizes the creation of department objects such as Lab and ER, making the system easier to scale.

3. **Builder Pattern (`FileBuilder`)**  
   Provides a flexible step-by-step process for constructing complex `SecureFile` objects with required and optional security settings.

4. **Singleton Pattern (`AuditLogger`)**  
   Ensures that only one shared logger instance is used across the entire system for centralized audit logging.

5. **Observer Pattern (`FileObserver`, `AuditObserver`, `ViewTrackingObserver`, `VersionObserver`, `RecycleBinObserver`)**  
   Implements the required **behavioral design pattern** for Stage 3.  
   It allows the system to notify multiple components when secure file events occur, such as:
    - File opened
    - File updated
    - File deleted
    - File restored
    - Access denied 

---

## Stage 3 Update: Behavioral Design Pattern
In Stage 3, the project was extended with the **Observer Pattern** as the required behavioral design pattern.  
This pattern improves modularity by separating file event generation from event handling. As a result, logging, tracking, versioning, and recycle bin behavior can respond automatically to file actions without tightly coupling all logic into a single class. 

---

## Project Status
At this stage, the project is in a near-completion state with the main planned features implemented, including secure transfer logic, department-based access control, protected file handling, and behavioral event observation. 

---

## How to Run

### Option 1: Run from IntelliJ
1. Open the project in IntelliJ IDEA.
2. Load Maven dependencies.
3. Run `Main.java` to open the launcher.
4. Choose:
    - `Run Server`
    - `Run Client`

### Option 2: Run the JAR file
After building the project:
```bash
java -jar target/sfts-stage3.jar
```

---

## Build Instructions
To build the project as a JAR file using Maven:

```bash
mvn clean package
```

The generated binary file can be found in:

```bash
target/sfts-stage3.jar
```

---

## GitHub Project Management
The team uses a GitHub Kanban Board to manage Stage 3 tasks.  
Tasks are assigned to team members, tracked through progress columns, and closed using related commit messages as required in the project instructions. 

---

## Release
A release is created on GitHub for Stage 3, and the binary file is uploaded under the repository’s **Releases** section as required. 