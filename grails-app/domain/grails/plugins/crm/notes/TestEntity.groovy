package grails.plugins.crm.notes

import grails.plugins.crm.core.TenantEntity

@TenantEntity
class TestEntity {

    String name

    static noteable = true
}
