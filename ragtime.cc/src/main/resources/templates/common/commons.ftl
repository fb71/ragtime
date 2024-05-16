<#--
  Template link. 
-->
<#macro link title>
    <a href="">${title}</a>
</#macro>

<#--
  Denotes the data to load for this page.
  
  @param name
  @param model
  @param params
-->
<#macro data name model params>
   <#-- data: ${params}  -->
</#macro>

<#--
  Head URL 
  -->
<#macro headUrl path>
</#macro>

<#--
  Media URL 
  -->
<#macro mediaUrl name>
    'media/${name}'
</#macro>

<#--
  Wraps element for in-place editing. 
  -->
<#macro editable msg>
    <#if params.edit == "true">
        <#-- div class="CEditable" oncontextmenu="event.stopPropagation(); event.preventDefault(); window.top.postMessage('${msg}', '*');"-->
        <div class="CEditable">
            <#-- div class="CBadge"></div-->
            <#nested>
        </div>
        <script type="text/javascript">            
            var editable = document.currentScript.previousElementSibling;
            window.addLongPressListener( editable, ev => {
                window.console.log( "long press" );
                window.top.postMessage('${msg}', '*');
            });
        </script>
    <#else>
        <#nested>
    </#if>
</#macro>

