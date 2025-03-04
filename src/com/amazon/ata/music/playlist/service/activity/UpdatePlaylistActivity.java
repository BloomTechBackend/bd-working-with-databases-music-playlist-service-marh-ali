package com.amazon.ata.music.playlist.service.activity;

import com.amazon.ata.music.playlist.service.converters.ModelConverter;
import com.amazon.ata.music.playlist.service.dynamodb.models.Playlist;
import com.amazon.ata.music.playlist.service.exceptions.InvalidAttributeChangeException;
import com.amazon.ata.music.playlist.service.exceptions.InvalidAttributeValueException;
import com.amazon.ata.music.playlist.service.exceptions.PlaylistNotFoundException;
import com.amazon.ata.music.playlist.service.models.PlaylistModel;
import com.amazon.ata.music.playlist.service.models.requests.UpdatePlaylistRequest;
import com.amazon.ata.music.playlist.service.models.results.UpdatePlaylistResult;
import com.amazon.ata.music.playlist.service.dynamodb.PlaylistDao;

import com.amazon.ata.music.playlist.service.util.MusicPlaylistServiceUtils;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the UpdatePlaylistActivity for the MusicPlaylistService's UpdatePlaylist API.
 *
 * This API allows the customer to update their saved playlist's information.
 */
public class UpdatePlaylistActivity implements RequestHandler<UpdatePlaylistRequest, UpdatePlaylistResult> {
    private final Logger log = LogManager.getLogger();
    private final PlaylistDao playlistDao;

    /**
     * Instantiates a new UpdatePlaylistActivity object.
     *
     * @param playlistDao PlaylistDao to access the playlist table.
     */
    @Inject
    public UpdatePlaylistActivity(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
    }

    /**
     * This method handles the incoming request by retrieving the playlist, updating it,
     * and persisting the playlist.
     * <p>
     * It then returns the updated playlist.
     * <p>
     * If the playlist does not exist, this should throw a PlaylistNotFoundException.
     * <p>
     * If the provided playlist name or customer ID has invalid characters, throws an
     * InvalidAttributeValueException
     * <p>
     * If the request tries to update the customer ID,
     * this should throw an InvalidAttributeChangeException
     *
     * @param updatePlaylistRequest request object containing the playlist ID, playlist name, and customer ID
     *                              associated with it
     * @return updatePlaylistResult result object containing the API defined {@link PlaylistModel}
     */
    @Override
    public UpdatePlaylistResult handleRequest(final UpdatePlaylistRequest updatePlaylistRequest, Context context) {
        log.info("Received UpdatePlaylistRequest {}", updatePlaylistRequest);

        // validate playlist name from request
        if (!MusicPlaylistServiceUtils.isValidString(updatePlaylistRequest.getName())) throw new InvalidAttributeValueException();

        String playlistID = updatePlaylistRequest.getId();

        // propagate the exception if getPlaylist return with no item data
        try {
            playlistDao.getPlaylist(playlistID);
        } catch (PlaylistNotFoundException e) {
            throw new PlaylistNotFoundException();
        }

        Playlist playlist = playlistDao.getPlaylist(playlistID);

        // validate playlist customer ID matches request customer ID
        if (!(playlist.getCustomerId().equals(updatePlaylistRequest.getCustomerId()))) throw new InvalidAttributeChangeException();

        //update playlist (*For now, only supports updating the playlist name. Don't modify any of the other attributes.*)
        playlist.setName(updatePlaylistRequest.getName());

        //save playlist to DynamoDB
        playlistDao.savePlaylist(playlist);

        //create empty tags Set in case the playlist tag is null.
        // null is preventing ModelConverter from compiling. If the tags set is empty, ModelConverter will set it back to null.

        if (playlist.getTags() == null) playlist.setTags(new HashSet<>());

        PlaylistModel playlistModel = new ModelConverter().toPlaylistModel(playlist);

        return UpdatePlaylistResult.builder()
                .withPlaylist(playlistModel)
                .build();
    }
}
