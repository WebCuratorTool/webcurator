package org.webcurator.webapp.beans.config;

import org.apache.catalina.session.StandardSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.webcurator.auth.TransitionalPasswordEncoder;
import org.webcurator.auth.WCTAuthenticationFailureHandler;
import org.webcurator.auth.WCTAuthenticationSuccessHandler;
import org.webcurator.auth.dbms.WCTDAOAuthenticationProvider;
import org.webcurator.auth.ldap.WCTAuthoritiesPopulator;
import org.webcurator.core.coordinator.WctCoordinatorPaths;
import org.webcurator.domain.model.auth.User;

import javax.servlet.http.*;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Contains configuration that used to be found in {@code wct-core-security.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@Configuration
@EnableWebSecurity
public class WctSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(WctSecurityConfig.class);
    private final String SESSION_LOCK = "lock";

    @Value("${hibernate.default_schema}")
    private String hibernateDefaultSchema;

    @Value("${ldap.enable}")
    private String ldapEnable;

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.usrSearchBase}")
    private String ldapUsrSearchBase;

    @Value("${ldap.usrSearchFilter}")
    private String ldapUsrSearchFilter;

    @Value("${ldap.groupSearchBase}")
    private String ldapGroupSearchBase;

    @Value("${ldap.groupSearchFilter}")
    private String ldapGroupSearchFilter;

    @Value("${ldap.contextSource.root}")
    private String ldapContextSourceRoot;

    @Value("${ldap.contextSource.manager.dn}")
    private String ldapContextSourceManagerDn;

    @Value("${ldap.contextSource.managerPassword}")
    private String ldapContextSourceManagerPassword;


    @Autowired
    private LdapContextSource ldapContextSource;

    @Autowired
    private BaseConfig baseConfig;

//    public Collection<String> defaultUrlPatterns() {
//        Collection<String> urlPatterns = Arrays.asList("/curator/credentials/reset-password.html", "/curator/**",
//                "/jsp/**", "/j_acegi_security_check");
//
//        return urlPatterns;
//    }

    @Bean
    public AuthenticationSuccessHandler wctAuthenticationSuccessHandler() {
        WCTAuthenticationSuccessHandler wctAuthenticationSuccessHandler = new WCTAuthenticationSuccessHandler("/curator/home.html", false);
        wctAuthenticationSuccessHandler.setAuthDAO(baseConfig.userRoleDAO());
        wctAuthenticationSuccessHandler.setAuditor(baseConfig.audit());
        wctAuthenticationSuccessHandler.setLogonDurationDAO(baseConfig.logonDuration());
        return wctAuthenticationSuccessHandler;
    }

    @Bean
    public AuthenticationFailureHandler wctAuthenticationFailureHandler() {
        WCTAuthenticationFailureHandler wctAuthenticationFailureHandler = new WCTAuthenticationFailureHandler("/logon.jsp?failed=true");
        wctAuthenticationFailureHandler.setAuditor(baseConfig.audit());
        wctAuthenticationFailureHandler.setUseForward(true);
        return wctAuthenticationFailureHandler;
    }

