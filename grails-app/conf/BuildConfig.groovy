grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.repos.default = "crm"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn"
    legacyResolve false
    repositories {
        mavenRepo "http://labs.technipelago.se/repo/crm-releases-local/"
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
        compile "grails.crm:crm-core:latest.integration"
        runtime "grails.crm:crm-security:latest.integration"
    }
}
