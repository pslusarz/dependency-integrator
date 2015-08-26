package org.di.api.impl.carfax

import org.di.api.ProjectSource
import org.di.api.SourceRepository
import org.di.api.impl.carfax.util.Git
import org.di.api.impl.carfax.util.GitVersionTag


class CarfaxLibSourceRepository implements SourceRepository {
    File localDir


    Map<CarfaxGradleProjectSource, List<ImmutableProjectSource>> getPastProjectSources(Collection<CarfaxGradleProjectSource> projectSources) {
        def result = [:]
        projectSources.each { CarfaxGradleProjectSource projectSource ->
            List<GitVersionTag> gitVersions = GitVersionTag.parseFromGitLog(Git.getVersionTags(projectSource.projectDirectory))
            List<ImmutableProjectSource> pastProjects = []
            try {
                gitVersions.each { GitVersionTag tag ->
                    Git.checkout(projectSource.projectDirectory, tag.commitSha)
                    ImmutableProjectSource immutableProjectSource = new ImmutableProjectSource(version: tag.version, projectDirectory: projectSource.projectDirectory)
                    immutableProjectSource.dependencies //initialize
                    pastProjects << immutableProjectSource

                }
            } finally {
                Git.checkout(projectSource.projectDirectory, "master")
            }
            result[projectSource] = pastProjects
        }

        return result

    }

    @Override
    Collection<ProjectSource> init() {
        def result = []
        localDir.eachDir { File projectDir ->
            result.add(new CarfaxGradleProjectSource(projectDir))
        }
        return result
    }

    @Override
    File getLocalDir() {
        return localDir
    }

    @Override
    void setLocalDir(File localDir) {
        this.localDir = localDir
    }

    @Override
    void downloadAll() {
        if (localDir.exists()) {
            File original = new File(localDir.getCanonicalPath())
            original.renameTo(new File(localDir.parentFile, localDir.name+"-"+System.currentTimeMillis()) )

        }
        localDir.mkdirs()
        projectNames.collect() { String projectName ->

            String cmd ="git clone ssh://git@stash:7999/lib/${projectName}.git ${localDir.absolutePath.replaceAll("\\\\", "/")+'/'+projectName}"
            if (System.properties["os.name"]?.startsWith("Windows")) {
                cmd = "cmd /c "+cmd
            }
            println cmd
            cmd.execute()
        }.each {it.waitFor()}

    }


    @Override
    void upload(ProjectSource project) {

    }

