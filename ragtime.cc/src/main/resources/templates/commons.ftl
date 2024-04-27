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
  Wraps element for in-place editing. 
-->
<#macro editable msg>
    <#if params.edit == "true">
        <div class="Editable" onclick="event.stopPropagation(); event.preventDefault(); window.top.postMessage('${msg}', '*');">
            <#nested>
        </div>
    <#else>
        <#nested>
    </#if>
</#macro>

