/*
 * A class that provides support for multiple pages inside each Html container
 *
 */

package system.html;

/**
 *
 * @author Nuno Brito, 10th of April 2011 in Darmstadt, Germany.
 */
public class Section {

    private String
            titlePretty,  // title used for display (pretty)
            titleLink, // title used on links
            content; // content to be displayed

    private Boolean
            visibleNav = true, // should we see it listed on the navigation tab?
            selected   = false; // is it selected or not?
    /** get the page contents */
    public String getContent() {
        return content;
    }
    /** set the page contents */
    public void setContent(String content) {
        this.content = content;
    }
    /** get the titlePretty of this page */
    public String getTitlePretty() {
        return titlePretty;
    }
    /** set the titlePretty of this page */
    public void setTitlePretty(String title) {
        this.titlePretty = title;
    }

    public String getTitleLink() {
        return titleLink;
    }

    public void setTitleLink(String titleLink) {
        this.titleLink = titleLink;
    }

    public Boolean getVisibleNav() {
        return visibleNav;
    }

    public void setVisibleNav(Boolean visibleNav) {
        this.visibleNav = visibleNav;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    


}
