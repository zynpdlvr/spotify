
package com.example.spotify.mapper;

import com.example.spotify.dto.AuthDTO;
import com.example.spotify.entity.AppUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    AuthDTO.UserResponse toResponse(AppUser user);
}
