package eu.kennytv.maintenance.core.util;

import eu.kennytv.maintenance.core.config.Config;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class WebhookUtil {

    private static Config config;
    private static Logger logger;

    public static void init(Config cfg, Logger log) {
        config = cfg;
        logger = log;
    }

    public static void send(String messageKey, String... replace) {
        if (config == null) return;
        if (!config.getBoolean("webhook.enabled")) return;

        String message = config.getString("webhook.messages." + messageKey);
        if (message == null) return;

        for (int i = 0; i < replace.length; i += 2) {
            message = message.replace(replace[i], replace[i + 1]);
        }

        String webhookUrl = config.getString("webhook.url");
        if (webhookUrl == null || webhookUrl.isEmpty() || !webhookUrl.startsWith("https://discord.com/api/webhooks/")) return;

        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = "{\"content\":\"" + message.replace("\"", "\\\"") + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            conn.getInputStream().close();
            conn.disconnect();
        } catch (Exception e) {
            if (logger != null) {
                logger.warning("[MaintenanceWebhook] Falha ao enviar webhook: " + e.getMessage());
            }
        }
    }
} 