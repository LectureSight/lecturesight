package cv.lecturesight.manpages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.markdownj.MarkdownProcessor;

/** Model class that represents a manual page node.
 * 
 * @author wulff
 */
class ManPage {

  // Markdown processor
  private final static MarkdownProcessor processor = new MarkdownProcessor();
  
  // RegExp for finding the first H1
  private final static Pattern regexp = Pattern.compile("# .*");
  
  private final String title;       // page display name
  private final String markdown;    // Markdown code of page
  private String html = null;       // rendered html of page
  
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

  /** Returns the page title.
   * 
   * @return page title
   */
  public String getTitle() {
    return title;
  }

  /** Returns the Markdown code of this page.
   * 
   * @return Markdown code
   */
  public String getMarkdown() {
    return markdown;
  }

  /** Returns the rendered HTML.
   * 
   * @return HTML code
   */
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
