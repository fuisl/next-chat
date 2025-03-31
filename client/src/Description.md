# Description
## 1. Authentication

### Login & Signup
The system should support user registration and login. Usernames and passwords are accepted as input and verified against existing in-memory records. On signup, the system must ensure usernames are unique.

### Session Management
After a successful login, the backend generates a unique session token to identify the user. This token is stored temporarily in memory and checked during user interactions to confirm authorization.

### Server Interaction
Every feature that requires authentication should be protected by verifying the session token. Token handling, validation, and user identity checks are all done in-memory.

---

## 2. User Profile

### Storing Preferences
User settings (such as dark/light mode, sound notifications, language choices) should be stored in a user-specific in-memory structure. These preferences are loaded when the user logs in.

### Updating Preferences
When a user updates their preferences from the UI, the backend updates the in-memory store accordingly. If persistent behavior is needed across application restarts, this store can be saved and reloaded using flat files.

---

## 3. Messages

### Message Metadata
Messages must include:
- The username of the sender
- The content of the message
- The groupID it send to
- A system-generated timestamp marking when the message was sent

The timestamp is added automatically on the backend upon message creation.

### Storing Messages
Messages are grouped based on the conversation (chat room or private chat). These are stored in memory using a structure that makes it easy to retrieve and update conversation history.

### UI-to-Backend Flow
When a user sends a message through the UI, the application sends the message content and sender information to the backend. The backend generates the timestamp and stores the message in the appropriate chat history. The UI can request the list of messages at any time to display the full conversation.

