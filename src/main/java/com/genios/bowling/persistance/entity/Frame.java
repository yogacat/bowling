package com.genios.bowling.persistance.entity;

import com.genios.bowling.record.response.FrameScore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "frame", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "frame_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Frame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(10)
    @Column(name = "frame_number")
    private Integer frameNumber;

    @Column(name = "user_id")
    private Long userId;

    @Column(columnDefinition = "boolean default false")
    private boolean isFinalScore;

    @Min(0)
    @Max(300)
    private Integer frameScore;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Player player;

    @OneToMany(mappedBy = "frame", fetch = FetchType.EAGER)
    private List<Roll> rolls;

    public Frame(long id, int frameNumber, long userid, Player player) {
        this.id = id;
        this.frameNumber = frameNumber;
        this.userId = userid;
        this.player = player;
        this.rolls = List.of();
    }

    public FrameScore convertToRecord() {
        Integer score = this.isFinalScore ? this.frameScore : null;
        return new FrameScore(this.id, this.getFrameNumber(), this.isFinalScore, score,
            this.rolls.stream().map(Roll::convertToRecord).toList());
    }
}
