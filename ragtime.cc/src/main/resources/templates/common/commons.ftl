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
        <div class="CEditable" style="user-select: none;">
            <#-- div class="CBadge"></div-->
            <#nested>
        </div>
        <script type="text/javascript">
            function addLongPressListener(element, callback) {
              let timer;
            
              element.addEventListener('touchstart', ev => { 
                timer = setTimeout(() => {
                  timer = null;
                  callback( ev );
                }, 500);
              });
            
              function cancel() {
                clearTimeout(timer);
              }
            
              element.addEventListener('touchend', cancel);
              element.addEventListener('touchmove', cancel);
            }
            
            <#-- -->
            addLongPressListener( document.currentScript.previousElementSibling, ev => {
                ev.stopPropagation(); 
                ev.preventDefault();                  
                window.top.postMessage('${msg}', '*');
                
                /*ev.target.style.transform = "none";*/
            });
            <#-- Prevent default text selection behaviour. -->
            document.currentScript.previousElementSibling.addEventListener("contextmenu", (ev) => {
                ev.stopPropagation(); 
                ev.preventDefault();                  
                //window.top.postMessage('${msg}', '*');
            });
            <#--  Desktop -->
            document.currentScript.previousElementSibling.addEventListener("dblclick", (ev) => {
                ev.stopPropagation(); 
                ev.preventDefault();                  
                window.top.postMessage('${msg}', '*');
            });
        </script>
    <#else>
        <#nested>
    </#if>
</#macro>

