grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

grails.project.dependency.resolution = {
    inherits("global") {}
    log "warn"
    legacyResolve false
    repositories {
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
        test(":codenarc:0.21") { export = false }
        test(":code-coverage:1.2.7") { export = false }

        compile ":crm-core:2.0.2"
        compile ":crm-security:2.0.0"
        compile ":decorator:1.1"
    }
}
