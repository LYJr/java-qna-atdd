package codesquad.web;

import static codesquad.domain.QuestionTest.createByLoginUser;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import codesquad.domain.Answer;
import codesquad.domain.AnswerRepository;
import codesquad.domain.Question;
import codesquad.domain.QuestionRepository;
import support.test.BasicAuthAcceptanceTest;
import support.test.HtmlFormDataBuilder;

import java.util.Optional;

public class AnswerAcceptanceTest extends BasicAuthAcceptanceTest {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Test
    public void addAnswer() throws Exception {
        Question savedQuestion = questionRepository.save(createByLoginUser(loginUser));

        HttpEntity<MultiValueMap<String, Object>> request = HtmlFormDataBuilder.urlEncodedForm()
                .addParameter("contents", "당근 엄청 의미있는 활동이고 말고..").build();

        ResponseEntity<String> response = basicAuthTemplate.postForEntity(savedQuestion.generateUrl() + "/answers",
                request, String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
    }

    @Test
    public void deleteAnswer_글쓴이() throws Exception {
        Question savedQuestion = questionRepository.save(createByLoginUser(loginUser));
        Answer answer = new Answer(loginUser, "하지만 TDD는 너무 하기 힘들 활동임다.");
        Answer answerByQuestion = savedQuestion.addAnswer(answer);
        Answer savedAnswer = answerRepository.save(answerByQuestion);

        HttpEntity<MultiValueMap<String, Object>> request = HtmlFormDataBuilder.urlEncodedForm()
                .addParameter("_method", "delete").build();

        ResponseEntity<String> response = basicAuthTemplate.postForEntity(savedAnswer.generateUrl(), request,
                String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
        assertFalse(findAnswerById(savedAnswer).isPresent());
    }

    @Test
    public void deleteAnswer_다른_사람이_쓴_답변() throws Exception {
        Question savedQuestion = questionRepository.save(createByLoginUser(loginUser));
        Answer answer = new Answer(findByUserId("sanjigi"), "하지만 TDD는 너무 하기 힘들 활동임다.");
        Answer answerByQuestion = savedQuestion.addAnswer(answer);
        Answer savedAnswer = answerRepository.save(answerByQuestion);

        HttpEntity<MultiValueMap<String, Object>> request = HtmlFormDataBuilder.urlEncodedForm()
                .addParameter("_method", "delete").build();

        ResponseEntity<String> response = basicAuthTemplate.postForEntity(savedAnswer.generateUrl(), request,
                String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN));
        assertTrue(findAnswerById(savedAnswer).isPresent());
    }

    private Optional<Answer> findAnswerById(Answer savedAnswer) {
        return answerRepository.findById(savedAnswer.getId());
    }
}
