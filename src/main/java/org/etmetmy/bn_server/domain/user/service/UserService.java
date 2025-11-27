package org.etmetmy.bn_server.domain.user.service;

import org.etmetmy.bn_server.domain.user.dto.entity.UserDto;

public interface UserService {

    Long joinUser(UserDto userDto);
}
