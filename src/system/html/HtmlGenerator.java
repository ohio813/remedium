/*
 * The HTML class intended to provide a set of tools that aid in
 * the creation of HTML sections in a standardized manner.
 *
 * Includes snippets such as the default header and footer applied to on our
 * web sections and also handy snippets like break line, horizontal line, add
 * images and so on.
 */

package system.html;

import java.util.HashMap;

/**
 *
 * @author Nuno Brito, 8th of April 2011 in Darmstadt, Germany
 */
public final class HtmlGenerator {

    public final String
            SectionNotFound = "404", // used when a requested section is not found
            SectionAbout = "about",
            SectionSettings = "settings",
            SectionStatus = "settings",
            SectionHome = "home";

    public JS_CloseWindowTimer // handy javascript snippets
            javascript = new JS_CloseWindowTimer();

    /** public constructor of this class */
    public HtmlGenerator(String assignedTitle){
        setTitle(assignedTitle);

        // create our default sections
       doSections();

    }

    /** create our default sections to be listed on the web section */
    private void doSections(){

        Section section = new Section();

        // the default error message
        section.setTitlePretty(SectionNotFound);
        section.setTitleLink(SectionNotFound);
        section.setVisibleNav(false);
        section.setContent(  h1 + "404 - Page not found.. " + h1_
                            + "Are you sure this is the correct address?"
                            + br );
        addSection(section);

        // the home section
        section = new Section();
        section.setTitlePretty("Home");
        section.setTitleLink(SectionHome);
        section.setVisibleNav(true);
        section.setSelected(true);
        section.setContent( h1 + "Welcome" + h1_
                            + "This page is under construction."
                            + br );
        addSection(section);

        // the About section
        section = new Section();
        section.setTitlePretty("About");
        section.setTitleLink(SectionAbout);
        section.setVisibleNav(true);
        section.setSelected(false);
        section.setContent( "This page was proudly brought to you by Cheerios."
              + br
                );
        addSection(section);

    }


    // list all our registered sections
    private HashMap<String, Section>
            sections = new HashMap<String, Section>();

    private Tags tag = new Tags(); // our runtime tags
    private Templates template = new Templates(); // our template snippets
    
    public Navigation
            nav = new Navigation(), // the navigation panel
            sub = new Navigation(); // the sub-navigation panel

    public final String
            br = "<br>\n",
            h1= "<h1>",
            h1_= "</h1>\n",
            h2= "<h2>",
            h2_= "</h2>\n",
            h3= "<h3>",
            h3_= "</h3>\n",

            p = "<p>,",
            p_= "</p>\n";


    private String

            title = template.defaultTitle,
            html = template.defaultHTML,

    metaContentType = template.defaultMetaContentType,
    metaContenDesc = template.defaultMetaDesc,
    metaMetaMetaRobots = template.defaultMetaRobots,
    metaMetaKeywords= template.defaultMetaKeywords,
    metaCustomized = "", // the customized meta entries


    DOCTYPE = template.defaultDOCTYPE,
    html_ = template.defaultHTML_,
    head = template.defaultHead,
    head_ = template.defaultHead_,
    body = template.defaultBody,
    body_ = template.defaultBody_,

    navigationContent = template.defaultNavigation,
    sectionColumn1Content = template.defaultSectionColumn1Content,
    css = template.defaultCSS;

    /** Creates an HTML web link using a title and http address*/
    public final String doLink(String Title, String Address){
      //  <a href="http://example.com">test</a>
        return "<a href='"+Address+"'>"+Title+"</a>";
    }

    /** Set the content displayed on the main section */
    public void setTitle(String content){
      this.title = template.defaultTitle.replace
              (tag.title, content);
     navigationContent =
              template.defaultNavigation.replace
              (tag.title, content);
    }

    /** Set the customized meta tags for the generated web page */
    public void setMeta(String newMetadata){
//        if(!metaCustomized.equalsIgnoreCase(newMetadata))
//          setDirty(true);
        metaCustomized = newMetadata;
    }

    /** Set the content displayed on the main section */
    private void setContent(String content){
      sectionColumn1Content =
              template.defaultSectionColumn1Content.replace
              (tag.column1Content, content);
    }

    /** Set the content displayed on the main section */
    public void setCopyright(String content){
//      footerContent = template.defaultFooterContent.replace
//              (tag.copyright, content);
    }

    /** prepare the nav HTML text */
    private void doNavigation(){
        
        String content = this.nav.getNavLinks();

        navigationContent =
              template.defaultNavigation.replace
              (tag.navigation, content);

        // remove the top links for "Home", "About" and the such
        navigationContent = navigationContent.replace("<%SUBNAVIGATION%>",
                "");
//        content = this.sub.getSubLinks();
//              navigationContent = navigationContent.replace
//                (tag.subnavigation, content);

    }

    /** return all default and customized meta entries */
    private String getMeta(){
        return  metaContentType
              + metaContenDesc
              + metaMetaKeywords
              + metaMetaMetaRobots
              + metaCustomized;
    }

    /** makes available a web section customized to our needs */
    public String getPage(String who){

        // get the section, it will output 404 if not found
        Section section = this.getSection(who);
        // get the section contents
        this.setContent(section.getContent());

        // get an updated nav list
        doNavigation();

        String result =
                      DOCTYPE
                    + html
                    + head
                    + title
                    + getMeta()
                    + css
                    + head_
                    + body
                    + navigationContent
                    + sectionColumn1Content
                    //+ footerContent
                    + body_
                    + html_
                    ;
        return result;
    }



    /** Add a new section to our HTML section */
    public void addSection(Section section){
      
      // register on the sub navigation part if we are visible
        if(section.getVisibleNav())
          sub.addLink(
                  section.getTitleLink(), // we use this value as index
                  section.getTitlePretty(),
                "?show="+section.getTitleLink(),
                section.getSelected());

      // put it on our list
        sections.put(section.getTitleLink(), section);
    }

    /** Modify an existent section on our site. If it doesn't exist, fail */
    public void setSection(String title, String content){
      // create our section
        Section section = this.getSection(title);
        // if it doesn't exist then exit
        if(section == null)
            return;
        // do the changes
        section.setTitlePretty(title);
        section.setContent(content);
      // put it back on our list
        sections.put(title, section);
    }



    /** Gets a section registered on our list or provides a 404 if not found */
        private Section getSection(String title){
        // get our section out of the list
          Section section = sections.get(title);
            if(section == null) // we didn't found the request, provide a 404
               section= sections.get(SectionNotFound);
          // give back our reply
        return section;
    }

}
