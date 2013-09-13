<%@ page defaultCodec="html" %>

<g:if test="${list}">
    <div class="accordion" id="accordion-crmNotes">

        <g:each in="${list}" var="note" status="i">
            <div class="accordion-group" data-crm-id="${note.id}">
                <div class="accordion-heading">
                    <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion-crmNotes"
                       href="#crmNote-${note.id}">
                        <small class="pull-right">
                            <crm:user username="${note.username}" nouser="${note.username}">${name}</crm:user>
                            <g:formatDate date="${note.lastUpdated ?: note.dateCreated}" type="date" style="long"/>
                        </small>
                        ${note.subject}
                    </a>
                </div>

                <div id="crmNote-${note.id}"
                     class="accordion-body collapse ${pulse && (pulse == note.id) ? 'in' : ''}">
                    <div class="accordion-inner">
                        ${note.text}
                        <crm:hasPermission permission="crmNotes:edit">
                            <crm:noteIsEditable note="${note}">
                                <a href="javascript:void(0);" class="crm-delete text-error pull-right"
                                   data-crm-id="${note.id}"><small><g:message code="crmNote.button.delete.label"
                                                                              default="Delete"/></small></a>
                            </crm:noteIsEditable>
                        </crm:hasPermission>
                    </div>
                </div>
            </div>
        </g:each>
    </div>
</g:if>
<g:else>
    <p class="muted"><g:message code="crmNote.no.result.message" default="No notes found"/></p>
</g:else>