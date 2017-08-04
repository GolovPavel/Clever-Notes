package ru.pavel.noteproject.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ru.pavel.noteproject.model.Token;
import ru.pavel.noteproject.model.User;


/**
 * Created by pavel on 24.07.17.
 */
public class TokenDao {
    private static TokenDao instance;

    private TokenDao() {
    }

    public static TokenDao getInstance() {
        if (instance == null) {
            instance = new TokenDao();
        }

        return instance;
    }

    public void loginUser(Session session, Token token) {
        session.save(token);
    }

    public void logout(Session session, Token token) {
        session.delete(token);
    }

    public Token getTokenByLogin(Session session, String login) {
        User user = UserDao.getInstance().getUserByLogin(session, login);
        Criteria criteria = session.createCriteria(Token.class);
        return (Token) criteria.add(Restrictions.eq("user", user)).uniqueResult();
    }

    public Token getTokenByToken(Session session, String token) {
        Criteria criteria = session.createCriteria(Token.class);
        return (Token) criteria.add(Restrictions.eq("token", token)).uniqueResult();
    }

    public User getUserByToken(Session session, String token) {
        return getTokenByToken(session, token)
                .getUser();
    }
}
