package com.genios.bowling.persistance.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Data
public class Roll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "frame_id")
    private Long frameId;

    @Min(1)
    @Max(3)
    private Integer rollNumber;

    @Min(0)
    @Max(10)
    private Integer pins;

    @Nullable
    @Pattern(regexp = "[-X/]")
    private String status;

    @ManyToOne
    @JoinColumn(name = "frame_id", insertable = false, updatable = false)
    private Frame frame;
}
