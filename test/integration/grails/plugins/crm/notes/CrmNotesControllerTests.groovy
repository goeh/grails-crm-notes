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
        controller.params.reference = "crmUser@1"
        controller.params.text = "Hello World"
        controller.request.method = "POST"
        controller.create()
        assert controller.response.status != null
    }

    void testCreateWithLoggedInUser() {
        def user = crmSecurityService.getUser("test")
        controller.params.reference = crmCoreService.getReferenceIdentifier(user)
        controller.params.text = "Hello World"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.create()
        }
        def json = controller.response.json
        assert json.id != null
        assert json.error == null
    }

    void testUpdateNote() {
        def user = crmSecurityService.getUser("test")
        controller.params.reference = crmCoreService.getReferenceIdentifier(user)
        controller.params.text = "Hello World"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.create()
        }
        def json = controller.response.json
        assert json.id != null
        assert json.error == null
        assert json.text == "Hello World"

        controller.response.reset()

        controller.params.id = json.id
        controller.params.text = "Hello World!!!"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.edit()
        }
        json = controller.response.json
        assert json.id != null
        assert json.error == null
        assert json.text == "Hello World!!!"

        def tmp = CrmNote.get(json.id)
        tmp.dateCreated = new Date() - 1
        tmp.save(flush: true)

        controller.response.reset()

        controller.params.id = json.id
        controller.params.text = "Hello modified World"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.edit()
        }
        json = controller.response.json
        assert json.id != null
        assert json.error == null
        assert json.text == "Hello modified World"

        controller.response.reset()

        grailsApplication.config.crm.notes.editWindow = 4

        controller.params.id = json.id
        controller.params.text = "Hello another World"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.edit()
        }
        json = controller.response.json
        assert json.id == null
        assert json.error == "Editing is disabled for this note"
        assert json.text == null
    }
}
