package ru.pavel.noteproject;

import ru.pavel.noteproject.dao.Database;
import ru.pavel.noteproject.dao.TokenDao;
import ru.pavel.noteproject.dao.UserDao;
import ru.pavel.noteproject.model.Token;
import ru.pavel.noteproject.model.User;
import ru.pavel.noteproject.service.UserException;
import ru.pavel.noteproject.service.AuthService;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;


@Path("/auth")
public class AuthResources {

    @POST
    @Path("/register")
    @Consumes("application/x-www-form-urlencoded")
    public Response register(@FormParam("login") String login, @FormParam("password") String password) {
        if (login == null || password == null
                || login.length() == 0 || password.length() == 0
                || login.length() > 20 || password.length() > 20) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Illegal format of params").build();
        }

        User registeredUser = UserDao.getInstance().getUserByLogin(Database.session(), login);
        if (registeredUser != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User already registered").build();
        }

        try {
            AuthService.getInstance().registerUser(login, password);
        } catch (UserException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Something went wrong").build();
        }

        return Response.ok("Success registration!").build();
    }

    @POST
    @Path("/login")
    @Consumes("application/x-www-form-urlencoded")
    public Response login(@FormParam("login") String login, @FormParam("password") String password) {
        if (login == null || password == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Illegal format of params").build();
        }


        User registeredUser = UserDao.getInstance().getUserByLogin(Database.session(), login);
        if (registeredUser == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("User doesn't registered").build();
        }

        if (!registeredUser.getPasswordHash().equals(AuthService.makePasswordHash(password))) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Wrong password").build();
        }


        try {
            Token existingToken = TokenDao.getInstance().getTokenByLogin(Database.session(), login);
            if (existingToken != null) {
                AuthService.getInstance().logout(existingToken.getToken());
            }
            String token = AuthService.getInstance().loginUser(login);
            return Response.ok(token).build();
        } catch (UserException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Something went wrong").build();
        }
    }

    @POST
    @Path("/logout")
    @Consumes("application/x-www-form-urlencoded")
    public Response logout(@FormParam("token") String token) {
        if (token == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Illegal format of params").build();
        }

        Token loggedToken = TokenDao.getInstance().getTokenByToken(Database.session(), token);

        if (loggedToken == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Sorry, you don't logged").build();
        }

        try {
            AuthService.getInstance().logout(token);
            return Response.ok("User logged out successful!").build();
        } catch (UserException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Something went wrong").build();
        }

    }

}