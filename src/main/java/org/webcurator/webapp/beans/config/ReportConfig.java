package org.webcurator.webapp.beans.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.webcurator.core.report.Report;
import org.webcurator.core.report.ReportManager;
import org.webcurator.core.report.impl.*;
import org.webcurator.core.report.parameter.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains configuration that used to be found in {@code wct-core-report.xml}. This
 * is part of the change to move to using annotations for Spring instead of
 * XML files.
 */
@Configuration
public class ReportConfig {
    private static Logger LOGGER = LoggerFactory.getLogger(ReportConfig.class);

    @Autowired
    private BaseConfig baseConfig;

    @Bean
    public ReportManager reportMngr() {
        ReportManager bean = new ReportManager();

        List<Report> reports = new ArrayList<>();
        reports.add(systemUsageReport());
        reports.add(systemActivityReport());
        reports.add(crawlerActivityReport());
        reports.add(targetGroupSchedulesReport());
        reports.add(summaryTargetSchedulesReport());

        bean.setReports(reports);

        return bean;
    }

    @Bean
    public Report systemUsageReport() {
        Report bean = new Report();
        bean.setName("System Usage Report");
        bean.setDescription("A report showing who logged in to the system.");
        bean.setInfo("<FONT size=\"1\"> <I>Start Date is inclusive.<BR>End Date is exclusive.</I> </FONT>");
        bean.setReportGenerator(systemUsageReportGeneratorImpl());

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(defaultStartDate());
        parameters.add(defaultEndDate());
        parameters.add(defaultAgencyParameter());

        bean.setParameters(parameters);

        return bean;
    }

    @Bean
    public SystemUsageReportGeneratorImpl systemUsageReportGeneratorImpl() {
        SystemUsageReportGeneratorImpl bean = new SystemUsageReportGeneratorImpl();
        bean.setSessionFactory(baseConfig.sessionFactory().getObject());

        return bean;
    }

    @Bean
    public Report systemActivityReport() {
        Report bean = new Report();
        bean.setName("System Activity Report");
        bean.setDescription("A report showing the actions performed by users of the system.");
        bean.setInfo("<FONT size=\"1\"> <I>Start Date is inclusive.<BR>End Date is exclusive.</I> </FONT>");
        bean.setReportGenerator(systemActivityReportGeneratorImpl());

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(defaultStartDate());
        parameters.add(defaultEndDate());
        parameters.add(defaultAgencyParameter());
        parameters.add(defaultUserParameter());

        bean.setParameters(parameters);

        return bean;
    }

    @Bean
    public SystemActivityReportGeneratorImpl systemActivityReportGeneratorImpl() {
        SystemActivityReportGeneratorImpl bean = new SystemActivityReportGeneratorImpl();
        bean.setSessionFactory(baseConfig.sessionFactory().getObject());

        return bean;
    }

    @Bean
    public Report crawlerActivityReport() {
        Report bean = new Report();
        bean.setName("Crawler Activity Report");
        bean.setDescription("A report showing which sites have been crawled, and some statistics on those crawls.");
        bean.setInfo("<FONT size=\"1\"> <I>Start Date is inclusive.<BR>End Date is exclusive.</I> </FONT>");
        bean.setReportGenerator(crawlerReportGenerator());

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(defaultStartDate());
        parameters.add(defaultEndDate());
        parameters.add(defaultAgencyParameter());
        parameters.add(defaultUserParameter());

        bean.setParameters(parameters);

        return bean;
    }

    @Bean
    public CrawlerActivityReport crawlerReportGenerator() {
        CrawlerActivityReport bean = new CrawlerActivityReport();
        bean.setSessionFactory(baseConfig.sessionFactory().getObject());

        return bean;
    }

    @Bean
    public Report targetGroupSchedulesReport() {
        Report bean = new Report();
        bean.setName("Target/Group Schedules Report");
        bean.setDescription("A report showing the harvest schedules for &#096;Approved&#146; Targets/Groups.");
        bean.setInfo("");
        bean.setReportGenerator(targetGroupReportGenerator());

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(defaultAgencyParameter());
        parameters.add(defaultUserParameter());

        TargetTypeParameter targetTypeParameter = new TargetTypeParameter();
        targetTypeParameter.setName("targettype");
        targetTypeParameter.setDescription("Target Types");
        targetTypeParameter.setOptional(true);
        parameters.add(targetTypeParameter);

        bean.setParameters(parameters);

        return bean;
    }

    @Bean
    public TargetGroupSchedulesReport targetGroupReportGenerator() {
        TargetGroupSchedulesReport bean = new TargetGroupSchedulesReport();
        bean.setSessionFactory(baseConfig.sessionFactory().getObject());

        return bean;
    }

    @Bean
    public Report summaryTargetSchedulesReport() {
        Report bean = new Report();
        bean.setName("Summary Target Schedules Report");
        bean.setDescription("A summary report showing the harvest schedules for &#096;Approved&#146; Targets/Groups.");
        bean.setInfo("");
        bean.setReportGenerator(summaryTargetSchedulesReportGenerator());

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(defaultAgencyParameter());

        bean.setParameters(parameters);

        return bean;
    }

    @Bean
    public SummaryTargetSchedulesReport summaryTargetSchedulesReportGenerator() {
        SummaryTargetSchedulesReport bean = new SummaryTargetSchedulesReport();

        bean.setSessionFactory(baseConfig.sessionFactory().getObject());

        return bean;
    }

    public DateParameter defaultStartDate() {
        DateParameter startDate = new DateParameter();
        startDate.setName("startDate");
        startDate.setDescription("Start Date");
        startDate.setOptional(false);

        return startDate;
    }

    public DateParameter defaultEndDate() {
        DateParameter endDate = new DateParameter();
        endDate.setName("endDate");
        endDate.setDescription("End Date");
        endDate.setOptional(false);

        return endDate;
    }

    public AgencyParameter defaultAgencyParameter() {
        AgencyParameter agencyParameter = new AgencyParameter();
        agencyParameter.setName("agency");
        agencyParameter.setDescription("Agencies");
        agencyParameter.setOptional(true);
        agencyParameter.setAgencyUserManager(baseConfig.agencyUserManager());
        agencyParameter.setAuthorityManager(baseConfig.authorityManager());

        return agencyParameter;
    }

    public UserParameter defaultUserParameter() {
        UserParameter userParameter = new UserParameter();
        userParameter.setName("user");
        userParameter.setDescription("Users");
        userParameter.setOptional(true);
        userParameter.setAgencyUserManager(baseConfig.agencyUserManager());
        userParameter.setAuthorityManager(baseConfig.authorityManager());

        return userParameter;
    }
}
