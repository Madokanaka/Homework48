package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Candidate;
import model.User;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String FILE_PATH = "data/candidates.json";
    private static final String filePathToUsers = "data/users.json";

    private static List<Candidate> candidates;
    private static List<User> users;

    static {
        try {
            candidates = readFromJsonFile();
            initializeCandidates(candidates);
            loadUsers();
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

    public static int getTotalVotes() {
        return candidates.stream()
                .mapToInt(candidate -> candidate.getVotes() == null ? 0 : candidate.getVotes())
                .sum();

    }

    private static void loadUsers() {
        try (FileReader reader = new FileReader(filePathToUsers)) {
            Type employeeListType = new TypeToken<List<User>>(){}.getType();
            users = gson.fromJson(reader, employeeListType);
            if (users == null) {
                users = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeUsersToFile() {
        try (FileWriter writer = new FileWriter(filePathToUsers)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static User getUserById(UUID id) {
        return users.stream().filter(user -> user.getId().equals(id)).findFirst().orElse(null);
    }

    public static User getUserByEmail(String email) {
        return users.stream().filter(user -> user.getEmail().equals(email)).findFirst().orElse(null);
    }

    public static void addUser(User user) {
        user.setId(UUID.randomUUID());
        users.add(user);
        writeUsersToFile();
    }
}
