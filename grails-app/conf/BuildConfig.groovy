grails.project.work.dir = "target"
grails.project.target.level = 1.6

grails.project.repos.default = "crm"

grails.project.dependency.resolution = {
    inherits("global") {}
    log "warn"
    legacyResolve false
    repositories {
        mavenRepo "http://labs.technipelago.se/repo/crm-releases-local/"
        mavenRepo "http://labs.technipelago.se/repo/plugin-releases-local/"
        grailsCentral()
    }
    dependencies {
    }
    plugins {
        build(":tomcat:$grailsVersion",
                ":hibernate:$grailsVersion",
                ":release:2.2.1",
                ":rest-client-builder:1.0.3") {
            export = false
        }
        test(":codenarc:0.19") { export = false }
        test(":code-coverage:1.2.7") { export = false }

        compile "grails.crm:crm-core:latest.integration"
        runtime "grails.crm:crm-security:latest.integration"
        runtime ":decorator:1.0"
    }
}

codenarc {
    reports = {
        CrmXmlReport('xml') {
            outputFile = 'target/CodeNarcReport.xml'
            title = 'GR8 CRM CodeNarc Report'
        }
        CrmHtmlReport('html') {
            outputFile = 'target/CodeNarcReport.html'
            title = 'GR8 CRM CodeNarc Report'

        }
    }
    properties = {
        GrailsPublicControllerMethod.enabled = false
        CatchException.enabled = false
        CatchThrowable.enabled = false
        ThrowException.enabled = false
        ThrowRuntimeException.enabled = false
        GrailsStatelessService.enabled = false
        GrailsStatelessService.ignoreFieldNames = "dataSource,scope,sessionFactory,transactional,*Service,messageSource,grailsApplication,applicationContext,expose"
    }
    processTestUnit = false
    processTestIntegration = false
}

coverage {
    exclusions = ['**/radar/**']
}

