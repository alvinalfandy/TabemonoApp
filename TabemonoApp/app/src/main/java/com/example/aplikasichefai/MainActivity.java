package com.example.aplikasichefai;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.graphics.Typeface;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Color;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;
import android.graphics.Typeface;

public class MainActivity extends AppCompatActivity {
    private ImageButton backButton;

    // Pola untuk mendeteksi bahan makanan yang valid
    private static final Map<String, Pattern> VALID_INGREDIENTS = new HashMap<String, Pattern>() {{
        put("PROTEIN", Pattern.compile("\\b(ayam|ikan|daging|telur|tahu|tempe|udang|cumi|kepiting|kerang|bekicot|bebek|itik|belut|hati ayam|hati sapi|paru sapi|jeroan|kikil|daging kambing|daging sapi|daging domba|daging babi|ikan salmon|ikan tuna|ikan kembung|ikan tongkol|ikan teri|ikan lele|ikan patin|ikan gurame|ikan bandeng|ikan nila|ikan sarden|ikan bawal|kepala ikan|ikan kakap|ikan cakalang|kepiting rajungan|kalkun|kelinci|burung puyuh|burung dara|ikan pari|udang windu|scallop|kerang hijau|kerang dara|kerang batik|ikan makarel|teri medan|teri nasi|ikan gabus|ikan sidat)\\b", Pattern.CASE_INSENSITIVE));
        put("VEGETABLES", Pattern.compile("\\b(wortel|bayam|kangkung|brokoli|kol|sawi|terong|timun|tomat|kentang|kubis|labu|buncis|kacang panjang|jagung|pare|selada|daun bawang|seledri|paprika|jamur|zucchini|lobak|daun singkong|kacang merah|kacang hijau|kacang kedelai|kacang tanah|kacang polong|terong ungu|terong hijau|ubi ungu|ubi jalar|ubi putih|sawi putih|sawi hijau|kemangi|petai|jengkol|kelor|daun pepaya|bawang bombay|bawang merah|bawang putih|tauge|sawi asin|daun katuk|rebung|turi|cabe hijau|cabe rawit|cabe merah|cabe keriting|genjer|gambas|kecipir|selada air|daun mint|daun basil|daun kari|pakcoy|chicory|okra|artichoke|asparagus|bayam merah|ginseng|bit|seldri)\\b", Pattern.CASE_INSENSITIVE));
        put("SPICES", Pattern.compile("\\b(bawang merah|bawang putih|bawang bombay|daun bawang|merica|lada hitam|lada putih|garam|gula|gula merah|gula jawa|gula aren|jahe|kunyit|ketumbar|cabai|cabe merah|cabe hijau|cabe rawit|lengkuas|serai|daun salam|daun jeruk|penyedap rasa|kecap manis|kecap asin|kecap ikan|saus tiram|saus tomat|saus sambal|cuka|terasi|pala|cengkeh|kayu manis|kemiri|asam jawa|jinten|kapulaga|biji selasih|biji wijen|biji pala|gula batu|vanili|saffron|daun kari|sambal terasi|sambal bawang|sambal matah|sambal tomat|bubuk cabai|bubuk kayu manis|bubuk kari|bubuk kunyit|bubuk lada|saus BBQ|saus mustard|saus keju|saus spaghetti|miso|wasabi|bumbu kari|daun ketumbar|rempah-rempah|daun salam kering|bubuk paprika|nutmeg|thyme|oregano|rosemary|basil|parsley|tarragon|clove|lemon zest|chili flakes)\\b", Pattern.CASE_INSENSITIVE));
        put("CARBS", Pattern.compile("\\b(nasi|mie|bihun|kwetiau|roti|pasta|beras|tepung terigu|tepung beras|tepung ketan|tepung maizena|tepung tapioka|tepung jagung|tepung kanji|singkong|ubi jalar|talas|sagu|jagung pipil|spaghetti|lasagna|tortilla|quinoa|oatmeal|beras merah|beras hitam|beras ketan|beras putih|beras Jepang|beras basmati|beras jasmine|kentang goreng|kentang rebus|kentang panggang|kentang tumbuk|kentang manis|roti gandum|roti tawar|roti sandwich|roti burger|roti naan|roti pita|roti prata|roti sourdough|roti bagel|pasta fusilli|pasta penne|pasta macaroni|pasta ravioli|pasta fettuccine|pasta linguine|mie telur|mie shirataki|mie udon|mie soba|mie ramen|mie bihun|mie jagung|kerupuk|keripik singkong|keripik kentang|keripik pisang|keripik ubi|granola|muesli|roti bakar|pizza dough|roti croissant|roti baget)\\b", Pattern.CASE_INSENSITIVE));
        put("DAIRY", Pattern.compile("\\b(susu sapi|susu kambing|susu bubuk|susu cair|susu evaporasi|susu kental manis|susu skim|susu UHT|keju cheddar|keju parmesan|keju mozarella|keju edam|keju gouda|keju feta|keju ricotta|keju brie|keju blue cheese|keju camembert|keju krim|keju leleh|mentega|margarin|whipping cream|krim masak|krimer|santan|yogurt plain|yogurt greek|yogurt rasa buah|yogurt drink|es krim vanilla|es krim coklat|es krim stroberi|es krim matcha|es krim kelapa|es krim mangga|butter unsalted|butter salted|butter ghee|susu almond|susu oat|susu kedelai|susu mete|susu kelapa|custard|puding susu|keju spread|krim asam|keju leleh cheddar|cream cheese|keju mascarpone|krim susu)\\b", Pattern.CASE_INSENSITIVE));
    }};

