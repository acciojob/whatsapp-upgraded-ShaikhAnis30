package com.driver;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WhatsappService {
    WhatsappRepository whatsappRepository = new WhatsappRepository();

    public String createUser(String name, String mobNo) throws Exception {
        return whatsappRepository.createUser(name, mobNo);
    }

    public Group createGroup(List<User> user) {
        return whatsappRepository.createGroup(user);
    }

    public int createMessage(String messageContent){
        return whatsappRepository.createMessage(messageContent);
    }


    public int sendMessage(Message message, User sender, Group group) throws Exception{
        return whatsappRepository.sendMessage(message, sender, group);
    }


    public String changeAdmin(User approver, User user, Group group) throws Exception {
        return whatsappRepository.changeAdmin(approver, user, group);
    }

    public int removeUser(User user) throws Exception {
        return whatsappRepository.removeUser(user);
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        return whatsappRepository.findMessage(start, end, k);
    }
}
