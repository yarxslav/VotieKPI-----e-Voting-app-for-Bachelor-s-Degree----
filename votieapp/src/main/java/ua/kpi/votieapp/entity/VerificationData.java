package ua.kpi.votieapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import ua.kpi.votieapp.model.UserStatus;

@Entity
@Table(name = "verification_data")
@Data
public class VerificationData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "status")
    private UserStatus userStatus;

    @Column(name = "image_name", unique = true)
    private String imageName;

    @Column(name = "comment", columnDefinition = "LONGTEXT")
    private String comment;
}
