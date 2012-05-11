<%@ page defaultCodec="html" %>
<div class="modal hide fade" id="addNoteModal">

    <g:form class="form-horizontal" action="addNote">
        <g:hiddenField name="id" value="${bean?.id}"/>
        <div class="modal-header">
            <a class="close" data-dismiss="modal">×</a>

            <h3><g:message code="crmNote.create.title" default="Add Note"/></h3>
        </div>

        <div class="modal-body">
            <g:textArea name="note" required="" cols="70" rows="5" class="span6"
                        placeholder="${g.message(code:'crmNote.text.placeholder')}"/>
        </div>

        <div class="modal-footer">
            <crm:button action="addNote" visual="primary" icon="icon-ok icon-white"
                        label="crmNote.button.create.label"/>
            <a href="#" class="btn btn-danger" data-dismiss="modal"><i class="icon-remove icon-white"></i> <g:message
                    code="default.button.cancel.label" default="Cancel"/></a>
        </div>

    </g:form>
</div>
