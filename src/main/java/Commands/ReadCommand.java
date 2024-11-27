package Commands;

import Utils.HtmlContext;
import Utils.HtmlElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ReadCommand implements Command {

    private String[] filepath;

    public ReadCommand(String[] filepath) {
        this.filepath = filepath;
    }

    @Override

    public void execute() {
        if (filepath.length < 1) {
            System.out.println("wrong filepath");
            return;
        }

        String fileName = filepath[0];
        File file = new File(fileName);

        //文件名不存在
        if (!file.exists()) {
            System.out.println("The filepath doesn't exist:" + fileName);
            return;
        }

        //
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.out.println("error:" + e.getMessage());
            return;
        }
        //将String解析为Html
        HtmlElement rootElement = parseHtmlToElement(content.toString());
        //更新HTML缓冲区
        HtmlContext.getInstance().setHtmlContent(rootElement);
        System.out.println("Successfully load into buffer");
    }

    private HtmlElement parseHtmlToElement(String content) {
        content = content.trim();
        HtmlElement rootElement = parseElement(content); //递归解析
        return rootElement;
    }

    private HtmlElement parseElement(String htmlFragment) {
        String tagName = extractTagName(htmlFragment);
        String id = extractIdAttribute(htmlFragment);
        HtmlElement element = new HtmlElement(tagName, id,null);

        if(isSelfClosingTag(htmlFragment)) {
            return element;
        }

        String innerContent = extractInnerConntent(htmlFragment);

        parseInnerContent(innerContent, element);

        return element;
    }

    private String extractTagName(String htmlFragment) {

        int start = htmlFragment.indexOf("<") + 1;
        int end = htmlFragment.indexOf(" ", start);
        end = (end == -1) ? htmlFragment.indexOf('>', start) : end;

        return htmlFragment.substring(start, end).trim();
    }

    private String extractIdAttribute(String htmlFragment) {
        int idIndex = htmlFragment.indexOf("id=\"");
        if (idIndex == -1) {
            return null;
        }
        int start = idIndex + 4;
        int end = htmlFragment.indexOf('"', start);

        return htmlFragment.substring(start, end).trim();
    }

    private boolean isSelfClosingTag(String htmlFragment) {
        return htmlFragment.endsWith("/>");
    }

    private String extractInnerConntent(String htmlFragment) {
        int start = htmlFragment.indexOf(">") + 1;
        int end = htmlFragment.lastIndexOf('<');
        if (start < end) {
            return htmlFragment.substring(start, end).trim();
        }
        return "";
    }

    private void parseInnerContent(String innerContent, HtmlElement parent) {
        int currentIndex = 0;
        while (currentIndex < innerContent.length()) {
            int tagStart = innerContent.indexOf("<", currentIndex);

            if(tagStart == -1) {
                // 处理剩余文本
                String remainingText = innerContent.substring(currentIndex).trim();
                if (!remainingText.isEmpty()) {
                    parent.addChild(new HtmlElement("text", null, remainingText));
                }
                break;
            }

            // 处理标签前的文本
            if (tagStart > currentIndex) {
                String text = innerContent.substring(currentIndex, tagStart).trim();
                if(!text.isEmpty()) {
                    parent.addChild(new HtmlElement("text", null, text));
                }
            }

            // 提取完整标签
            int tagEnd = innerContent.indexOf(">", tagStart) + 1;
            String tagContent = extractCompleteTag(innerContent, tagStart, tagEnd);

            // 解析标签
            HtmlElement child = parseElement(tagContent);
            parent.addChild(child);

            // 更新当前索引
            currentIndex = tagStart + tagContent.length();
        }
    }

    private String extractCompleteTag(String content, int start, int end) {
        String tag = content.substring(start, end);
        if(isSelfClosingTag(tag)) {
            return tag;
        }

        String tagName = extractTagName(tag);
        int closeIndex = findClosingTag(content, tagName, end);
        return content.substring(start, closeIndex + tagName.length() + 3);
    }

    private int findClosingTag(String content, String tagName, int fromIndex) {
        String openTag = "<" + tagName;
        String closeTag = "</" + tagName + ">";

        int nestLevel = 1;
        int searchIndex = fromIndex;

        while (nestLevel > 0) {
            int nextOpenTag = content.indexOf(openTag, searchIndex);
            int nextCloseTag = content.indexOf(closeTag, searchIndex);

            // 没有找到闭合标签
            if (nextCloseTag == -1) {
                throw new IllegalArgumentException("Cannot find closing tag for: " + tagName);
            }

            // 如果下一个打开标签在闭合标签之前，说明有嵌套
            if (nextOpenTag != -1 && nextOpenTag < nextCloseTag) {
                nestLevel++;
                searchIndex = nextOpenTag + openTag.length();
            } else {
                nestLevel--;
                searchIndex = nextCloseTag + closeTag.length();
            }
        }

        return searchIndex;
    }

}
