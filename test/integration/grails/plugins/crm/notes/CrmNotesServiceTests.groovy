package grails.plugins.crm.notes

import org.junit.Before

/**
 * Tests for CrmNotesService.
 */
class CrmNotesServiceTests extends GroovyTestCase {

    def crmSecurityService
    def crmNotesService

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
            def instance = new TestEntity(name: "FOO")
            shouldFail(IllegalArgumentException) {
                crmNotesService.create(instance, "Hello World", "Hello Groovy world!", null, true)
            }
        }
    }

    void testEditable() {
        def note = crmSecurityService.runAs("test") {
            crmNotesService.create(crmSecurityService.getUser(), "Hello World", "Hello Groovy world!", null, true)
        }
        assert !crmNotesService.isLocked(note)
    }

    void testFindRecentNotes() {
        crmSecurityService.runAs("test") {
            def user = crmSecurityService.getUser()
            20.times {
                crmNotesService.create(user, "Note #$it", "This is note #$it", ((it % 2) ? 'todd' : 'steven'), true)
                Thread.sleep(100)
            }
        }
        def all = crmNotesService.findRecentNotes(max: 5)
        assert all.size() == 5
        assert all[0].subject == "Note #19"
        assert all[1].subject == "Note #18"
        assert all[2].subject == "Note #17"
        assert all[3].subject == "Note #16"
        assert all[4].subject == "Note #15"

        def stevens = crmNotesService.findRecentNotes(username: "steven", max: 5)
        assert stevens.size() == 5
        assert stevens[0].subject == "Note #18"
        assert stevens[1].subject == "Note #16"
        assert stevens[2].subject == "Note #14"
        assert stevens[3].subject == "Note #12"
        assert stevens[4].subject == "Note #10"

        def todds = crmNotesService.findRecentNotes(username: "todd", max: 5)
        assert todds.size() == 5
        assert todds[0].subject == "Note #19"
        assert todds[1].subject == "Note #17"
        assert todds[2].subject == "Note #15"
        assert todds[3].subject == "Note #13"
        assert todds[4].subject == "Note #11"
    }

    void testFindRecentNotesWithType() {
        crmSecurityService.runAs("test") {
            def types = ['crmContact', 'crmAgreement', 'crmTask']
            5.times {
                for (type in types) {
                    crmNotesService.create("${type}@${it + 1}", "${type}@${it + 1}", "Hello World!", null, true)
                    Thread.sleep(100)
                }
            }
            def result = crmNotesService.findRecentNotes(max: 5, type: 'crmContact')
            assert result.size() == 5
            for (note in result) {
                assert note.ref.startsWith('crmContact')
            }

            result = crmNotesService.findRecentNotes(max: 8, types: 'crmAgreement,crmTask')
            assert result.size() == 8
            for (note in result) {
                assert note.ref.startsWith('crmAgreement') || note.ref.startsWith('crmTask')
            }
        }
    }

    void testDomainAddNoteMethod() {
        def instance = new TestEntity(name: "FOO")
        def note = crmSecurityService.runAs("test") {
            shouldFail(IllegalArgumentException) {
                instance.addNote("fail", "It should not be possible to add notes to non-persistent entities")
            }
            assert instance.save(flush: true)
            assert instance.notes.isEmpty()
            instance.addNote("B", "This note was added with the 'addNote' method.")
            Thread.sleep(100)
            instance.addNote("C", "This note was added with the 'addNote' method.")
            Thread.sleep(100)
            instance.addNote("A", "This note was added with the 'addNote' method.")
            Thread.sleep(100)
            instance.addNote("D", "This note was added with the 'addNote' method.")
        }
        assert !crmNotesService.isLocked(note)
        assert note.username == "test"
        assert instance.notes[0].subject == "D" // Last added note comes first when using default sort order.
        assert instance.getNotes([sort: 'subject', order: 'asc'])[0].subject == "A" // Sort by subject ascending order.
    }
}
