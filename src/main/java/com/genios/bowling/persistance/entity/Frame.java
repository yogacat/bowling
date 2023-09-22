package com.genios.bowling.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Frame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(10)
    private Integer frameNumber;

    @Column(name = "user_id")
    private Long userId;

    @Min(0)
    @Max(300)
    private Integer frameScore;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Player player;

    @OneToMany(mappedBy = "frame")
    private List<Roll> rolls;
}
