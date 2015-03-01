package grails.plugins.crm.notes

import spock.lang.Shared

/**
 * Tests for CrmNotesController
 */
class CrmNotesControllerSpec extends grails.test.spock.IntegrationSpec {
    @Shared
    def controller
    @Shared
    def crmSecurityService
    @Shared
    def crmCoreService
    @Shared
    def crmNotesService
    @Shared
    def grailsApplication

    def setupSpec() {
        // Setup logic here
        controller = new CrmNotesController()
        controller.crmSecurityService = crmSecurityService
        controller.crmNotesService = crmNotesService
        controller.grailsApplication = grailsApplication

        if(! crmSecurityService.getUserInfo("test")) {
            crmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        }
    }

    def testCreateWithoutLoggedInUser() {
        given:
        controller.params.ref = "crmUser@1"
        controller.params.subject = "Hello World"
        controller.params.text = "Hello World"
        controller.request.method = "POST"

        when:
        controller.create()

        then:
        controller.response.status != null
    }

    def testCreateWithLoggedInUser() {
        given:
        def user = crmSecurityService.getUser("test")
        controller.params.ref = crmCoreService.getReferenceIdentifier(user)
        controller.params.subject = "Hello World"
        controller.params.text = "Hello World"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.create()
        }

        when:
        def json = controller.response.json


        then:
        json != null
        json.id != null
        json.error == null
    }

    def testUpdateNote() {
        when:
        def user = crmSecurityService.getUser("test")

        then:
        user != null


        when:
        def ref = crmCoreService.getReferenceIdentifier(user)

        then:
        ref != null


        when:
        controller.params.ref = ref
        controller.params.subject = "Hello World"
        controller.params.text = "Hello World"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.create()
        }
        def json = controller.response.json

        then:
        json != null
        json.id != null
        json.error == null
        json.subject == "Hello World"
        json.text == "Hello World"

        when:
        controller.response.reset()

        controller.params.id = json.id
        controller.params.subject = "Hello World"
        controller.params.text = "Hello World!!!"
        crmSecurityService.runAs(user.username) {
            controller.request.method = "POST"
            controller.edit()
        }
        json = controller.response.json

        then:
        json != null
        json.id != null
        json.error == null
        json.subject == "Hello World"
        json.text == "Hello World!!!"

        when:
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

        then:
        json != null
        json.id != null
        json.error == null
        json.subject == "Hello World"
        json.text == "Hello modified World"

        when:
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

        then:
        json != null
        json.id == null
        json.error == "Editing is disabled for this note"
        json.text == null
    }
}
