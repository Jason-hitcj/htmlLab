package Commands;

import Utils.HtmlContext;
import Utils.HtmlElement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.language.English;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

public class SpellCheckCommand implements Command{
    public static boolean checkGrammar(JLanguageTool langTool, HtmlElement element) throws Exception {
        boolean foundErrors = false;

        // Check current element's textContent
        String text = element.getTextContent();
        if (text != null && !text.isEmpty()) {
            List<RuleMatch> matches = langTool.check(text);
            for (RuleMatch match : matches) {
                // Output grammar issue details
                System.out.println("--------------------------------------------------");
                System.out.println("Element: <" + element.getTagName() + "> (id: " + element.getId() + ")");
                System.out.println("Issue: " + match.getMessage());
                System.out.println("Context: " + text.substring(match.getFromPos(), match.getToPos()));
                System.out.println("Suggested corrections: " + match.getSuggestedReplacements());
                System.out.println("From position: " + match.getFromPos() + " to " + match.getToPos());
                foundErrors = true;  // Mark that we found errors
            }
        }

        // Recursively check children
        for (HtmlElement child : element.getChildren()) {
            foundErrors |= checkGrammar(langTool, child);  // Combine results
        }

        return foundErrors;  // Return true if any error was found
    }

    @Override
    public void execute() {
        try {
            // Step 1: Get HTML root
            HtmlElement root = HtmlContext.getInstance().getHtmlContent();
            // Step 2: Initialize JLanguageTool
            JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode("en-GB"));

            // Step 3: Perform grammar checking
            boolean foundErrors = checkGrammar(langTool, root);

            // Step 4: If no errors were found, output a message
            if (!foundErrors) {
                System.out.println("No grammar issues found!");
            }

        }
        catch (Exception e){
            System.out.println(e);
        }

    }
}
