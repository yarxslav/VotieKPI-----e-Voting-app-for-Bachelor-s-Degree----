package ua.kpi.votieapp.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import ua.kpi.votieapp.dao.VotingDao;
import ua.kpi.votieapp.entity.Voting;

@Repository
@Transactional
public class VotingDaoImpl implements VotingDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(Voting voting) {
        entityManager.persist(voting);
    }

    @Override
    public Optional<Voting> get(Long votingId) {
        Voting voting = entityManager.find(Voting.class, votingId);
        return Optional.ofNullable(voting);
    }

    @Override
    public List<Voting> getAll() {
        TypedQuery<Voting> query = entityManager.createQuery("FROM Voting", Voting.class);
        return query.getResultList();
    }

    @Override
    public Voting update(Voting voting) {
        return entityManager.merge(voting);
    }

    @Override
    public void delete(Voting voting) {
        try {
            entityManager.remove(entityManager.contains(voting) ? voting : entityManager.merge(voting));
        } catch (Exception e) {
            throw new RuntimeException("Error deleting a voting: " + voting.getId(), e);
        }
    }

    @Override
    public List<Voting> searchByName(String name) {
        TypedQuery<Voting> query = entityManager.createQuery("FROM Voting v WHERE v.name LIKE :name", Voting.class);
        query.setParameter("name", "%%%s%%".formatted(name));
        return query.getResultList();
    }

    @Override
    public List<Voting> searchByPublicId(String publicId) {
        TypedQuery<Voting> query = entityManager.createQuery(
                "SELECT vr.voting FROM VoteResult vr WHERE vr.user.voterPublicId = :publicId", Voting.class);
        query.setParameter("publicId", publicId);
        return query.getResultList();
    }

    @Override
    public List<String> getPublicIds(Long votingId) {
        TypedQuery<String> query = entityManager.createQuery(
                "SELECT vr.user.voterPublicId FROM VoteResult vr WHERE vr.voting.id = :votingId", String.class);
        query.setParameter("votingId", votingId);

        return query.getResultList();
    }
}