    // Pola untuk mendeteksi niat mencari resep
    private static final Pattern RECIPE_INTENT = Pattern.compile("\\b(masak|bikin|buat|resep|olah|menu|makanan|masakan|recipe)\\b", Pattern.CASE_INSENSITIVE);

    // Pola untuk mendeteksi kata-kata tidak pantas
    private static final Map<String, Pattern> BADWORDS = new HashMap<String, Pattern>() {{
        put("GENERAL", Pattern.compile("\\b(anjing|anjg|anjrot|ajg|anj|kampret|kimak|kontol|ktl|kntl|memek|mmk|ngewe|ngentot|shit|tai|titid|tod|tolol|goblok|goblog|gblg|gbl|bego|bedebah|bangsat|bgst|bngst|bacot|bcot|jancok|jncok|jnck|cok|ngenthu|ngethu|ngeth|blekok|blkok|budug|budeg|sinting|gila|brengsek|bejad|keparat|sialan|sial|asu|asw|babi|pantek|peak|pek|pkl|pukl|koplok|plok|plk|author|ngtd|set|su|cuk|cok|coeg|fuck|fak|fk|ngehe|ngehh|ngeh|lol|ngentot|pepek)\\b", Pattern.CASE_INSENSITIVE));
        put("RACIST", Pattern.compile("\\b(negro|nigga|chink|nigger)\\b", Pattern.CASE_INSENSITIVE));
        put("INAPPROPRIATE", Pattern.compile("\\b(seks|sex|bokep|hentai|masturbasi|coli|colmek|ngewe|ngentot|kentot|entot)\\b", Pattern.CASE_INSENSITIVE));
    }};

    // Pola untuk mendeteksi jenis chat
    private static final Map<String, Pattern> CHAT_PATTERNS = new HashMap<String, Pattern>() {{
        put("GREETINGS", Pattern.compile("^((h[ae][il]l?o+|hi+|hey+|halo+|p+a+g+i+|morning|siang|sore|malam|malem)|ass?alam|shalom)", Pattern.CASE_INSENSITIVE));
        put("THANKS", Pattern.compile("^(makasih|thank|thx|thanks|tengkyu|makasi|terima\\s*kasih|sankyu)", Pattern.CASE_INSENSITIVE));
        put("HOW_ARE", Pattern.compile("^((apa|ap)\\s*(kabar|kabarz|kbr)|how\\s*are\\s*you)", Pattern.CASE_INSENSITIVE));
        put("GOODBYE", Pattern.compile("^((goodbye|bye|bye2|dadah|dah|sampai|sampe|jumpa|ketemu|gud\\s*bye)|ass?alam)", Pattern.CASE_INSENSITIVE));
        put("IDENTITY", Pattern.compile("^((siapa|who)\\s*(kamu|lo|u|you|namamu|nama\\s*kamu)|intro)", Pattern.CASE_INSENSITIVE));
        put("HELP", Pattern.compile("^(help|tolong|bantuan|bisa|bantu|cara|how|gimana)", Pattern.CASE_INSENSITIVE));
        put("RANDOM", Pattern.compile("^(random|acak|terserah|bebas|apa\\s*aja|sembarang)", Pattern.CASE_INSENSITIVE));
    }};

