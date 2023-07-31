package com.example.javamybot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    @Transactional
    public void saveUser(User user){
        userRepository.save(user);
    }


    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }
    @Transactional
    public void deleteUser(User user){
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByChatId(long chatId) {
        return userRepository.findByChatId(chatId);
    }


}
