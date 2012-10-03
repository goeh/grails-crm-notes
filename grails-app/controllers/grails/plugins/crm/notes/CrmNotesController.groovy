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

import grails.plugins.crm.core.WebUtils
import groovy.time.TimeCategory
import org.apache.commons.lang.StringUtils

import javax.servlet.http.HttpServletResponse
import grails.converters.JSON
import grails.plugins.crm.core.TenantUtils

class CrmNotesController {

    static allowedMethods = [show: ['GET', 'POST'], create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def crmNotesService
    def crmSecurityService
    def crmCoreService
    def grailsApplication

    def show(Long id) {
        def note = crmNotesService.getNote(id)
        if (note) {
            if ((note.tenantId != TenantUtils.tenant) || !crmSecurityService.isValidTenant(note.tenantId)) {
                crmSecurityService.alert(request, "accessDenied", "No permission to access tenant ${note.tenantId}")
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
                return
            }
            def timestamp = formatDate(date: (note.lastUpdated ?: note.dateCreated), type: 'date', style: 'LONG')
            def result = note.dao
            result.title = message(code: 'crmNote.show.title', default: 'Note by {0} {1}', args: [note.username, timestamp])
            WebUtils.shortCache(response)
            render result as JSON
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

    def list(String ref) {
        def instance = crmCoreService.getReference(ref)
        if (instance && crmCoreService.isDomainClass(instance)) {
            def result = crmNotesService.findNotesByReference(instance)
            // Peek at the first note to see that it's from the current/active tenant.
            def sample = result.find {it}
            if (sample && (sample.tenantId != TenantUtils.tenant)) {
                crmSecurityService.alert(request, "accessDenied", "No permission to access tenant ${sample.tenantId}")
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
                return
            }
            WebUtils.shortCache(response)
            render template: '/crmNotes/list', plugin: 'crm-notes',
                    model: [bean: instance, list: result, totalCount: result.size(), reference: ref, pulse: params.long('pulse')]
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }

    def create(String ref, String text) {

        def username = crmSecurityService.currentUser?.username
        if (!username) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }

        switch (request.method) {
            case 'GET':
                render template: '/crmNotes/form', plugin: 'crm-notes', model: [bean: new CrmNote(username: username, ref: params.ref)]
                break
            case 'POST':
                def result = [:]
                try {
                    def instance = crmCoreService.getReference(ref)
                    if (instance && crmCoreService.isDomainClass(instance)) {
                        if (!instance.ident()) {
                            result.error = message(code: "crmNotes.error.save.transient.message", default: "Domain instance [{0}] must be saved before adding notes", args: [instance])
                        } else if (instance?.hasProperty('tenantId') && (instance.tenantId != TenantUtils.tenant)) {
                            crmSecurityService.alert(request, "forbidden",
                                    "User [$username] tried to attach notes [${StringUtils.abbreviate(text, 20)}] to [${instance.tenantId}#$ref] from tenant [${TenantUtils.tenant}]")
                            response.sendError(HttpServletResponse.SC_FORBIDDEN)
                            return
                        }
                    } else {
                        result.error = message(code: "crmNotes.error.invalid.reference.message", default: "Invalid domain reference [{0}]", args: [ref])
                    }

                    if (!result.error) {
                        def note = crmNotesService.create(instance, text, username, true)
                        if (note.hasErrors()) {
                            result.error = message(code: "crmNotes.error.save.message", default: "The note could not be saved", args: note.errors.allErrors)
                        } else {
                            result = note.dao
                        }
                    }
                } catch (Exception e) {
                    result.error = e.message ?: e.class.name
                }
                WebUtils.shortCache(response)
                render result as JSON
                break
        }
    }

    def edit(Long id, String text) {

        def note = crmNotesService.getNote(id)
        if (!note) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        if (note.tenantId != TenantUtils.tenant) {
            crmSecurityService.alert(request, "accessDenied", "No permission to access tenant ${note.tenantId}")
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }
        switch (request.method) {
            case 'GET':
                render template: '/crmNotes/form', plugin: 'crm-notes', model: [bean: note]
                break
            case 'POST':
                def result = [:]
                try {
                    if (note.locked) {
                        result.error = message(code: "crmNotes.error.edit.window", default: "Editing is disabled for this note")
                    } else {
                        note.text = text
                        if (note.save(flush: true)) {
                            result = note.dao
                        } else {
                            result.error = message(code: "crmNotes.error.save.message", default: "The note could not be saved", args: note.errors.allErrors)
                        }
                    }
                } catch (Exception e) {
                    result.error = e.message
                }
                WebUtils.shortCache(response)
                render result as JSON
                break
        }
    }

    def delete(Long id) {

        def note = crmNotesService.getNote(id)
        if (!note) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        if (note.tenantId != TenantUtils.tenant) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        def result = note.dao

        try {
            crmNotesService.deleteNote(note)
        } catch (Exception e) {
            result.error = e.message
        }
        WebUtils.shortCache(response)
        render result as JSON
    }
}
