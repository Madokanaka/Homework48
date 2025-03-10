package util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Candidate;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class JsonUtil {

    private static final Gson gson = new Gson();
    private static final String FILE_PATH = "data/candidates.json";
    private static List<Candidate> candidates;

    static {
        try {
            candidates = readFromJsonFile();
            initializeCandidates(candidates);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToJsonFile() throws IOException {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(candidates, writer);
        }
    }

    private static List<Candidate> readFromJsonFile() throws IOException {
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type candidateListType = new TypeToken<List<Candidate>>() {}.getType();
            return gson.fromJson(reader, candidateListType);
        }
    }

    public static Candidate findCandidateById(Integer id) {
        return candidates.stream()
                .filter(candidate -> candidate.getId() != null && candidate.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private static void initializeCandidates(List<Candidate> candidates) {
        for (int i = 0; i < candidates.size(); i++) {
            Candidate candidate = candidates.get(i);
            if (candidate.getId() == null) {
                candidate.setId(i + 1);
            }
            if (candidate.getVotes() == null) {
                candidate.setVotes(0);
            }
        }
    }

    public static List<Candidate> getCandidates() {
        return candidates;
    }

    public static void voteForCandidate(Integer id) {
        Candidate candidate = findCandidateById(id);
        if (candidate != null) {
            candidate.setVotes(candidate.getVotes() + 1);
            try {
                writeToJsonFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
