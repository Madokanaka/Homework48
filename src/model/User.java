package model;

import java.util.UUID;

public class User {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private Integer candidateVoted;

    public User() {
    }

    public User(String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getCandidateVoted() {
        return candidateVoted;
    }

    public void setCandidateVoted(Integer candidateVoted) {
        this.candidateVoted = candidateVoted;
    }
}
