<#--
  insta
-->
<#import "/commons.ftl" as c>

<#--
  The main page layout. 
-->
<#macro layout title pageclass>
<html>
<head>
    <title>${title} - ${config.page.title}</title>
    <meta charset="iso-8859-1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="common/bs5.3.3/bootstrap.min.css" rel="stylesheet"/>
    <#-- Template styles -->
    <link href="css/common.css" rel="stylesheet"/>
    <link href="css/insta.css" rel="stylesheet"/>
    <#-- TemplateConfigEntity styles-->
    <link href="config.css" rel="stylesheet"/>
    <script src="common/bs5.3.3/bootstrap.bundle.min.js" type="text/javascript"></script>
</head>

<body class="${pageclass}">
    <#-- Page header -->
    <header class="IHeader IBgImage" style="background-image: url('media/Raumfoto.jpg')">
        <div class="IBgImage container" style="height:100%">
            <@c.editable msg="page.title">
                <h1>${config.page.title}</h1>
            </@c.editable>
            <#-- Face -->
                <div class="IFace">
            <@c.editable msg="page.title">
                    <img class="IFaceBordered" src="media/RikeRingeisCoaching.jpg"></img>
            </@c.editable>
                </div>        
            </div>
        </div>
    </header>

    <#-- Topics -->
    <div class="ITopics container">
        <div class="ITopicsScroller">
        <div class="row flex-nowrap">
            <#list topics?sequence?sort_by("order") as topic>
                <div class="ITopic col-auto">
                    <img src="media/bubble+R01_klein.png"></img>
                    <br/>
                    ${topic.title}
                </div>
            </#list>
        </div>
        </div>
    </div>
    
    <#-- Content -->
    <div class="IContent container">
    <#nested>
    </div>

    <#-- Footer -->
    <#-- 
    <footer class="CFooter">
        <div class="container">
            <p class="m-0 small">
            <@c.editable msg="page.footer">
                <nobr>${config.page.footer}</nobr>&nbsp;&nbsp;-&nbsp;
            </@c.editable>
            <@c.editable msg="page.navigation">
                <#list config.footerNavItems?sequence?sort_by("order") as item>
                    <nobr><a href="${item.href}">${item.title}</a></nobr>&nbsp;&nbsp;-&nbsp;
                </#list>
            </@c.editable>
            <nobr>Made by <a target="_blank" href="https://fb71.org/">Wizard & Crew</a></nobr>
            </p>
        </div>
    </footer>
     -->    
</body>
</html>
</#macro>
