package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.Question;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIGenerateQuestionsRequest {

    @NotNull(message = "bankId is required")
    private Integer bankId;

    @NotBlank(message = "subject is required")
    private String subject;

    @NotBlank(message = "topic is required")
    private String topic;

    private Question.Difficulty difficulty = Question.Difficulty.MEDIUM;

    @NotNull(message = "type is required")
    private Question.QuestionType type;

    @Min(value = 1, message = "count must be at least 1")
    @Max(value = 20, message = "count must not exceed 20")
    private int count = 5;

    private boolean saveToDb = false;

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Question.Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Question.Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Question.QuestionType getType() {
        return type;
    }

    public void setType(Question.QuestionType type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isSaveToDb() {
        return saveToDb;
    }

    public void setSaveToDb(boolean saveToDb) {
        this.saveToDb = saveToDb;
    }
}
