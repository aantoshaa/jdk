package com.example.demo.service.Interfaces;

import com.example.demo.dto.User.UnauthorizedUser;
import com.example.demo.dto.User.UserWithJwtToken;
import com.example.demo.entity.User;
import org.springframework.validation.BindingResult;

import javax.transaction.Transactional;
import java.util.Collection;


public interface UserService {
    UserWithJwtToken login(UnauthorizedUser User);
    @Transactional
    UserWithJwtToken register(UnauthorizedUser unauthorizedUser, BindingResult bindingResult);
    User getUserById(Long id);
    Collection<User> getAllUsers();
    Long getUserIdByLogin(String login);
    boolean isAdmin(String login);
}