    private static final String GEMINI_API_KEY = "AIzaSyAd4MKKBmpERoBVBQGIu_dk_-sXUyCRa2c"; // Ganti dengan API key baru
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + GEMINI_API_KEY;

    private static final int PERMISSION_REQUEST_CODE = 123;
    private LinearLayout chatMessages;
    private EditText userInput;
    private FloatingActionButton sendButton;
    private ScrollView scrollView;
    private RequestQueue requestQueue;
    private boolean isProcessing = false;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY = 1000;

    private final List<String> COOL_OPENINGS = new ArrayList<String>() {{
        add("Halo! Dari bahan yang Anda berikan, saya punya rekomendasi resep yang menarik! 🔥");
        add("Kabar baik! Saya telah menemukan resep yang cocok untuk Anda! 👊");
        add("Bersiaplah untuk terkejut! Ini resep spesial untuk Anda! 🚀");
        add("Permisi, sebentar! Ada resep istimewa dari bahan-bahan Anda! ⭐");
        add("Mari kita mulai! Saya punya resep yang akan membuat Anda tertarik! 🤤");
    }};

    private final List<String> COOL_CLOSINGS = new ArrayList<String>() {{
        add("Jika berkenan, silakan bagikan hasil masakan Anda. Saya menantikannya! 🔥");
        add("Anda pasti bisa! Teruslah berlatih hingga menjadi chef yang handal! 💪");
        add("Selamat memasak, Chef! Tunjukkan keahlian Anda! ⭐");
        add("Selamat memasak! Semoga hasilnya memuaskan! 🚀");
        add("Jika berkenan, Anda dapat membagikan foto hasil masakan Anda! 📸");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Periksa izin internet dan koneksi terlebih dahulu
        checkInternetPermissionAndConnection();
    }

    // Metode untuk memeriksa apakah pesan mengandung kata-kata tidak pantas
    private boolean containsBadwords(String message) {
        for (Pattern pattern : BADWORDS.values()) {
            if (pattern.matcher(message.toLowerCase()).find()) {
                return true;
            }
        }
        return false;
    }

    // Metode untuk memeriksa apakah pesan mengandung bahan makanan yang valid
    private Map<String, List<String>> containsValidIngredients(String message) {
        Map<String, List<String>> foundIngredients = new HashMap<>();

        for (Map.Entry<String, Pattern> entry : VALID_INGREDIENTS.entrySet()) {
            List<String> matches = new ArrayList<>();
            java.util.regex.Matcher matcher = entry.getValue().matcher(message);

            while (matcher.find()) {
                matches.add(matcher.group());
            }

            if (!matches.isEmpty()) {
                foundIngredients.put(entry.getKey(), matches);
            }
        }

        return foundIngredients;
    }

    // Metode untuk menentukan jenis pesan
    private String checkMessageType(String message) {
        if (containsBadwords(message)) {
            return "BADWORDS";
        }

        for (Map.Entry<String, Pattern> entry : CHAT_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(message).matches()) {
                return entry.getKey();
            }
        }
        return "DEFAULT";
    }

