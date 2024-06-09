package ua.kpi.votieapp.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import ua.kpi.votieapp.dao.CandidateDao;
import ua.kpi.votieapp.entity.Candidate;
import ua.kpi.votieapp.exception.CandidateDeletionException;

@Repository
@Transactional
public class CandidateDaoImpl implements CandidateDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(Candidate candidate) {
        entityManager.persist(candidate);
    }

    @Override
    public Optional<Candidate> get(Long candidateId) {
        Candidate candidate = entityManager.find(Candidate.class, candidateId);
        return Optional.ofNullable(candidate);
    }

    @Override
    public List<Candidate> getAll() {
        TypedQuery<Candidate> query = entityManager.createQuery("FROM Candidate", Candidate.class);
        return query.getResultList();
    }

    @Override
    public List<Candidate> getAllForVote(Long voteId) {
        String sqlQuery = "SELECT c.id, c.name, c.surname, c.patronymic, c.image_name, c.speech FROM candidates c " +
                "JOIN votings_candidates vc ON c.id = vc.candidate_id " +
                "JOIN votings v ON v.id = vc.voting_id WHERE v.id = " + voteId + ";";
        Query query = entityManager.createNativeQuery(sqlQuery, Candidate.class);
        return query.getResultList();
    }

    @Override
    public Candidate update(Candidate candidate) {
        return entityManager.merge(candidate);
    }

    @Override
    public void delete(Candidate candidate) {
        try {
            entityManager.remove(entityManager.contains(candidate) ? candidate : entityManager.merge(candidate));
        } catch (Exception e) {
            throw new CandidateDeletionException("Error deleting a candidate: " + candidate.getId());
        }
    }

    @Override
    public String getImageName(Long candidateId) {
        try {
            TypedQuery<String> query = entityManager.createQuery(
                    "SELECT c.imageName FROM Candidate c WHERE c.id = :candidateId", String.class);
            query.setParameter("candidateId", candidateId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
