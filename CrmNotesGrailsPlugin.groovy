import grails.plugins.crm.notes.CrmNote

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
class CrmNotesGrailsPlugin {
    // the plugin dependency group
    def groupId = "grails.crm"
    // the plugin version
    def version = "1.0-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Grails CRM Notes"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def description = "Let users add notes to domain instances "
    def documentation = "http://grails.org/plugin/crm-notes"
    def license = "APACHE"

    // Location of the plugin's issue tracker.
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-notes/issues"]

    // Online location of the plugin's browseable source code.
    def scm = [url: "https://github.com/goeh/grails-crm-notes"]

    def features = {
        crmNotes {
            description "Add notes to objects"
            permissions {
                guest "crmNotes:show"
                user "crmNotes:*"
                admin "crmNotes:*"
            }
            statistics {tenant ->
                def total = CrmNote.countByTenantId(tenant)
                def updated = CrmNote.countByTenantIdAndLastUpdatedGreaterThan(tenant, new Date() -31)
                def usage
                if (total > 0) {
                    def tmp = updated / total
                    if (tmp < 0.1) {
                        usage = 'low'
                    } else if (tmp < 0.3) {
                        usage = 'medium'
                    } else {
                        usage = 'high'
                    }
                } else {
                    usage = 'none'
                }
                return [usage: usage, objects: total]
            }
        }
    }

}
