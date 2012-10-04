package grails.plugins.crm.notes

import org.junit.After
import org.junit.Before

/**
 * Tests for CrmNotesController
 */
class CrmNotesControllerTests extends GroovyTestCase {
    def controller
    def crmSecurityService
    def crmCoreService
    def crmNotesService
    def grailsApplication

    @Before
    void setUp() {
        // Setup logic here
        controller = new CrmNotesController()
        controller.crmSecurityService = crmSecurityService
        controller.crmNotesService = crmNotesService
        controller.grailsApplication = grailsApplication

        crmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
    }

    void testCreateWithoutLoggedInUser() {
        controller.params.ref = "crmUser@1"
        controller.params.subject = "Hello World"
        controller.params.text = "Hello World"
        controller.request.method = "POST"
        controller.create()
        assert controller.response.status != null
        println controller.response.status.toString()
    }

    void testCreateWithLoggedInUser() {
        def user = crmSecurityService.getUser("test")
        controller.params.ref = crmCoreService.getReferenceIdentifier(user)
        controller.params.subject = "Hello World"
        controller.params.text = "Hello World"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.create()
        }
        def json = controller.response.json
        assert json != null
        assert json.id != null
        assert json.error == null
    }

    void testUpdateNote() {
        def user = crmSecurityService.getUser("test")
        assert user != null
        def ref = crmCoreService.getReferenceIdentifier(user)
        assert ref != null
        controller.params.ref = ref
        controller.params.subject = "Hello World"
        controller.params.text = "Hello World"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.create()
        }
        def json = controller.response.json
        assert json != null
        assert json.id != null
        assert json.error == null
        assert json.subject == "Hello World"
        assert json.text == "Hello World"

        controller.response.reset()

        controller.params.id = json.id
        controller.params.subject = "Hello World"
        controller.params.text = "Hello World!!!"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.edit()
        }
        json = controller.response.json
        assert json != null
        assert json.id != null
        assert json.error == null
        assert json.subject == "Hello World"
        assert json.text == "Hello World!!!"

        def tmp = CrmNote.get(json.id)
        tmp.dateCreated = new Date() - 1
        tmp.save(flush: true)

        controller.response.reset()

        controller.params.id = json.id
        controller.params.subject = "Hello World"
        controller.params.text = "Hello modified World"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.edit()
        }
        json = controller.response.json
        assert json != null
        assert json.id != null
        assert json.error == null
        assert json.subject == "Hello World"
        assert json.text == "Hello modified World"

        controller.response.reset()

        grailsApplication.config.crm.notes.editWindow = 4

        controller.params.id = json.id
        controller.params.subject = "Hello World"
        controller.params.text = "Hello another World"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.edit()
        }
        json = controller.response.json
        assert json != null
        assert json.id == null
        assert json.error == "Editing is disabled for this note"
        assert json.text == null
    }
}
