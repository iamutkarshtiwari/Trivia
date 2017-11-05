package io.github.iamutkarshtiwari.trivia.models;

/**
 * Created by utkarshtiwari on 05/11/17.
 */

public class Question {
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    String question;
    String answer;

    public Question(String q, String a) {
        question = q;
        answer = a;
    }
}
