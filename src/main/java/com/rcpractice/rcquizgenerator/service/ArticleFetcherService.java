package com.rcpractice.rcquizgenerator.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ArticleFetcherService {

    private static final String[] NOISE_SELECTORS = {
            "nav", "header", "footer", "aside", "script", "style",
            ".comments", ".related", ".sidebar", ".advertisement",
            ".nav", ".menu", ".social-share", ".newsletter"
    };

    public String extractArticleText(String url) throws IOException {
        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .referrer("https://www.google.com")
                    .timeout(10000)
                    .get();
        } catch (org.jsoup.HttpStatusException e) {
            throw new IOException("This site blocked automated access (HTTP " + e.getStatusCode() + "). Try pasting the text directly instead.");
        } catch (java.net.SocketTimeoutException e) {
            throw new IOException("That site took too long to respond. Try pasting the text directly instead.");
        } catch (java.net.UnknownHostException e) {
            throw new IOException("Couldn't reach that link — double check the URL, or try pasting the text directly.");
        } catch (IOException e) {
            throw new IOException("Couldn't load that link. Try pasting the text directly instead.");
        }

        // Strip out obvious non-content regions before doing anything else
        for (String selector : NOISE_SELECTORS) {
            doc.select(selector).remove();
        }

        // Attempt 1: common semantic containers (fast path for well-structured sites)
        Element articleEl = doc.selectFirst("article, main, .articleBody, .article-content, .post-content, .entry-content");
        if (articleEl != null) {
            String text = extractParagraphText(articleEl);
            if (text.length() > 300) return text;
        }

        // Attempt 2: density-based scoring (fallback for non-standard layouts)
        Element bestContainer = findBestContentContainer(doc);
        if (bestContainer != null) {
            String text = extractParagraphText(bestContainer);
            if (text.length() > 200) return text;
        }

        // Attempt 3: last resort, all paragraphs on the page
        String allParagraphs = extractParagraphText(doc);
        if (allParagraphs.length() > 200) return allParagraphs;

        throw new IOException("Could not extract readable article content from that link. " +
                "This site may require JavaScript to load its content — try pasting the text directly instead.");
    }

    // Scores every div/section by (paragraph text length) minus a penalty for link-heavy regions,
    // and returns the highest-scoring container — a simplified version of Mozilla's Readability algorithm
    private Element findBestContentContainer(Document doc) {
        Elements candidates = doc.select("div, section");
        Element best = null;
        double bestScore = 0;

        for (Element candidate : candidates) {
            Elements paragraphs = candidate.select("> p, > div > p");
            if (paragraphs.size() < 3) continue; // needs a reasonable number of paragraphs to count

            int textLength = 0;
            int linkTextLength = 0;
            for (Element p : paragraphs) {
                textLength += p.text().length();
            }
            for (Element link : candidate.select("a")) {
                linkTextLength += link.text().length();
            }

            double linkDensity = textLength == 0 ? 1 : (double) linkTextLength / textLength;
            double score = textLength * (1 - linkDensity); // penalize link-heavy (nav-like) regions

            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best;
    }

    private String extractParagraphText(Element container) {
        Elements paragraphs = container.select("p");
        StringBuilder sb = new StringBuilder();
        for (Element p : paragraphs) {
            String text = p.text().trim();
            if (text.length() > 40) { // skip short fragments (captions, nav remnants)
                sb.append(text).append(" ");
            }
        }
        return sb.toString().trim();
    }
}