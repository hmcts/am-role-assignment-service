package uk.gov.hmcts.reform.roleassignment.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "id_seq", sequenceName = "id_seq", allocationSize = 1)
@Entity(name = "flag_config")
public class FlagConfig {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    private Long id;

    @Column(name = "flag_name", nullable = false)
    private String flagName;

    @Column(name = "env", nullable = false)
    private String env;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "status", nullable = false)
    private Boolean status;

}
