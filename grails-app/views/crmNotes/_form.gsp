<g:hiddenField name="id" value="${bean.id}"/>
<g:hiddenField name="ref" value="${bean.ref}"/>

<div class="well">
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