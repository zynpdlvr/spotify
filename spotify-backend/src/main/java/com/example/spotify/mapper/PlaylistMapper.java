package com.example.spotify.mapper;

import com.example.spotify.dto.PlaylistDTO;
import com.example.spotify.entity.Playlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = SongMapper.class)
public interface PlaylistMapper {

    @Mapping(target = "ownerPublicId", source = "owner.publicId")
    PlaylistDTO.PlaylistResponse toResponse(Playlist playlist);
}