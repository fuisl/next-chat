# NextChat – Modular Spring Boot Messaging Server & Client

NextChat is a microservices‐style, command-based chat platform implemented in Java using Spring Boot. It provides:

- 🔐 **Authentication**  
- 👥 **Group & membership management**  
- 💬 **Real-time messaging** (MongoDB + in-memory relay)  
- 🌐 **Socket networking** with pluggable protocol  
- ⚙️ **Configurable via Docker & Docker Compose**
- 🖥️ **User friendly UI** built with JavaFX

---

## Table of Contents

1. [Features](#features)  
2. [Architecture](#architecture)  
3. [Getting Started](#getting-started)  
   - [Prerequisites](#prerequisites)  
   - [Docker Setup](#docker-setup)  
   - [Running Locally](#running-locally)  
4. [Project Structure](#project-structure)  
5. [Usage](#usage)  
   - [Server](#server)  
   - [Client](#client)  
6. [Protocol Reference](#protocol-reference)  
7. [Configuration](#configuration)  
8. [Contributing](#contributing)  
9. [License](#license)  

---

## Features

- **Modular design**: Separate modules for `auth`, `session`, `group`, `messaging`, `net`, `protocol` & `shared`  
- **Spring DI & Beans**: All core components wired via Spring Boot  
- **Hybrid sessions**: In-memory + persistent tracking for resilience and performance  
- **Command pattern**: Easy to extend protocol with new commands  
- **Dockerized**: Container images for server, client and database  

---

## Architecture

```text
+----------------+            +----------------+       +--------------+
|   Client App   | <–– TCP –> |  Server Socket |  ––>  |  Services &  |
|  (JavaFX UI)   |            |  (port 5001)   |       | Repositories |
+-------+--------+            +--------+-------+       +--------------+
        |                              |
        | JSON commands                | Spring-managed
        v                              | CommandFactoryRegistry
  ProtocolDecoder                      |
        |                              |
        v                              |
  CommandHandler ––> Auth, Session, Group, Messaging, Relay …
```

## Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- MySQL (or H2 for testing)
- MongoDB

### Docker Setup

```bash
# build and start all services

docker-compose up --build -d

# view logs

docker-compose logs -f
```

### Running Locally (without Docker)

1. Clone the repo
2. Configure your application.yml (see Configuration)
3. Start the server:

    ```bash
    ./gradlew :server:bootRun
    ```

4. Start the client

    ```bash
    ./gradlew :client:bootRun
    ```

## Project Structure

```text
server/
├── auth/       # User, Credential, Authenticator  
├── config/     # AppConfig, Bean registrations  
├── context/    # SpringContext helper  
├── group/      # Group entity, GroupService  
├── messaging/  # Message models, services, RelayService  
├── net/        # ClientHandler, ServerLauncher  
├── protocol/   # Command factories, impl, registry  
├── session/    # Session entity, HybridSessionService  
└── shared/     # DTOs (SessionToken, GroupResponse)
client/
└── dev.nextchat.client/…  # JavaFX app
docker-compose.yml  
entrypoint.sh
```

## Usage

### Server

1. Connect with telnet or netcat:

    ```bash
    telnet localhost 5001
    ```

2. Handshake:

    ```arduino
    < Server: HELLO_CLIENT
    > Client: HELLO_SERVER
    < Server: WELCOME
    ```

3. Send a single‐line JSON command:

    ```json
    {"type":"login","username":"duong","passwd":"matkhau"}
    ```

### Client

- JavaFX GUI: browse, signup/login, list groups, chat in real time
- See [`client/`](\client) module for controllers & FXML layouts

## Protocol Reference

| Command Type      | Description                                 | Example JSON Payload                                                                 |
|-------------------|---------------------------------------------|--------------------------------------------------------------------------------------|
| `signup`          | Registers a new user                        | `{"type": "signup", "username": "duong", "passwd": "matkhau"}`                         |
| `login`           | Logs in an existing user                    | `{"type": "login", "username": "duong", "passwd": "matkhau"}`                          |
| `create_group`    | Creates a new chat group                    | `{"type": "create_group", "name": "Dev", "description": "test description"}`       |
| `join_group`      | Joins a user to an existing group           | `{"type": "join_group", "groupId": "b3262775-6044-44c2-b116-5bbddb6804a0"}`         |
| `send_message`    | Sends a message to a group                  | `{"type": "send_message", "groupId": "b3262775-6044-44c2-b116-5bbddb6804a0", "content": "Hello!"}` |
| `fetch_recent`    | Fetches the 20 most recent messages in group| `{"type": "fetch_recent", "groupId": "b3262775-6044-44c2-b116-5bbddb6804a0"}`       |
| `fetch_before`    | Fetches the messages sent before a timestamp | `{"type": "fetch_before", "groupId": "cd8c9454-59b9-42ee-9b49-23997f3da2ff", "timestamp": "2025-04-28T13:26:00.616Z"}`       |

> _More commands in protocol/impl & documented in code._

## Configuration

You can configure database connections, ports, and credentials in [application.yml](server/src/main/resources/application.yml).

For quick tests, you may switch to an in-memory H2 by adjusting the JPA URL and driver.

## Contributing

1. Fork & clone
2. Create a feature branch
3. Add tests & documents
4. Submit a Pull Request

## License

This project is licensed under the MIT License
