# InstantMessaging

A simple Java-based Instant Messaging application using client-server architecture.

## Features

- Real-time text messaging between multiple clients  
- Multithreaded server for handling concurrent client connections  
- Console-based interface  
- Built using Java and Maven

## Project Structure

```text
src/ ├── main/ 
     │ 
     └── java/ │ 
               └── InstantMessaging/ │ 
                                     ├── IMClient.java # Client-side logic 
                                     │ 
                                     ├── IMServer.java # Server-side entry point 
                                     │ 
                                     └── ServerWorker.java # Handles individual client connections
```
##Notes
- Ensure the server is running before starting any clients.
- The server listens on a default port defined in the source code (you may modify this if needed).
