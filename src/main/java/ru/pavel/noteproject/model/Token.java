package ru.pavel.noteproject.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "tokens", schema = "noteService")
public class Token implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne(cascade = CascadeType.PERSIST)
    private User user;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    public long getId() {
        return id;
    }

    public Token setId(long id) {
        this.id = id;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Token setUser(User user) {
        this.user = user;
        return this;
    }

    public String getToken() {
        return token;
    }

    public Token setToken(String token) {
        this.token = token;
        return this;
    }
}

