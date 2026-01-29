package org.edtp.carpet_edtp_addition.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Translation {
    private static final String TRANSLATION_PATH = "assets/carpet_edtp_addition/lang/%s.json";
    private static final Translation TRANSLATION = new Translation();
    private static final Gson GSON = new Gson();
    
    /**
     * Carpet EDTP Addition的所有翻译，键表示语言，值是嵌套的一个Map集合，分别表示翻译的键和值
     */
    private final ConcurrentHashMap<String, Map<String, String>> translations = new ConcurrentHashMap<>();

    private Translation() {
    }

    public static Translation getInstance() {
        return TRANSLATION;
    }

    public Map<String, String> getTranslation(String lang) {
        String selectedLang = (lang == null || lang.isBlank()) ? "en_us" : lang;
        return this.translations.computeIfAbsent(selectedLang, this::loadTranslation);
    }

    private Map<String, String> loadTranslation(String lang) {
        try {
            Map<String, String> map = readTranslation(lang);
            if (map.isEmpty()) {
                Map<String, String> enUs = this.translations.get("en_us");
                if (enUs == null) {
                    enUs = this.readTranslation("en_us");
                }
                return enUs;
            }
            return map;
        } catch (IOException e) {
            throw new IllegalStateException("未能成功读取语言文件", e);
        }
    }

    private Map<String, String> readTranslation(String lang) throws IOException {
        String path = String.format(TRANSLATION_PATH, lang);
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return new ConcurrentHashMap<>();
            }
            String content = IOUtils.toString(stream, StandardCharsets.UTF_8);
            return GSON.fromJson(content, new TypeToken<Map<String, String>>() {}.getType());
        }
    }
}