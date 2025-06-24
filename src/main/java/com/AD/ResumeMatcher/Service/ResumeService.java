package com.AD.ResumeMatcher.Service;

import com.AD.ResumeMatcher.Entity.Resume;
import com.AD.ResumeMatcher.Repository.ResumeRepository;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepo;

//    public Resume uploadAndExtract(MultipartFile file) throws Exception {
//        String text = new Tika().parseToString(file.getInputStream());
//
//        // Use first line instead of sentence model
//        String[] lines = text.split("\n");
//        String probableNameLine = lines.length > 0 ? lines[0] : text;
//
//        // Tokenize the line
//        TokenizerModel tokenizerModel = new TokenizerModel(getClass().getResourceAsStream("/models/en-token.bin"));
//        Tokenizer tokenizer = new TokenizerME(tokenizerModel);
//        String[] tokens = tokenizer.tokenize(probableNameLine);
//
//        // Load Name Finder Model
//        InputStream nameModelIn = getClass().getResourceAsStream("/models/en-ner-person.bin");
//        if (nameModelIn == null) {
//            throw new RuntimeException("Name model not found!");
//        }
//        TokenNameFinderModel nameModel = new TokenNameFinderModel(nameModelIn);
//        NameFinderME nameFinder = new NameFinderME(nameModel);
//
//        // Detect name
////        Span[] nameSpans = nameFinder.find(tokens);
////        String extractedName = "Unknown";
////        if (nameSpans.length > 0) {
////            StringBuilder nameBuilder = new StringBuilder();
////            for (int i = nameSpans[0].getStart(); i < nameSpans[0].getEnd(); i++) {
////                nameBuilder.append(tokens[i]).append(" ");
////            }
////            extractedName = nameBuilder.toString().trim();
////        }
//
////
//        int experience = extractExperience(text);
//        List<String> skills = extractSkills(text);
////
//        Resume resume = new Resume();
//
//
//        for (int i = 0; i < Math.min(5, lines.length); i++) {
//            String line = lines[i].trim();
//            // Skip empty or non-name lines
//            if (line.length() > 0 && line.matches("^[A-Z][a-z]+(\\s[A-Z][a-z]+)+$")) {
//                probableNameLine = line;
//                break;
//            }
//        }
//
//        if (probableNameLine.isEmpty()) {
//            probableNameLine = lines.length > 0 ? lines[0] : text; // fallback
//        }
//
//        System.out.println("🔍 Final Name Line: " + probableNameLine);
//
//        String[] token = tokenizer.tokenize(probableNameLine);
//        Span[] nameSpans = nameFinder.find(tokens);
//
//        String extractedName = "Unknown";
//        if (nameSpans.length > 0) {
//            StringBuilder nameBuilder = new StringBuilder();
//            for (int i = nameSpans[0].getStart(); i < nameSpans[0].getEnd(); i++) {
//                nameBuilder.append(tokens[i]).append(" ");
//            }
//            extractedName = nameBuilder.toString().trim();
//        }
//
//        System.out.println("🧠 Extracted Name: " + extractedName);
//
//       resume.setName(extractedName);
//        resume.setExperience(experience);
//        resume.setResumeText(text);
//        resume.setSkills(skills);
//
//        return resumeRepo.save(resume);
//    }
public Resume uploadAndExtract(MultipartFile file) throws Exception {
    // Extract text from resume
    String text = new Tika().parseToString(file.getInputStream());
    String[] lines = text.split("\n");

    // Load tokenizer model
    TokenizerModel tokenizerModel = new TokenizerModel(getClass().getResourceAsStream("/models/en-token.bin"));
    Tokenizer tokenizer = new TokenizerME(tokenizerModel);

    // Load Name Finder model
    InputStream nameModelIn = getClass().getResourceAsStream("/models/en-ner-person.bin");
    if (nameModelIn == null) {
        throw new RuntimeException("Name model file not found!");
    }
    TokenNameFinderModel nameModel = new TokenNameFinderModel(nameModelIn);
    NameFinderME nameFinder = new NameFinderME(nameModel);

    // Step 1: Try extracting name using fallback uppercase rule
    String fallbackName = null;
    for (int i = 0; i < Math.min(5, lines.length); i++) {
        String line = lines[i].trim();
        if (line.matches("^[A-Z][A-Z\\s]{5,}$") && line.split("\\s+").length >= 2) {
            fallbackName = toTitleCase(line.toLowerCase());
            break;
        }
    }

    // Step 2: Try using NameFinder on fallbackName
    String extractedName = "Unknown";
    if (fallbackName != null) {
        String[] tokens = tokenizer.tokenize(fallbackName);
        Span[] nameSpans = nameFinder.find(tokens);
        if (nameSpans.length > 0) {
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = nameSpans[0].getStart(); i < nameSpans[0].getEnd(); i++) {
                nameBuilder.append(tokens[i]).append(" ");
            }
            extractedName = nameBuilder.toString().trim();
        } else {
            extractedName = fallbackName;
        }
    }

    // Extract other fields
    int experience = extractExperience(text);
    List<String> skills = extractSkills(text);

    // Build and save Resume entity
    Resume resume = new Resume();
    resume.setName(extractedName);
    resume.setExperience(experience);
    resume.setResumeText(text);
    resume.setSkills(skills);

    return resumeRepo.save(resume);
}
//
//public Resume uploadAndExtract(MultipartFile file) throws Exception {
//    String text = new Tika().parseToString(file.getInputStream());
//
//    // Split lines safely
//    String[] lines = text.split("\n"); // handles \n, \r\n, etc.
//
//
//
//    // Load tokenizer model
//    TokenizerModel tokenizerModel = new TokenizerModel(getClass().getResourceAsStream("/models/en-token.bin"));
//    Tokenizer tokenizer = new TokenizerME(tokenizerModel);
//
//    // Load NER Name Finder model
//    InputStream nameModelIn = getClass().getResourceAsStream("/models/en-ner-person.bin");
//    if (nameModelIn == null) {
//        throw new RuntimeException("Name model not found");
//    }
//    TokenNameFinderModel nameModel = new TokenNameFinderModel(nameModelIn);
//    NameFinderME nameFinder = new NameFinderME(nameModel);
//
//    String fallbackName = null;
//
//    // Step 0: Try to find fallback name from ALL CAPS line like "SUBHAM KUMAR RANA"
//    for (int i = 0; i < Math.min(10, lines.length); i++) {
//        String line = lines[i].trim();
//        if (line.matches("^[A-Z][A-Z\\s]{5,}$") && line.split("\\s+").length >= 2) {
//            fallbackName = toTitleCase(line.toLowerCase());
//            break;
//        }
//    }
//
//    // Step 1: Try NER model on first 5 lines
//    String extractedName = "Unknown";
//    for (int i = 0; i < Math.min(5, lines.length); i++) {
//        String line = lines[i].trim();
//        if (line.length() > 0) {
//            String[] tokens = tokenizer.tokenize(line);
//            Span[] nameSpans = nameFinder.find(tokens);
//            if (nameSpans.length > 0) {
//                StringBuilder nameBuilder = new StringBuilder();
//                for (int j = nameSpans[0].getStart(); j < nameSpans[0].getEnd(); j++) {
//                    nameBuilder.append(tokens[j]).append(" ");
//                }
//                extractedName = nameBuilder.toString().trim();
//                break;
//            }
//        }
//    }
//
//    // Step 2: Fallback - use basic two-word name matching if NER fails
//    if (extractedName.equals("Unknown")) {
//        for (int i = 0; i < Math.min(5, lines.length); i++) {
//            String line = lines[i].trim();
//            if (line.matches("^[A-Za-z]+\\s+[A-Za-z]+$")) {
//                extractedName = toTitleCase(line);
//                break;
//            }
//        }
//    }
//
//    // Step 3: Final fallback to all-caps detected line
//    if (extractedName.equals("Unknown") && fallbackName != null) {
//        extractedName = fallbackName;
//    }
//
//    System.out.println("✅ Final Extracted Name: " + extractedName);
//
//    int experience = extractExperience(text);
//    List<String> skills = extractSkills(text);
//
//    Resume resume = new Resume();
//    resume.setName(extractedName);
//    resume.setExperience(experience);
//    resume.setResumeText(text);
//    resume.setSkills(skills);
//
//    return resumeRepo.save(resume);
//}




    private String toTitleCase(String input) {
        StringBuilder result = new StringBuilder();
        for (String word : input.split("\\s+")) {
            if (word.length() > 1) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1)).append(" ");
            } else {
                result.append(word.toUpperCase()).append(" ");
            }
        }
        return result.toString().trim();
    }


    private String[] tokenizeText(String text) throws IOException {
        InputStream tokenModelIn = getClass().getResourceAsStream("/models/en-token.bin");
        if (tokenModelIn == null) {
            throw new FileNotFoundException("Tokenizer model not found");
        }
        TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
        Tokenizer tokenizer = new TokenizerME(tokenModel);
        return tokenizer.tokenize(text);
    }

//    private String extractName(String tokens) throws IOException {
//        // Load NER model
//        InputStream nameModelIn = getClass().getResourceAsStream("/models/en-ner-person.bin");
//        TokenNameFinderModel nameModel = new TokenNameFinderModel(nameModelIn);
//        NameFinderME nameFinder = new NameFinderME(nameModel);
//
//
//
//
//
//        // Find name spans
//        Span[] nameSpans = nameFinder.find(tokens);
//
//        // Extract the first name found
//        for (Span span : nameSpans) {
//            StringBuilder nameBuilder = new StringBuilder();
//            for (int i = span.getStart(); i < span.getEnd(); i++) {
//                nameBuilder.append(tokens[i]).append(" ");
//            }
//            return nameBuilder.toString().trim(); // Return the first full name
//        }
//
//        return null; // If no name is found
//    }


    private int extractExperience(String text) {
        Pattern pattern = Pattern.compile("(\\d+)\\s+years?\\s+of\\s+experience", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }


    private List<String> extractSkills(String text) {
        List<String> knownSkills = List.of("java", "spring", "hibernate", "kafka", "mysql", "docker");
        return knownSkills.stream()
                .filter(skill -> text.toLowerCase().contains(skill))
                .collect(Collectors.toList());
    }
}