    private String getRandomResponse(String type) {
        Map<String, List<String>> responsesMap = new HashMap<String, List<String>>() {{
            put("GREETINGS", Arrays.asList(
                    "Halo! Ada yang bisa saya bantu? 🔥",
                    "Selamat datang! Ingin memasak apa hari ini? 👊",
                    "Selamat datang! Siap membuat masakan spesial? 🚀",
                    "Halo! Chef AI siap membantu Anda! Ada bahan apa saja? 😎",
                    "Hai! Ingin mengeksplorasi resep baru? Mari mulai! ⭐"
            ));
            put("THANKS", Arrays.asList(
                    "Sama-sama! Kapan saja Anda membutuhkan bantuan! 🤙",
                    "Terima kasih kembali! Silakan kembali lagi jika membutuhkan resep! 🔥",
                    "Dengan senang hati! Senang bisa membantu! 😎",
                    "Sama-sama! Jika berkenan, bagikan hasil masakannya ya! 📸",
                    "Sama-sama! Semoga sukses memasaknya! 👊"
            ));
            put("HOW_ARE", Arrays.asList(
                    "Saya dalam kondisi baik! Siap membantu Anda memasak! 💪",
                    "Saya sangat bersemangat! Ingin memasak apa? 🔥",
                    "Saya selalu siap! Mari membuat masakan! 🚀",
                    "Saya dalam kondisi terbaik! Siap menjelajahi resep bersama Anda! 😎",
                    "Baik sekali! Ingin mengeksplorasi menu apa hari ini? ⭐"
            ));
            put("GOODBYE", Arrays.asList(
                    "Sampai jumpa! Silakan kembali lagi jika ingin memasak! 👋",
                    "Sampai jumpa kembali! Ditunggu project selanjutnya! 🔥",
                    "Selamat tinggal! Anda sudah menjadi chef yang hebat sekarang! 😎",
                    "Sampai bertemu lagi! Tetap memasak! 👊",
                    "Sampai jumpa! Jangan lupa praktikkan resepnya ya! 🚀"
            ));
            put("IDENTITY", Arrays.asList(
                    "Saya adalah Chef AI! Partner memasak Anda yang menyenangkan! 😎",
                    "Panggil saja saya Chef AI! Siap membantu Anda menjadi master chef! 🔥",
                    "Chef AI di sini! Ahli dalam mengubah bahan menjadi masakan spesial! ⭐",
                    "Saya Chef AI, AI yang akan membantu Anda mahir memasak! 🚀",
                    "Chef AI siap bertugas! Siap membantu Anda memasak! 💪"
            ));
            put("HELP", Arrays.asList(
                    "Caranya mudah! Silakan ketik bahan yang Anda punya, nanti saya berikan resep yang cocok! 🔥",
                    "Dengan senang hati! Beritahu saja bahan yang ada di dapur, saya akan membantu membuat resepnya! 👊",
                    "Sangat mudah! Silakan list bahan-bahannya, nanti saya berikan ide resep yang spesial! 💡",
                    "Sangat simpel! Bagikan saja bahan yang Anda punya, saya akan memberikan resep yang lezat! 🚀",
                    "Jangan khawatir! Tuliskan saja bahan yang tersedia, saya akan menjadi panduan Anda! 😎"
            ));
            put("RANDOM", Arrays.asList(
                    "Baik, saya akan memberikan resep acak yang menarik! Namun, beritahu dulu bahan yang ada ya! 🎲",
                    "Siap memberikan resep kejutan! Tapi list dulu bahan yang Anda punya ya! 🎯",
                    "Resep acak akan segera hadir! Namun, bagikan dulu bahan-bahannya ya! 🎪",
                    "Tantangan diterima! Tapi beritahu dulu bahan yang ada ya! 🎨",
                    "Siap membuat resep kejutan! Tapi informasikan dulu bahan yang tersedia ya! 🎭"
            ));
            put("BADWORDS", Arrays.asList(
                    "Mohon maaf, bisakah kita menggunakan bahasa yang lebih sopan? 🙏",
                    "Maaf, bisakah kita berkomunikasi dengan bahasa yang lebih baik? 😊",
                    "Mari kita jaga bahasa kita! Kita bisa tetap santai namun tetap sopan! 😎",
                    "Sepertinya kata-katanya kurang tepat. Mari kita berbicara dengan lebih baik! 🌟",
                    "Maaf, mari kita berkomunikasi dengan bahasa yang lebih pantas! ✨"
            ));
            put("DEFAULT", Arrays.asList(
                    "Silakan beritahu bahan yang Anda punya! Agar saya bisa memberikan resep yang sesuai! 🔥",
                    "Harap list dulu bahan-bahannya ya! Nanti saya berikan resep yang spesial! 👊",
                    "Silakan bagikan bahan yang ada di dapur! Agar saya bisa membantu membuat resep! 🚀",
                    "Mohon informasikan bahan yang Anda punya! Agar saya bisa memberikan ide resep yang menarik! ⭐",
                    "Silakan tuliskan bahan-bahan yang ada ya! Agar saya bisa memberikan resep yang baik! 😎"
            ));
        }};

        List<String> responses = responsesMap.getOrDefault(type, responsesMap.get("DEFAULT"));
        return responses.get(new Random().nextInt(responses.size()));
    }

