package com.genios.bowling.persistance.entity;

import com.genios.bowling.record.response.FrameScore;
import com.genios.bowling.record.response.IntermediateScore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import java.util.Comparator;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(
        min = 2,
        max = 25)
    @NonNull
    @NotEmpty
    private String name;

    @Min(0)
    @Max(300)
    private Integer totalScore;

    private boolean isFinished;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private List<Frame> frames;

    public IntermediateScore getIntermediateScore() {
        return new IntermediateScore(this.id, this.name, this.isFinished, this.totalScore,
            this.getFrames().stream()
                .map(Frame::convertToRecord)
                .sorted(Comparator.comparing(FrameScore::frameNumber))
                .toList());
    }
}
