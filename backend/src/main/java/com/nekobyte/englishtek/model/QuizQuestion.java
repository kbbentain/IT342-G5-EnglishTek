package com.nekobyte.englishtek.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Integer page;

    @NotBlank
    private String type;

    @NotBlank
    private String title;

    @ElementCollection
    @CollectionTable(
        name = "quiz_question_choices",
        joinColumns = @JoinColumn(name = "question_id")
    )
    @Column(name = "choice")
    private List<String> choices = new ArrayList<>();

    @Column(columnDefinition = "json")
    private String correctAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    @NotNull
    private Quiz quiz;
}
