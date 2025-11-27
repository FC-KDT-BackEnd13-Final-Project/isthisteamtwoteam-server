package org.etmetmy.bn_server.domain.user.service;

import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;
import org.etmetmy.bn_server.domain.user.dto.request.UserLoginDto;
import org.etmetmy.bn_server.domain.user.entity.User;

public interface UserService {

    Long joinUser(UserDto userDto);
    User login(UserLoginDto loginDto);
}
