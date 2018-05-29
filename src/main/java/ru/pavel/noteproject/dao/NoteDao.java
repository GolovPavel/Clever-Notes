package ru.pavel.noteproject.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ru.pavel.noteproject.model.Note;
import ru.pavel.noteproject.model.User;

import java.util.List;

/**
 * Created by pavel on 28.07.17.
 */
public class NoteDao {
    private static NoteDao instance;

    private NoteDao() {
    }

    public static NoteDao getInstance() {
        if (instance == null) {
            instance = new NoteDao();
        }

        return instance;
    }

    public void addNote(Session session, Note note) {
        session.persist(note);
    }

    public List<Note> getNotes(Session session, String token) {
        User user = TokenDao.getInstance().getUserByToken(session, token);
        Criteria criteria = session.createCriteria(Note.class);
        return criteria.add(Restrictions.eq("user", user)).list();
    }

    public Note getNoteById(Session session, long id) {
        Criteria criteria = session.createCriteria(Note.class);
        return (Note) criteria.add(Restrictions.eq("id", id)).uniqueResult();
    }

    public void deleteNote(Session session, Note note) {
        session.delete(note);
    }

    public void updateNote(Session session, long id, String caption, String content) {
        Note note = session.load(Note.class, id);
        note.setCaption(caption);
        note.setContent(content);
        session.flush();
    }

}
