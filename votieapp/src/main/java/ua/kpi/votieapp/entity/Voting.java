package ua.kpi.votieapp.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import ua.kpi.votieapp.model.VotingStatus;

@Entity
@Table(name = "votings")
@Data
public class Voting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 1000, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "LONGTEXT", nullable = false)
    private String description;

    @Column(name = "status", length = 100, nullable = false)
    private VotingStatus status;

    @Column(name = "date_time_from", nullable = false)
    private LocalDateTime dateTimeFrom;

    @Column(name = "date_time_to", nullable = false)
    private LocalDateTime dateTimeTo;

    @Column(name = "category", length = 100, nullable = false)
    private String category;

    @Column(name = "university", length = 300)
    private String university;

    @Column(name = "`group`", length = 100)
    private String group;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "votings_candidates",
            joinColumns = @JoinColumn(name = "voting_id"),
            inverseJoinColumns = @JoinColumn(name = "candidate_id"))
    private List<Candidate> candidates;
}