# 💬 InstaChat

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![Swing](https://img.shields.io/badge/Swing-0078D7?style=for-the-badge&logo=java&logoColor=white)]()
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

---

## 🧠 Overview

**InstaChat** is a secure, real-time, multi-client chat application built in **Java**, inspired by the sleek dark theme of Instagram.  
It allows multiple users to communicate simultaneously through a server-client architecture, featuring encryption, typing indicators, and persistent chat logs.

---

## 🚀 Features

✨ **Real-Time Messaging** – Chat instantly with multiple users connected to the server  
🔐 **End-to-End AES Encryption** – Ensures message privacy  
💾 **Chat History Logging** – All chats stored securely in an SQLite database  
👥 **Online Users Panel** – Displays live online users with status indicators  
💬 **Typing Indicator** – Shows when someone is typing  
🖤 **Dark Mode GUI** – Modern, Instagram-like interface using Swing  
⚙️ **Multi-Threaded Server** – Supports concurrent client connections

---

## 🧰 Tech Stack

| Component | Technology |
|------------|-------------|
| **Language** | Java |
| **GUI** | Java Swing |
| **Networking** | Java Sockets |
| **Database** | SQLite (via JDBC) |
| **Encryption** | AES (Advanced Encryption Standard) |
| **Concurrency** | Java Multithreading |

---

## ⚙️ How to Run

### 🧩 1. Compile the Project
Open a terminal in the project directory and run:
```bash
javac -cp "sqlite-jdbc-3.50.3.0.jar;." *.java
🖥️ 2. Start the Server
Start the server (keep this window open):

bash
Copy code
java -cp "sqlite-jdbc-3.50.3.0.jar;." ChatServer
💬 3. Run the Client
For each new user, open a separate terminal and run:

bash
Copy code
java -cp "sqlite-jdbc-3.50.3.0.jar;." ChatClientGUI
🧑‍💻 Author
Yash Panchal
💼 Passionate Java Developer | 🔐 Building Secure Systems | ☕ Always Learning
📫 GitHub

📄 License
This project is licensed under the MIT License — feel free to use and modify it for learning and development.

🌟 Show Your Support
If you like this project, consider giving it a ⭐ on GitHub — it helps a lot!

🔮 Future Enhancements
🗂️ Add group chat feature

📱 Implement mobile version with JavaFX

☁️ Integrate with cloud-based database for global access

🧑‍🤝‍🧑 Add friend list and private DMs
