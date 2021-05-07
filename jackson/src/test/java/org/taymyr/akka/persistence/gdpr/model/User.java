package org.taymyr.akka.persistence.gdpr.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.taymyr.akka.persistence.gdpr.WithDataSubjectId;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private final String login;
    private final WithDataSubjectId<IdentityCard> identityCard;

    @JsonCreator
    public User(String login, WithDataSubjectId<IdentityCard> identityCard) {
        this.login = login;
        this.identityCard = identityCard;
    }

    public String getLogin() {
        return login;
    }

    public WithDataSubjectId<IdentityCard> getIdentityCard() {
        return identityCard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login) && Objects.equals(identityCard, user.identityCard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, identityCard);
    }
}
