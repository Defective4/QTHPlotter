package io.github.defective4.ham.locresolver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CountryResolver {
    private final Map<String, List<String>> codes = new LinkedHashMap<>();

    public CountryResolver() throws IOException {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/itu.json"))) {
            JsonObject codes = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("codes");
            for (String key : codes.keySet()) {
                JsonArray array = codes.getAsJsonArray(key);
                this.codes.put(key, array.asList().stream().map(JsonElement::getAsString).collect(Collectors.toList()));
            }
        }
    }

    public String resolve(String code) {
        if (code.isEmpty()) return null;
        for (Map.Entry<String, List<String>> entry : codes.entrySet()) {
            for (String localCode : entry.getValue()) {
                String[] split = localCode.split("-");
                if (split.length == 1) {
                    if (code.toUpperCase().startsWith(split[0])) return entry.getKey();
                } else {
                    String lower = split[0];
                    String upper = split[1];
                    try {
                        for (String pref : ITURangeResolver.resolve(lower, upper))
                            if (code.toUpperCase().startsWith(pref)) return entry.getKey();
                    } catch (Exception e) {
                    }
                }
            }
        }
        return null;
    }
}
