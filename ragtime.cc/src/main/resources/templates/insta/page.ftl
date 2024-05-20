<#--
  insta
-->
<#import "/commons.ftl" as c>

<#--
  The main page layout. 
-->
<#macro layout title pageClass="IHome" hideTopicBio=false>
<html>
<head>
    <title>${title} - ${config.page.title}</title>
    <meta charset="iso-8859-1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <link href="common/bs5.3.3/bootstrap.min.css" rel="stylesheet"/>
    <#-- Template styles -->
    <link href="css/common.css" rel="stylesheet"/>
    <link href="insta/css/insta.css" rel="stylesheet"/>
    <#-- TemplateConfigEntity styles-->
    <link href="config.css" rel="stylesheet"/>

    <script src="common/bs5.3.3/bootstrap.bundle.min.js" type="text/javascript"></script>
    <script src="common/js/common.js" type="text/javascript"></script>
</head>

<body class="${pageClass}">
    <#-- Page header -->
    <header class="IHeader IBgImage" style="background-image: url(<@c.mediaUrl name="Raumfoto.jpg"/>)">
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
                <div class="ITopic col-auto"> <#-- style="background-color: ${topic.color};"-->
                <@c.editable msg="topic.${topic.id}">
                  <div onclick="location.href='${topic.urlPart}';">

                      <#if topic.medias?sequence?size gt 0>
                        <#assign media = topic.medias?sequence?first>
                        <img src="media/${media.id}?w=50&h=50" class="img-fluid" alt="${media.name}"/>
                      </#if>
                      <#-- img src="media/bubble+R01_klein.png"></img-->
                      
                      <br/>
                      ${topic.title}
                  </div>
                </@c.editable>
                </div>
            </#list>
        </div>
        </div>
    </div>

    <#if !hideTopicBio>
        <@c.editable msg="topic.${topic.id}">
          <div class="ITopicBio container">
            ${topic.description}
          </div>
        </@c.editable>
    </#if>
    
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
