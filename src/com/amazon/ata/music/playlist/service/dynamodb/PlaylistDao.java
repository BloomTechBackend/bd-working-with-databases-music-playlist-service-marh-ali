package com.amazon.ata.music.playlist.service.dynamodb;

import com.amazon.ata.music.playlist.service.dynamodb.models.Playlist;
import com.amazon.ata.music.playlist.service.exceptions.PlaylistNotFoundException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import javax.inject.Inject;
import java.util.List;

/**
 * Accesses data for a playlist using {@link Playlist} to represent the model in DynamoDB.
 */
public class PlaylistDao {
    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a PlaylistDao object.
     *
     * @param dynamoDbMapper the {@link DynamoDBMapper} used to interact with the playlists table
     */
    @Inject
    public PlaylistDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the {@link Playlist} corresponding to the specified id.
     *
     * @param id the Playlist ID
     * @return the stored Playlist, or null if none was found.
     */
    public Playlist getPlaylist(String id) throws PlaylistNotFoundException {
        Playlist playlist = this.dynamoDbMapper.load(Playlist.class, id);

        if (playlist == null) {
            throw new PlaylistNotFoundException("Could not find playlist with id " + id);
        }

        return playlist;
    }

    public Playlist savePlaylist(Playlist playlist) {
        this.dynamoDbMapper.save(playlist);
        return playlist;
    }
}
