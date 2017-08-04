package ru.pavel.noteproject.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.pavel.noteproject.dao.Database;
import ru.pavel.noteproject.dao.NoteDao;
import ru.pavel.noteproject.dao.TokenDao;
import ru.pavel.noteproject.model.Note;
import ru.pavel.noteproject.model.User;

/**
 * Created by pavel on 28.07.17.
 */
public class NoteService {

    private static final Logger LOGGER = LogManager.getLogger(NoteService.class);
    private static NoteService instance;

    private NoteService() {

    }

    public static NoteService getInstance() {
        if (instance == null) {
            instance = new NoteService();
        }
        return instance;
    }

    public void addNote(String token, String capture, String content) throws NoteException {
        Transaction tnx = null;

        try (Session session = Database.session()) {
            tnx = session.beginTransaction();

            User currentUser = TokenDao.getInstance().getUserByToken(session, token);
            Note newNote = new Note()
                    .setCaption(capture)
                    .setContent(content)
                    .setUser(currentUser);

            NoteDao.getInstance().addNote(session, newNote);

            tnx.commit();

            LOGGER.info("Note with capture {} from token {} added successful!", capture, token);
        } catch (RuntimeException e) {
            if (tnx != null && tnx.isActive()) {
                tnx.rollback();
            }

            LOGGER.warn("Something went wrong while adding new note");
            throw new NoteException(e);
        }
    }

    public void deleteNote(long noteId) throws NoteException {
        Transaction tnx = null;

        try (Session session = Database.session()) {
            tnx = session.beginTransaction();
            Note note = NoteDao.getInstance().getNoteById(session, noteId);
            NoteDao.getInstance().deleteNote(session, note);
            tnx.commit();
            LOGGER.info("Note with id {} deleted successful!", note.getId());
        } catch (RuntimeException e) {
            if (tnx != null && tnx.isActive()) {
                tnx.rollback();
            }

            LOGGER.warn("Something went wrong while adding new note");
            throw new NoteException(e);
        }
    }

    public void updateNote(long noteId, String caption, String content) throws NoteException {
        Transaction tnx = null;

        try (Session session = Database.session()) {
            tnx = session.beginTransaction();
            NoteDao.getInstance().updateNote(session, noteId, caption, content);
            tnx.commit();
            LOGGER.info("Note with id {} updated successful!", noteId);
        } catch (RuntimeException e) {
            if (tnx != null && tnx.isActive()) {
                tnx.rollback();
            }

            LOGGER.warn("Something went wrong while updating note");
            throw new NoteException(e);
        }
    }
}
