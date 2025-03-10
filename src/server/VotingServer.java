package server;

import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import model.Candidate;
import util.JsonUtil;
import util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VotingServer extends BasicServer{
    private final static Configuration freemarker = initFreeMarker();

    public VotingServer(String host, int port) throws IOException {
        super(host, port);
        registerGet("/", this::handleCandidatesPage);
        registerGet("/vote", this::handleCandidatesVote);
        registerGet("/error", this::handleCandidateError);
        registerGet("/thank-you", this::handleThankYouPage);
        registerGet("/votes", this::handleVotesPage);
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
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("candidates", JsonUtil.getCandidates());
        renderTemplate(exchange, "/candidates.ftlh", dataModel);
    }

    private void handleCandidatesVote(HttpExchange exchange) {
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

        JsonUtil.voteForCandidate(id);
        redirect303(exchange, "/thank-you?id=" + id);
    }


    private void handleCandidateError(HttpExchange exchange) {
        renderTemplate(exchange, "error.html", null);
    }

    private void handleThankYouPage(HttpExchange exchange) {
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
    }

    private void handleVotesPage(HttpExchange exchange) {
        Map<String, Object> dataModel = new HashMap<>();
        List<Candidate> candidates = JsonUtil.getCandidates();

        Collections.sort(candidates, Comparator.comparingInt(Candidate::getVotes).reversed());
        dataModel.put("candidates", candidates);
        dataModel.put("totalVotes", JsonUtil.getTotalVotes());
        renderTemplate(exchange, "votes.ftlh", dataModel);
    }

}
