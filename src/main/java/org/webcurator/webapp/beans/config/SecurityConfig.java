package org.webcurator.webapp.beans.config;

import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.context.HttpSessionContextIntegrationFilter;
import org.acegisecurity.event.authentication.LoggerListener;
import org.acegisecurity.intercept.web.FilterInvocationDefinitionSource;
import org.acegisecurity.intercept.web.FilterSecurityInterceptor;
import org.acegisecurity.ldap.DefaultInitialDirContextFactory;
import org.acegisecurity.ldap.search.FilterBasedLdapUserSearch;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.acegisecurity.providers.dao.salt.SystemWideSaltSource;
import org.acegisecurity.providers.encoding.ShaPasswordEncoder;
import org.acegisecurity.providers.ldap.LdapAuthenticationProvider;
import org.acegisecurity.providers.ldap.authenticator.BindAuthenticator;
import org.acegisecurity.securechannel.ChannelDecisionManagerImpl;
import org.acegisecurity.securechannel.ChannelProcessingFilter;
import org.acegisecurity.securechannel.InsecureChannelProcessor;
import org.acegisecurity.securechannel.SecureChannelProcessor;
import org.acegisecurity.ui.ExceptionTranslationFilter;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilterEntryPoint;
import org.acegisecurity.util.FilterChainProxy;
import org.acegisecurity.vote.AffirmativeBased;
import org.acegisecurity.vote.RoleVoter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.webcurator.auth.WCTAuthenticationProcessingFilter;
import org.webcurator.auth.dbms.WCTDAOAuthenticationProvider;
import org.webcurator.auth.dbms.WCTForcePasswordChange;
import org.webcurator.auth.ldap.WCTAuthoritiesPopulator;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Contains configuration that used to be found in {@code wct-core-security.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 *
 */
@Configuration
@ImportResource({ "classpath*:security-config-filter-chain.xml" })
public class SecurityConfig {
    static final String USERS_BY_USERNAME_QUERY = "select usr_username, usr_password, usr_active, usr_force_pwd_change from ${hibernate.default_schema}.WCTUSER WHERE usr_username = ?";

    static final String[] FILTER_CHAIN_PROXY_FILTER_DEFINITION = {
            "FILTER_CHAIN_PROXY_FILTER_DEFINITION",
            "PATTERN_TYPE_APACHE_ANT",
            "/curator/credentials/reset-password.html=httpSessionContextIntegrationFilter,authenticationProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor",
            "/curator/**=httpSessionContextIntegrationFilter,authenticationProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor, wctPasswordExpiredFilter",
            "/jsp/**=httpSessionContextIntegrationFilter,authenticationProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor",
            "/j_acegi_security_check=httpSessionContextIntegrationFilter, authenticationProcessingFilter, exceptionTranslationFilter,filterInvocationInterceptor"
     };

    static final String[] CHANNEL_PROCESSING_FILTER_FILTER_DEFINITION = {
            "CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON",
            "PATTERN_TYPE_APACHE_ANT",
            "\\A/acegilogin.jsp.*\\Z=REQUIRES_SECURE_CHANNEL",
            "\\A.*\\Z=REQUIRES_INSECURE_CHANNEL"
    };

    static final String[] FILTER_INVOCATION_INTERCEPTOR_OBJECT_DEFINITION = {
            "CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON",
            "PATTERN_TYPE_APACHE_ANT",
            "/index.jsp=ROLE_ANONYMOUS,ROLE_USER",
            "/logon.jsp*=ROLE_ANONYMOUS,ROLE_USER",
            "/**=ROLE_ADM,ROLE_LOGIN"
    };

    @Value("${hibernate.default_schema}")
    private String hibernateDefaultSchema;

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.dn}")
    private String ldapDn;

    @Autowired
    private BaseConfig baseConfig;

    @Autowired
    private SecurityUserDnListConfig securityUserDnListConfig;

    // TODO from security-config-filter-chain.xml (needs to be replaced by actual classes)
    @Autowired
    private FilterChainProxy filterChainProxy;

    // TODO from security-config-filter-chain.xml (needs to be replaced by actual classes)
    @Autowired
    private FilterSecurityInterceptor filterInvocationInterceptorForObjectDefinitionSource;

    // TODO Figure out how to initialize this using {@link FILTER_CHAIN_PROXY_FILTER_DEFINITION}.
    // It's got something to do with how Spring Security (acegi security) initializes its beans. Or maybe update to
    // Spring 3.2 (see their docs).
    /*
     * ======================== FILTER CHAIN =======================
     * If you wish to use channel security, add "channelProcessingFilter," in front of "httpSessionContextIntegrationFilter" in the list below:
     *     /jsp/**=httpSessionContextIntegrationFilter,authenticationProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor
     *     /j_acegi_security_check=httpSessionContextIntegrationFilter, authenticationProcessingFilter, exceptionTranslationFilter,filterInvocationInterceptor
     */
    // TODO the filterChainProxy bean comes from security-config-filter-chain.xml (needs to be replaced by actual classes)
    //@Bean
    //public FilterChainProxy filterChainProxyByJavaAnnotation() {
    //    FilterChainProxy bean = new FilterChainProxy();
        // TODO CONFIGURATION In the XML, we have the following value for filterInvocationDefinitionSource:
        // CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON
        // PATTERN_TYPE_APACHE_ANT
        // /curator/credentials/reset-password.html=httpSessionContextIntegrationFilter,authenticationProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor
        // /curator/**=httpSessionContextIntegrationFilter,authenticationProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor, WCTPasswordExpiredFilter
        // /jsp/**=httpSessionContextIntegrationFilter,authenticationProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor
        // /j_acegi_security_check=httpSessionContextIntegrationFilter, authenticationProcessingFilter, exceptionTranslationFilter,filterInvocationInterceptor
        //
        // but there's no constructor that matches that, and acegisecurity/spring security may have changed in Spring 3.x
        // See:
        //  http://shazsterblog.blogspot.com/2014/07/spring-security-custom-filterchainproxy.html
        //  https://itexpertsconsultant.wordpress.com/2015/11/26/spring-security-custom-filterchainproxy-using-java-annotation-configuration/
        //  https://www.dineshonjava.com/spring-security-java-based-configuration-with-example/
        //  https://www.baeldung.com/spring-security-custom-filter
        //
        // And we may need to move to Spring 4 to actually switch the security.
        // For the moment we will use the XML just for the filterChainProxy and switch to pure Java later.

        //bean.setFilterInvocationDefinitionSource(filterInvocationDefinitionSource);

    //    return bean;
    //}

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public WCTForcePasswordChange wctPasswordExpiredFilter() {
        WCTForcePasswordChange bean = new WCTForcePasswordChange();
        bean.setAuditor(baseConfig.audit());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public ProviderManager authenticationManager() {
        ProviderManager bean = new ProviderManager();

        List providers = new ArrayList();

        // TODO CONFIGURATION if you want LDAP authentication to occur:
        // providers.add(ldapAuthenticator());

        providers.add(daoAuthenticationProvider());

        bean.setProviders(providers);

        return bean;
    }

    @Bean
    public DefaultInitialDirContextFactory initialDirContextFactory() {
        DefaultInitialDirContextFactory bean = new DefaultInitialDirContextFactory(ldapUrl);
        // TODO CONFIGURATION (optional):
        // <!-- <property name="managerDn"><value>OU=WCT Users,DC=webcurator,DC=org</value></property> -->
        // <!-- <property name="managerPassword"><value>itsAsecretWord</value></property> -->

        return bean;
    }

    // Note that the distinguished name patterns are in the class {@link SecurityUserDnListConfig}.
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public LdapAuthenticationProvider ldapAuthenticator() {
        BindAuthenticator bindAuthenticator = new BindAuthenticator(initialDirContextFactory());
        // TODO CONFIGURATION:
        // Use this if your users have the same ldap dn pattern, and the only difference is the user name:
        bindAuthenticator.setUserDnPatterns(new String[]{ ldapDn });

        // Use this and edit security-userdn-list.xml if you need to specify multiple dn patterns:
        //bindAuthenticator.setUserDnPatterns(userDnList());

        // Use this and uncomment the associated bean below if you need to specify a search
        // (e.g. active directory), or the dn does not use the WCT user name:
        //bindAuthenticator.setUserSearch(userSearch());

        WCTAuthoritiesPopulator wctAuthoritiesPopulator = new WCTAuthoritiesPopulator();
        wctAuthoritiesPopulator.setAuthDAO(baseConfig.userRoleDAO());

        LdapAuthenticationProvider bean = new LdapAuthenticationProvider(bindAuthenticator, wctAuthoritiesPopulator);

        return bean;
    }

    // TODO CONFIGURATION
    // Active directory user search
    // Note that this bean has been commented out in the original xml configuration file.
//    @Bean
//    public FilterBasedLdapUserSearch userSearch() {
//        FilterBasedLdapUserSearch bean = new FilterBasedLdapUserSearch("OU=Users,DC=webcurator,DC=org",
//                "(sAMAccountName={0})", initialDirContextFactory());
//        bean.setSearchSubtree(true);
//
//        return bean;
//    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider bean = new DaoAuthenticationProvider();
        bean.setUserDetailsService(jdbcDaoImpl());
        bean.setSaltSource(saltSource());
        bean.setPasswordEncoder(passwordEncoder());

        return bean;
    }

    @Bean
    public SystemWideSaltSource saltSource() {
        SystemWideSaltSource bean = new SystemWideSaltSource();
        bean.setSystemWideSalt("Rand0mS4lt");

        return bean;
    }

    @Bean
    public ShaPasswordEncoder passwordEncoder() {
        return new ShaPasswordEncoder();
    }

    @Bean
    public WCTDAOAuthenticationProvider jdbcDaoImpl() {
        WCTDAOAuthenticationProvider bean = new WCTDAOAuthenticationProvider();
        System.out.println("* * * DEBUG: baseConfig=" + baseConfig + ", baseConfig.dataSource()=" + baseConfig.dataSource());
        bean.setDataSource((DataSource) baseConfig.dataSource());
        bean.setUsersByUsernameQuery(USERS_BY_USERNAME_QUERY);
        bean.setAuthoritiesByUsernameQuery(getAuthoritiesByUsernameQuery());
        bean.setRolePrefix("ROLE_");

        return bean;
    }

    public String getAuthoritiesByUsernameQuery() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT distinct PRV_CODE FROM ");
        stringBuilder.append(hibernateDefaultSchema);
        stringBuilder.append(".WCTUSER, ");
        stringBuilder.append(hibernateDefaultSchema);
        stringBuilder.append(".WCTROLE, ");
        stringBuilder.append(hibernateDefaultSchema);
        stringBuilder.append(".USER_ROLE, ");
        stringBuilder.append(hibernateDefaultSchema);
        stringBuilder.append(".ROLE_PRIVILEGE ");
        stringBuilder.append("WHERE ");
        stringBuilder.append("PRV_ROLE_OID = ROL_OID and ");
        stringBuilder.append("URO_USR_OID = USR_OID and ");
        stringBuilder.append("URO_ROL_OID = ROL_OID and ");
        stringBuilder.append("usr_username = ?");

        return stringBuilder.toString();
    }

    // Automatically receives AuthenticationEvent messages.
    @Bean
    public LoggerListener loggerListener() {
        return new LoggerListener();
    }

    @Bean
    public HttpSessionContextIntegrationFilter httpSessionContextIntegrationFilter() {
        return new HttpSessionContextIntegrationFilter();
    }

    // TODO Figure out how to initialize this using {@link CHANNEL_PROCESSING_FILTER_FILTER_DEFINITION}.
    // It's got something to do with how Spring Security (acegi security) initializes its beans. Or maybe update to
    // Spring 3.2 (see their docs).
    // TODO CONFIGURATION
    // ===================== HTTP CHANNEL REQUIREMENTS ====================
    // You will need to uncomment the "Acegi Channel Processing Filter"
    // <filter-mapping> in web.xml for the following beans to be used
    @Bean
    public ChannelProcessingFilter channelProcessingFilter() {
        ChannelProcessingFilter bean = new ChannelProcessingFilter();
        bean.setChannelDecisionManager(channelDecisionManager());

        FilterInvocationDefinitionSource filterInvocationDefinitionSource = new FilterInvocationDefinitionSource() {
            @Override
            public ConfigAttributeDefinition getAttributes(Object o) throws IllegalArgumentException {
                return null;
            }

            @Override
            public Iterator getConfigAttributeDefinitions() {
                return null;
            }

            @Override
            public boolean supports(Class aClass) {
                return false;
            }
        };
        bean.setFilterInvocationDefinitionSource(filterInvocationDefinitionSource);

        return bean;
    }

    @Bean
    public ChannelDecisionManagerImpl channelDecisionManager() {
        ChannelDecisionManagerImpl bean = new ChannelDecisionManagerImpl();
        bean.setChannelProcessors(new ArrayList(Arrays.asList(secureChannelProcessor(), insecureChannelProcessor())));

        return bean;
    }

    @Bean
    public SecureChannelProcessor secureChannelProcessor() {
        return new SecureChannelProcessor();
    }

    @Bean
    public InsecureChannelProcessor insecureChannelProcessor() {
        return new InsecureChannelProcessor();
    }

    // ===================== HTTP REQUEST SECURITY ====================

    @Bean
    public ExceptionTranslationFilter exceptionTranslationFilter() {
        ExceptionTranslationFilter bean = new ExceptionTranslationFilter();
        bean.setAuthenticationEntryPoint(authenticationProcessingFilterEntryPoint());

        return bean;
    }

    @Bean
    public WCTAuthenticationProcessingFilter authenticationProcessingFilter() {
        WCTAuthenticationProcessingFilter bean = new WCTAuthenticationProcessingFilter();
        bean.setAuthenticationManager(authenticationManager());
        bean.setAuthenticationFailureUrl("/logon.jsp?failed=true");
        bean.setDefaultTargetUrl("/");
        bean.setFilterProcessesUrl("/j_acegi_security_check");
        bean.setAuthDAO(baseConfig.userRoleDAO());
        bean.setAuditor(baseConfig.audit());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    @Autowired(required = false) // default when default-autowire="no"
    public AuthenticationProcessingFilterEntryPoint authenticationProcessingFilterEntryPoint() {
        AuthenticationProcessingFilterEntryPoint bean = new AuthenticationProcessingFilterEntryPoint();
        bean.setLoginFormUrl("/logon.jsp");
        bean.setForceHttps(false);

        return bean;
    }

    @Bean
    public AffirmativeBased httpRequestAccessDecisionManager() {
        AffirmativeBased bean = new AffirmativeBased();
        bean.setAllowIfAllAbstainDecisions(false);
        bean.setDecisionVoters(new ArrayList(Arrays.asList(roleVoter())));

        return bean;
    }

    @Bean
    public RoleVoter roleVoter() {
        return new RoleVoter();
    }

    // TODO Figure out how to initialize this using {@link FILTER_INVOCATION_INTERCEPTOR_OBJECT_DEFINITION}.
    // It's got something to do with how Spring Security (acegi security) initializes its beans. Or maybe update to
    // Spring 3.2 (see their docs).
    // TODO CONFIGURATION
    // Note the order that entries are placed against the objectDefinitionSource is critical.
    // The FilterSecurityInterceptor will work from the top of the list down to the FIRST pattern that matches the request URL.
    // Accordingly, you should place MOST SPECIFIC (ie a/b/c/d.*) expressions first, with LEAST SPECIFIC (ie a/.*) expressions last.
    // TODO the filterInvocationInterceptorForObjectDefinitionSource bean comes from security-config-filter-chain.xml
    //  (needs to be replaced by actual classes)
    @Bean
    public FilterSecurityInterceptor filterSecurityInterceptor() {
        FilterSecurityInterceptor bean = new FilterSecurityInterceptor();
        bean.setAuthenticationManager(authenticationManager());
        bean.setAccessDecisionManager(httpRequestAccessDecisionManager());
        // TODO CONFIGURATION This is coming from the XML security-config-filter-chain-only.xml:
        bean.setObjectDefinitionSource(filterInvocationInterceptorForObjectDefinitionSource.getObjectDefinitionSource());

        // TODO CONFIGURATION In the XML security-config-filter-chain-only.xml, we have the following value for
        // filterInvocationDefinitionSource:
        // CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON
        // PATTERN_TYPE_APACHE_ANT
        // /index.jsp=ROLE_ANONYMOUS,ROLE_USER
        // /logon.jsp*=ROLE_ANONYMOUS,ROLE_USER
        // /**=ROLE_ADM,ROLE_LOGIN
        //
        // but there's no constructor that matches that, and acegisecurity/spring security may have changed in Spring 3.x
        // See:
        //  http://shazsterblog.blogspot.com/2014/07/spring-security-custom-filterchainproxy.html
        //  https://itexpertsconsultant.wordpress.com/2015/11/26/spring-security-custom-filterchainproxy-using-java-annotation-configuration/
        //  https://www.dineshonjava.com/spring-security-java-based-configuration-with-example/
        //  https://www.baeldung.com/spring-security-custom-filter
        //
        // And we may need to move to Spring 4 to actually switch the security.
        // For the moment we will use the XML just for the filterChainProxy and switch to pure Java later.

        //bean.setObjectDefinitionSource(filterInvocationDefinitionSource);

        return bean;
    }
}
