package ua.kpi.votieapp.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.votieapp.dao.VoteResultDao;
import ua.kpi.votieapp.entity.VoteResult;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class VoteResultDaoImpl implements VoteResultDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(VoteResult voteResult) {
        entityManager.persist(voteResult);
    }

    @Override
    public Optional<VoteResult> get(Long voteResultId) {
        VoteResult voteResult = entityManager.find(VoteResult.class, voteResultId);
        return Optional.ofNullable(voteResult);
    }

    @Override
    public List<VoteResult> getAll() {
        TypedQuery<VoteResult> query = entityManager.createQuery("FROM VoteResult", VoteResult.class);
        return query.getResultList();
    }

    @Override
    public VoteResult update(VoteResult voteResult) {
        return entityManager.merge(voteResult);
    }

    @Override
    public void delete(VoteResult voteResult) {
        try {
            entityManager.remove(entityManager.contains(voteResult) ? voteResult : entityManager.merge(voteResult));
        } catch (Exception e) {
            throw new RuntimeException("Error deleting a vote result: " + voteResult.getId(), e);
        }
    }

    @Override
    public boolean hasUserVoted(Long userId, Long votingId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(vr) FROM VoteResult vr WHERE vr.user.id = :userId AND vr.voting.id = :votingId", Long.class);
        query.setParameter("userId", userId);
        query.setParameter("votingId", (votingId));

        Long count = query.getSingleResult();
        return count > 0;
    }

    @Override
    public List<VoteResult> getByVotingId(Long votingId) {
        TypedQuery<VoteResult> query = entityManager.createQuery(
                "SELECT vr FROM VoteResult vr WHERE vr.voting.id = :votingId", VoteResult.class);
        query.setParameter("votingId", votingId);
        return query.getResultList();
    }
}
