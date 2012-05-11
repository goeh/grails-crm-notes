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

import javax.servlet.http.HttpServletResponse
import grails.converters.JSON

class CrmNotesController {

    def crmNotesService

    def show(Long id) {
        def note = crmNotesService.getNote(id)
        if(note) {
            def timestamp = formatDate(date:(note.lastUpdated ?: note.dateCreated), type:'date')
            def title = message(code:'crmNote.show.title', default:'Note by {0} at {1}', args:[note.username, timestamp])
            def result = [id:id, username:note.username, timestamp:(note.lastUpdated ?: note.dateCreated), title:title, text:note.text]
            render result as JSON
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }
}
