package com.nekobyte.englishtek.util;

import com.nekobyte.englishtek.model.Lesson;
import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LessonPdfConverter {
    private final HtmlPdfConverter htmlPdfConverter;
    private final Parser markdownParser = Parser.builder()
            .extensions(Arrays.asList(
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    AutolinkExtension.create(),
                    TaskListItemsExtension.create()
            ))
            .build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
            .extensions(Arrays.asList(
                    TablesExtension.create(),
                    StrikethroughExtension.create(),
                    AutolinkExtension.create(),
                    TaskListItemsExtension.create()
            ))
            .build();

    public byte[] convertLessonToPdf(Lesson lesson) {
        try {
            StringBuilder html = new StringBuilder();
            
            // Convert markdown content to HTML
            StringBuilder contentHtml = new StringBuilder();
            List<String> content = lesson.getContent();
            if (content != null && !content.isEmpty()) {
                for (int i = 0; i < content.size(); i++) {
                    String section = content.get(i);
                    if (!section.trim().isEmpty()) {
                        Node document = markdownParser.parse(section);
                        contentHtml.append("<div class=\"content-section\">")
                                 .append(htmlRenderer.render(document))
                                 .append("</div>");
                        
                        // Add separator if not the last section
                        if (i < content.size() - 1) {
                            contentHtml.append("<div class=\"section-divider\"><hr/></div>");
                        }
                    }
                }
            }
            
            // Build HTML document
            html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
                .append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n")
                .append("<head>\n")
                .append("<title>").append(lesson.getTitle()).append("</title>\n")
                .append("<style type=\"text/css\">\n")
                .append("body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; padding: 40px; }\n")
                .append("h1 { color: #2c3e50; text-align: center; margin-bottom: 30px; font-size: 28px; }\n")
                .append("h2 { color: #3498db; margin-top: 30px; border-bottom: 2px solid #3498db; padding-bottom: 10px; font-size: 24px; }\n")
                .append("h3 { color: #2980b9; margin-top: 20px; font-size: 20px; }\n")
                .append(".description { background: #f8f9fa; border-left: 4px solid #3498db; padding: 15px; margin: 20px 0; color: #2c3e50; }\n")
                .append(".content { background: white; padding: 20px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n")
                .append(".content-section { margin: 30px 0; padding: 20px; background: white; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n")
                .append(".section-divider { margin: 40px 0; text-align: center; }\n")
                .append(".section-divider hr { border: 0; height: 2px; background-image: linear-gradient(to right, rgba(0, 0, 0, 0), rgba(52, 152, 219, 0.75), rgba(0, 0, 0, 0)); margin: 0; }\n")
                .append("pre { background: #f8f9fa; padding: 15px; border-radius: 5px; overflow-x: auto; margin: 15px 0; }\n")
                .append("code { font-family: 'Courier New', Courier, monospace; background: #f8f9fa; padding: 2px 5px; border-radius: 3px; }\n")
                .append("pre > code { background: none; padding: 0; border-radius: 0; display: block; }\n")
                .append("blockquote { border-left: 4px solid #3498db; margin: 0; padding-left: 20px; color: #7f8c8d; }\n")
                .append("img { max-width: 100%; height: auto; display: block; margin: 20px auto; }\n")
                .append("ul, ol { padding-left: 25px; }\n")
                .append("li { margin: 8px 0; }\n")
                .append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n")
                .append("th, td { padding: 12px; border: 1px solid #ddd; }\n")
                .append("th { background: #3498db; color: white; }\n")
                .append("tr:nth-child(even) { background: #f8f9fa; }\n")
                .append("</style>\n")
                .append("</head>\n<body>\n")
                .append("<h1>").append(lesson.getTitle()).append("</h1>\n")
                .append("<div class=\"description\">").append(lesson.getDescription()).append("</div>\n")
                .append("<div class=\"content\">").append(contentHtml.toString()).append("</div>\n")
                .append("</body></html>");

            return htmlPdfConverter.convertHtmlToPdf(html.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
