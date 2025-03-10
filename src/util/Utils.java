package util;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    private Utils() {}

    public static Map<String, String> parseUrlEncoded(String rawLines, String delimeter) {
        String[] lines = rawLines.split(delimeter);

        Stream<Map.Entry<String, String>> stream = Arrays.stream(lines)
                .map(Utils::decode)
                .filter(Optional::isPresent)
                .map(Optional::get);
        return stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static Optional<Map.Entry<String, String>> decode(String kv) {
        if (!kv.contains("=")) {
            return Optional.empty();
        }

        String[] parts = kv.split("=");
        if (parts.length != 2) {return Optional.empty();}
        Charset charset = StandardCharsets.UTF_8;

        String key = URLDecoder.decode(parts[0], charset);
        String value = URLDecoder.decode(parts[1], charset);

        return Optional.of(Map.entry(key.strip(), value.strip()));
    }
}
