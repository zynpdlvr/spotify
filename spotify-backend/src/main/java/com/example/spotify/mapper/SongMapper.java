
package com.example.spotify.mapper;

import com.example.spotify.dto.SongDTO;
import com.example.spotify.entity.Song;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SongMapper {

    // Entity -> Response DTO
    @Mapping(target = "albumPublicId", source = "album.publicId")
    @Mapping(target = "albumName", source = "album.name")
    SongDTO.SongResponse toResponse(Song song);

    // Request DTO -> yeni Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(
            target = "title",
            expression = "java(request.title().trim())"
    )
    @Mapping(
            target = "artist",
            expression = "java(request.artist().trim())"
    )
    @Mapping(target = "playlists", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    Song toEntity(SongDTO.SongRequest request);

    // PUT: mevcut Entity'nin düzenlenebilir alanlarını güncelle
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(
            target = "title",
            expression = "java(request.title().trim())"
    )
    @Mapping(
            target = "artist",
            expression = "java(request.artist().trim())"
    )
    @Mapping(target = "playlists", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    void updateEntity(
            SongDTO.SongRequest request,
            @MappingTarget Song song
    );

    // PATCH: yalnızca null olmayan alanları güncelle
    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "playlists", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    void updatePartial(
            SongDTO.UpdateSongRequest request,
            @MappingTarget Song song
    );


}
