// package dev.nextchat.server.protocol.impl;

// import dev.nextchat.server.group.service.GroupService;
// import dev.nextchat.server.messaging.handler.SendMessageHandler;
// import dev.nextchat.server.messaging.model.Message;
// import dev.nextchat.server.messaging.service.MessageService;
// import dev.nextchat.server.protocol.Command;
// import dev.nextchat.server.protocol.CommandContext;

// import java.util.UUID;

// public class SendMessageCommand implements Command {

//     private final MessageService messageService;
//     private final GroupService groupService;

//     public SendMessageCommand(MessageService messageService, GroupService groupService) {
//         this.messageService = messageService;
//         this.groupService = groupService;
//     }

//     @Override
//     public void execute(String[] args, CommandContext ctx) {
//         if (!ctx.isAuthenticated()) {
//             System.out.println("❌ You must be logged in to send a message.");
//             return;
//         }

//         if (args.length < 3) {
//             System.out.println("⚠️ Usage: SEND_MESSAGE <groupId> <content>");
//             return;
//         }

//         try {
//             UUID groupId = UUID.fromString(args[1]);
//             String content = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

//             SendMessageHandler handler = new SendMessageHandler(messageService, groupService);
//             Message message = handler.handle(groupId, content, ctx);

//             System.out.println("✅ Message sent: " + message.getContent());
//         } catch (IllegalArgumentException ex) {
//             System.out.println("❌ Invalid group ID or format: " + ex.getMessage());
//         } catch (Exception e) {
//             System.out.println("❌ Failed to send message: " + e.getMessage());
//         }
//     }
// }
