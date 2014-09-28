package cv.lecturesight.manpages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.markdownj.MarkdownProcessor;

class ManPage {

  private final static MarkdownProcessor processor = new MarkdownProcessor();
  private final static Pattern regexp = Pattern.compile("# .*");
  
  private final String title;
  private final String markdown;
  private String html = null;
  
  public ManPage(String markdown) {
    this.markdown = markdown;
    
    // extract title from markdown
    Matcher m = regexp.matcher(markdown);
    if (m.find()) {
      title = m.group().substring(2);
    } else {
      title = ">> no title <<";
    }
  }

  public String getTitle() {
    return title;
  }

  public String getMarkdown() {
    return markdown;
  }

  public String getHtml() {
    if (html == null) {
      html = processor.markdown(markdown);
    }
    return html;
  }
  
  @Override
  public String toString() {
    return title;
  }
}
