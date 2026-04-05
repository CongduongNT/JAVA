package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.Question;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QuestionCreateRequest {
    @NotNull(message = "bankId is required")
    private Integer bankId;

    @NotBlank(message = "content is required")
    private String content;

    @NotNull(message = "type is required")
    private Question.QuestionType type;

    private Question.Difficulty difficulty = Question.Difficulty.MEDIUM;
    private String topic;
    private List<Map<String, Object>> options;
    private String correctAnswer;
    private String explanation;

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Question.QuestionType getType() {
        return type;
    }

    public void setType(Question.QuestionType type) {
        this.type = type;
    }

    public Question.Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Question.Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<Map<String, Object>> getOptions() {
        return options;
    }

    public void setOptions(List<Map<String, Object>> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
