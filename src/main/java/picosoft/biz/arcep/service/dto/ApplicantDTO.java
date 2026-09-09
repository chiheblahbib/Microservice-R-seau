package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.shared.enumeration.ApplicantType;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private ApplicantType applicantType;

    private String applicantName;

    private String qualification;

    private String company;

    private String tradeRegisterNumber;

    private String nationality;

    private String nationalityComplet;

    private String address;

    private String phone;

    private String fax;

    private String email;

    private String website;

}
