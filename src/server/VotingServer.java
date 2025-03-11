package server;

import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import model.Candidate;
import model.User;
import util.JsonUtil;
import util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class VotingServer extends BasicServer {
    private final static Configuration freemarker = initFreeMarker();
    protected final Map<String, User> sessions = new ConcurrentHashMap<>();

    public VotingServer(String host, int port) throws IOException {
        super(host, port);
        registerGet("/", this::handleCandidatesPage);
        registerPost("/vote", this::handleCandidatesVote);
        registerGet("/error", this::handleCandidateError);
        registerGet("/thank-you", this::handleThankYouPage);
        registerGet("/votes", this::handleVotesPage);
        registerGet("/login", this::loginGet);
        registerPost("/login", this::loginPost);
        registerGet("/login-error", this::loginError);
        registerGet("/register", this::handleRegisterPage);
        registerPost("/register", this::handleRegisterPost);
        registerGet("/register-error", this::registerErrorPage);
        registerGet("/register-success", this::registerSuccessPage);
        registerGet("/logout", this::logoutHandler);
    }


    private static Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
            cfg.setDirectoryForTemplateLoading(new File("data"));
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
        try {

            Template temp = freemarker.getTemplate(templateFile);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {

                temp.process(dataModel, writer);
                writer.flush();

                byte[] data = stream.toByteArray();

                sendByteData(exchange, ResponseCodes.OK, ContentType.TEXT_HTML, data);
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }

    private void handleCandidatesPage(HttpExchange exchange) {
        String cookieString = getCookie(exchange);
        Map<String, String> cookies = Cookie.parse(cookieString);
        String sessionId = cookies.get("sessionId");

        User loggedInUser = sessionId != null ? sessions.get(sessionId) : null;

        if (loggedInUser != null) {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("candidates", JsonUtil.getCandidates());
            renderTemplate(exchange, "/candidates.ftlh", dataModel);
        } else {
            renderTemplate(exchange, "register-login/unlogged.html", Collections.emptyMap());
        }

    }

    private void handleCandidatesVote(HttpExchange exchange) {
        String cookieString = getCookie(exchange);
        Map<String, String> cookies = Cookie.parse(cookieString);
        String sessionId = cookies.get("sessionId");

        User loggedInUser = sessionId != null ? sessions.get(sessionId) : null;

        if (loggedInUser != null) {
            String queryParams = getBody(exchange);
            Map<String, String> params = Utils.parseUrlEncoded(queryParams, "&");
            String idStr = params.get("id");

            if (idStr == null) {
                redirect303(exchange, "/error");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                redirect303(exchange, "/error");
                return;
            }

            Candidate candidate = JsonUtil.findCandidateById(id);
            if (candidate == null) {
                redirect303(exchange, "/error");
                return;
            }
            if (loggedInUser.getCandidateVoted() != null) {
                Map<String, Object> dataModel = new HashMap<>();
                dataModel.put("user", loggedInUser);
                dataModel.put("candidate", JsonUtil.findCandidateById(loggedInUser.getCandidateVoted()));

                renderTemplate(exchange, "alreadyvoted.ftlh", dataModel);
                return;
            }

            JsonUtil.voteForCandidate(id);
            loggedInUser.setCandidateVoted(id);
            JsonUtil.writeUsersToFile();
            redirect303(exchange, "/thank-you?id=" + id);
        } else {
            renderTemplate(exchange, "register-login/unlogged.html", Collections.emptyMap());
        }

    }


    private void handleCandidateError(HttpExchange exchange) {
        String cookieString = getCookie(exchange);
        Map<String, String> cookies = Cookie.parse(cookieString);
        String sessionId = cookies.get("sessionId");

        User loggedInUser = sessionId != null ? sessions.get(sessionId) : null;

        if (loggedInUser != null) {
            renderTemplate(exchange, "error.html", null);
        } else {
            renderTemplate(exchange, "register-login/unlogged.html", Collections.emptyMap());
        }
    }

    private void handleThankYouPage(HttpExchange exchange) {
        String cookieString = getCookie(exchange);
        Map<String, String> cookies = Cookie.parse(cookieString);
        String sessionId = cookies.get("sessionId");

        User loggedInUser = sessionId != null ? sessions.get(sessionId) : null;

        if (loggedInUser != null) {
            String queryParams = getQueryParams(exchange);
            Map<String, String> params = Utils.parseUrlEncoded(queryParams, "&");
            String idStr = params.get("id");

            if (idStr == null) {
                redirect303(exchange, "/error");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                redirect303(exchange, "/error");
                return;
            }

            Candidate candidate = JsonUtil.findCandidateById(id);
            if (candidate == null) {
                redirect303(exchange, "/error");
                return;
            }

            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("candidate", candidate);
            dataModel.put("totalVotes", JsonUtil.getTotalVotes());
            renderTemplate(exchange, "/thankyou.ftlh", dataModel);
        } else {
            renderTemplate(exchange, "register-login/unlogged.html", Collections.emptyMap());
        }
    }

    private void handleVotesPage(HttpExchange exchange) {
        Map<String, Object> dataModel = new HashMap<>();
        List<Candidate> candidates = JsonUtil.getCandidates();

        Collections.sort(candidates, Comparator.comparingInt(Candidate::getVotes).reversed());
        dataModel.put("candidates", candidates);
        dataModel.put("totalVotes", JsonUtil.getTotalVotes());
        renderTemplate(exchange, "votes.ftlh", dataModel);
    }

    private void loginGet(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", null);
        renderTemplate(exchange, "register-login/login.ftlh", data);
    }

    private void loginPost(HttpExchange exchange) {
        String raw = getBody(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(raw, "&");
        String email = params.get("email");
        String password = params.get("password");

        if (email == null || email.isEmpty() ||
                password == null || password.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("error", "Все поля должны быть заполнены!");
            renderTemplate(exchange, "register-login/login.ftlh", data);
            return;
        }

        User user = JsonUtil.getUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            String sessionId = UUID.randomUUID().toString();
            sessions.put(sessionId, user);

            Cookie sessionCookie = Cookie.make("sessionId", sessionId);
            sessionCookie.setMaxAge(600);
            sessionCookie.setHttpOnly(true);
            setCookie(exchange, sessionCookie);

            redirect303(exchange, "/");
        } else {
            redirect303(exchange, "/login-error");
        }
    }

    private void loginError(HttpExchange exchange) {
        renderTemplate(exchange, "register-login/login-error.ftlh", Collections.emptyMap());
    }

    private void handleRegisterPage(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        data.put("error", null);
        renderTemplate(exchange, "register-login/register.ftlh", data);
    }

    private void handleRegisterPost(HttpExchange exchange) {

        String raw = getBody(exchange);

        Map<String, String> params = Utils.parseUrlEncoded(raw, "&");
        String email = params.get("email");
        String name = params.get("name");
        String password = params.get("password");

        if (email == null || email.isEmpty() ||
                name == null || name.isEmpty() ||
                password == null || password.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("error", "Все поля должны быть заполнены!");
            renderTemplate(exchange, "register-login/register.ftlh", data);
            return;
        }

        if (JsonUtil.getUserByEmail(email) != null) {
            redirect303(exchange, "/register-error");
        } else {
            User newUser = new User(name, email, password);
            JsonUtil.addUser(newUser);

            redirect303(exchange, "/register-success");
        }
    }

    private void registerSuccessPage(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Регистрация успешна! Теперь вы можете войти.");

        renderTemplate(exchange, "register-login/register_result.ftlh", data);
    }

    private void registerErrorPage(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", false);
        data.put("message", "Пользователь с таким email уже зарегистрирован!");

        renderTemplate(exchange, "register-login/register_result.ftlh", data);
    }

    private void logoutHandler(HttpExchange exchange) {
        String cookieString = getCookie(exchange);
        Map<String, String> cookies = Cookie.parse(cookieString);
        String sessionId = cookies.get("sessionId");

        if(sessionId != null) {
            sessions.remove(sessionId);
        }

        Cookie expiredCookie = Cookie.make("sessionId", "");
        expiredCookie.setMaxAge(0);
        expiredCookie.setHttpOnly(true);
        setCookie(exchange, expiredCookie);

        redirect303(exchange, "/login");
    }
}
