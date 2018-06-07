package com.ucbcba.taller.services;

import com.ucbcba.taller.entities.User;

/**
 * Created by amolina on 10/05/17.
 */
public interface UserService {
    void save(User user);

    User findByUsername(String username);

    Iterable<User> listAllUsers();
}