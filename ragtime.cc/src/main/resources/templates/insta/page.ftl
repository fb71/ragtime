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
    <link href="topic/css/topic.css" rel="stylesheet"/>
    <link href="insta/css/insta.css" rel="stylesheet"/>
    <#-- TemplateConfigEntity styles-->
    <link href="config.css" rel="stylesheet"/>
    <script src="common/bs5.3.3/bootstrap.bundle.min.js" type="text/javascript"></script>
    <script src="common/js/common.js" type="text/javascript"></script>
</head>

<body class="${pageClass}">
    <#-- Page header -->
    <header class="IHeader" <#-- style="background-image: url(<@c.mediaUrl name="Raumfoto.jpg"/>)" -->>
        <div class="IBgImage container" style="height:100%;
            <#if config.bannerImage??>
                background-image: url(media/${config.bannerImage.id});
            </#if>
            ">
            <#-- Face -->
            <div class="IFace">
                <@c.editable msg="page.title">
                    <#if config.leadImage??>
                        <img class="IFaceBordered" src="media/${config.leadImage.id}?w=180&h=180"></img>
                    </#if>
                </@c.editable>
            </div>
        </div>
    </header>

    <#-- Title -->
    <div class="ITitle ISection container">
        <@c.editable msg="page.title">
            <h5><strong>${config.page.title}</strong></h5>
            <h6>${config.page.title2}</h6>
        </@c.editable>
    </div>

    <#-- Topics -->
    <@topicsSection/>

    <#-- TopicBio -->
    <#if !hideTopicBio>
        <@topicBioSection/>
    </#if>
    
    <#-- Content -->
    <@contentSection>
        <#nested>        
    </@contentSection>

    <#-- Footer -->
    <@footerSection/>
</body>
</html>
</#macro>


<#-- topicsSection -->
<#macro topicsSection>
    <div class="ITopics ISection container">
        <div class="ITopicsScroller">
        <div class="row flex-nowrap">
            <#assign selected = topic>
            <#list topics?sequence?sort_by("order") as topic>
                <#--  style="background-color: ${topic.color};">  -->
                <#if selected.id = topic.id>
                    <div class="ITopic col-auto active" aria-current="page"> 
                <#else>
                    <div class="ITopic col-auto"> 
                </#if>
                <@c.editable msg="topic.${topic.id}">
                    <a href="${topic.permName}" style="display:block;" title="${topic.title}">

                      <#if topic.medias?sequence?size gt 0>
                        <#assign media = topic.medias?sequence?first>
                        <img src="media/${media.id}?w=75&h=75" class="img-fluid" alt="${media.name}"/>
                      </#if>                      
                      <br/>
                      <span>${topic.title?replace(" ", "&nbsp;")}</span>

                    </a>
                </@c.editable>
                </div>
            </#list>
        </div>
        </div>
    </div>
</#macro>

<#-- topicBioSection -->
<#macro topicBioSection>
    <@c.editable msg="topic.${topic.id}">
      <div class="ITopicBio ISection container">
        ${topic.description}
      </div>
    </@c.editable>
</#macro>

<#--
  contentSection 
-->
<#macro contentSection>
    <div class="IContent ISection container">
        <#nested>
    </div>
</#macro>

<#--
  footerSection 
-->
<#macro footerSection>
    <footer class="IFooter ISection">
        <div class="container">
            <p class="m-0 small">
            <@c.editable msg="page.footer">
                <nobr>${config.page.footer}</nobr>&nbsp;&nbsp;-&nbsp;
                <#list config.footerNavItems?sequence?sort_by("order") as item>
                    <nobr><a href="${item.href}">${item.title}</a></nobr>&nbsp;&nbsp;-&nbsp;
                </#list>
            </@c.editable>
            <nobr>Made by <a target="_blank" href="https://fb71.org/">Wizard & Crew</a></nobr>
            </p>
        </div>
    </footer>
</#macro>


