package ru.pavel.noteproject.model;

import org.hibernate.annotations.GeneratorType;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by pavel on 28.07.17.
 */
@Entity
@Table(name = "notes", schema = "noteService")
public class Note implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "caption", nullable = false)
    private String caption;

    @Column(name="content")
    private String content;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private User user;

    public long getId() {
        return id;
    }

    public Note setId(long id) {
        this.id = id;
        return this;
    }

    public String getCaption() {
        return caption;
    }

    public Note setCaption(String caption) {
        this.caption = caption;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Note setContent(String content) {
        this.content = content;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Note setUser(User user) {
        this.user = user;
        return this;
    }
}
