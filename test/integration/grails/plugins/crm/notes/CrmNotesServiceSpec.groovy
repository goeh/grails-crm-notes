package grails.plugins.crm.notes

import spock.lang.Shared

/**
 * Tests for CrmNotesService.
 */
class CrmNotesServiceSpec extends grails.test.spock.IntegrationSpec {

    @Shared
    def crmSecurityService
    @Shared
    def crmNotesService

    def setupSpec() {
        if (!crmSecurityService.getUserInfo("test")) {
            crmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        }
    }

    def testCreateWithoutLoggedInUser() {
        given:
        def user = crmSecurityService.getUser("test")

        when:
        crmNotesService.create(user, "Hello World", "Hello Groovy world!", null, true)

        then:
        thrown(IllegalArgumentException)
    }

    def testCreateWithLoggedInUser() {
        expect:
        crmSecurityService.runAs("test") {
            def user = crmSecurityService.getUser()
            crmNotesService.create(user, "Hello World", "Hello Groovy world!", null, true)
        }
    }

    def testCreateWithoutSavedInstance() {
        when:
        crmSecurityService.runAs("test") {
            def instance = new TestEntity(name: "FOO")
            crmNotesService.create(instance, "Hello World", "Hello Groovy world!", null, true)
        }

        then:
        thrown(IllegalArgumentException)
    }

    def testEditable() {
        when:
        def note = crmSecurityService.runAs("test") {
            crmNotesService.create(crmSecurityService.getUser(), "Hello World", "Hello Groovy world!", null, true)
        }
        then:
        !crmNotesService.isLocked(note)
    }

    def testFindRecentNotes() {
        given:
        crmSecurityService.runAs("test") {
            def user = crmSecurityService.getUser()
            20.times {
                crmNotesService.create(user, "Note #$it", "This is note #$it", ((it % 2) ? 'todd' : 'steven'), true)
                Thread.sleep(100)
            }
        }

        when:
        def all = crmNotesService.findRecentNotes(max: 5)

        then:
        all.size() == 5
        all[0].subject == "Note #19"
        all[1].subject == "Note #18"
        all[2].subject == "Note #17"
        all[3].subject == "Note #16"
        all[4].subject == "Note #15"

        when:
        def stevens = crmNotesService.findRecentNotes(username: "steven", max: 5)

        then:
        stevens.size() == 5
        stevens[0].subject == "Note #18"
        stevens[1].subject == "Note #16"
        stevens[2].subject == "Note #14"
        stevens[3].subject == "Note #12"
        stevens[4].subject == "Note #10"

        when:
        def todds = crmNotesService.findRecentNotes(username: "todd", max: 5)

        then:
        todds.size() == 5
        todds[0].subject == "Note #19"
        todds[1].subject == "Note #17"
        todds[2].subject == "Note #15"
        todds[3].subject == "Note #13"
        todds[4].subject == "Note #11"
    }

    def testFindRecentNotesWithType() {
        when:
        def result = crmSecurityService.runAs("test") {
            def types = ['crmContact', 'crmAgreement', 'crmTask']
            5.times {
                for (type in types) {
                    crmNotesService.create("${type}@${it + 1}", "${type}@${it + 1}", "Hello World!", null, true)
                    Thread.sleep(100)
                }
            }
            crmNotesService.findRecentNotes(max: 5, type: 'crmContact')
        }

        then:
        result.size() == 5
        result[0].ref.startsWith('crmContact')
        result[1].ref.startsWith('crmContact')
        result[2].ref.startsWith('crmContact')
        result[3].ref.startsWith('crmContact')
        result[4].ref.startsWith('crmContact')

        when:
        result = crmSecurityService.runAs("test") {
            crmNotesService.findRecentNotes(max: 8, types: 'crmAgreement,crmTask')
        }

        then:
        result.size() == 8
        result[0].ref.startsWith('crmAgreement') || result[0].ref.startsWith('crmTask')
    }

    def testDomainAddNoteMethod() {
        given:
        def instance = new TestEntity(name: "FOO")

        when:
        def note = crmSecurityService.runAs("test") {
            instance.addNote("fail", "It should not be possible to add notes to non-persistent entities")
        }
        then:
        thrown(IllegalArgumentException)

        when:
        crmSecurityService.runAs("test") {
            instance.save(flush: true)
        }

        then:
        instance.notes.isEmpty()

        when:
        note = crmSecurityService.runAs("test") {
            instance.addNote("B", "This note was added with the 'addNote' method.")
            Thread.sleep(100)
            instance.addNote("C", "This note was added with the 'addNote' method.")
            Thread.sleep(100)
            instance.addNote("A", "This note was added with the 'addNote' method.")
            Thread.sleep(100)
            instance.addNote("D", "This note was added with the 'addNote' method.")
        }

        then:
        !crmNotesService.isLocked(note)
        note.username == "test"
        instance.notes[0].subject == "D" // Last added note comes first when using default sort order.
        instance.getNotes([sort: 'subject', order: 'asc'])[0].subject == "A" // Sort by subject ascending order.
    }

}
