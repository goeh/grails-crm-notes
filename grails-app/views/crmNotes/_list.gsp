<g:if test="${list}">
    <table class="table table-striped">
        <thead>
        <th><g:message code="crmNote.text.label" default="What"/></th>
        <th><g:message code="crmNote.dateCreated.label" default="When"/></th>
        <th><g:message code="crmNote.username.label" default="Who"/></th>
        <th></th>
        </thead>
        <tbody>
        <g:each in="${list}" var="note" status="i">
            <tr class="${note.id == pulse ? 'pulse' : ''}">
                <td><a href="javascript:void(0)" class="crm-show" data-crm-id="${note.id}">${note.encodeAsHTML()}</a></td>
                <td><g:formatDate date="${note.lastUpdated ?: note.dateCreated}" type="date"/></td>
                <td><crm:user username="${note.username}">${name}</crm:user></td>
                <td>
                    <crm:hasPermission permission="${controllerName + ':edit'}">
                        <g:unless test="${note.locked}">
                            <a href="javascript:void(0);" class="crm-delete" data-crm-id="${note.id}"><i class="icon-trash"></i></a>
                        </g:unless>
                    </crm:hasPermission>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</g:if>
<g:else>
    <p class="muted"><g:message code="crmNote.no.result.message" default="No notes found"/></p>
</g:else>