    // Metode untuk memproses pesan
    private String processMessage(String message) {
        String messageType = checkMessageType(message);

        // Jika pesan adalah tipe khusus (greeting, thanks, dll), kembalikan respons acak
        if (!messageType.equals("DEFAULT")) {
            return getRandomResponse(messageType);
        }

        // Periksa apakah pesan mengandung bahan makanan yang valid
        Map<String, List<String>> validIngredients = containsValidIngredients(message);

        // Jika tidak ada bahan valid
        if (validIngredients.isEmpty()) {
            boolean hasRecipeIntent = RECIPE_INTENT.matcher(message).find();

            if (hasRecipeIntent) {
                return getRandomResponse("HELP");
            } else {
                return getRandomResponse("DEFAULT");
            }
        }

        // Jika ada bahan valid, siapkan untuk mendapatkan resep
        return null; // Akan diganti dengan proses Gemini API
    }

    // Metode untuk membuat prompt resep
    private String createRecipePrompt(String message) {
        String randomOpening = getRandomPhrase(COOL_OPENINGS);
        String randomClosing = getRandomPhrase(COOL_CLOSINGS);

        return String.format(
                "Berperan sebagai chef profesional yang sopan , berikan satu resep dalam bahasa Indonesia yang bisa dibuat dengan bahan-bahan berikut: %s.\n\n" +
                        "**%s**\n\n" +
                        "🍳 Nama Resep: [Nama resep yang keren]\n\n" +
                        "📝 Bahan-bahan:\n" +
                        "[Daftar bahan dengan ukuran yang jelas]\n\n" +
                        "👩‍🍳 Cara Masak:\n" +
                        "[Langkah-langkah detail dengan bahasa gaul]\n\n" +
                        "💡 Tips Chef:\n" +
                        "[Tips keren dan praktis]\n\n" +
                        "**%s**",
                message, randomOpening, randomClosing
        );
    }

    // Metode untuk mendapatkan frase acak dari sebuah list
    private String getRandomPhrase(List<String> phrases) {
        return phrases.get(new Random().nextInt(phrases.size()));
    }

    // Metode untuk mengirim pesan dengan retry
    private void sendMessageWithRetry(String message, View typingIndicator) {
        try {
            // Coba proses pesan terlebih dahulu
            String processedMessage = processMessage(message);

            // Jika sudah memiliki respons khusus
            if (processedMessage != null) {
                chatMessages.removeView(typingIndicator);
                addMessage(processedMessage, true);
                resetState();
                return;
            }

            // Buat request body untuk Gemini API
            JSONObject requestBody = createRequestBody(message);
            JsonObjectRequest request = createApiRequest(message, typingIndicator, requestBody);

            new Handler().postDelayed(
                    () -> requestQueue.add(request),
                    1000 + (int)(Math.random() * 1000)
            );

        } catch (JSONException e) {
            handleApiError(typingIndicator, message);
        }
    }

    // Metode untuk membuat request body
    private JSONObject createRequestBody(String message) throws JSONException {
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();

        String prompt = createRecipePrompt(message);
        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        requestBody.put("contents", contents);

        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.8);
        generationConfig.put("maxOutputTokens", 1000);
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    // Metode untuk membuat API request
    private JsonObjectRequest createApiRequest(String message, View typingIndicator, JSONObject requestBody) {
        return new JsonObjectRequest(
                Request.Method.POST,
                GEMINI_API_URL,
                requestBody,
                response -> handleApiResponse(response, typingIndicator),
                error -> handleApiError(typingIndicator, message)
        );
    }

    // Metode untuk menangani respons API
    private void handleApiResponse(JSONObject response, View typingIndicator) {
        try {
            String recipeText = response
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            chatMessages.removeView(typingIndicator);
            addMessage(recipeText, true);
            resetState();

        } catch (JSONException e) {
            handleApiError(typingIndicator, "Failed to parse API response");
        }
    }

