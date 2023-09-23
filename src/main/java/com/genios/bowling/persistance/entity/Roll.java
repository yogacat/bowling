package com.genios.bowling.persistance.entity;

import com.genios.bowling.record.response.RollScore;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "roll", uniqueConstraints = @UniqueConstraint(columnNames = {"frame_id", "roll_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Roll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "frame_id")
    @NonNull
    private Long frameId;

    @Column(name = "roll_number")
    @Min(1)
    @Max(3)
    @NonNull
    private Integer rollNumber;

    @Min(0)
    @Max(10)
    @NonNull
    private Integer pins;

    @Nullable
    @Pattern(regexp = "[-X/]")
    private String status;

    @ManyToOne
    @JoinColumn(name = "frame_id", insertable = false, updatable = false)
    private Frame frame;

    public RollScore convertToRecord() {
        return new RollScore(this.id, this.rollNumber, this.pins, this.getStatus());
    }
}
