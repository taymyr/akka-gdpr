package org.taymyr.akka.persistence.gdpr.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public class IdentityCard implements Serializable {
    private final String firstName;
    private final String lastName;
    private final String series;
    private final String number;

    @JsonCreator
    public IdentityCard(String firstName, String lastName, String series, String number) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.series = series;
        this.number = number;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSeries() {
        return series;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentityCard that = (IdentityCard) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(series, that.series) && Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, series, number);
    }
}