    // Metode untuk menangani error API
    private void handleApiError(View typingIndicator, String message) {
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            new Handler().postDelayed(
                    () -> sendMessageWithRetry(message, typingIndicator),
                    RETRY_DELAY * retryCount
            );
        } else {
            chatMessages.removeView(typingIndicator);
            addErrorMessage("Waduh, sorry nih ada error! Coba lagi ya dalam beberapa saat! 🙏");
            resetState();
        }
    }

    private void resetState() {
        isProcessing = false;
        sendButton.setEnabled(true);
        retryCount = 0;
    }

    private void addMessage(String message, boolean isBot) {
        try {
            View messageView;

            if (isBot) {
                messageView = getLayoutInflater().inflate(R.layout.bot_message, chatMessages, false);
                LinearLayout recipeCard = messageView.findViewById(R.id.recipeCard);
                recipeCard.removeAllViews();
                formatRecipeToViews(message, recipeCard);
            } else {
                messageView = getLayoutInflater().inflate(R.layout.user_message, chatMessages, false);
                TextView messageText = messageView.findViewById(R.id.messageText);
                messageText.setText(message);
            }

            chatMessages.addView(messageView);
            scrollToBottom();
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Failed to display message");
        }
    }

    private void addErrorMessage(String errorMessage) {
        try {
            View messageView = getLayoutInflater().inflate(R.layout.error_message, chatMessages, false);
            TextView messageText = messageView.findViewById(R.id.messageText);
            messageText.setText(errorMessage);

            chatMessages.addView(messageView);
            scrollToBottom();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Critical error: " + errorMessage);
        }
    }

    private void formatRecipeToViews(String recipe, LinearLayout recipeCard) {
        // Hapus asterisk dari teks
        recipe = recipe.replace("**", "").trim();

        String[] sections = recipe.split("\n\n");

        for (String section : sections) {
            try {
                section = section.trim();
                if (section.isEmpty()) continue;

                // Pesan pembuka dengan gaya khusus
                if (section.startsWith("Yo bro!") || section.startsWith("Mantap jiwa!")) {
                    addMessageWithStyle(section, recipeCard, true);
                }
                // Judul resep
                else if (section.startsWith("🍳 Nama Resep:")) {
                    String title = section.replace("🍳 Nama Resep:", "").trim();
                    TextView titleView = new TextView(this);
                    titleView.setText(title);
                    titleView.setTextSize(18);
                    titleView.setTypeface(null, Typeface.BOLD);
                    titleView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 16, 0, 16);
                    titleView.setLayoutParams(params);

                    // Tambahkan garis pembatas
                    View divider = new View(this);
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            2
                    );
                    dividerParams.setMargins(32, 0, 32, 16);
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

                    recipeCard.addView(titleView);
                    recipeCard.addView(divider);
                }
                // Bahan-bahan
                else if (section.contains("📝 Bahan-bahan:")) {
                    String[] ingredientLines = section.split("\n");
                    LinearLayout ingredientsSection = new LinearLayout(this);
                    ingredientsSection.setOrientation(LinearLayout.VERTICAL);
                    ingredientsSection.setBackgroundColor(Color.parseColor("#F5F5F5"));
                    ingredientsSection.setPadding(32, 16, 32, 16);

                    // Judul section
                    TextView sectionTitle = new TextView(this);
                    sectionTitle.setText("📝 Bahan-bahan");
                    sectionTitle.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    sectionTitle.setTypeface(null, Typeface.BOLD);
                    sectionTitle.setTextSize(16);
                    ingredientsSection.addView(sectionTitle);

                    // Bahan-bahan
                    for (int i = 1; i < ingredientLines.length; i++) {
                        String ingredient = ingredientLines[i].trim();
                        if (!ingredient.isEmpty()) {
                            TextView ingredientView = new TextView(this);
                            ingredientView.setText("• " + ingredient.replaceFirst("^[-*]\\s*", ""));
                            ingredientView.setTextSize(14);
                            ingredientView.setPadding(0, 8, 0, 8);
                            ingredientsSection.addView(ingredientView);
                        }
                    }

                    recipeCard.addView(ingredientsSection);
                }
                // Cara Masak
                else if (section.contains("👩‍🍳 Cara Masak:")) {
                    String[] cookingLines = section.split("\n");
                    LinearLayout cookingSection = new LinearLayout(this);
                    cookingSection.setOrientation(LinearLayout.VERTICAL);
                    cookingSection.setBackgroundColor(Color.parseColor("#F0F0F0"));
                    cookingSection.setPadding(32, 16, 32, 16);

                    // Judul section
                    TextView sectionTitle = new TextView(this);
                    sectionTitle.setText("👩‍🍳 Cara Masak");
                    sectionTitle.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    sectionTitle.setTypeface(null, Typeface.BOLD);
                    sectionTitle.setTextSize(16);
                    cookingSection.addView(sectionTitle);

                    // Langkah-langkah
                    for (int i = 1; i < cookingLines.length; i++) {
                        String step = cookingLines[i].trim();
                        if (!step.isEmpty()) {
                            TextView stepView = new TextView(this);
                            stepView.setText(i + ". " + step.replaceFirst("^[-*]\\s*", ""));
                            stepView.setTextSize(14);
                            stepView.setPadding(0, 8, 0, 8);
                            cookingSection.addView(stepView);
                        }
                    }

                    recipeCard.addView(cookingSection);
                }
                // Tips
                else if (section.contains("💡 Tips Chef:")) {
                    String[] tipsLines = section.split("\n");
                    LinearLayout tipsSection = new LinearLayout(this);
                    tipsSection.setOrientation(LinearLayout.VERTICAL);
                    tipsSection.setBackgroundColor(Color.parseColor("#FFF3E0"));
                    tipsSection.setPadding(32, 16, 32, 16);

                    // Judul section
                    TextView sectionTitle = new TextView(this);
                    sectionTitle.setText("💡 Tips Chef");
                    sectionTitle.setTextColor(Color.parseColor("#FF4B2B"));
                    sectionTitle.setTypeface(null, Typeface.BOLD);
                    sectionTitle.setTextSize(16);
                    tipsSection.addView(sectionTitle);

                    // Tips
                    for (int i = 1; i < tipsLines.length; i++) {
                        String tip = tipsLines[i].trim();
                        if (!tip.isEmpty()) {
                            TextView tipView = new TextView(this);
                            tipView.setText("• " + tip.replaceFirst("^[-*]\\s*", ""));
                            tipView.setTextSize(14);
                            tipView.setPadding(0, 8, 0, 8);
                            tipsSection.addView(tipView);
                        }
                    }

                    recipeCard.addView(tipsSection);
                }
                // Pesan penutup dengan gaya khusus
                else if (section.startsWith("Jangan lupa") || section.startsWith("Lo bisa!") || section.startsWith("Selamat masak")) {
                    addMessageWithStyle(section, recipeCard, false);
                }
                // Teks lainnya
                else {
                    addGenericText(section, recipeCard);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addMessageWithStyle(String message, LinearLayout recipeCard, boolean isOpening) {
        TextView textView = new TextView(this);
        textView.setText(message);

        // Warna dan gaya teks berbeda untuk pembuka dan penutup
        textView.setTextColor(isOpening ?
                ContextCompat.getColor(this, R.color.colorPrimary) :
                ContextCompat.getColor(this, R.color.colorAccent)
        );

        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTypeface(null, Typeface.BOLD_ITALIC);
        textView.setTextSize(16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, isOpening ? 0 : 16, 0, isOpening ? 16 : 0);
        textView.setLayoutParams(params);

        recipeCard.addView(textView);
    }

    private void addRecipeTitle(String section, LinearLayout recipeCard) {
        String title = section.replace("🍳 Nama Resep:", "").trim();

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(18);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);
        titleView.setLayoutParams(params);

        recipeCard.addView(titleView);

        // Tambahkan garis pembatas
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        );
        dividerParams.setMargins(32, 0, 32, 16);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        recipeCard.addView(divider);
    }

    private void addRecipeSection(String section, boolean isIngredients, LinearLayout recipeCard) {
        // Hapus asterisk dan bersihkan teks
        section = section.replace("**", "").trim();

        String[] lines = section.split("\n");

        View sectionView = getLayoutInflater().inflate(R.layout.recipe_section, recipeCard, false);
        TextView titleView = sectionView.findViewById(R.id.sectionTitle);
        TextView contentView = sectionView.findViewById(R.id.sectionContent);

        // Set judul section
        titleView.setText(lines[0].trim());

        // Gabungkan baris-baris selain judul
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 1; i < lines.length; i++) {
            // Hapus karakter '-' atau '*' di awal baris
            String cleanLine = lines[i].replaceFirst("^[-*]\\s*", "").trim();

            // Tambahkan nomor urut untuk cara masak, bullet untuk bahan
            if (!isIngredients) {
                contentBuilder.append((i) + ". ").append(cleanLine).append("\n");
            } else {
                contentBuilder.append("• ").append(cleanLine).append("\n");
            }
        }

        contentView.setText(contentBuilder.toString().trim());

        recipeCard.addView(sectionView);
    }

    private void addRecipeTips(String section, LinearLayout recipeCard) {
        // Hapus asterisk dan bersihkan teks
        section = section.replace("**", "").trim();

        String[] lines = section.split("\n");

        View tipView = getLayoutInflater().inflate(R.layout.recipe_tip, recipeCard, false);
        TextView titleView = tipView.findViewById(R.id.tipTitle);
        TextView contentView = tipView.findViewById(R.id.tipContent);

        // Set judul section
        titleView.setText(lines[0].trim());

        // Gabungkan baris tips
        StringBuilder tipsBuilder = new StringBuilder();
        for (int i = 1; i < lines.length; i++) {
            // Hapus karakter '-' atau '*' di awal baris
            String cleanLine = lines[i].replaceFirst("^[-*]\\s*", "").trim();
            tipsBuilder.append("• ").append(cleanLine).append("\n");
        }

        contentView.setText(tipsBuilder.toString().trim());

        recipeCard.addView(tipView);
    }

    private void addGenericText(String text, LinearLayout recipeCard) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        textView.setTextSize(14);
        textView.setPadding(16, 8, 16, 8);

        recipeCard.addView(textView);
    }




    private void scrollToBottom() {
        try {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleError(String message, Exception e) {
        e.printStackTrace();
        addErrorMessage(message);
        resetState();
    }

    private void showErrorDialog(String message) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage(message);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (requestQueue != null) {
                requestQueue.cancelAll(request -> true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }private void checkInternetPermissionAndConnection() {
        // Periksa izin internet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // Jika izin belum diberikan, minta izin
            requestInternetPermission();
        } else {
            // Jika izin sudah diberikan, periksa koneksi internet
            checkInternetConnection();
        }
    }

    private void requestInternetPermission() {
        // Tampilkan dialog penjelasan jika perlu
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.INTERNET)) {
            new AlertDialog.Builder(this)
                    .setTitle("Izin Internet Diperlukan")
                    .setMessage("Aplikasi membutuhkan izin internet untuk mengambil resep.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Minta izin
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.INTERNET},
                                PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("Batal", (dialog, which) -> {
                        // Tutup aplikasi jika izin ditolak
                        finish();
                    })
                    .create()
                    .show();
        } else {
            // Minta izin langsung
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void checkInternetConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            // Tidak ada koneksi internet
            showNoInternetDialog();
        } else {
            // Lanjutkan inisialisasi komponen
            initializeComponents();
        }
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tidak Ada Koneksi Internet")
                .setMessage("Harap aktifkan koneksi internet untuk menggunakan aplikasi.")
                .setPositiveButton("Buka Pengaturan", (dialog, which) -> {
                    // Buka pengaturan jaringan
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                })
                .setNegativeButton("Tutup Aplikasi", (dialog, which) -> {
                    // Tutup aplikasi
                    finish();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void initializeComponents() {
        // Existing initializations
        chatMessages = findViewById(R.id.chatMessages);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);
        scrollView = findViewById(R.id.scrollView);
        requestQueue = Volley.newRequestQueue(this);

        // New: Initialize back button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Pindah ke HomeActivity
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            // Tambahkan flag untuk membersihkan aktivitas sebelumnya
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Tutup aktivitas saat ini
            finish();
        });

        // Existing methods
        setupSendButton();
        showWelcomeMessage();
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> sendMessage());

        userInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void showWelcomeMessage() {
        addMessage("🔥 Selamat Datang Para Pecinta Kuliner! 🤙\n\n" +
                "Saya Chef AI, partner memasak Anda yang siap membantu membuat masakan spesial dari bahan yang tersedia!\n\n" +
                "Silakan beritahu bahan yang Anda miliki, nanti saya akan memberikan resep yang paling sesuai untuk Anda! 🚀✨\n\n" +
                "Mari, mulai chat! 🔥", true);
    }

    private void sendMessage() {
        String message = userInput.getText().toString().trim();

        if (message.isEmpty() || isProcessing) return;

        isProcessing = true;
        sendButton.setEnabled(false);
        userInput.setText("");

        // Tambahkan pesan pengguna
        addMessage(message, false);

        // Tambahkan indikator typing
        View typingIndicator = getLayoutInflater().inflate(R.layout.typing_indicator, chatMessages, false);
        chatMessages.addView(typingIndicator);
        scrollToBottom();

        // Kirim pesan dengan mekanisme retry
        sendMessageWithRetry(message, typingIndicator);


    }
}

