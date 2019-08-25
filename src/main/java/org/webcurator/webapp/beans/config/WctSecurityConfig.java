package org.webcurator.webapp.beans.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.event.LoggerListener;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.channel.ChannelDecisionManagerImpl;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.channel.InsecureChannelProcessor;
import org.springframework.security.web.access.channel.SecureChannelProcessor;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.webcurator.auth.WCTAuthenticationProcessingFilter;
import org.webcurator.auth.dbms.WCTDAOAuthenticationProvider;
import org.webcurator.auth.dbms.WCTForcePasswordChange;
import org.webcurator.auth.ldap.WCTAuthoritiesPopulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Contains configuration that used to be found in {@code wct-core-security.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 *
 */
@Configuration
@PropertySource(value = "classpath:wct-webapp.properties")
public class WctSecurityConfig {
    @Value("${hibernate.default_schema}")
    private String hibernateDefaultSchema;

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.dn}")
    private String ldapDn;

    @Autowired
    private LdapContextSource ldapContextSource;

    @Autowired
    private BaseConfig baseConfig;

    public Collection<String> defaultUrlPatterns() {
        Collection<String> urlPatterns = Arrays.asList("/curator/credentials/reset-password.html", "/curator/**",
                "/jsp/**", "/j_acegi_security_check");

        return urlPatterns;
    }

    // NOTE: If you wish to use channel security, then uncomment this filter (it precedes securityFilter()).
//    @Bean
//    public FilterRegistrationBean<ChannelProcessingFilter> channelFilter() {
//        FilterRegistrationBean<ChannelProcessingFilter> bean = new FilterRegistrationBean<>();
//        bean.setFilter(channelProcessingFilter());
//        // TODO Patterns were originally case-insensitive
//        bean.addUrlPatterns("/jsp/**", "/j_acegi_security_check");
//        bean.setOrder(50);
//
//        return bean;
//    }

    @Bean
    public FilterRegistrationBean<SecurityContextPersistenceFilter> securityFilter() {
        FilterRegistrationBean<SecurityContextPersistenceFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(securityContextPersistenceFilter());
        // TODO Patterns were originally case-insensitive
        bean.setUrlPatterns(defaultUrlPatterns());
        bean.setOrder(100);

        return bean;
    }

    @Bean
    public FilterRegistrationBean<WCTAuthenticationProcessingFilter> authenticationFilter() {
        FilterRegistrationBean<WCTAuthenticationProcessingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(authenticationProcessingFilter());
        // TODO Patterns were originally case-insensitive
        bean.setUrlPatterns(defaultUrlPatterns());
        bean.setOrder(200);

        return bean;
    }

    @Bean
    public FilterRegistrationBean<ExceptionTranslationFilter> exceptionFilter() {
        FilterRegistrationBean<ExceptionTranslationFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(exceptionTranslationFilter());
        // TODO Patterns were originally case-insensitive
        bean.setUrlPatterns(defaultUrlPatterns());
        bean.setOrder(300);

        return bean;
    }

    @Bean
    public FilterRegistrationBean<FilterSecurityInterceptor> securityInterceptorFilter() {
        FilterRegistrationBean<FilterSecurityInterceptor> bean = new FilterRegistrationBean<>();
        bean.setFilter(filterSecurityInterceptor());
        // TODO Patterns were originally case-insensitive
        bean.setUrlPatterns(defaultUrlPatterns());
        bean.setOrder(400);

        return bean;
    }

    @Bean
    public FilterRegistrationBean<WCTForcePasswordChange> expiredPasswordFilter() {
        FilterRegistrationBean<WCTForcePasswordChange> bean = new FilterRegistrationBean<>();
        bean.setFilter(wctPasswordExpiredFilter());
        // TODO Patterns were originally case-insensitive
        bean.addUrlPatterns("/curator/**");
        bean.setOrder(500);

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public WCTForcePasswordChange wctPasswordExpiredFilter() {
        WCTForcePasswordChange bean = new WCTForcePasswordChange();
        bean.setAuditor(baseConfig.audit());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public ProviderManager authenticationManager() {
        List<AuthenticationProvider> providers = new ArrayList<>();
        // TODO CONFIGURATION if you want LDAP authentication to occur:
        // providers.add(ldapAuthenticator());

        providers.add(daoAuthenticationProvider());

        ProviderManager bean = new ProviderManager(providers);

        return bean;
    }

    // TODO Move this configuration suggestion to application.properties/sample ldif file
    // as per https://spring.io/guides/gs/authenticating-ldap/
//    @Bean
//    public BaseLdapPathContextSource ldapPathContextSource() {
//        DefaultSpringSecurityContextSource bean = new DefaultSpringSecurityContextSource(ldapUrl);
//        // TODO CONFIGURATION (optional):
//        // <!-- <property name="managerDn"><value>OU=WCT Users,DC=webcurator,DC=org</value></property> -->
//        // <!-- <property name="managerPassword"><value>itsAsecretWord</value></property> -->
//
//        return bean;
//    }

    // Note that the distinguished name patterns are in the class {@link SecurityUserDnListConfig}.
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public LdapAuthenticationProvider ldapAuthenticator() {
        BindAuthenticator bindAuthenticator = new BindAuthenticator(ldapContextSource);
        // TODO CONFIGURATION:
        // Use this if your users have the same ldap dn pattern, and the only difference is the user name:
        bindAuthenticator.setUserDnPatterns(new String[]{ ldapDn });

        // Use this and edit SecurityUserDnListConfig.java if you need to specify multiple dn patterns:
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
        bean.setPasswordEncoder(passwordEncoder());

        return bean;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // The original ShaPasswordEncoder would encrypt passwords with SystemWideSalt of "Rand0mS4lt"
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WCTDAOAuthenticationProvider jdbcDaoImpl() {
        WCTDAOAuthenticationProvider bean = new WCTDAOAuthenticationProvider();
        bean.setDataSource(baseConfig.dataSource());
        bean.setUsersByUsernameQuery(getUsersByUsernameQuery(hibernateDefaultSchema));
        bean.setAuthoritiesByUsernameQuery(getAuthoritiesByUsernameQuery());
        bean.setRolePrefix("ROLE_");

        return bean;
    }

    // TODO A common place for queries?
    private String getUsersByUsernameQuery(String schema) {
        return "select usr_username, usr_password, usr_active, usr_force_pwd_change from " + schema +
                ".WCTUSER WHERE usr_username = ?";
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
    public SecurityContextPersistenceFilter securityContextPersistenceFilter() {
        return new SecurityContextPersistenceFilter();
    }

    // TODO CONFIGURATION
    // ===================== HTTP CHANNEL REQUIREMENTS ====================
    // You will need to uncomment the "Acegi Channel Processing Filter"
    // <filter-mapping> in web.xml for the following beans to be used
    @Bean
    public ChannelProcessingFilter channelProcessingFilter() {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<>();
        requestMap.put(new AntPathRequestMatcher("\\A/acegilogin.jsp.*\\Z", null, false),
                SecurityConfig.createList("REQUIRES_SECURE_CHANNEL"));
        requestMap.put(new AntPathRequestMatcher("\\A.*\\Z", null, false),
                SecurityConfig.createList("REQUIRES_INSECURE_CHANNEL"));

        FilterInvocationSecurityMetadataSource source = new DefaultFilterInvocationSecurityMetadataSource(requestMap);

        ChannelProcessingFilter bean = new ChannelProcessingFilter();
        bean.setChannelDecisionManager(channelDecisionManager());
        bean.setSecurityMetadataSource(source);

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
        ExceptionTranslationFilter bean = new ExceptionTranslationFilter(authenticationEntryPoint());

        return bean;
    }

    @Bean
    public WCTAuthenticationProcessingFilter authenticationProcessingFilter() {
        WCTAuthenticationProcessingFilter bean = new WCTAuthenticationProcessingFilter("/j_acegi_security_check");
        bean.setAuthenticationManager(authenticationManager());
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("/");
        bean.setAuthenticationSuccessHandler(successHandler);
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/logon.jsp?failed=true");
        bean.setAuthenticationFailureHandler(failureHandler);
        bean.setFilterProcessesUrl("/j_acegi_security_check");
        bean.setAuthDAO(baseConfig.userRoleDAO());
        bean.setAuditor(baseConfig.audit());

        return bean;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Lazy(false)
    public AuthenticationEntryPoint authenticationEntryPoint() {
        LoginUrlAuthenticationEntryPoint bean = new LoginUrlAuthenticationEntryPoint("/logon.jsp");
        bean.setForceHttps(false);

        return bean;
    }

    @Bean
    public AffirmativeBased httpRequestAccessDecisionManager() {
        List<AccessDecisionVoter<? extends Object>> roleVoters = Arrays.asList(roleVoter());
        AffirmativeBased bean = new AffirmativeBased(roleVoters);
        bean.setAllowIfAllAbstainDecisions(false);

        return bean;
    }

    @Bean
    public RoleVoter roleVoter() {
        return new RoleVoter();
    }

    @Bean
    public SecurityMetadataSource securityMetadataSource() {
        SecurityMetadataSource bean = new SecurityMetadataSource() {
            @Override
            public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
                return null;
            }

            @Override
            public Collection<ConfigAttribute> getAllConfigAttributes() {
                return null;
            }

            @Override
            public boolean supports(Class<?> clazz) {
                return false;
            }
        };

        return bean;
    }

    @Bean
    public FilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource() {
        // This is from the former XML wct-security-config.xml.
        // Note the order that entries are placed against the objectDefinitionSource is critical.
        // The FilterSecurityInterceptor will work from the top of the list down to the FIRST pattern that matches the
        // request URL.
        // Accordingly, you should place MOST SPECIFIC (ie a/b/c/d.*) expressions first, with LEAST SPECIFIC (ie a/.*)
        // expressions last.
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<>();
        requestMap.put(new AntPathRequestMatcher("/index.jsp", null, false),
                SecurityConfig.createListFromCommaDelimitedString("ROLE_ANONYMOUS,ROLE_USER"));
        requestMap.put(new AntPathRequestMatcher("/logon.jsp*", null, false),
                SecurityConfig.createListFromCommaDelimitedString("ROLE_ANONYMOUS,ROLE_USER"));
        requestMap.put(new AntPathRequestMatcher("/**", null, false),
                SecurityConfig.createListFromCommaDelimitedString("ROLE_ADM,ROLE_LOGIN"));

        FilterInvocationSecurityMetadataSource bean = new DefaultFilterInvocationSecurityMetadataSource(requestMap);

        return bean;
    }

    @Bean
    public FilterSecurityInterceptor filterSecurityInterceptor() {
        FilterSecurityInterceptor bean = new FilterSecurityInterceptor();
        bean.setAuthenticationManager(authenticationManager());
        bean.setAccessDecisionManager(httpRequestAccessDecisionManager());
        bean.setSecurityMetadataSource(filterInvocationSecurityMetadataSource());

        return bean;
    }
}
