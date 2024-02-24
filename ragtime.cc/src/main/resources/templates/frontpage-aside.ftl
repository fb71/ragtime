
<#assign style = "style=\"color:#695ea1\"">

<#--  -->
<#macro float aside>
<aside class="d-none d-md-block float-md-end border-start ms-5 ps-5" ${style}>
    ${aside.content}
</aside>
</#macro>

<#--  -->
<#macro bottom aside>
<div class="d-block d-md-none border-top" ${style}>
    ${aside.content}
</div>
</#macro>