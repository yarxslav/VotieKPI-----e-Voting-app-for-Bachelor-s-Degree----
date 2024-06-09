package ua.kpi.votieapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "vote-result")
@Data
public class VoteResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voting")
    private Voting voting;

    @ManyToOne
    @JoinColumn(name = "user")
    private User user;

    @ManyToOne
    @JoinColumn(name = "candidate")
    private Candidate candidate;

    public VoteResult() {
    }

    public VoteResult(Voting voting, User user, Candidate candidate) {
        this.voting = voting;
        this.user = user;
        this.candidate = candidate;
    }
}
