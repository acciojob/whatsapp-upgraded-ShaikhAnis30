package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class WhatsappRepository {

    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private int customGroupCount;
    private int messageId;

    // my HashMaps
    private HashMap<String, User> userMap;

    HashMap<Date, List<Message>> dateMessageMap;

    Group userGroup;

    public WhatsappRepository() {
        this.groupMessageMap = new HashMap<Group, List<Message>>();  // group and all messages in that group

        this.groupUserMap = new HashMap<Group, List<User>>();

        this.senderMap = new HashMap<Message, User>(); // message -> user

        this.adminMap = new HashMap<Group, User>();  // group mapped with its admin

        this.customGroupCount = 0;
        this.messageId = 0;

        this.userMap = new HashMap<String, User>();   // mob no is unique key

        this.dateMessageMap = new HashMap<Date, List<Message>>();

        this.userGroup = null;
    }



    public String createUser(String name, String mobile) throws Exception {
        if(!userMap.containsKey(mobile))
            userMap.put(mobile, new User(name, mobile));
        else
            throw new Exception("User already Exist");

        return "SUCCESS";
    }


    public Group createGroup(List<User> users) {
        if(users.size() == 2)  // so, this is a personal chat
            return createPersonalChat(users);

        this.customGroupCount++;
        Group group = new Group("Group " + this.customGroupCount, users.size());
        groupUserMap.put(group, users);
        adminMap.put(group, users.get(0)); // coz all users have unique name
        return group;
    }

    public Group createPersonalChat(List<User> users) {
        String groupName = users.get(1).getName();
        Group personalGroup = new Group(groupName, 2);
        groupUserMap.put(personalGroup, users);
        return personalGroup;
    }

    public int createMessage(String content) {
        this.messageId++;
        Message message = new Message(messageId, content, new Date());
        List<Message> messageList = dateMessageMap.get(message.getTimestamp());
        messageList.add(message);
        dateMessageMap.put(message.getTimestamp(), messageList);
        return this.messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)) throw new Exception("Group does not exist");
        if(!checkUserExistsInGroup(group, sender)) throw  new Exception("You are not allowed to send message");

        List<Message> messages = new ArrayList<>();
        if(groupMessageMap.containsKey(group)) messages = groupMessageMap.get(group);

        messages.add(message);
        groupMessageMap.put(group, messages);
        senderMap.put(message, sender);
        return messages.size();
    }

    //Function to check if user exist in the group or not
    public boolean checkUserExistsInGroup(Group group, User sender) {
        List<User> users = groupUserMap.get(group);
        for(User user: users) {
            if(user.equals(sender)) return true;
        }

        return false;
    }


    // Change Admin
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!groupUserMap.containsKey(group)) throw new Exception("Group does not exist");
        if(!adminMap.get(group).equals(approver)) throw new Exception("Approver does not have rights");
        if(!checkUserExistsInGroup(group, user)) throw  new Exception("User is not a participant");

        adminMap.put(group, user); // user became the Admin now
        return "SUCCESS";
    }


    //Remove a user from a group by providing the user
//    public int removeUser(User user) throws Exception {
//        int updatedUsersInGroup = 0;
//        int updatedMessagesInGroup = 0;
//        int overallMessages = 0;
//        // i will traverse in all groups and if in some group i found this user then that will be the Group
//        for (Group group : groupUserMap.keySet()) {
//            List<User> userListOfGroup = groupUserMap.get(group);
//            for (User user1 : userListOfGroup) {  // User list of all groups
//                if(user1.equals(user)) { // so this is my Group of Interest
//                    if(adminMap.get(group).equals(user)) throw new Exception("Cannot remove admin");
//                    // not a admin
//                    userListOfGroup.remove(user);
//                    updatedUsersInGroup = userListOfGroup.size();
//
//                    // now i will delete all messages of this user
//                    List<Message> messageList = groupMessageMap.get(group);  // all messages of this group
//                    for (Message message : senderMap.keySet()) {  // map which have all messages with its user
//                        if(senderMap.get(message).equals(user)) {  // user of this message is same as our user then,
//                            for (Message message1 : messageList) {  // this loop will remove all messages of this User
//                                if(message1.equals(message)) messageList.remove(message);
//                            }
//                            senderMap.remove(message); // also removed from sender map
//                            // now all processing done so,
//                            break;
//                        }
//                    }
//                    updatedMessagesInGroup = messageList.size();
//                    overallMessages = senderMap.size();
//                }
//            }
//        }
//
//        return updatedUsersInGroup + updatedMessagesInGroup + overallMessages;
//    }

    //    User foundUser = null;
    private boolean checkUserInAllGroups(User user) {
        for (Group group : groupUserMap.keySet()) {
            List<User> users = groupUserMap.get(group);
            for (User user1 : users) {
                if(user1.equals(user)) {
//                    foundUser = user1;
                    userGroup = group;
                    return true;
                }
            }
        }
        return false;
    }

    //remove user
    public int removeUser(User user) throws Exception {
        //If the user is not found in any group, the application will throw an exception.
        if(!checkUserInAllGroups(user))
            throw new Exception("User not found");
        else { //If the user is found in a group and is the admin, the application will throw an exception.
            if (adminMap.get(userGroup).equals(user))
                throw new Exception("Cannot remove admin");
        }

        //valid user to remove
        if(groupUserMap.containsKey(userGroup)) {
            groupUserMap.get(userGroup).remove(user); //taken list and removed user
        }
        //remove all its messages
        for (Message message : senderMap.keySet()) {
            if(senderMap.get(message).equals(user)) {
                senderMap.remove(message); //message is a key
                dateMessageMap.get(message.getTimestamp()).remove(message);
                groupMessageMap.get(userGroup).remove(message); //this message deleted form group also
            }
        }

        int updatedUsersInGroup = groupUserMap.get(userGroup).size();
        int updatedMessagesInGroup = groupMessageMap.get(userGroup).size();

        /**int updatedMessagesOfAllGroups = 0; //O(N)
         for (Group group : groupMessageMap.keySet()) {
         updatedMessagesOfAllGroups += groupMessageMap.get(group).size();
         }
         **/
        int updatedMessagesOfAllGroups = senderMap.size(); //O(1)
        return updatedUsersInGroup + updatedMessagesInGroup + updatedMessagesOfAllGroups;
    }



    //find messages between start and end date
    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message> messageList = new ArrayList<>();

        for (Date date : dateMessageMap.keySet()) {
            if(date.compareTo(start) > 0 && date.compareTo(end) < 0) {
                messageList = dateMessageMap.get(date);
            }
        }

        if(messageList.size() < k) throw new Exception("K is greater than the number of messages");
        String kthLatestMessage = messageList.get(k-1).getContent();
        return kthLatestMessage;
    }
}
