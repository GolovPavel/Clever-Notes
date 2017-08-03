package ru.pavel.noteproject.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ru.pavel.noteproject.model.User;

/**
 * Created by pavel on 23.07.17.
 */
public class UserDao {
    private static UserDao instance;
    private static final Logger LOGGER = LogManager.getLogger(UserDao.class);

    private UserDao() {

    }

    public static UserDao getInstance() {
        if (instance == null) {
            instance = new UserDao();
        }

        return instance;
    }

    public void registerUser(Session session, User user) {
        session.persist(user);
    }

    public User getUserByLogin(Session session, String login) {
        Criteria criteria = session.createCriteria(User.class);
        return (User) criteria.add(Restrictions.eq("login", login)).uniqueResult();
    }

}
