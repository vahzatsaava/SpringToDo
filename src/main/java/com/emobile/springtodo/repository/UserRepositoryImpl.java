package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.utills.HibernateUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {


    @Override
    public void save(User user) {

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.createQuery(SELECT_BY_USERNAME, User.class)
                    .setParameter("username", username)
                    .uniqueResult();

            return user != null ? Optional.of(user) : Optional.empty();
        }
    }
}
