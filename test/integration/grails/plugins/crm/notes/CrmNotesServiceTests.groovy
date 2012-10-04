package grails.plugins.crm.notes

import grails.plugins.crm.security.CrmUser
import org.junit.Before

/**
 * Tests for CrmNotesService.
 */
class CrmNotesServiceTests extends GroovyTestCase {

    def crmSecurityService
    def crmNotesService
    def grailsApplication

    @Before
    void setUp() {
        crmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
    }

    void testCreateWithoutLoggedInUser() {
        def user = crmSecurityService.getUser("test")
        shouldFail(IllegalArgumentException) {
            crmNotesService.create(user, "Hello World", "Hello Groovy world!", null, true)
        }
    }

    void testCreateWithLoggedInUser() {
        crmSecurityService.runAs("test") {
            def user = crmSecurityService.getUser()
            crmNotesService.create(user, "Hello World", "Hello Groovy world!", null, true)
        }
    }

    void testCreateWithoutSavedInstance() {
        crmSecurityService.runAs("test") {
            def instance = new CrmUser(username: "foo", name: "FOO")
            shouldFail(IllegalArgumentException) {
                crmNotesService.create(instance, "Hello World", "Hello Groovy world!", null, true)
            }
        }
    }
}
