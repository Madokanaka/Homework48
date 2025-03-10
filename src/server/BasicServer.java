package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class BasicServer {

    private final HttpServer server;
    private final String dataDir = "data";
    private Map<String, RouteHandler> routes = new HashMap<>();

    protected BasicServer(String host, int port) throws IOException {
        server = createServer(host, port);
        registerCommonHandlers();
    }

    private static String makeKey(String method, String route) {
        route = ensureStartsWithSlash(route);
        return String.format("%s %s", method.toUpperCase(), route);
    }

    private static String makeKey(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        int index = path.lastIndexOf(".");
        String extOrPath = index != -1 ? path.substring(index).toLowerCase() : path;

        return makeKey(method, extOrPath);
    }

    private static String ensureStartsWithSlash(String route) {
        if (route.startsWith(".")) return route;
        return route.startsWith("/") ? route : "/" + route;
    }

    private static void setContentType(HttpExchange exchange, ContentType type) {
        exchange.getResponseHeaders().set("Content-Type", String.valueOf(type));
    }

    private static HttpServer createServer(String host, int port) throws IOException {
        String msg = "Starting server on http://%s:%s/%n";
        System.out.printf(msg, host, port);
        InetSocketAddress address = new InetSocketAddress(host, port);
        return HttpServer.create(address, 50);
    }

    private void registerCommonHandlers() {

        server.createContext("/", this::handleIncomingServerRequests);

        registerGet("/", exchange -> sendFile(exchange, makeFilePath("index.html"), ContentType.TEXT_HTML));

        // эти обрабатывают запросы с указанными расширениями
        registerFileHandler(".css", ContentType.TEXT_CSS);
        registerFileHandler(".html", ContentType.TEXT_HTML);
        registerFileHandler(".jpg", ContentType.IMAGE_JPEG);
        registerFileHandler(".jpeg", ContentType.IMAGE_JPEG);
        registerFileHandler(".png", ContentType.IMAGE_PNG);
        registerFileHandler(".ftlh", ContentType.TEXT_FTLH);
    }

    protected final void registerGet(String route, RouteHandler handler) {
        registerGenericHandler("GET", route, handler);
    }

    protected final void registerPost(String route, RouteHandler handler) {
        registerGenericHandler("POST", route, handler);
    }


    private void registerGenericHandler(String method, String route, RouteHandler handler) {
        getRoutes().put(makeKey(method, route), handler);

    }

    protected final void registerFileHandler(String fileExt, ContentType type) {
        registerGet(fileExt, exchange -> sendFile(exchange, makeFilePath(exchange), type));
    }

    protected final Map<String, RouteHandler> getRoutes() {
        return routes;
    }

    protected final void sendFile(
            HttpExchange exchange,
            Path pathToFile,
            ContentType contentType
    ) {
        try {
            if (Files.notExists(pathToFile)) {
                respond404(exchange);
                return;
            }
            byte[] data = Files.readAllBytes(pathToFile);
            sendByteData(exchange, ResponseCodes.OK, contentType, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path makeFilePath(HttpExchange exchange) {
        return makeFilePath(exchange.getRequestURI().getPath());
    }

    protected Path makeFilePath(String... s) {
        return Path.of(dataDir, s);
    }

    protected final void sendByteData(
            HttpExchange exchange,
            ResponseCodes responseCode,
            ContentType contentType,
            byte[] data
    ) throws IOException {
        try (OutputStream output = exchange.getResponseBody()) {
            setContentType(exchange, contentType);
            exchange.sendResponseHeaders(responseCode.getCode(), 0);
            output.write(data);
            output.flush();
        }
    }

    private void respond404(HttpExchange exchange) {
        try {
            byte[] data = "404 Not found".getBytes();
            sendByteData(exchange, ResponseCodes.NOT_FOUND, ContentType.TEXT_PLAIN, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingServerRequests(HttpExchange exchange) {
        RouteHandler route = getRoutes().getOrDefault(makeKey(exchange), this::respond404);
        route.handle(exchange);
    }

    public final void start() {
        server.start();
    }

    public String getBody(HttpExchange exchange) {
        InputStream in = exchange.getRequestBody();
        Charset charset = StandardCharsets.UTF_8;
        InputStreamReader isr = new InputStreamReader(in, charset);

        try (BufferedReader reader = new BufferedReader(isr)) {
            return reader.lines().collect(Collectors.joining(""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void redirect303(HttpExchange exchange, String path) {
        try {
            exchange.getResponseHeaders().add("Location", path);
            exchange.sendResponseHeaders(ResponseCodes.REDIRECT.getCode(), 0);
            exchange.getResponseBody().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String getQueryParams(HttpExchange exchange) {
        String queryParams = exchange.getRequestURI().getQuery();
        return Objects.nonNull(queryParams) ? queryParams : "";
    }
}
