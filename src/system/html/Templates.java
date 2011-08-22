/*
 * This class stores our default HTML templates. They are hard coded inside
 * the system and the idea is to provide a safe template before allowing the
 * use of external ones.
 */

package system.html;

/**
 *
 * @author Nuno Brito, 9th of April 2011 in Darmstadt, Germany
 */
public class Templates {

    Tags tag = new Tags();

    public final String
            defaultDOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN"
            + "\" \"http://www.w3.org/TR/html4/strict.dtd\">"+"\n",

             defaultHTML = "<html>"+"\n",

             defaultHTML_ = "</html>"+"\n",
             defaultHead = "<head>"+"\n",
             defaultHead_ = "</head>"+"\n",
             defaultBody = "<body>"+"\n",
             defaultBody_ = "</body>"+"\n",

             defaultMetaContentType = "<meta http-equiv=\"Content-Type\" "
                + "content=\"application/xhtml+xml; charset=utf-8\" />"+"\n",
             defaultMetaDesc = "<meta name=\"description\" content=\"\" />"+"\n",
             defaultMetaKeywords = "<meta name=\"keywords\" content=\"\" />"+"\n",
             defaultMetaRobots = "<meta name=\"robots\" "
                + "content=\"index, follow\" />"+"\n",


            defaultTitle = "<title><%TITLE%></title>"+"\n",


            defaultFooterContent =
                    "<div id=\"footer\" style=\"text-align: right;\">"
                   // +"<p>"
                    + "<i><small>"
                    +"<%copyright%>"
                    +"&nbsp;</small></i>"
                   // + "</p>"
                    +"</div>",


            defaultNavigation =

            "<div id=\"header\">"+"\n"
            +"<ul>"+"\n"
            +tag.subnavigation+"\n"
            +"</ul>"+"\n"
            +"<p id=\"layoutdims\">"+"\n"
            +tag.navigation+"\n"
            + "</p>"+"\n"
            +"</div>"+"\n",


            defaultSectionColumn1Content =

        "<div class=\"colmask fullpage\">"+"\n"
        +"<div class=\"col1\"><!-- Column 1 start -->"+"\n"
        +"<br>"+"\n"
        +tag.column1Content+"\n"
        +"<!-- Column 1 end --> </div>"+"\n"
        +"</div>"+"\n"
            ,


        defaultCSS =
            "<style media=\"screen\" type=\"text/css\">"
            +"/* General styles */"
            +"body {"
            +"margin:0;"
            +"padding:0;"
            +"border:0; /* removes the border around viewport in old IE */"
            +"width:100%;"
            +"background:#fff;"
            +"min-width:200px; /* Minimum width of layout */"
            +"font-size:90%;"
            +"}"
            +"a {"
            +"color:#369;"
            +"text-decoration:none;"
            +"font-weight:bold;"
            +"}"
            +"a:hover {"
            +"color:#fff;"
            +"background:#369;"
            +"text-decoration:none;"
            +"}"
            +"h1, h2, h3 {"
            +"margin:.8em 0 .2em 0;"
            +"padding:0;"
            +"}"
            +"p {"
            +"margin:.4em 0 .8em 0;"
            +"padding:0;"
            +"}"
//            +"img {"
//            +"margin:10px 0 5px;"
//            +"}"
            +"/* Header styles */"
            +"#header {"
            +"clear:both;"
            +"float:left;"
            +"width:100%;"
            +"}"
            +"#header {"
            +"border-bottom:1px solid #000;"
            +"}"
            +"#header p,"
            +"#header h1,"
            +"#header h2 {"
            +"padding:.4em 15px 0 15px;"
            +"margin:0;"
            +"}"
            +"#header ul {"
            +"clear:left;"
            +"float:left;"
            +"width:100%;"
            +"list-style:none;"
            +"margin:10px 0 0 0;"
            +"padding:0;"
            +"}"
            +"#header ul li {"
            +"display:inline;"
            +"list-style:none;"
            +"margin:0;"
            +"padding:0;"
            +"}"
            +"#header ul li a {"
            +"display:block;"
            +"float:left;"
            +"margin:0 0 0 1px;"
            +"padding:3px 10px;"
            +"text-align:center;"
            +"background:#eee;"
            +"color:#000;"
            +"text-decoration:none;"
            +"position:relative;"
            +"left:15px;"
            +"line-height:1.3em;"
            +"}"
            +"#header ul li a:hover {"
            +"background:#369;"
            +"color:#fff;"
            +"}"
            +"#header ul li a.active,"
            +"#header ul li a.active:hover {"
            +"color:#fff;"
            +"background:#000;"
            +"font-weight:bold;"
            +"}"
            +"#header ul li a span {"
            +"display:block;"
            +"}"
            +"/* 'widths' sub menu */"
            +"#layoutdims {"
            +"clear:both;"
            +"background:#eee;"
            +"border-top:4px solid #000;"
            +"margin:0;"
            +"padding:6px 15px !important;"
            +"text-align:left;"
            +"}"
            +"/* column container */"
            +".colmask {"
            +"position:relative; /* This fixes the IE7 overflow hidden bug */"
            +"clear:both;"
            +"float:left;"
            +"width:100%; /* width of whole page */"
            +"overflow:hidden; /* This chops off any overhanging divs */"
            +"}"
            +"/* common column settings */"
            +".colright,"
            +".colmid,"
            +".colleft {"
            +"float:left;"
            +"width:100%;"
            +"position:relative;"
            +"}"
            +".col1,"
            +".col2,"
            +".col3 {"
            +"float:left;"
            +"position:relative;"
            +"padding:0 0 1em 0;"
            +"overflow:hidden;"
            +"}"
            +"/* Full page settings */"
            +".fullpage {"
            +"background:#fff; /* page background colour */"
            +"}"
            +".fullpage .col1 {"
            +"width:96%; /* page width minus left and right padding */"
            +"left:2%; /* page left padding */"
            +"}"
            +"/* Footer styles */"
            +"#footer {"
            +"clear:both;"
            +"float:right;"
            +"width:100%;"
            +"border-top:1px solid #000;"
            +"}"
            +"#footer p {"
            +"padding:10px;"
            +"margin:0;"
            +"}"
            +""
            +"</style>";


}
