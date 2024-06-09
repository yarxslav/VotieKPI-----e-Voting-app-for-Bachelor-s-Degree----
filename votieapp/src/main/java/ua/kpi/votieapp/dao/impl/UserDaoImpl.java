package ua.kpi.votieapp.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import ua.kpi.votieapp.dao.UserDao;
import ua.kpi.votieapp.dto.LoginDto;
import ua.kpi.votieapp.entity.Role;
import ua.kpi.votieapp.entity.User;
import ua.kpi.votieapp.exception.UserDeletionException;
import ua.kpi.votieapp.model.UserStatus;

@Repository
@Transactional
public class UserDaoImpl implements UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(User user) {
        entityManager.persist(user);

        TypedQuery<Role> query = entityManager.createQuery("FROM Role WHERE name = :roleName", Role.class);
        query.setParameter("roleName", "user");
        Role userRole = query.getSingleResult();
        user.getRoles().add(userRole);

        entityManager.merge(user);
    }

    @Override
    public Optional<User> login(LoginDto loginDto) {
        try {
            TypedQuery<User> query = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username AND u.password = :password", User.class);
            query.setParameter("username", loginDto.getLogin());
            query.setParameter("password", loginDto.getPassword());
            User user = query.getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> get(Long userId) {
        User user = entityManager.find(User.class, userId);
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> getAll() {
        TypedQuery<User> query = entityManager.createQuery("FROM User", User.class);
        return query.getResultList();
    }

    @Override
    public User update(User user) {
        return entityManager.merge(user);
    }

    @Override
    public void delete(User user) {
        try {
            entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
        } catch (Exception e) {
            throw new UserDeletionException("Error deleting a user: " + user.getId());
        }
    }

    @Override
    public String getImageName(Long userId) {
        try {
            TypedQuery<String> query = entityManager.createQuery(
                    "SELECT v.imageName FROM User u JOIN u.verificationData v WHERE u.id = :userId", String.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public String getPassword(String email) {
        try {
            TypedQuery<String> query = entityManager.createQuery(
                    "SELECT u.password FROM User u WHERE u.email = :email", String.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public UserStatus getUserStatus(Long userId) {
        try {
            TypedQuery<UserStatus> query = entityManager.createQuery(
                    "SELECT u.verificationData.userStatus FROM User u WHERE u.id = :userId", UserStatus.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean isEmailExists(String email) {
        TypedQuery<Long> query = entityManager.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
        query.setParameter("email", email);
        return query.getSingleResult() != 0;
    }

    @Override
    public boolean isUsernameExists(String username) {
        TypedQuery<Long> query = entityManager.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :login", Long.class);
        query.setParameter("login", username);
        return query.getSingleResult() != 0;
    }
}
