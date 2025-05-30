## Instructions for the current Message Database

### Components

The PendingMessage collection is not removed yet. Basic collections are already created and can be used by using the `Repository` classes such as `ReceivedMessageRepository` or `PendingMessageRepository`. Change the `Models` in the model folder to change the structure of the documents in `MongoDB`.

### How to query?

Queries must be placed in the `Repository`. You can use the `Spring Boot` way by writing query name or by define your own custom query. For example:

```java
public interface ReceivedMessageRepository extends MongoRepository<ReceivedMessage, UUID> {

    List<ReceivedMessage> findByGroupId(UUID groupId); // ~ SELECT * FROM received_message WHERE groupId == groupId (argument)
}
```

You should use the `Repository` in services. Any services using `@Service` provided by `Spring Boot` can declare using dependency injection. For example, 

```java
@Service
public class MessageTesting {
    private final ReceivedMessageRepository receivedMessageRepository;
    private final PendingMessageRepository pendingMessageRepository;

    public MessageTesting(ReceivedMessageRepository received_repo, PendingMessageRepository pending_repo) {
        this.receivedMessageRepository = received_repo;
        this.pendingMessageRepository = pending_repo;
    }
}
```

This class will be automatically declared and instantiated by `Spring Boot`, hence you don't need to instantiate using the `new()` keyword.

### How to use the Service for testing

If you want to run certain services for testing either the `Repository` or the `Service`, you could run it like this:

```java
@SpringBootApplication
public class App{

    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(App.class, args);

        MessageTesting messageTesting = context.getBean(MessageTesting.class); // get the existing instance of that service
        messageTesting.printReceivedMessages("6563d8a8-03b7-4422-bf18-78eecd5f707c");
    }
}
```