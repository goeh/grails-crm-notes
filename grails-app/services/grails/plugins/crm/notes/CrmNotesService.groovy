/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.notes

import grails.events.Listener
import grails.plugins.crm.core.TenantUtils
import groovy.time.TimeCategory

class CrmNotesService {

    def grailsApplication
    def crmCoreService
    def crmSecurityService

    @Listener(namespace = "crmTenant", topic = "requestDelete")
    def requestDeleteTenant(event) {
        def tenant = event.id
        def count = CrmNote.countByTenantId(tenant)
        return count ? [namespace: "crmNotes", topic: "deleteTenant"] : null
    }

    @Listener(namespace = "crmNotes", topic = "deleteTenant")
    def deleteTenant(event) {
        def tenant = event.id
        def result = CrmNote.findAllByTenantId(tenant)
        result*.delete()
        log.warn("Deleted ${result.size()} notes in tenant $tenant")
    }

    /**
     * Add a note to a domain instance.
     *
     * @param reference a domain instance to attach notes to
     * @param subject the note subject
     * @param text the note content
     * @params author (optional) the user who wrote the note
     * @return the created CrmNote instance
     */
    CrmNote create(Object reference, String subject, String text, String author = null, boolean save = false) {

        if (crmCoreService.isDomainClass(reference) && !reference.ident()) {
            throw new IllegalArgumentException(
                    "You must save the domain instance [$reference] before you can add notes to it")
        }

        if (!author) {
            author = crmSecurityService.currentUser?.username
            if (!author) {
                throw new IllegalArgumentException("Only logged in users can create notes")
            }
        }

        def note = new CrmNote(username: author, subject: subject, text: text)
        note.ref = crmCoreService.getReferenceIdentifier(reference)

        if (save && !note.hasErrors()) {
            note.save()
        }

        return note
    }

    CrmNote getNote(Long id) {
        CrmNote.get(id)
    }

    /**
     * List all notes attached to a reference object.
     *
     * @param reference domain instance or reference name
     * @param params optional pagination or sorting parameters
     * @return List of CrmNote instances
     */
    List findNotesByReference(reference, params = [:]) {
        if (!reference) {
            throw new RuntimeException("Reference is null.")
        }
        def referenceIsDomain = crmCoreService.isDomainClass(reference)
        if (referenceIsDomain && !reference.ident()) {
            throw new RuntimeException("You must save the domain instance [$reference] before calling findNotesByReference")
        }

        if (!params.sort) params.sort = 'dateCreated'
        if (!params.order) params.order = 'desc'
        if (params.cache == null) params.cache = true

        CrmNote.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            eq('ref', crmCoreService.getReferenceIdentifier(reference))
        }
    }

    boolean deleteNote(CrmNote note) {
        def tenant = TenantUtils.tenant
        if (note?.tenantId != tenant) {
            throw new RuntimeException("Cannot delete CrmNote [${note.id}] because it's not associated with the current tenant [$tenant]")
        }
        note.delete()
        return true
    }

    /**
     * Delete all notes attached to a reference.
     *
     * @param reference domain instance or reference name
     * @return number of notes deleted
     */
    int deleteAllNotes(reference) {
        if (!reference) {
            throw new RuntimeException("Reference is null.")
        }
        def referenceIsDomain = crmCoreService.isDomainClass(reference)
        if (referenceIsDomain && !reference.ident()) {
            throw new RuntimeException("You must save the domain instance [$reference] before calling deleteAllNotes")
        }

        def result = CrmNote.createCriteria().list() {
            eq('tenantId', TenantUtils.tenant)
            eq('ref', crmCoreService.getReferenceIdentifier(reference))
        }
        result*.delete()
        result.size()
    }

    boolean isLocked(CrmNote note) {
        def editWindow = grailsApplication.config.crm.notes.editWindow
        def rval = false
        if (editWindow) {
            use(TimeCategory) {
                if ((new Date() - note.dateCreated) > editWindow.hours) {
                    rval = true
                }
            }
        }
        return rval
    }

    List<CrmNote> findRecentNotes(Map params = [:]) {
        def username = params.username
        def types = params.types ?: params.type
        if (types) {
            if (!(types instanceof Collection)) {
                types = types.toString().split(/\s*,\s*/).toList()
            }
        }
        if (!params.max) params.max = 5
        CrmNote.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            if (username) {
                eq('username', username)
            }
            if (types) {
                if(types.size() > 1) {
                    or {
                        for (type in types) {
                            ilike('ref', type + '@%')
                        }
                    }
                } else {
                    ilike('ref', types[0] + '@%')
                }
            }
            gt('dateCreated', new Date() - (grailsApplication.config.crm.notes.recent.days ?: 45))
        }
    }
}
