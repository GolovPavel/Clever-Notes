package ru.pavel.noteproject.service;

import org.apache.commons.codec.digest.DigestUtils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.pavel.noteproject.dao.Database;
import ru.pavel.noteproject.dao.TokenDao;
import ru.pavel.noteproject.dao.UserDao;
import ru.pavel.noteproject.model.Token;
import ru.pavel.noteproject.model.User;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by pavel on 23.07.17.
 */
public class AuthService {

    private static final Logger LOGGER = LogManager.getLogger(AuthService.class);

    private static SecureRandom random = new SecureRandom();

    private static AuthService instance;

    private AuthService() {
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }

        return instance;
    }

    public void registerUser(String login, String password) throws UserException {
        Transaction tnx = null;
        try (Session session = Database.session()) {
            tnx = session.beginTransaction();

            User newUser = new User()
                    .setLogin(login)
                    .setPasswordHash(makePasswordHash(password));

            UserDao.getInstance().registerUser(session, newUser);

            tnx.commit();

            LOGGER.info("User {} registered!", newUser.getLogin());
        } catch (RuntimeException e) {
            if (tnx != null && tnx.isActive()) {
                tnx.rollback();
            }
            LOGGER.warn("Something went wrong while register user.");
            throw new UserException(e);
        }
    }

    public String loginUser(String login) throws UserException {
        Transaction tnx = null;
        try (Session session = Database.session()) {
            tnx = session.beginTransaction();

            User registeredUser = UserDao.getInstance().getUserByLogin(session, login);
            String userToken = makeToken();

            Token token = new Token()
                    .setUser(registeredUser)
                    .setToken(userToken);

            TokenDao.getInstance().loginUser(session, token);

            tnx.commit();

            LOGGER.info("User {} logined!", registeredUser.getLogin());
            return userToken;
        } catch (RuntimeException e) {
            if (tnx != null && tnx.isActive()) {
                tnx.rollback();
            }

            LOGGER.warn("Something went wrong while login user");
            throw new UserException(e);
        }
    }

    public void logout(String token) throws UserException {
        Transaction tnx = null;
        try (Session session = Database.session()) {
            tnx = session.beginTransaction();

            TokenDao tokenDao = TokenDao.getInstance();
            Token existingToken = tokenDao.getTokenByToken(session, token);
            tokenDao.logout(session, existingToken);
            tnx.commit();
            LOGGER.info("User with token {} is logged out!");
        } catch (RuntimeException e) {
            if (tnx != null && tnx.isActive()) {
                tnx.rollback();
            }

            LOGGER.warn("Something went wrong while login user");
            throw new UserException(e);
        }
    }

    public static String makePasswordHash(String password) {
        return DigestUtils.md5Hex(password);
    }

    private static String makeToken() {
        return new BigInteger(130, random).toString(32);
    }
}

