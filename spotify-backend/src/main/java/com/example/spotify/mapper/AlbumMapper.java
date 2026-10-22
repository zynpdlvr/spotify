
package com.example.spotify.mapper;

import com.example.spotify.dto.AlbumDTO;
import com.example.spotify.entity.Album;
import org.mapstruct.Mapper;

//entity to song response dto
@Mapper(componentModel = "spring", uses = SongMapper.class)
public interface AlbumMapper {

    AlbumDTO.AlbumResponse toResponse(Album album);
}
