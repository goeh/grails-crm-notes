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
import grails.plugins.crm.core.TenantEntity

@TenantEntity
@AuditEntity
class CrmNote implements Serializable {

    String ref
    String username
    String subject
    String text

    static constraints = {
        ref(maxSize: 80, blank: false)
        username(maxSize: 80, blank: false)
        subject(maxSize: 100, blank: false)
        text(maxSize: 100000, nullable: true, widget: 'textarea')
    }

    static mapping = {
        sort 'dateCreated': 'desc'
        ref index: 'crm_note_ref_idx'
    }

    static transients = ['dao']

    transient Map<String, Object> getDao() {
        [tenant: tenantId, id: id, dateCreated: dateCreated, lastUpdated: lastUpdated, ref: ref, username: username, subject: subject, text: text]
    }

    String toString() {
        subject.toString()
    }
}