    List<String> projectNames = ['aamva-authentication-client',
                                 'aamva-consumer-access-client',
                                 'alert-log-domain',
                                 'amqp-configuration-domain',
                                 'answer-delivery-domain',
                                 'auction-partner-internal',
                                 'bbg-domain',
                                 'bmc-impact-service',
                                 'build-levels',
                                 'captcha-busta',
                                 'captcha-extensions',
                                 'captcha-struts1-extensions',
                                 'carfax-annotations',
                                 'carfax-assertions',
                                 'carfax-auction-bridge',
                                 'carfax-autoreports-summary',
                                 'carfax-autoreports-summary-acceptance',
                                 'carfax-branding-domain',
                                 'carfax-commons-controlm',
                                 'carfax-connection-cache',
                                 'carfax-connection-manager',
                                 'carfax-core-consumer',
                                 'carfax-core-partner',
                                 'carfax-core-subscriber',
                                 'carfax-core-subscriber-acceptance',
                                 'carfax-core-usage',
                                 'carfax-core-usage-acceptance',
                                 'carfax-core-utilities',
                                 'carfax-core-vhr',
                                 'carfax-core-vindecode',
                                 'carfax-cvs-navigator',
                                 'carfax-datetime-framework',
                                 'carfax-db-utils',
                                 'carfax-deeplinks',
                                 'carfax-dns',
                                 'carfax-email-commons',
                                 'carfax-fitnesse-commons',
                                 'carfax-integrator-files',
                                 'carfax-integrator-files-acceptance',
                                 'carfax-language-tools',
                                 'carfax-logging-commons',
                                 'carfax-messaging-commons',
                                 'carfax-oncontact-testing',
                                 'carfax-partner',
                                 'carfax-product-commons',
                                 'carfax-product-consumer-ratings',
                                 'carfax-product-glossary',
                                 'carfax-product-testing',
                                 'carfax-purchase-internal',
                                 'carfax-sampling',
                                 'carfax-shared',
                                 'carfax-snmp',
                                 'carfax-spring-datasources',
                                 'carfax-sql-framework',
                                 'carfax-struts-validator',
                                 'carfax-struts2-extensions',
                                 'carfax-testing',
                                 'carfax-web-dbaccess',
                                 'carfax-websidestory',
                                 'carfax-xinfo',
                                 'carfaxonline-auction-internal',
                                 'carfaxonline-encryption',
                                 'carfaxonline-java',
                                 'carfaxonline-java-acceptance',
                                 'click-tracking-domain',
                                 'coffeescript-extensions',
                                 'coldfusionerrorhandler-domain',
                                 'configuration-domain',
                                 'configuration-extensions',
                                 'consignor-alert-domain',
                                 'consumer-account-domain',
                                 'consumer-email-domain',
                                 'consumer-fitnesse',
                                 'consumer-partner-domain',
                                 'consumer-startup-domain',
                                 'consumer-tags',
                                 'consumer-testing-internal',
                                 'controlm-web-service-client',
                                 'CoreVip',
                                 'corevip-acceptance',
                                 'cvs-repository-plugin',
                                 'dartads-domain',
                                 'datasource-provider-domain',
                                 'date-time-domain',
                                 'datetime-converters-jackson',
                                 'datetime-converters-mybatis',
                                 'dealer-internal',
                                 'dealer-inventory-domain',
                                 'dealer-inventory-domain-acceptance',
                                 'dealer-user-domain',
                                 'dealer-user-domain-acceptance',
                                 'dealerautoreports-commons',
                                 'dealerautoreports-commons-acceptance',
                                 'dealerautoreports-dataqualityengine',
                                 'DealerAutoReports-DataQualityEngine-acceptance',
                                 'dealerpartnerfitnesse',
                                 'deploy-gradle-plugins',
                                 'dris-xml',
                                 'encryption-extensions',
                                 'file-repository-plugin',
                                 'fitnesse-extensions',
                                 'fitnesse-wiki-widgets',
                                 'ftp-repository-plugin',
                                 'git-repository-plugin',
                                 'gradle-plugins',
                                 'grails-logging-defaults',
                                 'gson-extensions',
                                 'harness',
                                 'hotlisting-connection-manager',
                                 'hudson-monitor',
                                 'in-memory-database',
                                 'jaguar-common',
                                 'jaguar-vms',
                                 'jasmine-extensions',
                                 'jetty-extensions',
                                 'jspec-assertions',
                                 'jsspec-runner',
                                 'junit-extensions',
                                 'kount-domain',
                                 'last-reported-service-domain',
                                 'ldap-commons',
                                 'location-domain',
                                 'lucene-extensions',
                                 'magrathea-database-domain',
                                 'magrathea-domain',
                                 'magrathea-internal',
                                 'message-domain',
                                 'messaging-adapters',
                                 'mrv-analysis-domain',
                                 'name-in-lights',
                                 'name-in-lights-acceptance',
                                 'one-account-domain',
                                 'oracle-connection-manager',
                                 'oracle-repository-plugin',
                                 'partner-domain',
                                 'partner-encryption',
                                 'paymentech-sdk',
                                 'paypal-domain',
                                 'phoenix-permutation',
                                // 'pool-member-domain',
                                 'postal-domain',
                                 'ProgressMonitoring',
                                 'project-build-results',
                                 'purchase-domain',
                                 'purchase-testing-internal',
                                 'quick-vin-plate-match-internal',
                                 'quickvin-client',
                                 'quickvin-domain',
                                 'rabbit-extensions',
                                 'rbs-domain',
                                 'recordcheck-domain',
                                 'reflection-extensions',
                                 'report-delivery-domain',
                                 'repository-plugin-domain',
                                 'rest-client',
                                 'rest-client-extensions',
                                 'sdb-domain',
                                 'self-documenting-api',
                                 'serialization-extensions',
                                 'service-history-check-domain',
                                 'servlet-extensions',
                                 'silverpop-api',
                                 'snapshot-domain',
                                 'socket-java',
                                 'spring-amqp-extras',
                                 'spring-email-extentions',
                                 'spring-extensions',
                                 'spring-flash-map',
                                 'sql-extensions',
                                 'sql-framework-extensions',
                                 'status-page-domain',
                                 'struts1-extensions',
                                 'subscriber-domain',
                                 'survey',
                                 'survey-acceptance',
                                 'systemtest-domain',
                                 'test-helpers',
                                 'test-user-credentials-domain',
                                 'testhelpers',
                                 'ThreadedBatchManager',
                                 'timing-utils-extensions',
                                 'UdpLogging',
                                 'user-abstraction-layer-extensions',
                                 'velocity-extensions',
                                 'vhdata-cache-client',
                                 'vhdb-commons',
                                 'vhr-header-domain',
                                 'vin-exchange-domain',
                                 'vin-life-cycle-domain',
                                 'vinalert-domain',
                                 'vinhunter-domain',
                                 'vinlogger-files',
                                 'vinlogger-files-acceptance',
                                 'vinThreaderDatabaseInterface',
                                 'vzlite',
                                 'vzlite-dsl',
                                 'VzMetadata',
                                 'web-encryption-extensions',
                                 'webdriver-extensions',
                                 'webdriver-fitnesse-extensions',
                                 'weblogic-admin-extensions',
                                 'xml-http-fixture',
                                 'xml-service-domain',
                                 'xml-testing',
                                 'xml-utils']

}
