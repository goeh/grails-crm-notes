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

import grails.plugins.crm.core.AuditEntity
import groovy.time.TimeCategory
import org.apache.commons.lang.StringUtils
import grails.plugins.crm.core.TenantEntity

@TenantEntity
@AuditEntity
class CrmNote {

    def crmCoreService
    def grailsApplication

    String ref
    String username
    String subject
    String text

    static constraints = {
        ref(maxSize: 80, blank: false)
        username(maxSize: 80, blank: false)
        subject(maxSize: 100, blank: false)
        text(maxSize: 100000, nullable:true)
    }

    static mapping = {
        sort 'dateCreated': 'desc'
    }
    static transients = ['reference', 'dao', 'locked']

    transient void setReference(object) {
        ref = crmCoreService.getReferenceIdentifier(object)
    }

    transient Object getReference() {
        crmCoreService.getReference(ref)
    }

    transient boolean isLocked() {
        def editWindow = grailsApplication.config.crm.notes.editWindow
        def rval = false
        if (editWindow) {
            use(TimeCategory) {
                if ((new Date() - dateCreated) > editWindow.hours) {
                    rval = true
                }
            }
        }
        return rval
    }

    transient Map<String, Object> getDao() {
        [tenant: tenantId, id: id, dateCreated: dateCreated, lastUpdated: lastUpdated, ref: ref, username: username, subject: subject, text: text]
    }

    String toString() {
        subject.toString()
    }
}
