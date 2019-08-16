package uk.gov.hmcts.reform.idam.client.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class UserDetails {

    private final String id;
    private final String email;

    private final String forename;
    private final String surname;
    private final List<String> roles;

    public Optional<String> getSurname() {
        return Optional.ofNullable(surname);
    }

    @JsonIgnore
    public String getFullName() {
        return getSurname().map(s -> String.join(" ", forename, s))
            .orElse(forename);
    }
}
