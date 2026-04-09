# Secure File Transfer System (SFTS)E2


# Group Members

Motaz Alsayed – 2339709

Abdulaziz Bukhari - 2338742

Abdulmalik Aldahari - 2337862

## Description

Security Gaps in Medical Data Exchange
Healthcare collaboration is currently compromised by three critical issues:

1. Insecure Transfer Methods: Reliance on USB drives and public
email makes patient data vulnerable to loss and interception.

2. Lack of Access Control: Existing systems fail to restrict data within
teams, allowing unauthorized staffto view sensitive records.

4. Cyber Vulnerabilities: Unencrypted network transfers expose the
hospital to Man-in-the-Middle attacks and legal penalties for data
breaches.

## Features

• Data Isolation: Segregates records by department(e.g., Lab vs. ER)
to prevent unauthorized cross-team access

• Identity s Accountability: Enforces strict user verification and logs
allfile activities forfull accountability.

• Secure Networking: Utilizes a central server with isolated
processing threads to ensure stable and safe data transmission.

• Controlled Operations: Assigns unique IDs to documents and
provides a secure interface for file management, ensuring data
remains under authorized control.

A. Granular Access & Usage Control

• Time-Limited Access: Administrators can grant temporary
access to files that automatically expires after a specific
duration for example 4 or 8 hours.

• Quantitative View Limits: To prevent data from exposure,
the system will allow setting a maximum number of times a
file can be viewed. Once the limit is reached, access is
revoked.

• Download Restrictions: A View Only mode will be
implemented, allowing users to preview documents in a
secure web-based viewer while disabling the ability to
download or save the file locally.

B. Protection & Tracking

• Dynamic Watermarking: To deter unauthorized sharing
and screenshots, the system will overlay a dynamic
watermark on all viewed documents containing the viewer's
ID.

• Comprehensive View Tracking: Beyond basic logs, the
system will track and record every instance a file is
opened.

C. Dropbox Management Features

• File Versioning: The system will maintain a history of
document versions, allowing users to track changes over
time and restore previous versions.

• Recycle Bin & Recovery: Deleted files will be moved to a
Recycle Bin for a set period, allowing authorized recovery
and preventing accidental data loss.


## Usage

To build and run the app, use:

```shell
```

## Screenshots


## License

Pick a project license
