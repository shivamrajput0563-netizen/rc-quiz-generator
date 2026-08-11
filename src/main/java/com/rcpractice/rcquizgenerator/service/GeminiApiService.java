package com.rcpractice.rcquizgenerator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcpractice.rcquizgenerator.dto.*;
import com.rcpractice.rcquizgenerator.validation.*;
import com.rcpractice.rcquizgenerator.entity.*;
import com.rcpractice.rcquizgenerator.repository.PassageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiApiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Autowired
    private PassageRepository passageRepository;

    @Autowired
    private QuizValidator quizValidator;

    @Autowired
    private PromptFactory promptFactory;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    

    public Passage generateDailyRc(String examType, String articleText) throws Exception {
        Passage passage = generateQuiz(examType, articleText, null); // sessionId not relevant for admin uploads
        passage.setDailyRc(true);
        return passageRepository.save(passage);
    }

    public Passage generateQuiz(String examType, String articleText, String sessionId) throws Exception {
        String prompt = promptFactory.buildPrompt(examType, articleText);
        QuizResponse quizResponse = null;
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                quizResponse = callGeminiAndParse(prompt);
                ValidationResult validation = quizValidator.validate(quizResponse);

                if (validation.isValid()) {
                    break;
                }

                System.out.println("Attempt " + attempt + " failed validation: " + validation.getIssues());

                if (attempt == maxRetries) {
                    System.out.println("Max retries reached. Proceeding with last generated quiz despite issues.");
                } else {
                    prompt = prompt + "\n\nNote: your previous attempt had these issues, please fix them: "
                            + validation.getIssues();
                }
            } catch (Exception parseError) {
                System.out.println("Attempt " + attempt + " failed to parse JSON: " + parseError.getMessage());
                if (attempt == maxRetries) {
                    throw new RuntimeException("Failed to generate a valid quiz after " + maxRetries + " attempts.", parseError);
                }
                // no change to prompt needed here — just retry, model may just have glitched
            }
        }

        if (quizResponse == null) {
            throw new RuntimeException("Quiz generation failed after all retries.");
        }

        return convertAndSave(examType, articleText, quizResponse, sessionId);
    }

    private QuizResponse callGeminiAndParse(String prompt) throws Exception {
        GeminiRequest.Part part = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(List.of(part));
        GeminiRequest.GenerationConfig config = new GeminiRequest.GenerationConfig("application/json");
        GeminiRequest request = new GeminiRequest(List.of(content), config);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        String urlWithKey = apiUrl + "?key=" + apiKey;
        GeminiResponse response = restTemplate.postForObject(urlWithKey, entity, GeminiResponse.class);

        String quizJson = response.getCandidates().get(0).getContent().getParts().get(0).getText();
        return objectMapper.readValue(quizJson, QuizResponse.class);
    }

    private Passage convertAndSave(String examType, String articleText, QuizResponse quizResponse, String sessionId) {
        Passage passage = new Passage();
        passage.setSessionId(sessionId);
        passage.setExamType(examType);
        passage.setCleanedText(articleText);
        passage.setPassageSummary(quizResponse.getPassageSummary());

        List<Question> questionEntities = new ArrayList<>();

        for (QuizQuestion q : quizResponse.getQuestions()) {
            Question question = new Question();
            question.setQuestionText(q.getQuestionText());
            question.setType(q.getType());
            question.setCorrectOptionIndex(q.getCorrectOptionIndex());
            question.setExplanation(q.getExplanation());
            question.setPassage(passage);

            List<Option> optionEntities = new ArrayList<>();
            List<String> optionTexts = q.getOptions();
            for (int i = 0; i < optionTexts.size(); i++) {
                Option option = new Option();
                option.setOptionText(optionTexts.get(i));
                option.setOptionIndex(i);
                option.setQuestion(question);
                optionEntities.add(option);
            }
            question.setOptions(optionEntities);
            questionEntities.add(question);
        }

        passage.setQuestions(questionEntities);

        return passageRepository.save(passage);
    }
}