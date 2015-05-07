<g:set var="entityName" value="${message(code: 'crmNote.label', defualt: 'Note')}"/>

<div class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="notesModalLabel" aria-hidden="true">

    <g:form controller="crmNotes" action="edit">
        <input type="hidden" name="id" value="${bean.id}"/>

        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>

            <h3 id="notesModalLabel"><g:message code="crmNote.edit.title" default="Edit Note" args="${[entityName, bean]}"/></h3>
        </div>

        <div class="modal-body">
            <div class="control-group">
                <label class="control-label"><g:message code="crmNote.subject.label" default="Subject"/></label>

                <div class="controls">
                    <g:textField id="crm-note-subject" name="subject" value="${bean.subject}" maxlength="80" required=""
                                 class="span11"
                                 placeholder="${g.message(code: 'crmNote.subject.placeholder', default: '')}"/>
                </div>
            </div>


            <div class="control-group">
                <label class="control-label"><g:message code="crmNote.text.label" default="Message"/></label>

                <div class="controls">
                    <g:textArea id="crm-note-text" name="text" value="${bean.text}" cols="70" rows="5" class="span11"
                                placeholder="${g.message(code: 'crmNote.text.placeholder', default: '')}"/>
                </div>
            </div>
        </div>

        <div class="modal-footer">
            <button type="submit" class="btn btn-warning"><g:message code="crmNote.button.save.label" default="Save"/></button>
            <button type="button" class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="crmNote.button.cancel.label" default="Cancel"/></button>
        </div>

    </g:form>

</div>