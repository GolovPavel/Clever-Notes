package ru.pavel.noteproject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hibernate.Session;
import ru.pavel.noteproject.dao.Database;
import ru.pavel.noteproject.dao.NoteDao;
import ru.pavel.noteproject.dao.TokenDao;
import ru.pavel.noteproject.model.Note;
import ru.pavel.noteproject.model.Token;
import ru.pavel.noteproject.service.NoteException;
import ru.pavel.noteproject.service.NoteService;

import javax.ws.rs.Consumes;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import java.util.List;

/**
 * Created by pavel on 28.07.17.
 */
@Path("/notes")
public class NoteResources {

    /**а
     * Method add new note to database
     *
     * @param json - json object, matching the pattern:
     *             {"token": %token, "capture": %capture, "content": %content}
     * @return Response object
     */
    @POST
    @Path("/add")
    @Consumes("application/json")
    public Response addNote(String json) {
        //Parsing input json
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject mainObject = parser.parse(json).getAsJsonObject();

        String token = mainObject.get("token").getAsString();
        String capture = mainObject.get("capture").getAsString();
        String content = mainObject.get("content").getAsString();

        if (token == null || capture == null || content == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Illegal format of params.").build();
        }

        Token existingToken = TokenDao.getInstance().getTokenByToken(Database.session(), token);

        if (existingToken == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User not logged").build();
        }

        try {
            NoteService.getInstance().addNote(token, capture, content);
            return Response.ok("Note added successful!").build();
        } catch (NoteException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Something went wrong.").build();
        }
    }

    /**
     * This method return json object, contains list of Notes of user with token <code>userToken</code>.
     *
     * @return Response, contain's json of list of Notes objects.
     */
    @POST
    @Path("/getAll")
    @Consumes("application/x-www-form-urlencoded")
    public Response getNotes(String userToken) {
        Token existingToken = TokenDao.getInstance().getTokenByToken(Database.session(), userToken);

        if (existingToken == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User not logged").build();
        }

        List<Note> allNotes = NoteDao.getInstance().getNotes(Database.session(), userToken);

        if (allNotes.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).entity("Notes not found").build();
        }

        Gson gson = new Gson();
        String json = gson.toJson(allNotes);


        return Response.ok(json).build();
    }

    /**
     * This method return json object, contains one note with certain id.
     *
     * @param data - data in format "token&id", where id - identificatior of note;
     * @return Response, contains one note with certain id in json format.
     */
    @POST
    @Path("/get")
    @Consumes("application/x-www-form-urlencoded")
    public Response getNote(String data) {
        String[] splitedData = data.split("&");

        String token = splitedData[0];
        long id = Long.parseLong(splitedData[1]);

        //The session for validate parameters
        Session session = Database.session();

        Token existingToken = TokenDao.getInstance().getTokenByToken(session, token);

        if (existingToken == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User not logged").build();
        }

        Note note = NoteDao.getInstance().getNoteById(session, id);

        if (note == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Note doesn't exist").build();
        }

        session.close();

        Gson gson = new Gson();
        String responseJSON = gson.toJson(note);

        return Response.ok(responseJSON).build();
    }

    /**
     * Edit note with certain id, set new content, capture for note.
     *
     * @param jsonString - json string in format {"token": %userToken, "noteId": %id, "caption": %caption, "content": %content}
     * @return Response 200ok or error message.
     */
    @POST
    @Path("/edit")
    @Consumes("application/json")
    public Response editNote(String jsonString) {
        //Parsing input json
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject mainObject = parser.parse(jsonString).getAsJsonObject();

        String token = mainObject.get("token").getAsString();
        long noteId = mainObject.get("noteId").getAsLong();
        String caption = mainObject.get("caption").getAsString();
        String content = mainObject.get("content").getAsString();

        if (caption == null || content == null || caption.equals("") || content.equals("")) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Capture or content is empty").build();
        }
        //The session for validate parameters
        Session session = Database.session();

        Token existingToken = TokenDao.getInstance().getTokenByToken(session, token);

        if (existingToken == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User not logged").build();
        }

        Note note = NoteDao.getInstance().getNoteById(session, noteId);

        if (note == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Note doesn't exist").build();
        }

        session.close();

        try {
            NoteService.getInstance().updateNote(noteId, caption, content);
            return Response.ok("Note updated successful!").build();
        } catch (NoteException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Something went wrong.").build();
        }
    }


    /**
     * Delete note with certain <code>id</code>.
     *
     * @param data - data in format "token&id", where id - identificatior of note;
     * @return Response object.
     */
    @POST
    @Path("/delete")
    @Consumes("application/x-www-form-urlencoded")
    public Response deleteNote(String data) {
        String[] dataArr = data.split("&");
        String userToken = dataArr[0];
        long longId = Long.parseLong(dataArr[1]);

        //The session for validate parameters;
        Session sess = Database.session();

        Token token = TokenDao.getInstance().getTokenByToken(sess, userToken);

        if (token == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User not logged.").build();
        }

        Note currentNote = NoteDao.getInstance().getNoteById(sess, longId);

        if (currentNote == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Note doesn't exist.").build();
        }

        sess.close();

        try {
            NoteService.getInstance().deleteNote(longId);
            return Response.ok("Note Deleted").build();
        } catch (NoteException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Something went wrong.").build();
        }
    }
}
