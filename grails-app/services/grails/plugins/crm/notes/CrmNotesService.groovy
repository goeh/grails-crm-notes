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
 * under the License.
 */

package grails.plugins.crm.notes

class CrmNotesService {

    def crmCoreService
    def crmSecurityService

    /**
     * Add a note to a domain instance.
     *
     * @param reference a domain instance or a String
     * @param text the note content
     * @params author (optional) the user who wrote the note
     * @return the created CrmNote instance
     */
    CrmNote addNote(Object reference, String text, String author = null) {
        def referenceIsDomain = crmCoreService.isDomainClass(reference)
        if (referenceIsDomain && !reference.ident()) {
            throw new RuntimeException(
                    "You must save the domain instance [$reference] before calling addNote")
        }
        if(! author) {
            author = crmSecurityService.currentUser?.username
        }
        def note = new CrmNote(username:author, text:text)
        note.reference = reference

        // interceptors
        if (reference.respondsTo('onAddNote')) {
            reference.onAddNote(note)
        }

        note.save(failOnError: true)
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

        CrmNote.findAllByRef(crmCoreService.getReferenceIdentifier(reference), params)
    }

    boolean deleteNote(CrmNote note) {
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

        def result = CrmNote.findAllByRef(crmCoreService.getReferenceIdentifier(reference))
        result*.delete()
        result.size()
    }
}
