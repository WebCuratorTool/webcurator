package org.webcurator.webapp.beans.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles2.TilesConfigurer;
import org.webcurator.ui.tools.controller.BrowseController;
import org.webcurator.ui.tools.controller.BrowseHelper;
import org.webcurator.ui.tools.controller.RegexReplacer;
import org.webcurator.ui.tools.controller.StringReplacer;

import java.util.*;

/**
 * Contains configuration that used to be found in {@code wct-browse-servlet.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@Configuration
public class BrowseServletConfig {

    @Value("${browseHelper.prefix}")
    private String browseHelperPrefix;

    @Value("${browse.double_escape}")
    private boolean browseDoubleEscape;

    @Autowired
    private BaseConfig baseConfig;

    // This method is declared static as BeanFactoryPostProcessor types need to be instatiated early. Instance methods
    // interfere with other bean lifecycle instantiations. See {@link Bean} javadoc for more details.
    // TODO I don't understand how this bean can have the same name as the bean in BaseConfig...
    @Bean
    public static PropertyPlaceholderConfigurer wctCoreConfigurer() {
        PropertyPlaceholderConfigurer bean = new PropertyPlaceholderConfigurer();
        bean.setLocations(new ClassPathResource("wct-core.properties"));
        bean.setIgnoreResourceNotFound(true);
        bean.setIgnoreUnresolvablePlaceholders(true);
        bean.setOrder(150);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public OpenSessionInViewInterceptor openSessionInViewInterceptor() {
        OpenSessionInViewInterceptor bean = new OpenSessionInViewInterceptor();
        bean.setSessionFactory(baseConfig.sessionFactory().getObject());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public SimpleUrlHandlerMapping simpleUrlMapping() {
        SimpleUrlHandlerMapping bean = new SimpleUrlHandlerMapping();
        bean.setInterceptors(new Object[]{ openSessionInViewInterceptor() });

        Properties properties = new Properties();
        properties.setProperty("**", "browseController");
        bean.setMappings(properties);

        return bean;
    }

    @Bean
    public UrlBasedViewResolver tilesViewResolver() {
        UrlBasedViewResolver bean = new UrlBasedViewResolver();
        bean.setRequestContextAttribute("requestContext");
        bean.setViewClass(TilesConfigurer.class);

        return bean;
    }

    // Helper class to configure Tiles 2.x for the Spring Framework
    // See http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/servlet/view/tiles2/TilesConfigurer.html
    // The actual tiles templates are in the tiles-definitions.xml
    @Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer bean = new TilesConfigurer();
        bean.setDefinitions("/WEB-INF/tiles-defs.xml");

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public SimpleMappingExceptionResolver exceptionResolver() {
        SimpleMappingExceptionResolver bean = new SimpleMappingExceptionResolver();
        bean.setDefaultErrorView("browse-tool-error");
        Properties properties = new Properties();
        properties.setProperty("org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException",
                "NoObjectFound");

        bean.setExceptionMappings(properties);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public BrowseController browseController() {
        BrowseController bean = new BrowseController();
        bean.setSupportedMethods("GET");
        bean.setQualityReviewFacade(baseConfig.qualityReviewFacade());
        bean.setBrowseHelper(browseHelper());

        // A Map of token fixes to apply. Sites sometimes use the javascript:'top.location = self.location;'
        // or 'window.location = ..' to issue a client side redirect but this will cause the target instance
        // list to be redirected when using the browse tool in the webpage preview (iframe).
        // We add a comment to this javascript to prevent the redirect.
        //  NB:     http-equiv=&quot;refresh&quot; is http-equiv="refresh";
        Map<String, String> fixTokensMap = new HashMap<>();
        fixTokensMap.put("top.location", "//top.location");
        fixTokensMap.put("window.location", "//window.location");
        // Ensure that meta refresh redirect to the root path "/" is replaced by a relative path "./"
        fixTokensMap.put("http-equiv=&quot;refresh&quot; content=&quot;0; url=/",
                "http-equiv=&quot;refresh&quot; content=&quot;0; url=./");
        bean.setFixTokens(fixTokensMap);

        return bean;
    }

    @Bean
    public BrowseHelper browseHelper() {
        BrowseHelper bean = new BrowseHelper();
        bean.setPrefix(browseHelperPrefix);

        // A Map of Content-Type to list of replacement patterns. These
        // patterns are used by the BrowseHelper to replace URLs within
        // resources of the particular content-type. For HTML tag/attribute
        // patterns, please refer to the htmlTagPatterns attribute below
        // as this is an easier way to create standard patterns.
        // To make an expression case insensitive add (?i) to the beginning
        // of the expression.
        Map<String, List<String>> contentTypePatternsMap = new HashMap<>();
        List<String> textHtmlPatternsList = new ArrayList<>();

        textHtmlPatternsList.add("(?i)\\burl\\((?![\"'].)([^\\)]*)\\)");
        textHtmlPatternsList.add("(?i)\\burl\\(\"([^\"]*)\"\\)");
        textHtmlPatternsList.add("(?i)\\burl\\('([^']*)'\\)");

        // patternsList.add("(?i)background-image\\s*:\\s+url\\(([^\\)]*)\\)");
        // patternsList.add("(?i)background-image\\s*:\\s+url\\('([^']*)'\\)");
        // patternsList.add("(?i)background-image\\s*:\\s+url\\(\"([^\"]*)\"\\)");
        // patternsList.add("(?i)@import\\s+url\\(\"([^\"]*)\"\\)");
        // patternsList.add("(?i)@import\\s+url\\((?!\")([^\\)]*)\\)");

        textHtmlPatternsList.add("(?i)@import\\s+\"([^\"]*)\"");

        // A:HREF
        textHtmlPatternsList.add("(?i)&lt;\\s*A\\s+[^&gt;]*\\bHREF\\s*=\\s*\"((?!javascript:)[^\"]*)\"");
        textHtmlPatternsList.add("(?i)&lt;\\s*A\\s+[^&gt;]*\\bHREF\\s*=\\s*'((?!javascript:)[^']*)'");
        textHtmlPatternsList.add("(?i)&lt;\\s*a\\s+[^&gt;]*\\bhref=((?!javascript:)[^\\t\\n\\x0B\\f\\r>\\\"']+)");

        // META URL
        textHtmlPatternsList.add("(?i)&lt;\\s*META\\s+[^&gt;]*\\bURL\\s*=\\s*\"([^\"]*)\"");
        textHtmlPatternsList.add("(?i)&lt;\\s*META\\s+[^&gt;]*\\bURL\\s*=\\s*'([^']*)'");
        textHtmlPatternsList.add("(?i)&lt;\\s*META\\s+[^&gt;]*\\bURL=([^\\t\\n\\x0B\\f\\r>\\\"']+)");

        // OBJECT CODEBASE
        textHtmlPatternsList.add("(?i)&lt;\\s*OBJECT\\s+[^&gt;]*\\bCODEBASE\\s*=\\s*\"([^\"]*)\"");
        textHtmlPatternsList.add("(?i)&lt;\\s*OBJECT\\s+[^&gt;]*\\bCODEBASE\\s*=\\s*'([^']*)'");
        textHtmlPatternsList.add("(?i)&lt;\\s*OBJECT\\s+[^&gt;]*\\bCODEBASE=([^\\t\\n\\x0B\\f\\r>\\\"']+)");

        // OBJECT DATA
        textHtmlPatternsList.add("(?i)&lt;\\s*OBJECT\\s+[^&gt;]*\\bDATA\\s*=\\s*\"([^\"]*)\"");
        textHtmlPatternsList.add("(?i)&lt;\\s*OBJECT\\s+[^&gt;]*\\bDATA\\s*=\\s*'([^']*)'");
        textHtmlPatternsList.add("(?i)&lt;\\s*OBJECT\\s+[^&gt;]*\\bDATA=([^\\t\\n\\x0B\\f\\r>\\\"']+)");

        // APPLET CODEBASE
        textHtmlPatternsList.add("(?i)&lt;\\s*APPLET\\s+[^&gt;]*\\bCODEBASE\\s*=\\s*\"([^\"]*)\"");
        textHtmlPatternsList.add("(?i)&lt;\\s*APPLET\\s+[^&gt;]*\\bCODEBASE\\s*=\\s*'([^']*)'");
        textHtmlPatternsList.add("(?i)&lt;\\s*APPLET\\s+[^&gt;]*\\bCODEBASE=([^\\t\\n\\x0B\\f\\r>\\\"']+)");

        // APPLET ARCHIVE
        textHtmlPatternsList.add("(?i)&lt;\\s*APPLET\\s+[^&gt;]*\\bARCHIVE\\s*=\\s*\"([^\"]*)\"");
        textHtmlPatternsList.add("(?i)&lt;\\s*APPLET\\s+[^&gt;]*\\bARCHIVE\\s*=\\s*'([^']*)'");

        // BODY/TD BACKGROUND
        textHtmlPatternsList.add("(?i)&lt;\\s*(?:BODY|TD)\\s+[^&gt;]*\\bBACKGROUND\\s*=\\s*\"([^\"]*)\"");
        textHtmlPatternsList.add("(?i)&lt;\\s*(?:BODY|TD)\\s+[^&gt;]*\\bBACKGROUND\\s*=\\s*'([^']*)'");
        textHtmlPatternsList.add("(?i)&lt;\\s*(?:BODY|TD)\\s+[^&gt;]*\\bBACKGROUND=(?!\\\\\")([^\\t\\n\\x0B\\f\\r&gt;\"']+)");

        // Note that the following regular expressions are merged sets. They have proven
        // to be slightly faster than specifying each one independently, and prevent a
        // lot of duplication.

        // Regular expression for HREFs attributes
        textHtmlPatternsList.add("(?i)&lt;\\s*(?:LINK|AREA)\\s+[^&gt;]*\\bHREF=\"([^\"]*)\"");
        textHtmlPatternsList.add("(?i)&lt;\\s*(?:LINK|AREA)\\s+[^&gt;]*\\bHREF='([^']*)'");
        textHtmlPatternsList.add("(?i)&lt;\\s*(?:LINK|AREA)\\s+[^&gt;]*\\bHREF=([^\\\\t\\\\n\\\\x0B\\\\f\\\\r>\\\"']+)");

        // Regular expressions for HREF attributes
        textHtmlPatternsList.add("(?i)&lt;\\s*(?:IMG|FRAME|SCRIPT|EMBED|INPUT)\\s+[^&gt;]*\\bSRC\\s*=\\s*\"([^\"]*)\"");
        textHtmlPatternsList.add("(?i)&lt;\\s*(?:IMG|FRAME|SCRIPT|EMBED|INPUT)\\s+[^&gt;]*\\bSRC\\s*=\\s*'([^']*)'");
        textHtmlPatternsList.add("(?i)&lt;\\s*(?:IMG|FRAME|SCRIPT|EMBED|INPUT)\\s+[^&gt;]*\\bSRC=([^\\t\\n\\x0B\\f\\r>\\\"']+)");

        // Simple JavaScript replacement
        textHtmlPatternsList.add("window.location=\"([^\"]*)\";");

        contentTypePatternsMap.put("text/html", textHtmlPatternsList);

        List<String> applicationJavascriptPatternsList = new ArrayList<>();
        applicationJavascriptPatternsList.add("window.location=\"([^\"]*)\";");
        contentTypePatternsMap.put("application/javascript", applicationJavascriptPatternsList);

        List<String> textCssPatternsList = new ArrayList<>();
        textCssPatternsList.add("(?i)\\burl\\((?![\"'].)([^\\)]*)\\)");
        textCssPatternsList.add("(?i)\\burl\\(\"([^\"]*)\"\\)");
        textCssPatternsList.add("(?i)\\burl\\('([^']*)'\\)");
        // textCssPatternsList.add("(?i)background-image\\s*:\\s+url\\(([^\\)]*)\\)");
        // textCssPatternsList.add("background:\\s*url\\(([^\\)]*)\\)");
        // textCssPatternsList.add("background: transparent url\\(\"([^\\\"]*)\"\\)");
        // textCssPatternsList.add("background: transparent url\\('([^\\']*)'\\)");
        // textCssPatternsList.add("background: transparent url\\((?!'|\")([^\\)]*)\\)");
        // textCssPatternsList.add("@import\\s+url\\(\"([^\"]*)\"\\)");
        // textCssPatternsList.add("@import\\s+url\\((?!\")([^\\)]*)\\)");
        contentTypePatternsMap.put("text/css", textCssPatternsList);

        bean.setContentTypePatterns(contentTypePatternsMap);

        List<StringReplacer> stringReplacers = new ArrayList<>();
        stringReplacers.add(urlConversionReplacementsRegexReplacer());
        bean.setUrlConversionReplacements(stringReplacers);

        bean.setUseUrlConversionReplacements(browseDoubleEscape);

        // A list of HTML tag/attribute patterns that should be used to
        // replace content in resources with a content-type of text/html.
        // Each pattern should be in the format TAG:ATTRIBUTE. These
        // patterns are added to any text/html patterns defined above.
        //
        // NOTE: You should prefer additional regex patterns to using these
        //       patterns. Support for the htmlTagPatterns may be discontinued
        //       at a later date.
        List<String> htmlTagPatternsList = new ArrayList<>();
        // Below is an example of the tag format. However, IMG:SRC is
        // already handled in the regular expression above.
        //htmlTagPatternsList.add("IMG:SRC");
        bean.setHtmlTagPatterns(htmlTagPatternsList);

        return bean;
    }

    // Include this Regex when using ModJK and spaces or other escaped characters are
    // being unescaped by ModJK. This will result in Tomcat being passed a URL with
    // unescaped character, and the browse tool will fail to find the appropriate resource.
    //
    // Note this is now enabled/disabled depending on the value of the boolean property
    // "useUrlConversionReplacements"
    @Bean
    public RegexReplacer urlConversionReplacementsRegexReplacer() {
        RegexReplacer bean = new RegexReplacer();
        bean.setSearch("%(\\d\\d)");
        bean.setReplace("%25$1");

        return bean;
    }
}
