package uk.gov.hmcts.reform.roleassignment.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "databasechangeloglock")
public class DatabaseChangelogLockEntity {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "locked")
    private boolean locked;

    @Column(name = "lockgranted")
    private Date lockgranted;

    @Column(name = "lockedby")
    private String lockedby;

}
