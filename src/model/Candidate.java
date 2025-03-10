package model;

public class Candidate {
    private Integer id;
    private String name;
    private String photo;
    private Integer votes;

    public Candidate() {
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoto() {
        return photo;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