//    @Bean
//    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
////        StrictHttpFirewall firewall = new StrictHttpFirewall();
////        firewall.setAllowUrlEncodedSlash(true);
////        return firewall;
//        return new DefaultHttpFirewall();
//    }


    /**
     * Used by the ReST API
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/**").permitAll() // The ResT API has its own authentication
                .antMatchers("/auth/**").permitAll() // The ResT API has its own authentication
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/curator/**").hasRole("LOGIN")
//                .antMatchers("/curator/**").permitAll()
                .antMatchers("/jsp/**").hasRole("LOGIN")
                .antMatchers("/replay/**").hasRole("LOGIN")
                .antMatchers("/help/**").hasRole("LOGIN")
                .antMatchers("/styles/**").permitAll()
                .antMatchers("/images/**").permitAll()
                .antMatchers("/scripts/**").permitAll()
                .antMatchers(WctCoordinatorPaths.ROOT_PATH + "/**").permitAll()
                .antMatchers("**/digital-asset-store/**").permitAll()
                .antMatchers("/spa/**").permitAll()
                .antMatchers("/api/**").permitAll()
//                .antMatchers("/visualization/**").permitAll()
                .anyRequest().authenticated()
                .and().formLogin()
                .loginPage("/logon.jsp")
                .permitAll()
                .loginProcessingUrl("/login")
                .successHandler(wctAuthenticationSuccessHandler())
                //TODO configure default page for app
                .failureHandler(wctAuthenticationFailureHandler())
//                .failureUrl("/logon.jsp?failed=true")
                .and().logout()
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/logon.jsp");

        http.headers().frameOptions().sameOrigin();

//                .and().sessionManagement().maximumSessions(1).maxSessionsPreventsLogin(true)
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // Set optional LDAP/AD configuration
        if (ldapEnable.toLowerCase().equals("true")) {
            auth.ldapAuthentication()
                    .userSearchBase(ldapUsrSearchBase)
                    .userSearchFilter(ldapUsrSearchFilter)
                    .groupSearchBase(ldapGroupSearchBase)
                    .groupSearchFilter(ldapGroupSearchFilter)
                    .ldapAuthoritiesPopulator(authoritiesPopulator())
                    .contextSource()
                    .url(ldapUrl)
                    .managerDn(ldapContextSourceManagerDn)
                    .managerPassword(ldapContextSourceManagerPassword)
                    .root(ldapContextSourceRoot);
        }

        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider bean = new DaoAuthenticationProvider();
        bean.setUserDetailsService(jdbcDaoImpl());
        bean.setPasswordEncoder(passwordEncoder());

        return bean;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // for now support both bcrypt and legacy SHA-1
        return new TransitionalPasswordEncoder();
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

    @Bean
    public WCTAuthoritiesPopulator authoritiesPopulator() {
        WCTAuthoritiesPopulator bean = new WCTAuthoritiesPopulator();
        bean.setAuthDAO(baseConfig.userRoleDAO());
        return bean;
    }

    // NOTE: If you wish to use channel security, then uncomment this filter (it precedes securityFilter()).
//    //@Bean
//    public FilterRegistrationBean<ChannelProcessingFilter> channelFilter() {
//        FilterRegistrationBean<ChannelProcessingFilter> bean = new FilterRegistrationBean<>();
//        bean.setFilter(channelProcessingFilter());
//        // TODO Patterns were originally case-insensitive
//        bean.addUrlPatterns("/jsp/**", "/j_acegi_security_check");
//        bean.setOrder(50);
//
//        return bean;
//    }

////    @Bean
//    public FilterRegistrationBean<SecurityContextPersistenceFilter> securityFilter() {
//        FilterRegistrationBean<SecurityContextPersistenceFilter> bean = new FilterRegistrationBean<>();
//        bean.setFilter(securityContextPersistenceFilter());
//        // TODO Patterns were originally case-insensitive
//        bean.setUrlPatterns(defaultUrlPatterns());
//        bean.setOrder(100);
//
//        return bean;
//    }
//
////    @Bean
//    public FilterRegistrationBean<WCTAuthenticationProcessingFilter> authenticationFilter() {
//        FilterRegistrationBean<WCTAuthenticationProcessingFilter> bean = new FilterRegistrationBean<>();
//        bean.setFilter(authenticationProcessingFilter());
//        // TODO Patterns were originally case-insensitive
//        bean.setUrlPatterns(defaultUrlPatterns());
//        bean.setOrder(200);
//
//        return bean;
//    }
//
////    @Bean
//    public FilterRegistrationBean<ExceptionTranslationFilter> exceptionFilter() {
//        FilterRegistrationBean<ExceptionTranslationFilter> bean = new FilterRegistrationBean<>();
//        bean.setFilter(exceptionTranslationFilter());
//        // TODO Patterns were originally case-insensitive
//        bean.setUrlPatterns(defaultUrlPatterns());
//        bean.setOrder(300);
//
//        return bean;
//    }
//
////    @Bean
//    public FilterRegistrationBean<FilterSecurityInterceptor> securityInterceptorFilter() {
//        FilterRegistrationBean<FilterSecurityInterceptor> bean = new FilterRegistrationBean<>();
//        bean.setFilter(filterSecurityInterceptor());
//        // TODO Patterns were originally case-insensitive
//        bean.setUrlPatterns(defaultUrlPatterns());
//        bean.setOrder(400);
//
//        return bean;
//    }
//
////    @Bean
//    public FilterRegistrationBean<WCTForcePasswordChange> expiredPasswordFilter() {
//        FilterRegistrationBean<WCTForcePasswordChange> bean = new FilterRegistrationBean<>();
//        bean.setFilter(wctPasswordExpiredFilter());
//        // TODO Patterns were originally case-insensitive
//        bean.addUrlPatterns("/curator/**");
//        bean.setOrder(500);
//
//        return bean;
//    }
//
////    @Bean
////    @Scope(BeanDefinition.SCOPE_SINGLETON)
////    @Lazy(false)
//    public WCTForcePasswordChange wctPasswordExpiredFilter() {
//        WCTForcePasswordChange bean = new WCTForcePasswordChange();
//        bean.setAuditor(baseConfig.audit());
//
//        return bean;
//    }
//
////    @Bean
////    @Scope(BeanDefinition.SCOPE_SINGLETON)
////    @Lazy(false)
//    public ProviderManager authenticationManager() {
//        List<AuthenticationProvider> providers = new ArrayList<>();
//        // TODO CONFIGURATION if you want LDAP authentication to occur:
//        // providers.add(ldapAuthenticator());
//
//        providers.add(daoAuthenticationProvider());
//
//        ProviderManager bean = new ProviderManager(providers);
//
//        return bean;
//    }
//
//    // TODO Move this configuration suggestion to application.properties/sample ldif file
//    // as per https://spring.io/guides/gs/authenticating-ldap/
////    //@Bean
////    public BaseLdapPathContextSource ldapPathContextSource() {
////        DefaultSpringSecurityContextSource bean = new DefaultSpringSecurityContextSource(ldapUrl);
////        // TODO CONFIGURATION (optional):
////        // <!-- <property name="managerDn"><value>OU=WCT Users,DC=webcurator,DC=org</value></property> -->
////        // <!-- <property name="managerPassword"><value>itsAsecretWord</value></property> -->
////
////        return bean;
////    }
//
//    // Note that the distinguished name patterns are in the class {@link SecurityUserDnListConfig}.
////    @Bean
////    @Scope(BeanDefinition.SCOPE_SINGLETON)
////    @Lazy(false)
//    public LdapAuthenticationProvider ldapAuthenticator() {
//        BindAuthenticator bindAuthenticator = new BindAuthenticator(ldapContextSource);
//        // TODO CONFIGURATION:
//        // Use this if your users have the same ldap dn pattern, and the only difference is the user name:
//        bindAuthenticator.setUserDnPatterns(new String[]{ ldapDn });
//
//        // Use this and edit SecurityUserDnListConfig.java if you need to specify multiple dn patterns:
//        //bindAuthenticator.setUserDnPatterns(userDnList());
//
//        // Use this and uncomment the associated bean below if you need to specify a search
//        // (e.g. active directory), or the dn does not use the WCT user name:
//        //bindAuthenticator.setUserSearch(userSearch());
//
//        WCTAuthoritiesPopulator wctAuthoritiesPopulator = new WCTAuthoritiesPopulator();
//        wctAuthoritiesPopulator.setAuthDAO(baseConfig.userRoleDAO());
//
//        LdapAuthenticationProvider bean = new LdapAuthenticationProvider(bindAuthenticator, wctAuthoritiesPopulator);
//
//        return bean;
//    }
//
//    // TODO CONFIGURATION
//    // Active directory user search
//    // Note that this bean has been commented out in the original xml configuration file.
////    //@Bean
////    public FilterBasedLdapUserSearch userSearch() {
////        FilterBasedLdapUserSearch bean = new FilterBasedLdapUserSearch("OU=Users,DC=webcurator,DC=org",
////                "(sAMAccountName={0})", initialDirContextFactory());
////        bean.setSearchSubtree(true);
////
////        return bean;
////    }
//
//
//    // Automatically receives AuthenticationEvent messages.
//    @Bean
//    public LoggerListener loggerListener() {
//        return new LoggerListener();
//    }
//
////    @Bean
//    public SecurityContextPersistenceFilter securityContextPersistenceFilter() {
//        return new SecurityContextPersistenceFilter();
//    }
//
//    // TODO CONFIGURATION
//    // ===================== HTTP CHANNEL REQUIREMENTS ====================
//    // You will need to uncomment the "Acegi Channel Processing Filter"
//    // <filter-mapping> in web.xml for the following beans to be used
//    //@Bean
//    public ChannelProcessingFilter channelProcessingFilter() {
//        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<>();
//        requestMap.put(new AntPathRequestMatcher("\\A/acegilogin.jsp.*\\Z", null, false),
//                SecurityConfig.createList("REQUIRES_SECURE_CHANNEL"));
//        requestMap.put(new AntPathRequestMatcher("\\A.*\\Z", null, false),
//                SecurityConfig.createList("REQUIRES_INSECURE_CHANNEL"));
//
//        FilterInvocationSecurityMetadataSource source = new DefaultFilterInvocationSecurityMetadataSource(requestMap);
//
//        ChannelProcessingFilter bean = new ChannelProcessingFilter();
//        bean.setChannelDecisionManager(channelDecisionManager());
//        bean.setSecurityMetadataSource(source);
//
//        return bean;
//    }
//
////    @Bean
//    public ChannelDecisionManagerImpl channelDecisionManager() {
//        ChannelDecisionManagerImpl bean = new ChannelDecisionManagerImpl();
//        bean.setChannelProcessors(new ArrayList(Arrays.asList(secureChannelProcessor(), insecureChannelProcessor())));
//
//        return bean;
//    }
//
////    @Bean
//    public SecureChannelProcessor secureChannelProcessor() {
//        return new SecureChannelProcessor();
//    }
//
////    @Bean
//    public InsecureChannelProcessor insecureChannelProcessor() {
//        return new InsecureChannelProcessor();
//    }
//
//    // ===================== HTTP REQUEST SECURITY ====================
//
////    @Bean
//    public ExceptionTranslationFilter exceptionTranslationFilter() {
//        ExceptionTranslationFilter bean = new ExceptionTranslationFilter(authenticationEntryPoint());
//
//        return bean;
//    }
//
////    @Bean
//    public WCTAuthenticationProcessingFilter authenticationProcessingFilter() {
//        WCTAuthenticationProcessingFilter bean = new WCTAuthenticationProcessingFilter("/j_acegi_security_check");
//        bean.setAuthenticationManager(authenticationManager());
//        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
//        successHandler.setDefaultTargetUrl("/");
//        bean.setAuthenticationSuccessHandler(successHandler);
//        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/logon.jsp?failed=true");
//        bean.setAuthenticationFailureHandler(failureHandler);
//        bean.setFilterProcessesUrl("/j_acegi_security_check");
//        bean.setAuthDAO(baseConfig.userRoleDAO());
//        bean.setAuditor(baseConfig.audit());
//
//        return bean;
//    }
//
////    @Bean
////    @Scope(BeanDefinition.SCOPE_SINGLETON)
////    @Lazy(false)
//    public AuthenticationEntryPoint authenticationEntryPoint() {
//        LoginUrlAuthenticationEntryPoint bean = new LoginUrlAuthenticationEntryPoint("/logon.jsp");
//        bean.setForceHttps(false);
//
//        return bean;
//    }
//
////    @Bean
//    public AffirmativeBased httpRequestAccessDecisionManager() {
//        List<AccessDecisionVoter<? extends Object>> roleVoters = Arrays.asList(roleVoter());
//        AffirmativeBased bean = new AffirmativeBased(roleVoters);
//        bean.setAllowIfAllAbstainDecisions(false);
//
//        return bean;
//    }
//
////    @Bean
//    public RoleVoter roleVoter() {
//        return new RoleVoter();
//    }
//
////    @Bean
//    public SecurityMetadataSource securityMetadataSource() {
//        SecurityMetadataSource bean = new SecurityMetadataSource() {
//            @Override
//            public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
//                return null;
//            }
//
//            @Override
//            public Collection<ConfigAttribute> getAllConfigAttributes() {
//                return null;
//            }
//
//            @Override
//            public boolean supports(Class<?> clazz) {
//                return false;
//            }
//        };
//
//        return bean;
//    }
//
////    @Bean
//    public FilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource() {
//        // This is from the former XML wct-security-config.xml.
//        // Note the order that entries are placed against the objectDefinitionSource is critical.
//        // The FilterSecurityInterceptor will work from the top of the list down to the FIRST pattern that matches the
//        // request URL.
//        // Accordingly, you should place MOST SPECIFIC (ie a/b/c/d.*) expressions first, with LEAST SPECIFIC (ie a/.*)
//        // expressions last.
//        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<>();
//        requestMap.put(new AntPathRequestMatcher("/index.jsp", null, false),
//                SecurityConfig.createListFromCommaDelimitedString("ROLE_ANONYMOUS,ROLE_USER"));
//        requestMap.put(new AntPathRequestMatcher("/logon.jsp*", null, false),
//                SecurityConfig.createListFromCommaDelimitedString("ROLE_ANONYMOUS,ROLE_USER"));
//        requestMap.put(new AntPathRequestMatcher("/**", null, false),
//                SecurityConfig.createListFromCommaDelimitedString("ROLE_ADM,ROLE_LOGIN"));
//
//        FilterInvocationSecurityMetadataSource bean = new DefaultFilterInvocationSecurityMetadataSource(requestMap);
//
//        return bean;
//    }
//
////    @Bean
//    public FilterSecurityInterceptor filterSecurityInterceptor() {
//        FilterSecurityInterceptor bean = new FilterSecurityInterceptor();
//        bean.setAuthenticationManager(authenticationManager());
//        bean.setAccessDecisionManager(httpRequestAccessDecisionManager());
//        bean.setSecurityMetadataSource(filterInvocationSecurityMetadataSource());
//
//        return bean;
//    }

    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent hse) {
                synchronized (SESSION_LOCK) {
                    log.info("Created session: {}", hse.getSession().getId());
                    sessions.put(hse.getSession().getId(), hse.getSession());
                }
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent hse) {
                synchronized (SESSION_LOCK) {
                    sessions.remove(hse.getSession().getId());
                    log.info("Removed session: {}", hse.getSession().getId());
                }
            }
        };
    }

    private static final Map<String, HttpSession> sessions = new HashMap<>();

    public String getCurrentSessionMessage(HttpServletRequest req, HttpServletResponse rsp) {
        StringBuilder buf = new StringBuilder();

        try {
            buf.append(req.getRequestURI()).append(" ").append(rsp.getStatus()).append("\r\n");

            Enumeration<String> reqHeaderNames = req.getHeaderNames();
            buf.append("[RequestHeader]\r\n");
            while (reqHeaderNames.hasMoreElements()) {
                String key = reqHeaderNames.nextElement();
                buf.append(key).append("=").append(req.getHeader(key)).append("\r\n");
            }

            if (rsp.getHeaderNames().size() > 0) {
                buf.append("[ResponseHeader]").append("\r\n");

                rsp.getHeaderNames().forEach(key -> {
                    if (key.equals("DEBUG_MSG")) {
                        String encodedMsg = rsp.getHeader(key);
                        String msg = new String(Base64.getDecoder().decode(encodedMsg));
                        buf.append("^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^").append("\r\n");
                        buf.append(key).append("=").append(msg).append("\r\n");
                        buf.append("^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^").append("\r\n");
                    } else {
                        buf.append(key).append("=").append(rsp.getHeader(key)).append("\r\n");
                    }
                });
            }


            StandardSession ssCur = null;
            try {
                ssCur = getPrivateSessionField(req.getSession());
            } catch (Exception e) {
//                log.error("Failed to get current session", e);
            }

            if (ssCur != null) {
                buf.append(String.format("[Current Session], isValid=%b", ssCur.isValid())).append("\r\n");
                buf.append("\tDetails: ").append(getSessionDetails(ssCur)).append("\r\n");
                buf.append("\tAuth: ").append(getAuthDetails(ssCur)).append("\r\n");
            }

            synchronized (SESSION_LOCK) {
                sessions.forEach((key, session) -> {
                    StandardSession ss = getPrivateSessionField(session);
                    if (ss != null) {
                        buf.append(String.format("[Existing Session], key=%s, isValid=%b", key, ss.isValid())).append("\r\n");
                        buf.append("\tDetails: ").append(getSessionDetails(ss)).append("\r\n");
                        buf.append("\tAuth: ").append(getAuthDetails(ss)).append("\r\n");
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to generate message", e);
        }

        return buf.toString();
    }

    private String getSessionDetails(StandardSession ss) {
        if (ss == null) {
            return "null";
        }

        try {
            if (ss.isValid()) {
                return String.format("SessionId: %s, CreationTime: %s, LatestAccessTime: %s, Time Used: %d, MaxInactiveInterval: %d, isNew: %b",
                        ss.getId(),
                        getReadableDatetime(ss.getCreationTime()),
                        getReadableDatetime(ss.getLastAccessedTime()),
                        System.currentTimeMillis() - ss.getLastAccessedTime(),
//                        ss.getServletContext().getSessionTimeout(),
                        ss.getMaxInactiveInterval(),
                        ss.isNew());
            } else {
                return String.format("SessionId: %s", ss.getId());
            }
        } catch (Throwable e) {
            return e.getMessage();
        }
    }

    private StandardSession getPrivateSessionField(HttpSession session) {
        if (session == null) {
            return null;
        }

        Field f = null;
        try {
            f = session.getClass().getDeclaredField("session");
        } catch (NoSuchFieldException e) {
            return null;
        }
        f.setAccessible(true);
        StandardSession ss = null;
        try {
            ss = (StandardSession) f.get(session);
        } catch (IllegalAccessException e) {
            return null;
        }
        return ss;
    }

    private String getAuthDetails(StandardSession ss) {
        if (ss == null) {
            return "null";
        }

        if (!ss.isValid()) {
            return "invalid";
        }
        SecurityContext auth = (SecurityContext) ss.getAttribute("SPRING_SECURITY_CONTEXT");
        if (auth == null) {
            return "null";
        }
        UsernamePasswordAuthenticationToken userAuth = (UsernamePasswordAuthenticationToken) auth.getAuthentication();
        if (userAuth == null) {
            return "null";
        }

        User user = (User) userAuth.getDetails();
        if (user == null) {
            return "null";
        }

        return String.format("Username: %s, Fullname: %s", user.getUsername(), user.getFullName());
    }

    private String getReadableDatetime(long milliseonds) {
        ZoneId zoneId = OffsetDateTime.now().getOffset().normalized();
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseonds), zoneId);
        return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }


}
