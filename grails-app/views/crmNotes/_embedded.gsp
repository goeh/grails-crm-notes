<%@ page defaultCodec="html" %>

<r:script>
    $(document).ready(function() {
        // Slide down the text panel when user selects a note from the list.
        $("#${view.id}-container .crm-show").click(function(ev) {
            ev.stopImmediatePropagation();
            var noteId = $(this).data('crm-id');
            $.getJSON("${createLink(controller: 'crmNotes', action: 'show')}", {id: noteId}, function(data) {
                $("#${view.id}-container .crm-panel").html($('<div/>').text(data.text).html().replace(/\n/g, "<br/>"));
                $("#${view.id}-container .crm-panel").slideDown('slow');
            });
            return false;
        });

        $("#${view.id}-container .crm-create").click(function(ev) {
            ev.stopImmediatePropagation();
            var panel = $("#${view.id}-container .crm-panel")
            panel.slideUp('fast');
            panel.load("${createLink(controller: 'crmNotes', action: 'create', params: [ref: reference])}", function(data) {
                panel.slideDown('normal', function() {
                    $("#${view.id}-container .toggle").children().toggle();
                    $("textarea", panel).focus();
                });
            });
            return false;
        });

        $("#${view.id}-container .crm-close").click(function(ev) {
            var panel = $("#${view.id}-container .crm-panel");
            panel.slideUp('fast', function() {
                $("#${view.id}-container .toggle").children().toggle();
            });
        });

        var deleteHandler = function(ev) {
            var msg = $("<div/>").html("${message(code: 'crmNote.button.delete.confirm.message', default: 'Are you sure you want to delete the note?')}").text();
            if(confirm(msg)) {
                var id = $(this).data('crm-id');
                $.ajax({
                    type: 'POST',
                    url: "${createLink(controller: 'crmNotes', action: 'delete')}",
                    data: {id: id},
                    dataType: 'json',
                    success: function(data) {
                        $("#${view.id}-container .crm-list").load("${createLink(controller: 'crmNotes', action: 'list', params: [ref: reference])}", function() {
                            $("#${view.id}-container .crm-delete").click(deleteHandler);
                            updateTabCounter("li.nav-${view.id} a", $("table tbody tr", $(this)).length);
                        });
                    },
                    error: function(data) {
                        alert("ERROR: " + data);
                    }
                });
            }
            return false;
        };

        $("#${view.id}-container .crm-delete").click(deleteHandler);

        $("#${view.id}-container form").submit(function(ev) {
            ev.preventDefault();
            var panel = $("#${view.id}-container .crm-panel");
            var form = $(this);
            $.ajax({
                type: 'POST',
                url: "${createLink(controller: 'crmNotes', action: 'create')}",
                data: form.serialize(),
                dataType: 'json',
                success: function(data) {
                    panel.slideUp('fast', function() {
                        $("#${view.id}-container .toggle").children().toggle();
                        $("#${view.id}-container .crm-list").load("${createLink(controller: 'crmNotes', action: 'list', params: [ref: reference])}&pulse=" + data.id, function() {
                            $("#${view.id}-container .crm-delete").click(deleteHandler);
                            updateTabCounter("li.nav-${view.id} a", $("table tbody tr", $(this)).length);
                        });
                    });
                },
                error: function(data) {
                    alert("ERROR: " + data);
                }
            });
            return false;
        });
    });
</r:script>

<div id="${view.id}-container">
    <g:form controller="crmNotes" action="create">

        <div class="crm-list">
            <g:render template="/crmNotes/list" plugin="crm-notes" model="${[list: list]}"/>
        </div>

        <div class="crm-panel hide"></div>

        <crm:hasPermission permission="${controllerName + ':edit'}">
            <div class="form-actions toggle">
            <crm:button type="link" controller="crmNotes" action="create" visual="primary" class="crm-create"
                        icon="icon-plus icon-white"></i>
                <g:message code="crmNote.button.create.label" default="Add note"/>
            </crm:button>

            <div class="hide">
                <crm:button action="create" visual="primary" icon="icon-ok icon-white" class="crm-save"
                            label="crmNote.button.save.label" style="margin-right: 20px;"/>

                <a href="javascript:void(0);" class="crm-close"><g:message
                        code="default.button.cancel.label" default="Cancel"/></a>
            </div>
            </div>
        </crm:hasPermission>
    </g:form>
</div>